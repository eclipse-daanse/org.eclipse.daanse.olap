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

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.catalog.CatalogReader;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.xmla.connector.discover.csdl.AssociationEmitter.RelationshipUsage;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.BiPackage;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TEntityContainer;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TEntityType;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.EntityContainerType;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TSchema;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.ExtendedMetaData;

/**
 * The one implementation of {@link CsdlEmitter}.
 * <p>
 * It walks the cubes once and hands each part to the emitter that knows it -
 * dimensions, hierarchies, measures, calculated measures, KPIs, associations -
 * with an {@link EmitContext} carrying the shared state between them.
 */
public class CsdlEmitterImpl implements CsdlEmitter {
    private static EdmFactory edmFactory = EdmFactory.eINSTANCE;
    private static BiFactory biFactory = BiFactory.eINSTANCE;

    @Override
    public TSchema emit(CatalogReader reader, CsdlRequest req) {
        Optional<String> oPerspective = req.perspective();
        if (oPerspective.isPresent()) {
            String cubeName = oPerspective.get();
            Optional<Cube> oCube = reader.getCubes().stream().filter(c -> cubeName.equals(c.getName())).findFirst();
            if (oCube.isPresent()) {
                Catalog catalog = reader.getCatalog();
                Cube cube = oCube.get();
                CsdlNames names = new CsdlNamesImpl();
                TypeMapper types = new DefaultTypeMapper();

                TSchema schema = edmFactory.createTSchema();
                schema.setNamespace(names.namespaceOf(catalog));

                EStructuralFeature versionFeature = ExtendedMetaData.INSTANCE.demandFeature(BiPackage.eNS_URI,
                        "Version", false);
                schema.getAnyAttribute().add(versionFeature, req.version());

                EntityContainerType container = edmFactory.createEntityContainerType();
                container.setName(names.namespaceOf(catalog));

                TEntityContainer biContainer = biFactory.createTEntityContainer();
                biContainer.setCaption(cube.getName());
                biContainer.setCulture(req.localePolicy().locale().getDisplayName());
                container.setBiEntityContainer(biContainer);
                schema.getEntityContainer().add(container);

                EmitContext ctx = new EmitContext(names, types, req, reader, schema);
                DisplayFolderBuilder folders = new DisplayFolderBuilder();
                HierarchyEmitter hierarchyEmitter = new HierarchyEmitter(ctx, types, folders);

                MeasuryEmiter measuryEmiter = new MeasuryEmiter();
                AssociationEmitter associationEmitter = new AssociationEmitter(ctx);
                DimensionEntityEmitter dimensionEntityEmitter = new DimensionEntityEmitter();

                List<? extends Dimension> dimensions = reader.getCubeDimensions(cube);
                Optional<? extends Dimension> oMeasureDimension = dimensions.stream().filter(d -> d.isMeasures())
                        .findAny();
                if (dimensions != null) {
                    for (Dimension dimension : dimensions) {
                        org.eclipse.daanse.xmla.model.csdl.v2.edm.TEntityType edmEntityType = edmFactory
                                .createTEntityType();
                        TEntityType biEntityType = biFactory.createTEntityType();
                        edmEntityType.setBiEntityType(biEntityType);
                        edmEntityType.setName(names.tableNameOf(dimension));
                        dimensionEntityEmitter.emitDimensionEntity(schema, container, cube, dimension, ctx);

                        schema.getEntityType().add(edmEntityType);
                        if (dimension.isMeasures()) {
                            List<Member> measures = cube.getMeasures();
                            if (measures != null) {
                                measuryEmiter.emitMeasures(schema, edmEntityType, cube, ctx);

                                MeasuresEntity measuresEntity = new MeasuresEntity(edmEntityType, biEntityType);
                                CalculatedMeasureEmitter calculatedMeasureEmitter = new CalculatedMeasureEmitter(ctx,
                                        types, measuresEntity, folders);
                                KpiEmitter kpiEmitter = new KpiEmitter(ctx, measuresEntity, folders);
                                kpiEmitter.emitKpis(cube);
                                calculatedMeasureEmitter.emit(reader, cube);
                            }
                        } else {
                            RelationshipUsage relationshipUsage = new RelationshipUsage(oMeasureDimension.get(),
                                    Optional.empty(), Optional.empty(), true);
                            hierarchyEmitter.emitDimension(dimension, edmEntityType, biEntityType);
                            associationEmitter.emit(cube, dimension, relationshipUsage);
                        }
                    }
                }
                return schema;
            } else {
                throw new CsdlEmitException("Cube is absent");
            }
        } else {
            throw new CsdlEmitException("perspective is absent in requers");
        }
    }

}
