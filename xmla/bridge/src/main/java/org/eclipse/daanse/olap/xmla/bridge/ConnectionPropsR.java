/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.xmla.bridge;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.daanse.olap.api.connection.ConnectionProps;

public record ConnectionPropsR(List<String> roles, boolean useCatalogCache, Locale locale, Duration pinSchemaTimeout, Optional<String> aggregateScanSchema, Optional<String> aggregateScanCatalog) implements ConnectionProps {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(-1);
    
    public ConnectionPropsR(List<String> roles) {
        this(roles, true, Locale.getDefault(), DEFAULT_TIMEOUT, Optional.empty(), Optional.empty());
    }
    
    public ConnectionPropsR(List<String> roles, Locale locale) {
        this(roles, true, locale, DEFAULT_TIMEOUT, Optional.empty(), Optional.empty());
    }

}
