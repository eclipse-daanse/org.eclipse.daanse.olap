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

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.BiPackage;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TState;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.AssociationSetType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EndType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TAssociation;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TAssociationEnd;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TConstraint;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TMultiplicity;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TNavigationProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TPropertyRef;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TReferentialConstraintRoleElement;


public final class AssociationEmitter {

    private static final EdmFactory EDM = EdmFactory.eINSTANCE;
    private static final BiFactory BI = BiFactory.eINSTANCE;

    private final EmitContext ctx;

    public AssociationEmitter(EmitContext ctx) {
        this.ctx = ctx;
    }

    public record RelationshipUsage(
            Dimension dimension,
            Optional<String> factForeignKeyColumn,   
            Optional<String> dimensionKeyColumn,     
            boolean active) {
    }

    public void emit(Cube cube, Dimension measuresDimension, RelationshipUsage usage) {
        String factEntity = ctx.entityName(measuresDimension);
        String dimEntity  = ctx.entityName(usage.dimension());

        String fk  = usage.factForeignKeyColumn().map(ctx::sanitize).orElse(null);
        String key = usage.dimensionKeyColumn().map(ctx::sanitize)
                .orElseGet(() -> lowestLevelKeyProperty(usage.dimension()).orElse(dimEntity));
        String assocName = ctx.unique(fk != null
                ? factEntity + "_" + dimEntity + "_" + dimEntity + "_" + fk
                : factEntity + "_" + dimEntity);
        String factRole = fk != null ? factEntity + "_" + fk : factEntity;
        String dimRole  = dimEntity + "_" + key;

        TAssociation association = EDM.createTAssociation();
        association.setName(assocName);
        TAssociationEnd factEnd = EDM.createTAssociationEnd();
        factEnd.setRole(factRole);
        factEnd.setType(ctx.qualified(factEntity));
        factEnd.setMultiplicity(TMultiplicity.__);      // "*"
        TAssociationEnd dimEnd = EDM.createTAssociationEnd();
        dimEnd.setRole(dimRole);
        dimEnd.setType(ctx.qualified(dimEntity));
        dimEnd.setMultiplicity(TMultiplicity._01);      // "0..1"
        association.getEnd().add(factEnd);
        association.getEnd().add(dimEnd);

        if (fk != null && ctx.factEntityHasProperty(factEntity, fk)) {
            TConstraint constraint = EDM.createTConstraint();
            constraint.setPrincipal(roleElement(dimRole, key));
            constraint.setDependent(roleElement(factRole, fk));
            association.setReferentialConstraint(constraint);
        }
        ctx.schema().getAssociation().add(association);

        AssociationSetType set = EDM.createAssociationSetType();
        set.setName(assocName);
        set.setAssociation(ctx.qualified(assocName));
        EndType factSetEnd = EDM.createEndType();
        factSetEnd.setEntitySet(factEntity);
        factSetEnd.setRole(factRole);
        EndType dimSetEnd = EDM.createEndType();
        dimSetEnd.setEntitySet(dimEntity);
        dimSetEnd.setRole(dimRole);
        set.getEnd().add(factSetEnd);
        set.getEnd().add(dimSetEnd);

        var biSet = BI.createTAssociationSet();
        if (!usage.dimension().isVisible() || ctx.isHiddenByPerspective(usage.dimension())) {
            biSet.setHidden(true);
        }
        if (!usage.active()) {
            biSet.setState(TState.INACTIVE);
        }
        set.getAny().add(BiPackage.eINSTANCE.getDocumentRoot_AssociationSet(), biSet);
        ctx.container().getAssociationSet().add(set);

        TNavigationProperty nav = EDM.createTNavigationProperty();
        nav.setName(dimRole);
        nav.setRelationship(ctx.qualified(assocName));
        nav.setFromRole(factRole);
        nav.setToRole(dimRole);
        nav.getAny().add(BiPackage.eINSTANCE.getDocumentRoot_NavigationProperty(),
                BI.createTNavigationProperty());
        ctx.entityType(factEntity).getNavigationProperty().add(nav);
    }

    private TReferentialConstraintRoleElement roleElement(String role, String property) {
        TReferentialConstraintRoleElement e = EDM.createTReferentialConstraintRoleElement();
        e.setRole(role);
        TPropertyRef ref = EDM.createTPropertyRef();
        ref.setName(property);
        e.getPropertyRef().add(ref);
        return e;
    }

    private Optional<String> lowestLevelKeyProperty(Dimension dimension) {
        List<? extends Hierarchy> hs = dimension.getHierarchies();
        if (hs == null || hs.isEmpty()) {
            return Optional.empty();
        }
        List<? extends Level> levels = hs.get(0).getLevels();
        if (levels == null) {
            return Optional.empty();
        }
        return levels.stream().filter(l -> !l.isAll())
                .reduce((first, second) -> second)
                .map(l -> ctx.sanitize(l.getUniqueName()));
    }
}