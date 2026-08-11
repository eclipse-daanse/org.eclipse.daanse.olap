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
package org.eclipse.daanse.olap.xmla.connector.discover;

import java.util.Map;

import org.eclipse.daanse.xmla.model.xmla.Discover;
import org.eclipse.daanse.xmla.model.xmla.RequestTypeEnum;
import org.eclipse.daanse.xmla.model.xmla.RestrictionEntry;
import org.eclipse.daanse.xmla.model.xmla.XmlaFactory;

/**
 * Builds the model {@code Discover} the way the transport would have read it
 * off the wire.
 */
final class Requests {

    private Requests() {
        // static access only
    }

    static Discover discover(String requestType, Map<String, String> restrictions) {
        XmlaFactory factory = XmlaFactory.eINSTANCE;
        Discover request = factory.createDiscover();
        request.setRequestType(RequestTypeEnum.get(requestType));
        request.setRestrictions(factory.createRestrictions());
        for (Map.Entry<String, String> restriction : restrictions.entrySet()) {
            RestrictionEntry entry = factory.createRestrictionEntry();
            entry.setName(restriction.getKey());
            entry.setValue(restriction.getValue());
            request.getRestrictions().getRestrictionList().add(entry);
        }
        return request;
    }
}
