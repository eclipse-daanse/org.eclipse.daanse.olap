/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.xmla.connector;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.daanse.lcid.api.LcidService;
import org.eclipse.daanse.olap.api.ContextGroup;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.xmla.connector.execute.OlapExecute;
import org.eclipse.daanse.olap.xmla.connector.session.SessionScenarios;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.daanse.xmla.model.xmla.Discover;
import org.eclipse.daanse.xmla.model.xmla.Execute;
import org.eclipse.daanse.xmla.api.XmlaConnector;
import org.eclipse.daanse.xmla.api.XmlaRefusedException;
import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.eclipse.daanse.xmla.api.SimpleSessionHandler;
import org.eclipse.daanse.xmla.api.XmlaSession;
import org.eclipse.daanse.xmla.api.XmlaSessionHandler;
import org.eclipse.daanse.xmla.api.auth.AuthenticatedIdentity;
import org.eclipse.daanse.olap.xmla.connector.api.ocd.SessionConfig;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osgi.namespace.unresolvable.UnresolvableNamespace;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves XMLA from the OLAP engine through the EMF-native SPI.
 * <p>
 * The request arrives as the Ecore model read it off the wire - a
 * {@link Discover} or an {@link Execute} - and the rows go back as the EObjects
 * the model writes. One method per verb, since which rowset it is lives in the
 * request.
 * <p>
 * This component registers as {@link XmlaSessionHandler} too: a session holds
 * one connection per context with the caller's roles, keyed by the id the
 * transport carries in the SOAP header. Anonymity is the endpoint's policy,
 * enforced by the transport before dispatch - this connector holds none of its
 * own.
 */
@Component(service = { XmlaConnector.class, XmlaSessionHandler.class }, configurationPid = OlapXmlaConnector.PID)
@Designate(ocd = SessionConfig.class)
public class OlapXmlaConnector extends SimpleSessionHandler implements XmlaConnector {

    public static final String PID = "daanse.olap.xmla.connector.OlapXmlaConnector";
    public static final String REF_NAME_CONTEXT_GROUP = "contextGroup";

    private static final Logger LOGGER = LoggerFactory.getLogger(OlapXmlaConnector.class);

    private ContextGroup contextGroup;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private LcidService lcidService;

    /**
     * The whiteboard: one entry per registered rowset service, keyed by the request
     * type its {@code xmla.rowset.requestType} property names. Instances come
     * through {@code ComponentServiceObjects}, so a provider registered with
     * PROTOTYPE scope gets its own instance per registration.
     */
    private final Map<String, ComponentServiceObjects<RowsetProvider<ContextListSupplyer>>> providers = new ConcurrentHashMap<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void bindRowsetProvider(ComponentServiceObjects<RowsetProvider<ContextListSupplyer>> provider,
            Map<String, Object> properties) {
        String requestType = requestTypeOf(properties);
        if (requestType != null) {
            providers.put(requestType, provider);
        }
    }

    void unbindRowsetProvider(ComponentServiceObjects<RowsetProvider<ContextListSupplyer>> provider,
            Map<String, Object> properties) {
        String requestType = requestTypeOf(properties);
        if (requestType != null) {
            providers.remove(requestType, provider);
        }
    }

    private static String requestTypeOf(Map<String, Object> properties) {
        Object value = properties.get(RowsetProvider.PROPERTY_REQUEST_TYPE);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * The two rowsets a client cannot do without, as static mandatory references: a
     * deployment missing either of them does not get a half-working endpoint, it
     * gets a connector that never comes up.
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY, target = "(" + RowsetProvider.PROPERTY_REQUEST_TYPE
            + "=DISCOVER_PROPERTIES)")
    private RowsetProvider<ContextListSupplyer> discoverProperties;

    @Reference(cardinality = ReferenceCardinality.MANDATORY, target = "(" + RowsetProvider.PROPERTY_REQUEST_TYPE
            + "=DISCOVER_SCHEMA_ROWSETS)")
    private RowsetProvider<ContextListSupplyer> discoverSchemaRowsets;

    private ContextsSupplyerImpl contexts;
    private OlapExecute execute;

    /**
     * The pending writeback values of each session, released when the session ends.
     */
    private final SessionScenarios scenarios = new SessionScenarios();

    private volatile SessionConfig config;
    private ScheduledExecutorService sweeper;

