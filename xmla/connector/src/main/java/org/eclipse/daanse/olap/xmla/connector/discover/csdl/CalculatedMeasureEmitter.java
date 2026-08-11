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
package org.eclipse.daanse.olap.xmla.connector.discover.csdl;

import java.math.BigInteger;
import java.util.Objects;

import org.eclipse.daanse.olap.api.catalog.CatalogReader;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.NamedSet;
import org.eclipse.daanse.olap.common.StandardProperty;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TMeasure;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TEntityProperty;

/**
 * The calculated measures of a cube, and the named sets it has to leave out.
 * <p>
 * A named set has no CSDL form, so MDSCHEMA_SETS stays the only channel for
 * those.
 */
public final class CalculatedMeasureEmitter {

    private final BiFactory bi = BiFactory.eINSTANCE;
    private final EdmFactory edm = EdmFactory.eINSTANCE;
    private final EmitContext ctx;
    private final TypeMapper typeMapper;
    private final MeasuresEntity measures;
    private final DisplayFolderBuilder folders;

    public CalculatedMeasureEmitter(EmitContext ctx, TypeMapper typeMapper, MeasuresEntity measures,
            DisplayFolderBuilder folders) {
        super();
        this.ctx = ctx;
        this.typeMapper = typeMapper;
        this.measures = measures;
        this.folders = folders;
    }

    public void emit(CatalogReader reader, Cube cube) {
        Hierarchy measuresHierarchy = cube.getDimensions().stream().filter(Dimension::isMeasures)
                .flatMap(d -> d.getHierarchies().stream()).findFirst().orElseThrow();
        for (Member member : reader.getCalculatedMembers(measuresHierarchy)) {
            emitCalculatedMeasure(member);
        }

        NamedSet[] sets = cube.getNamedSets();
        if (sets != null && sets.length > 0) {
            ctx.debug("{} named set(s) have no CSDL form; MDSCHEMA_SETS stays the channel for " + "them", sets.length);
        }
    }

    private void emitCalculatedMeasure(Member member) {
        TEntityProperty p = edm.createTEntityProperty();
        p.setName(ctx.mangle(member.getUniqueName()));

        Object datatype = member.getPropertyValue(StandardProperty.DATATYPE.getName());
        boolean typed = typeMapper.applyFromDatatypeProperty(p, datatype);

        TMeasure m = bi.createTMeasure();
        m.setIsSimpleMeasure(false);
        if (!typed) {
            p.setType("Decimal");
            p.setPrecision(BigInteger.valueOf(19));
            p.setScale(BigInteger.valueOf(4));
            m.setActualType("Variant");
        }
        m.setReferenceName(member.getName());
        if (!Objects.equals(member.getCaption(), member.getName())) {
            m.setCaption(member.getCaption());
        }
        Object format = member.getPropertyValue(StandardProperty.FORMAT_STRING.getName());
        if (format instanceof String fs && !fs.isBlank()) {
            m.setFormatString(fs);
        }
        if (!member.isVisible()) {
            m.setHidden(true);
        }

        p.setBiMeasure(m);
        measures.edmEntityType().getProperty().add(p);

        Object folder = member.getPropertyValue(StandardProperty.DISPLAY_FOLDER.getName());
        if (folder instanceof String df && !df.isBlank()) {
            folders.forProperty(measures.biEntityType(), df, p.getName());
        }
    }
}