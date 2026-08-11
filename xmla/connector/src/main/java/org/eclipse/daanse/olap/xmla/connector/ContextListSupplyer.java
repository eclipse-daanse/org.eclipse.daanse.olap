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
import java.util.Optional;

import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Catalog;

public interface ContextListSupplyer {

    List<Context<?>> getContexts();

    /**
     * The catalogs this caller may see.
     * <p>
     * The caller rather than the session id, because the roles decide what a
     * catalog shows and a request without a session has them just the same.
     */
    List<Catalog> get(XmlaRequest caller);

    /**
     * The named catalog, or empty when this server does not have it.
     * <p>
     * Empty is an answer, not an error: a client that restricts CATALOG_NAME to a
     * catalog which is not here has asked a well-formed question whose answer is no
     * rows. Every caller relies on that and turns empty into an empty rowset rather
     * than a fault, so an implementation must not throw for an unknown name.
     */
    Optional<Catalog> tryGetFirstByName(String catalogName, XmlaRequest caller);

    Optional<Context<?>> getContext(String name);

    /**
     * The connection to serve this caller from: the one the session holds, or a new
     * one opened with the caller's roles.
     */
    Connection getConnection(XmlaRequest caller, String catalogName);
}