    /*
     * target must be configured. no auto fetch of a ContextGroup
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY, name = REF_NAME_CONTEXT_GROUP, target = UnresolvableNamespace.UNRESOLVABLE_FILTER)
    void bindContextGroup(ContextGroup contextGroup) {
        this.contextGroup = contextGroup;
    }

    /**
     * Assembles a connector outside OSGi, from a map of request type to provider.
     * <p>
     * The bind methods below are declarative-services callbacks, not an interface:
     * a framework calls them, in an order it decides, and nothing else should. An
     * embedding has to do the same wiring by hand, and does it through here - one
     * entry point stating what a complete connector needs, rather than four
     * package-private calls made in the right order by luck.
     * <p>
     * The session rowset is built here because it is the one provider that needs
     * the connector itself: it reports the connector's own sessions.
     *
     * @param sessionRowset the DISCOVER_SESSIONS provider, built from the
     *                      connector handed to it; {@code null} to leave that
     *                      rowset unserved
     * @param config        the session settings, usually the metatype defaults
     */
    public static OlapXmlaConnector assemble(ContextGroup contextGroup,
            Map<String, RowsetProvider<ContextListSupplyer>> providers,
            java.util.function.Function<OlapXmlaConnector, RowsetProvider<ContextListSupplyer>> sessionRowset,
            SessionConfig config) {
        OlapXmlaConnector connector = new OlapXmlaConnector();
        connector.bindContextGroup(contextGroup);
        providers.forEach((requestType, provider) -> connector.bindRowsetProvider(fixed(provider),
                Map.of(RowsetProvider.PROPERTY_REQUEST_TYPE, requestType)));
        if (sessionRowset != null) {
            connector.bindRowsetProvider(fixed(sessionRowset.apply(connector)),
                    Map.of(RowsetProvider.PROPERTY_REQUEST_TYPE, "DISCOVER_SESSIONS"));
        }
        connector.activate(config);
        return connector;
    }

    /** A service-objects wrapper over one instance, for an assembly with no OSGi. */
    private static ComponentServiceObjects<RowsetProvider<ContextListSupplyer>> fixed(
            RowsetProvider<ContextListSupplyer> provider) {
        return new ComponentServiceObjects<>() {

            @Override
            public RowsetProvider<ContextListSupplyer> getService() {
                return provider;
            }

            @Override
            public void ungetService(RowsetProvider<ContextListSupplyer> service) {
                // one instance, nothing to release
            }

            @Override
            public org.osgi.framework.ServiceReference<RowsetProvider<ContextListSupplyer>> getServiceReference() {
                return null;
            }
        };
    }

    @Activate
    void activate(SessionConfig config) {
        this.config = config;
        this.contexts = new ContextsSupplyerImpl(contextGroup);
        this.execute = new OlapExecute(contexts, scenarios, lcidService, this::dispatch);
        startSweeper();
    }

    /**
     * A configuration change must not take the sessions with it.
     * <p>
     * Without this, Declarative Services deactivates and re-activates the component
     * for every change - every session vanishes from the client's point of view,
     * and every connection it held is dropped without being closed.
     */
    @Modified
    void modified(SessionConfig config) {
        this.config = config;
        stopSweeper();
        startSweeper();
    }

    @Deactivate
    void deactivate() {
        // The sweeper first, so it cannot race the drain and close the same connections
        // twice.
        stopSweeper();
        for (XmlaSession session : sessions()) {
            expire(session.id());
        }
    }

