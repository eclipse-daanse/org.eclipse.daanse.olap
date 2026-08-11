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

import java.util.List;

import org.eclipse.daanse.olap.xmla.connector.discover.OtherDiscover;
import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * {@code DISCOVER_PROPERTIES} — one of the two rowsets a client needs before it
 * can ask anything else, which is why the connector requires this service to be
 * present rather than treating it as optional like the rest.
 */
@Component(service = RowsetProvider.class, scope = ServiceScope.PROTOTYPE, property = RowsetProvider.PROPERTY_REQUEST_TYPE
        + "=DISCOVER_PROPERTIES")
public class DiscoverPropertiesProvider implements RowsetProvider<ContextListSupplyer> {

    @Override
    public List<EObject> rows(RowsetScope<ContextListSupplyer> scope) {
        return new OtherDiscover(scope.backend()).properties(scope.restrictions());
    }
}
