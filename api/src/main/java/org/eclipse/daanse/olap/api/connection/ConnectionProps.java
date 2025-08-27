/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.api.connection;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Connection properties for configuring OLAP connections.
 * 
 * @param roles                List of role names to adopt for this connection.
 *                             If empty, the connection uses a role which has
 *                             access to every object in the schema.
 * @param useCatalogCache      Whether to enable the catalog cache. If false,
 *                             the catalog is not shared with connections which
 *                             have a textually identical schema. Default is
 *                             true.
 * @param locale               The requested locale for the
 *                             LocalizingDynamicSchemaProcessor. Example values
 *                             are "en", "en_US", "hu". If not specified, the
 *                             system's default locale will be used.
 * @param pinCatalogTimeout    Defines how long Daanse keeps a hard reference to
 *                             Catalog objects within the cache. After the
 *                             timeout is reached, the hard reference will be
 *                             cleared and the catalog will be made a candidate
 *                             for garbage collection. If the timeout is zero,
 *                             the catalog will get pinned permanently. If the
 *                             timeout is negative, the reference will behave
 *                             the same as a SoftReference (default behavior).
 * @param sessionId            Optional session identifier for connection
 *                             isolation.
 * @param aggregateScanSchema  Optional database schema name to scan when
 *                             looking for aggregate tables. If defined, Daanse
 *                             will only look for aggregate tables within this
 *                             schema. If not defined, Daanse will scan every
 *                             schema that the database connection has access
 *                             to.
 * @param aggregateScanCatalog Optional database catalog name to scan when
 *                             looking for aggregate tables. If defined, Daanse
 *                             will only look for aggregate tables within this
 *                             catalog. If not defined, Daanse will scan every
 *                             catalog the database connection has access to.
 */

public record ConnectionProps(List<String> roles, boolean useCatalogCache, Locale locale, Duration pinCatalogTimeout,
        Optional<String> sessionId, Optional<String> aggregateScanSchema, Optional<String> aggregateScanCatalog) {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(-1);

    public ConnectionProps() {
        this(List.of(), true, Locale.getDefault(), DEFAULT_TIMEOUT, Optional.empty(), Optional.empty(),
                Optional.empty());
    }

    public ConnectionProps(List<String> roles) {
        this(roles, true, Locale.getDefault(), DEFAULT_TIMEOUT, Optional.empty(), Optional.empty(), Optional.empty());
    }

    public ConnectionProps(List<String> roles, Locale locale) {
        this(roles, true, locale, DEFAULT_TIMEOUT, Optional.empty(), Optional.empty(), Optional.empty());
    }

    public ConnectionProps(List<String> roles, String sessionId) {
        this(roles, true, Locale.getDefault(), DEFAULT_TIMEOUT, Optional.ofNullable(sessionId), Optional.empty(),
                Optional.empty());
    }

    public ConnectionProps(List<String> roles, Locale locale, String sessionId) {
        this(roles, true, locale, DEFAULT_TIMEOUT, Optional.ofNullable(sessionId), Optional.empty(), Optional.empty());
    }

}
