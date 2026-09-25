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
 *   dbulahov - initial
 */
package org.eclipse.daanse.olap.api.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.olap.api.connection.Connection;

/**
 * A query language served next to MDX, SQL and DMV without the OLAP side
 * knowing it: an implementation registers as a service, and a caller that
 * receives statement texts - the XMLA connector - hands it those it
 * {@link #accepts(String) accepts} instead of parsing them itself.
 * <p>
 * The results are plain {@link ResultSet}s, so a caller renders them as it
 * renders any tabular result, e.g. as an XMLA rowset.
 * </p>
 * <p>
 * Implementations must be thread-safe.
 * </p>
 */
public interface StatementLanguage {

    /**
     * Tells whether a statement text is in this language. Decided lexically,
     * e.g. by its first keyword, and cheaply: it is asked for every statement
     * before MDX parsing.
     *
     * @param statement the statement text
     * @return whether to hand the statement to {@link #execute}
     */
    boolean accepts(String statement);

    /**
     * Executes a statement on a connection, with the connection's catalog, role
     * and locale.
     *
     * @param connection the connection; the caller keeps it open while it reads
     *                   the result sets and closes it afterwards
     * @param statement  a statement this language {@link #accepts(String)
     *                   accepts}
     * @param properties properties of the caller's request that concern the
     *                   statement, by name, e.g. {@code Catalog} or
     *                   {@code Cube}; a language ignores those it does not know
     * @return the result sets, one per result table, in order; the caller
     *         closes them
     * @throws SQLException if the statement failed; its message is meant for
     *                      the client, and its SQL state tells how it failed
     */
    List<ResultSet> execute(Connection connection, String statement, Map<String, String> properties)
            throws SQLException;
}