    private void startSweeper() {
        long interval = config.sessionSweepIntervalSeconds();
        if (interval <= 0 || (config.sessionIdleTimeoutSeconds() <= 0 && config.sessionMaxLifetimeSeconds() <= 0)) {
            // Nothing expires, so there is nothing to sweep and no thread to justify.
            return;
        }
        sweeper = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "daanse-xmla-session-sweeper");
            thread.setDaemon(true);
            return thread;
        });
        // Fixed delay, not fixed rate: a slow sweep must not queue up behind itself.
        sweeper.scheduleWithFixedDelay(this::sweep, interval, interval, TimeUnit.SECONDS);
    }

    private void stopSweeper() {
        ScheduledExecutorService running = sweeper;
        sweeper = null;
        if (running != null) {
            running.shutdownNow();
        }
    }

    private void sweep() {
        Instant now = Instant.now();
        for (XmlaSession session : sessions()) {
            if (hasExpired(session, now)) {
                LOGGER.info("ending session {} after {} idle", session.id(), session.idle(now));
                expire(session.id());
            }
        }
    }

    /**
     * Idle since last use, not age since creation. Measuring age would end a
     * session a client is actively querying, which is the failure the writeback
     * scenarios still have.
     */
    private boolean hasExpired(XmlaSession session, Instant now) {
        SessionConfig current = config;
        long idle = current.sessionIdleTimeoutSeconds();
        if (idle > 0 && session.idle(now).getSeconds() > idle) {
            return true;
        }
        long lifetime = current.sessionMaxLifetimeSeconds();
        return lifetime > 0 && session.elapsed(now).getSeconds() > lifetime;
    }

    @Override
    public List<org.eclipse.emf.ecore.EObject> discover(Discover request, XmlaRequest context) {
        String requestType = request.getRequestType().getLiteral();
        return dispatch(requestType, request, context);
    }

    /**
     * The whiteboard is the dispatch: whoever registered for this request type
     * answers it. A DMV query comes through here too - already past the Execute
     * guard, so unguarded.
     * <p>
     * Nothing registered means the server does not have that rowset, and it says so
     * the way a live server says it rather than answering an empty rowset, which a
     * client would read as "there is no such data".
     */
    private List<org.eclipse.emf.ecore.EObject> dispatch(String requestType, Discover request, XmlaRequest context) {
        ComponentServiceObjects<RowsetProvider<ContextListSupplyer>> registered = providers.get(requestType);
        if (registered == null) {
            throw XmlaRefusedException.unknownRequestType(requestType);
        }
        RowsetProvider<ContextListSupplyer> provider = registered.getService();
        try {
            return provider.rows(RowsetScope.of(request, context, contexts, Set.copyOf(providers.keySet())));
        } finally {
            registered.ungetService(provider);
        }
    }

    /**
     * The request types registered on the whiteboard, which is exactly what this
     * connector can answer.
     * <p>
     * A provider already gets this set through {@code RowsetScope}; the transport
     * asks separately, when no provider answered {@code DISCOVER_SCHEMA_ROWSETS}
     * itself, so that it announces what can be answered rather than the whole model
     * - a rowset announced and then refused is a promise broken on the next
     * request.
     */
    @Override
    public Set<String> served() {
        return Set.copyOf(providers.keySet());
    }

    @Override
    public org.eclipse.emf.ecore.EObject execute(Execute request, XmlaRequest context) {
        return execute.execute(request, context);
    }

    // --- sessions: the id lifecycle is the base's; what a session means is a
    // connection
    // cache per context, with the caller's roles ---

    /**
     * Whether this session may still be used, asked once per request that presents
     * its id.
     * <p>
     * This closes the gap between two sweeps: a request arriving after the timeout
     * is refused straight away rather than served until the sweeper happens to run.
     */
    @Override
    protected boolean stillHeld(XmlaSession session) {
        return !hasExpired(session, Instant.now());
    }

    @Override
    protected boolean mayOpenSession(XmlaRequest request) {
        int limit = config.maxSessions();
        return limit <= 0 || sessions().size() < limit;
    }

    /**
     * Registers the session and opens nothing; connections come on first use.
     * <p>
     * Opening one per context here would charge a client that only wants
     * DISCOVER_DATASOURCES for every catalog, leave a half-populated session behind
     * when one fails, and - because the in-band handshake binds an identity
     * <em>after</em> BeginSession - give every connection the roles of a caller who
     * is still anonymous, which is none.
     */
    @Override
    protected void onBeginSession(String sessionId, XmlaRequest request) {
        contexts.getSessionCache().put(sessionId, new ConcurrentHashMap<>());
    }

    @Override
    protected void onEndSession(String sessionId) {
        scenarios.clear(sessionId);
        close(contexts.getSessionCache().remove(sessionId));
    }

    /**
     * The session has just learned who is calling, so what it opened while the
     * caller was still anonymous carries the wrong access and is thrown away.
     */
    @Override
    protected void onIdentityBound(String sessionId, AuthenticatedIdentity identity) {
        Map<String, Connection> held = contexts.getSessionCache().get(sessionId);
        if (held != null) {
            Map<String, Connection> opened = Map.copyOf(held);
            held.clear();
            close(opened);
        }
    }

    private static void close(Map<String, Connection> held) {
        if (held == null) {
            return;
        }
        for (Connection connection : held.values()) {
            try {
                connection.close();
            } catch (RuntimeException e) {
                LOGGER.error("failed to close a session connection", e);
            }
        }
    }
}
