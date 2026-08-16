/*
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.ContextGroup;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.connection.ConnectionProps;
import org.eclipse.daanse.olap.api.element.Catalog;

public class ContextsSupplyerImpl implements ContextListSupplyer {

    private final ContextGroup contextsGroup;
    // Mutated from request threads, so not a plain HashMap.
    private final Map<String, Map<String, Connection>> sessionCache = new ConcurrentHashMap<>();

    public ContextsSupplyerImpl(ContextGroup contextsGroup) {
        this.contextsGroup = contextsGroup;
    }

    @Override
    public List<Catalog> get(XmlaRequest caller) {
        return getContexts().stream().map(context -> getConnection(caller, context.getName()))
                .map(Connection::getCatalog).toList();
    }

    @Override
    public Optional<Catalog> tryGetFirstByName(String catalogName, XmlaRequest caller) {
        // Ask whether the catalog is here before opening a connection to it. This was
        // Optional.of(getConnection(...)), which could never be empty and so made every
        // caller's not-found branch unreachable: a client naming a catalog this server
        // does not have got the RuntimeException out of getConnection instead of the
        // empty rowset those branches were written for.
        return getContext(catalogName).map(found -> getConnection(caller, catalogName).getCatalog());
    }

    @Override
    public List<Context<?>> getContexts() {
        return contextsGroup.getValidContexts();
    }

    /**
     * The catalog of that name, matched exactly first and then ignoring case.
     * <p>
     * A catalog name reaches this from the {@code Catalog} property, where a client
     * repeats what it read from DBSCHEMA_CATALOGS - but not always in the same
     * case, and a name that does not match answers as if the catalog did not exist.
     * The exact match is tried first so that two catalogs differing only in case
     * still resolve to the one that was asked for.
     */
    @Override
    public Optional<Context<?>> getContext(String name) {
        if (name == null) {
            return Optional.empty();
        }
        List<Context<?>> all = getContexts();
        if (all == null) {
            return Optional.empty();
        }
        return all.stream().filter(c -> name.equals(c.getName())).findFirst()
                .or(() -> all.stream().filter(c -> name.equalsIgnoreCase(c.getName())).findFirst());
    }

    public Map<String, Map<String, Connection>> getSessionCache() {
        return this.sessionCache;

    }

    @Override
    public Connection getConnection(XmlaRequest caller, String catalogName) {
        Context<?> context = getContext(catalogName)
                .orElseThrow(() -> new RuntimeException("No context found for catalog " + catalogName));
        String sessionId = caller == null ? null : caller.sessionId();
        Map<String, Connection> held = sessionId == null ? null : sessionCache.get(sessionId);
        if (held != null) {
            // A session opens a connection when one is first wanted, not when it begins,
            // and keeps it until the session ends - which is also what closes it. A catalog
            // that appears mid-session becomes usable here with no special handling.
            return held.computeIfAbsent(catalogName, name -> open(caller, context));
        }
        // No session: one connection for this request, with the caller's own roles.
        // Opening it without them would answer with the catalog's default role, which
        // is
        // how metadata used to escape a restricted caller that had not opened a
        // session.
        return open(caller, context);
    }

    private Connection open(XmlaRequest caller, Context<?> context) {
        return context.getConnection(new ConnectionProps(rolesOf(caller, context)));
    }

    /**
     * The roles this caller holds that the catalog actually defines. A name the
     * catalog does not know is refused by the rolap layer, so an unknown one is
     * dropped rather than passed on.
     */
    private static List<String> rolesOf(XmlaRequest caller, Context<?> context) {
        if (caller == null) {
            return List.of();
        }
        return context.getAccessRoles().stream().filter(caller::hasRole).toList();
    }

}
