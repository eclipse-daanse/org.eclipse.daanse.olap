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
package org.eclipse.daanse.olap.xmla.connector.rowset.provider;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.eclipse.daanse.xmla.api.XmlaSession;
import org.eclipse.daanse.xmla.api.XmlaSessionHandler;
import org.eclipse.daanse.xmla.model.rowset.server.DiscoverSessionsRow;
import org.eclipse.daanse.xmla.model.rowset.server.RowsetServerFactory;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * {@code DISCOVER_SESSIONS} ([MS-SSAS] 3.1.4.2.2.1.3.44): the sessions this
 * server currently holds.
 * <p>
 * Without it nothing about a session is observable from outside - not that it
 * exists, not how long it has been idle, not that it was ended. It reports only
 * what this server actually measures; the columns it does not measure are left
 * unset, which the model allows, because answering {@code 0} would be a
 * statement rather than a silence.
 * <p>
 * A caller sees its own sessions. Analysis Services gates this rowset behind
 * administrator rights and this server has no notion of one, so the narrow rule
 * is the safe one: widening it is a policy decision, not a default.
 */
@Component(service = RowsetProvider.class, scope = ServiceScope.PROTOTYPE, property = RowsetProvider.PROPERTY_REQUEST_TYPE
        + "=DISCOVER_SESSIONS")
public class DiscoverSessionsProvider implements RowsetProvider<ContextListSupplyer> {

    /** The activity status the specification defines; nothing here ever blocks. */
    private static final int IDLE = 0;
    private static final int ACTIVE = 1;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
    private volatile XmlaSessionHandler sessions;

    public DiscoverSessionsProvider() {
        // for Declarative Services, which injects the handler
    }

    /** For an embedded host, which holds the handler itself. */
    public DiscoverSessionsProvider(XmlaSessionHandler sessions) {
        this.sessions = sessions;
    }

    @Override
    public List<EObject> rows(RowsetScope<ContextListSupplyer> scope) {
        XmlaSessionHandler held = sessions;
        if (held == null) {
            // A stateless endpoint holds none, which is an empty answer rather than a
            // missing rowset.
            return List.of();
        }
        Instant now = Instant.now();
        List<EObject> rows = new ArrayList<>();
        for (XmlaSession session : held.sessions()) {
            if (belongsTo(session, scope.caller())) {
                rows.add(rowOf(session, now));
            }
        }
        return rows;
    }

    private static boolean belongsTo(XmlaSession session, XmlaRequest caller) {
        if (caller == null) {
            return false;
        }
        if (session.id().equals(caller.sessionId())) {
            return true;
        }
        return session.identity().map(bound -> bound.name().equals(caller.userName())).orElse(false)
                && caller.isAuthenticated();
    }

    private static DiscoverSessionsRow rowOf(XmlaSession session, Instant now) {
        DiscoverSessionsRow row = RowsetServerFactory.eINSTANCE.createDiscoverSessionsRow();
        row.setSessionId(session.id());
        session.identity().ifPresent(bound -> row.setSessionUserName(bound.name()));
        row.setSessionStartTime(utc(session.startedAt()));
        row.setSessionElapsedTimeMs(millis(session.elapsed(now)));
        row.setSessionIdleTimeMs(millis(Duration.between(session.lastUsedAt(), now)));
        session.lastCommandStartedAt().ifPresent(started -> row.setSessionLastCommandStartTime(utc(started)));
        session.lastCommandEndedAt().ifPresent(ended -> row.setSessionLastCommandEndTime(utc(ended)));
        row.setSessionCommandCount((int) Math.min(session.commandCount(), Integer.MAX_VALUE));
        row.setSessionStatus(session.busy() ? ACTIVE : IDLE);
        return row;
    }

    /** The specification asks for UTC, and says so for every time column. */
    private static LocalDateTime utc(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static BigInteger millis(Duration duration) {
        return BigInteger.valueOf(Math.max(duration.toMillis(), 0));
    }
}
