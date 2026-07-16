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

import java.util.function.Consumer;

import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.OlapElement;
import org.eclipse.daanse.olap.xmla.bridge.discover.csdl.dimensional.TypeMapper.EdmType;
import org.eclipse.daanse.olap.xmla.bridge.discover.csdl.dimensional.TypeMapper.EdmType.Facets;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntitySet;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EntityContainerType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EntitySetType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TDocumentation;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TSchema;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TText;
import org.eclipse.emf.ecore.util.FeatureMapUtil;

public class DimensionEntityEmitter {

    void emitDimensionEntity(TSchema schema, EntityContainerType container,
                             Cube cube, Dimension dim, EmitContext ctx) {
        String table = ctx.names().tableNameOf(dim);
        EntitySetType entitySet = ctx.edm().createEntitySetType();
        entitySet.setName(table);
        entitySet.setEntityType(ctx.qualified(table));
        
        attachDocumentation(entitySet::setDocumentation, dim, ctx);
        
        TEntitySet biEntitySet = ctx.bi().createTEntitySet();
        if (!dim.isVisible()) {
            biEntitySet.setHidden(true);
        }
        biEntitySet.setCaption(table);
        entitySet.setBiEntitySet(biEntitySet);

        container.getEntitySet().add(entitySet);
    }
    
    
    void attachDocumentation(Consumer<TDocumentation> slot, OlapElement el, EmitContext ctx) {
        String desc = el.getDescription();
        if (desc == null || desc.isBlank()) {
            return;
        }
        TDocumentation doc = ctx.edm().createTDocumentation();
        TText summary = ctx.edm().createTText();
        summary.getMixed().add(FeatureMapUtil.createRawTextEntry(firstLine(desc)));
        doc.setSummary(summary);
        if (!firstLine(desc).equals(desc.strip())) {
            TText longDesc = ctx.edm().createTText();
            longDesc.getMixed().add(FeatureMapUtil.createRawTextEntry(desc));
            doc.setLongDescription(longDesc);
        }
        slot.accept(doc);
    }

    private String firstLine(String desc) {
        if (desc == null) return null;
        return desc.split("\n", 2)[0];
    }
}
