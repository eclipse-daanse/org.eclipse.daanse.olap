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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.DimensionType;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Property;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.ContainsHiddenMembersType;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.SourceType;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TDocumentation;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntityType;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.THideMembers;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.THierarchy;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TLevel;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TPropertyRefs;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TPropertyStatistics;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityProperty;


public final class HierarchyEmitter {

    private final BiFactory bi = BiFactory.eINSTANCE;
    private final EdmFactory edm = EdmFactory.eINSTANCE;
    private final EmitContext ctx;
    private final TypeMapper typeMapper;
    private final DisplayFolderBuilder folders;

    public HierarchyEmitter(EmitContext ctx, TypeMapper typeMapper, DisplayFolderBuilder folders) {
        super();
        this.ctx = ctx;
        this.typeMapper = typeMapper;
        this.folders = folders;
    }

    public void emitDimension(Dimension dimension,
                              org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType edmEntity,
                              TEntityType biEntity) {
        if (dimension.isMeasures()) {
            return;
        }
        if (dimension.getDimensionType() == DimensionType.TIME_DIMENSION) {
            biEntity.setContents("Time");
        }
        dimension.getHierarchies().stream()
            .sorted(Comparator.comparingInt(Hierarchy::getOrdinalInCube))
            .forEach(h -> emitHierarchy(h, edmEntity, biEntity));
    }

    private void emitHierarchy(Hierarchy hierarchy,
                               org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType edmEntity,
                               TEntityType biEntity) {
        List<? extends Level> levels = hierarchy.getLevels();
        List<? extends Level> real = levels.stream()
            .filter(l -> !l.isAll())
            .filter(l -> l.getLevelType() != null)
            .toList();
        if (real.isEmpty()) {
            ctx.info("Hierarchie {} ohne emittierbare Level uebersprungen",
                     hierarchy.getUniqueName());
            return;
        }
        if (real.stream().anyMatch(Level::isParentChild)) {
            ctx.warn("Parent-Child-Hierarchie {} nicht als bi:Hierarchy abbildbar; "
                   + "Spalten werden flach emittiert", hierarchy.getUniqueName());
            real.forEach(l -> emitLevelProperty(l, edmEntity));
            return;
        }

        THierarchy tHierarchy = bi.createTHierarchy();
        tHierarchy.setName(ctx.mangle(hierarchy.getUniqueName()));
        tHierarchy.setReferenceName(hierarchy.getName());
        if (!Objects.equals(hierarchy.getCaption(), hierarchy.getName())) {
            tHierarchy.setCaption(hierarchy.getCaption());
        }
        if (!hierarchy.isVisible()) {
            tHierarchy.setHidden(true);
        }
        docSummary(hierarchy.getDescription())
            .ifPresent(tHierarchy::setDocumentation);

        for (Level level : real) {
            TEntityProperty levelProp = emitLevelProperty(level, edmEntity);
            tHierarchy.getLevel().add(emitLevel(level, levelProp));
            emitMemberProperties(level, levelProp, edmEntity);
        }
        biEntity.getHierarchy().add(tHierarchy);

        folders.forHierarchy(biEntity, hierarchy.getDisplayFolder(),
                             tHierarchy.getName());
        if (hierarchy.hasAll()
                && hierarchy.getDefaultMember() != hierarchy.getAllMember()) {
            ctx.debug("DefaultMember {} von {} ist im CSDL nicht darstellbar",
                      hierarchy.getDefaultMember().getUniqueName(),
                      hierarchy.getUniqueName());
        }
    }

    private TLevel emitLevel(Level level, TEntityProperty sourceProp) {
        TLevel tLevel = bi.createTLevel();
        tLevel.setName(ctx.mangle(level.getUniqueName()));
        if (!Objects.equals(level.getCaption(), level.getName())) {
            tLevel.setCaption(level.getCaption());
        }
        SourceType source = bi.createSourceType();
        org.eclipse.daanse.xmla.csdl.model.v2.bi.TPropertyRef ref =
                bi.createTPropertyRef();
        ref.setName(ctx.requireProperty(sourceProp.getName()));
        source.setPropertyRef(ref);
        tLevel.setSource(source);

        hideMembersOf(level).ifPresent(hm -> {
            ContainsHiddenMembersType chm = bi.createContainsHiddenMembersType();
            chm.setHideMembers(hm);
            tLevel.setContainsHiddenMembers(chm);
        });
        return tLevel;
    }

    private Optional<THideMembers> hideMembersOf(Level level) {
        //TODO
        //if (level instanceof RolapLevel rl) {
        //    return switch (rl.getHideMemberCondition()) {
        //        case Never         -> Optional.empty();          // Default -> weglassen
        //        case IfBlankName   -> Optional.of(THideMembers.NO_NAME);
        //        case IfParentsName -> Optional.of(THideMembers.PARENT_NAME);
        //    };
        //}
        return Optional.empty();
    }

    private TEntityProperty emitLevelProperty(
            Level level,
            org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType edmEntity) {
        TEntityProperty p = edm.createTEntityProperty();
        p.setName(ctx.mangle(level.getUniqueName()));
        typeMapper.apply(p, level.getDatatype());
        TProperty biProp = bi.createTProperty();
        orderByOf(level).ifPresent(biProp::setOrderBy);
        statisticsOf(level).ifPresent(biProp::setStatistics);
        p.setBiProperty(biProp);
        edmEntity.getProperty().add(p);
        ctx.registerProperty(p.getName());
        return p;
    }

    private Optional<TPropertyRefs> orderByOf(Level level) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    private Optional<TPropertyStatistics> statisticsOf(Level level) {
        int approx = level.getApproxRowCount();
        if (approx < 0) {
            return Optional.empty();
        }
        TPropertyStatistics stats = bi.createTPropertyStatistics();
        stats.setDistinctValueCount(approx);
        return Optional.of(stats);
    }

    private void emitMemberProperties(
            Level level, TEntityProperty levelProp,
            org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType edmEntity) {
        for (Property property : level.getProperties()) {
            if (property.isInternal() || property.getName().startsWith("$")) {
                continue;
            }
            TEntityProperty p = edm.createTEntityProperty();
            p.setName(ctx.mangle(level.getUniqueName() + "_" + property.getName()));
            typeMapper.apply(p, property.getType());
            TProperty biProp = bi.createTProperty();
            TPropertyRefs relatedTo = bi.createTPropertyRefs();
            org.eclipse.daanse.xmla.csdl.model.v2.bi.TPropertyRef ref =
                    bi.createTPropertyRef();
            ref.setName(levelProp.getName());
            relatedTo.getPropertyRef().add(ref);
            biProp.setRelatedTo(relatedTo);
            if (!Objects.equals(property.getCaption(), property.getName())) {
                biProp.setCaption(property.getCaption());
            }
            p.setBiProperty(biProp);
            edmEntity.getProperty().add(p);
            ctx.registerProperty(p.getName());
        }
    }

    private Optional<TDocumentation> docSummary(String description) {
        if (description == null || description.isBlank()) {
            return Optional.empty();
        }
        TDocumentation doc = bi.createTDocumentation();
        doc.setSummary(description);
        return Optional.of(doc);
    }
}