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

    TEntityProperty levelProperty(TEntityType owner, Level level, EmitContext ctx) {
        TEntityProperty p = ctx.edm().createTEntityProperty();
        p.setName(ctx.names().columnNameOf(level));
        EdmType t = ctx.types().levelType(level.getDatatype());
        p.setType(t.type().getLiteral());
        applyFacets(p, t.facets());
        TProperty biProp = ctx.bi().createTProperty();
        biProp.setCaption(level.getCaption());
        p.setBiProperty(biProp);
        owner.getProperty().add(p);
        ctx.registerProperty(owner.getName(), p.getName());
        return p;
    }
    
 private void applyFacets(TEntityProperty p, Facets f) {
     if (f != null) {
         p.setMaxLength(f.maxLength());
         p.setNullable(f.nullable());
         p.setPrecision(f.precision());
         p.setScale(f.scale());
         p.setUnicode(f.unicode());
         p.setFixedLength(f.fixedLength());
     }
        
    }

    void emitDimensionEntity(TSchema schema, EntityContainerType container,
                             Cube cube, Dimension dim, EmitContext ctx) {
        String table = ctx.names().tableNameOf(dim);
        String qname = ctx.names().namespaceOf(ctx.catalog()) + "." + table;

        EntitySetType entitySet = ctx.edm().createEntitySetType();
        entitySet.setName(table);
        entitySet.setEntityType(qname);
        TEntitySet biSet = ctx.bi().createTEntitySet();
        biSet.setCaption(dim.getCaption());
        if (!dim.isVisible()) {
            biSet.setHidden(true);
        }
        if (!table.equals(dim.getName())) {
            biSet.setReferenceName(dim.getName());
        }
        entitySet.setBiEntitySet(biSet);
        container.getEntitySet().add(entitySet);

        TEntityType entityType = ctx.edm().createTEntityType();
        entityType.setName(table);
        entityType.setBiEntityType(ctx.bi().createTEntityType());
        attachDocumentation(entityType::setDocumentation, dim, ctx);
        schema.getEntityType().add(entityType);
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
