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
 */
package org.eclipse.daanse.olap.xmla.bridge.discover.csdl.dimensional;

import java.util.Optional;

import org.eclipse.daanse.xmla.csdl.model.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TDocumentation;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TText;
import org.eclipse.emf.ecore.util.FeatureMapUtil;

public class MeasuresEntity {

    private static EdmFactory edmFactory = EdmFactory.eINSTANCE;
    private final TEntityType edmEntityType;
    private final org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntityType biEntityType;
    
    public MeasuresEntity(TEntityType edmEntityType, org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntityType biEntityType) {
        this.edmEntityType = edmEntityType;
        this.biEntityType = biEntityType;
    }

    public Optional<TEntityProperty> resolveMeasureProperty(String value) {
        return edmEntityType.getProperty().stream().filter(p -> p.getName().equals(value)).findAny();
    }

    public void setEdmDocumentation(TEntityProperty anchor, String description) {
        TDocumentation documentation = edmFactory.createTDocumentation();
        TText summary = edmFactory.createTText();
        summary.getMixed().add(FeatureMapUtil.createRawTextEntry(description));
        anchor.setDocumentation(documentation);
    }

    public org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntityType biEntityType() {
        return biEntityType;
    }

    public TEntityType edmEntityType() {
        return edmEntityType;
    }

}
