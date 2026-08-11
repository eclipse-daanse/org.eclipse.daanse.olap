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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.StoredMeasure;
import org.eclipse.daanse.olap.common.StandardProperty;
import org.eclipse.daanse.olap.xmla.connector.discover.csdl.TypeMapper.EdmType;
import org.eclipse.daanse.olap.xmla.connector.discover.csdl.TypeMapper.EdmType.Facets;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.DefaultMeasureType;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TDisplayFolder;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TDisplayFolders;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TMeasure;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TMemberRef;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TDocumentation;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TEntityProperty;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TEntityType;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TSchema;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TText;
import org.eclipse.emf.ecore.util.FeatureMapUtil;

/**
 * The measures of a cube, stored and calculated alike, as properties of the
 * measures entity.
 */
public class MeasuryEmiter {

    public void emitMeasures(TSchema schema, TEntityType measuresType, Cube cube, EmitContext ctx) {
        for (Member member : cube.getMeasures()) { // MEA-01
            MeasureKind kind = member instanceof StoredMeasure ? MeasureKind.STORED
                    : member.isCalculated() ? MeasureKind.CALCULATED : MeasureKind.STORED;
            emitMeasureProperty(measuresType, member, kind, ctx);
        }
        emitDisplayFolders(measuresType, cube, ctx);
    }

    private void emitDisplayFolders(TEntityType measuresType, Cube cube, EmitContext ctx) {
        TDisplayFolders root = ctx.bi().createTDisplayFolders();
        Map<String, TDisplayFolder> folderByPath = new LinkedHashMap<>();

        for (Member member : cube.getMeasures()) {
            Object df = member.getPropertyValue(StandardProperty.DISPLAY_FOLDER.getName());
            if (!(df instanceof String path) || path.isBlank()) {
                continue;
            }
            for (String assignment : path.split(";")) {
                TDisplayFolder parent = null;
                StringBuilder walked = new StringBuilder();
                for (String segment : assignment.split("[\\\\/]")) {
                    walked.append('\\').append(segment);
                    TDisplayFolder folder = folderByPath.get(walked.toString());
                    if (folder == null) {
                        folder = ctx.bi().createTDisplayFolder();
                        folder.setName(ctx.names().encode(segment));
                        folder.setCaption(segment);
                        (parent == null ? root.getDisplayFolder() : parent.getDisplayFolder()).add(folder);
                        folderByPath.put(walked.toString(), folder);
                    }
                    parent = folder;
                }
                var ref = ctx.bi().createTPropertyRef();
                String propName = ctx.names().measureNameOf(member);
                ref.setName(propName);
                parent.getPropertyRef().add(ref);
            }
        }
        if (!root.getDisplayFolder().isEmpty()) {
            measuresType.getBiEntityType().setDisplayFolders(root);
        }
    }

    private void emitMeasureProperty(TEntityType owner, Member member, MeasureKind kind, EmitContext ctx) {
        TEntityProperty p = ctx.edm().createTEntityProperty();
        p.setName(ctx.names().measureNameOf(member));

        Optional<String> aggName = (member instanceof StoredMeasure sm) ? Optional.of(sm.getAggregateFunction())
                : Optional.empty();

        Optional<TypeMapper.EdmType> source = sourceTypeOf(member, ctx); // s. u.
        TypeMapper.EdmType type = ctx.types().measureType(aggName.orElse("None"), source);
        p.setType(type.type().getLiteral());
        applyFacets(p, type.facets());

        TMeasure bi = ctx.bi().createTMeasure();
        bi.setCaption(member.getCaption());
        if (!p.getName().equals(member.getName())) {
            bi.setReferenceName(member.getName());
        }
        boolean simple = kind == MeasureKind.STORED && aggName.map(SIMPLE_AGGREGATORS::contains).orElse(false);
        bi.setIsSimpleMeasure(simple);
        if (aggName.isPresent() && !SIMPLE_AGGREGATORS.contains(aggName.get())) {
            attachDocSummary(p, "server-side aggregation: " + aggName.get(), ctx);
        }
        applyFormatAndVisibility(bi, member);
        p.setBiMeasure(bi);

        owner.getProperty().add(p);
    }

    private void attachDocSummary(TEntityProperty p, String string, EmitContext ctx) {
        TDocumentation documentation = ctx.edm().createTDocumentation();
        TText summary = ctx.edm().createTText();
        summary.getMixed().add(FeatureMapUtil.createRawTextEntry(string));
        documentation.setSummary(summary);
        p.setDocumentation(documentation);
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

    void emitDefaultMeasure(TEntityType measuresType, Cube cube, EmitContext ctx) {
        Hierarchy measuresHierarchy = cube.getDimensions().stream().filter(Dimension::isMeasures).findFirst()
                .map(d -> d.getHierarchies().getFirst()).orElse(null);
        if (measuresHierarchy == null) {
            return;
        }
        Member def = measuresHierarchy.getDefaultMember();
        if (def == null || !isEmittedVisibleMeasure(def, ctx)) {
            return;
        }
        DefaultMeasureType dm = ctx.bi().createDefaultMeasureType();
        TMemberRef ref = ctx.bi().createTMemberRef();
        String propName = ctx.names().measureNameOf(def);
        ref.setName(propName);
        dm.setMemberRef(ref);
        measuresType.getBiEntityType().setDefaultMeasure(dm);
    }

    private boolean isEmittedVisibleMeasure(Member def, EmitContext ctx) {
        return !def.isHidden();
    }

    private void applyFormatAndVisibility(TMeasure bi, Member member) {
        Object fmt = member.getPropertyValue(StandardProperty.FORMAT_STRING.getName());
        if (fmt instanceof String s && !s.isBlank()) {
            bi.setFormatString(s);
        }
        Object visible = member.getPropertyValue(StandardProperty.VISIBLE.getName());
        if (Boolean.FALSE.equals(visible)) {
            bi.setHidden(true);
        }
    }

    private static final Set<String> SIMPLE_AGGREGATORS = Set.of("sum", "count", "avg", "min", "max", "distinct-count");

    Optional<TypeMapper.EdmType> sourceTypeOf(Member member, EmitContext ctx) {
        Object dt = member.getPropertyValue(StandardProperty.DATATYPE.getName());
        if (dt instanceof String s) {
            return dataTypeLiteralToEdm(s, ctx);
        }
        if (member instanceof StoredMeasure sm) {
            return ctx.types().forMeasure(sm.getDataType());
        }

        return Optional.empty();
    }

    private Optional<EdmType> dataTypeLiteralToEdm(String s, EmitContext ctx) {
        return ctx.types().stringEdmType(s);
    }

    enum MeasureKind {
        STORED, CALCULATED
    }
}
