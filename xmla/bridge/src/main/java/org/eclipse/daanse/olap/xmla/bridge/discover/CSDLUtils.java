/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.xmla.bridge.discover;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.daanse.jdbc.db.dialect.api.type.Datatype;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.KPI;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.common.StandardProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.BiPackage;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.KpiGoalType;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.KpiStatusType;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.KpiTrendType;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.SourceType;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntityContainer;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntitySet;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntityType;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.THierarchy;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TKpi;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TLevel;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TMeasure;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.AssociationSetType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EdmPackage;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EndType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EntityContainerType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EntitySetType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TAssociation;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TAssociationEnd;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TDocumentation;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityKeyElement;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TMultiplicity;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TNavigationProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TPropertyRef;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TSchema;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TText;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.util.EdmResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class CSDLUtils {
    private static final String V = "v_";
    private static final String NAMESPACE = "Sandbox";
    private static EdmFactory edmFactory = EdmFactory.eINSTANCE;
    private static BiFactory biFactory = BiFactory.eINSTANCE;

    public static TSchema getCSDLModel(Catalog catalog, Optional<String> perspectiveName) {
        if (perspectiveName.isPresent()) {
            String cubeName = perspectiveName.get();
            Optional<Cube> oCube = catalog.getCubes().stream().filter(c -> cubeName.equals(c.getName())).findFirst();
            if (oCube.isPresent()) {
                Cube cube = oCube.get();
                TSchema schema = edmFactory.createTSchema();
                schema.setNamespace(NAMESPACE);

                EntityContainerType container = edmFactory.createEntityContainerType();
                //container.setName(catalog.getName());
                container.setName("Sandbox");

                TEntityContainer biContainer = biFactory.createTEntityContainer();
                biContainer.setCaption(cube.getName());
                biContainer.setCulture("en-US");
                //biContainer.setCulture("de-DE");
                container.setBiEntityContainer(biContainer);
                schema.getEntityContainer().add(container);

                List<? extends Dimension> dimensions = cube.getDimensions();
                Optional<? extends Dimension> oMeasureDimension = dimensions.stream().filter(d -> d.isMeasures()).findAny();
                if (dimensions != null) {
                    for (Dimension dimension : dimensions) {
                        addEntitySet(container, removeSquareBrackets(dimension.getUniqueName()), dimension.getCaption(), NAMESPACE + "." + removeSquareBrackets(dimension.getUniqueName()), null, !dimension.isVisible());

                        org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType dimensionType = edmFactory
                                .createTEntityType();
                        dimensionType.setName(removeSquareBrackets(dimension.getUniqueName()));

                        TEntityType entityType = biFactory.createTEntityType();
                        dimensionType.setBiEntityType(entityType);

                        if (dimension.isMeasures()) {
                            List<Member> measures = cube.getMeasures();
                            if (measures != null) {

                                TEntityKeyElement keyElement = edmFactory.createTEntityKeyElement();
                                TPropertyRef propertyRef = edmFactory.createTPropertyRef();
                                propertyRef.setName("RowNumber"); //TODO get measure key
                                keyElement.getPropertyRef().add(propertyRef);
                                dimensionType.setKey(keyElement);

                                TProperty p = biFactory.createTProperty();
                                TEntityProperty keyProperty = edmFactory.createTEntityProperty();
                                keyProperty.setName("RowNumber"); //TODO get measure key
                                keyProperty.setType("Int32");
                                keyProperty.setNullable(false);
                                keyProperty.setBiProperty(p);
                                dimensionType.getProperty().add(keyProperty);

                                for (Member member : measures) {
                                    /*
                                    boolean keyNeedFlag = true;

                                    if (keyNeedFlag) {
                                        TEntityKeyElement keyElement = edmFactory.createTEntityKeyElement();
                                        TPropertyRef propertyRef = edmFactory.createTPropertyRef();
                                        propertyRef.setName(member.getUniqueName());
                                        keyElement.getPropertyRef().add(propertyRef);
                                        dimensionType.setKey(keyElement);
                                        keyNeedFlag = false;
                                    }*/

                                    TEntityProperty valueProperty = edmFactory.createTEntityProperty();
                                    valueProperty.setName(removeSquareBrackets(member.getUniqueName()));
                                    String type = getType(member.getPropertyValue(StandardProperty.DATATYPE.getName()));
                                    //Type="Decimal" Precision="19" Scale="4"
                                    valueProperty.setType("Decimal");
                                    valueProperty.setPrecision(BigInteger.valueOf(19l));
                                    valueProperty.setScale(BigInteger.valueOf(4l));
                                    valueProperty.setNullable(false);
                                    TProperty prop = biFactory.createTProperty();
                                    valueProperty.setBiProperty(prop);

                                    // add measures
                                    TMeasure biValueMeasure = biFactory.createTMeasure();
                                    biValueMeasure.setCaption(member.getCaption());
                                    biValueMeasure.setReferenceName(member.getCaption());
                                    biValueMeasure.setFormatString("\\$#,0.00;(\\$#,0.00);\\$#,0.00"); //TODO add format from member
                                    // add kpi
                                    addKpi(biValueMeasure, member, cube.getKPIs());
                                    valueProperty.setBiMeasure(biValueMeasure);

                                    dimensionType.getProperty().add(valueProperty);
                                    schema.getEntityType().add(dimensionType);
                                }
                                TEntityType biMeasureSumType = biFactory.createTEntityType();
                                dimensionType.setBiEntityType(biMeasureSumType);
                            }
                            //add NavigationProperty
                            if (oMeasureDimension.isPresent()) {
                                for (Dimension dim : dimensions) {
                                    if (!dim.isMeasures()) {
                                        dimensionType.getNavigationProperty().add(getNavigationProperty(oMeasureDimension.get(), dim));
                                    }
                                }
                            }
                        } else {
                            List<TLevel> levels = new ArrayList<>();
                            List<? extends Hierarchy> hierarchies = dimension.getHierarchies();
                            if (hierarchies != null) {
                                for (Hierarchy hierarchy : hierarchies) {
                                    List<? extends Level> ls = hierarchy.getLevels();
                                    if (ls != null) {
                                        boolean keyNeedFlag = true;
                                        for (Level l : ls) {
                                            if (!l.isAll()) {
                                                if (keyNeedFlag) {
                                                    TEntityKeyElement keyElement = edmFactory.createTEntityKeyElement();
                                                    TPropertyRef propertyRef = edmFactory.createTPropertyRef();
                                                    propertyRef.setName(removeSquareBrackets(l.getUniqueName()));
                                                    keyElement.getPropertyRef().add(propertyRef);
                                                    dimensionType.setKey(keyElement);
                                                    keyNeedFlag = false;
                                                }
                                                TLevel tLevel = biFactory.createTLevel();
                                                tLevel.setName(removeSquareBrackets(l.getUniqueName()));
                                                tLevel.setCaption(l.getCaption());
                                                tLevel.setReferenceName(removeSquareBrackets(l.getUniqueName()));
                                                SourceType sourceType = biFactory.createSourceType();
                                                org.eclipse.daanse.xmla.csdl.model.v2.bi.TPropertyRef propertyRef = biFactory.createTPropertyRef();
                                                propertyRef.setName(removeSquareBrackets(l.getUniqueName()));
                                                sourceType.setPropertyRef(propertyRef);
                                                tLevel.setSource(sourceType);
                                                levels.add(tLevel);

                                                TEntityProperty levelProperty = edmFactory.createTEntityProperty();
                                                levelProperty.setName(removeSquareBrackets(l.getUniqueName()));
                                                levelProperty.setType(getType(l.getDatatype())); 
                                                levelProperty.setNullable(false);
                                                dimensionType.getProperty().add(levelProperty);
                                            }
                                        }
                                    }
                                    THierarchy hierarchyTHierarchy = biFactory.createTHierarchy();
                                    hierarchyTHierarchy.setCaption(hierarchy.getCaption());
                                    hierarchyTHierarchy.setName(removeSquareBrackets(hierarchy.getUniqueName()));
                                    hierarchyTHierarchy.setReferenceName(removeSquareBrackets(hierarchy.getUniqueName()));

                                    hierarchyTHierarchy.getLevel().addAll(levels);
                                    entityType.getHierarchy().add(hierarchyTHierarchy);

                                    schema.getEntityType().add(dimensionType);
                                }
                            }
                        }
                    }
                    for (Dimension dimension : dimensions) {
                    //create AssociationSet
                        if (oMeasureDimension.isPresent()) {
                            if (!dimension.isMeasures()) {
                                container.getAssociationSet().add(getAssociationSetType(oMeasureDimension.get(), dimension));
                                schema.getAssociation().add(getAssociation(oMeasureDimension.get(), dimension));
                            }
                        }
                    }
                }
                return schema;
            }
        }
        return edmFactory.createTSchema();
    }

    private static void addKpi(TMeasure biValueMeasure, Member member, List<? extends KPI> kpIs) {
        if (kpIs != null) {
            Optional<? extends KPI> oKpi = kpIs.stream().filter(
                    k -> (member.getUniqueName().equals(k.getGoal())
                    || member.getUniqueName().equals(k.getStatus())
                    || member.getUniqueName().equals(k.getTrend()))).findAny();
            if (oKpi.isPresent()) {
                KPI kpi = oKpi.get();
                TKpi tKpi = biFactory.createTKpi();
                tKpi.setStatusGraphic(kpi.getName());
                if (member.getUniqueName().equals(kpi.getGoal())) {
                    KpiGoalType kpiGoalType = biFactory.createKpiGoalType();
                    org.eclipse.daanse.xmla.csdl.model.v2.bi.TPropertyRef tproperty = biFactory.createTPropertyRef();
                    tproperty.setName(V + removeSquareBrackets(member.getUniqueName())  + "_Goal");
                    kpiGoalType.setPropertyRef(tproperty);
                    tKpi.setKpiGoal(kpiGoalType);
                }
                if (member.getUniqueName().equals(kpi.getStatus())) {
                    KpiStatusType kpiGoalType = biFactory.createKpiStatusType();
                    org.eclipse.daanse.xmla.csdl.model.v2.bi.TPropertyRef tproperty = biFactory.createTPropertyRef();
                    tproperty.setName(V + removeSquareBrackets(member.getUniqueName())  + "_Status");
                    kpiGoalType.setPropertyRef(tproperty);
                    tKpi.setKpiStatus(kpiGoalType);
                }
                if (member.getUniqueName().equals(kpi.getTrend())) {
                    KpiTrendType kpiGoalType = biFactory.createKpiTrendType();
                    org.eclipse.daanse.xmla.csdl.model.v2.bi.TPropertyRef tproperty = biFactory.createTPropertyRef();
                    tproperty.setName(V + removeSquareBrackets(member.getUniqueName())  + "_Trend");
                    kpiGoalType.setPropertyRef(tproperty);
                    tKpi.setKpiTrend(kpiGoalType);
                }
                biValueMeasure.setKpi(tKpi);
            }
        }
        
    }

    public static void addEntitySet(EntityContainerType container, String name, String caption, String entityType, String docSummary,
            Boolean hidden) {
        EntitySetType entitySet = edmFactory.createEntitySetType();
        entitySet.setName(name);
        entitySet.setEntityType(entityType);

        if (docSummary != null) {
            TDocumentation doc = edmFactory.createTDocumentation();
            TText summary = edmFactory.createTText();
            summary.getMixed().add(FeatureMapUtil.createRawTextEntry(docSummary));
            doc.setSummary(summary);
            entitySet.setDocumentation(doc);
        }

        TEntitySet biEntitySet = biFactory.createTEntitySet();
        if (hidden != null && hidden) {
            biEntitySet.setHidden(true);
        }
        biEntitySet.setCaption(caption);
        entitySet.setBiEntitySet(biEntitySet);
        container.getEntitySet().add(entitySet);
    }

    private static TNavigationProperty getNavigationProperty(Dimension measureDimension, Dimension dimension) {
        TNavigationProperty navigationProperty = edmFactory.createTNavigationProperty();
        String name = getAssociationSetTypeName(measureDimension, dimension);
        navigationProperty.setName(removeSquareBrackets(dimension.getUniqueName())); //TODO add column name
        navigationProperty.setRelationship(NAMESPACE + "." + name);
        navigationProperty.setFromRole(removeSquareBrackets(measureDimension.getUniqueName())); //TODO add column name
        navigationProperty.setToRole(removeSquareBrackets(dimension.getUniqueName())); //TODO add column name
        return navigationProperty;
    }

    private static TAssociation getAssociation(Dimension measureDimension, Dimension dimension) {
        TAssociation association = edmFactory.createTAssociation();
        String name = getAssociationSetTypeName(measureDimension, dimension);
        association.setName(name);
        TAssociationEnd measureDimensionAssociationEnd = edmFactory.createTAssociationEnd();
        measureDimensionAssociationEnd.setRole(removeSquareBrackets(measureDimension.getUniqueName())); //TODO add name colunm to name
        measureDimensionAssociationEnd.setType(NAMESPACE + "." + removeSquareBrackets(measureDimension.getUniqueName()));
        measureDimensionAssociationEnd.setMultiplicity(TMultiplicity.__);
        TAssociationEnd dimensionAssociationEnd = edmFactory.createTAssociationEnd();
        dimensionAssociationEnd.setRole(removeSquareBrackets(dimension.getUniqueName())); //TODO add name colunm to name
        dimensionAssociationEnd.setType(NAMESPACE + "." + removeSquareBrackets(dimension.getUniqueName()));
        dimensionAssociationEnd.setMultiplicity(TMultiplicity._01);
        association.getEnd().add(measureDimensionAssociationEnd);
        association.getEnd().add(dimensionAssociationEnd);
        return association;
    }

    private static AssociationSetType getAssociationSetType(Dimension measureDimension, Dimension dimension) {
        AssociationSetType associationSetType = edmFactory.createAssociationSetType();
        String name = getAssociationSetTypeName(measureDimension, dimension);
        associationSetType.setName(name);
        associationSetType.setAssociation(NAMESPACE + "." + name);
        EndType measureDimensionEndType = edmFactory.createEndType();
        measureDimensionEndType.setEntitySet(removeSquareBrackets(measureDimension.getUniqueName()));
        EndType dimensionEndType = edmFactory.createEndType();
        dimensionEndType.setEntitySet(removeSquareBrackets(dimension.getUniqueName()));
        associationSetType.getEnd().add(measureDimensionEndType);
        associationSetType.getEnd().add(dimensionEndType);
        return associationSetType;
    }

    private static String getAssociationSetTypeName(Dimension measureDimension, Dimension dimension) {
        return removeSquareBrackets(measureDimension.getUniqueName()) + "_" + removeSquareBrackets(dimension.getUniqueName()); //TODO add name column to name
    }

    private static String removeSquareBrackets(String name) {
        return name.replace("[", "").replace("]", "").replace(".", "_").replace(" ", "_");
    }

    private static String getType(Object propertyValue) {
        if (propertyValue != null && propertyValue instanceof String value) {
            switch (value) {
            case "UNDEFINED":
            case "String":
                return "String";
            case "NUMERIC":
                return "Decimal";
            case "Integer":
                return "Int64";
            case "Boolean":
                return "Boolean";
            case "Date":
                return "DateTime";
            case "Time":
                return "DateTime";
            case "Timestamp":
                return "Timestamp";
            default:
                return "String";
            }
        }
        return "String";
    }

    private static String getType(Datatype type) {
        if (type != null) {
            switch (type) {
            case VARCHAR:
                return "String";
            case NUMERIC:
                return "Decimal";
            case INTEGER:
                return "Int64";
            case BIGINT:
                return "Int64";
            case SMALLINT:
                return "Int64";
            case BOOLEAN:
                return "Boolean";
            case DATE:
                return "DateTime";
            case TIME:
                return "DateTime";
            case TIMESTAMP:
                return "Timestamp";
            case DOUBLE:
                return "Double";
            case REAL:
                return "Double";
            case FLOAT:
                return "Double";
            case DECIMAL:
                return "Decimal";
            default:
                return "String";
            }
        }
        return "String";
    }

    public static String getCSDLModelAsString(TSchema schema) {
        //return fff;

        try {
            return serializeToXml(schema);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String serializeToXml(EObject eObject) throws IOException {
        ResourceSetImpl resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new EdmResourceFactoryImpl());
        // Register packages
        resourceSet.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(BiPackage.eNS_URI, BiPackage.eINSTANCE);

        Resource resource = resourceSet.createResource(URI.createURI("temp.xml"));
        resource.getContents().add(eObject);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Map<String, Object> options = new HashMap<>();
        options.put(XMLResource.OPTION_ENCODING, "UTF-8");
        options.put(XMLResource.OPTION_FORMATTED, Boolean.TRUE);
        options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);

        resource.save(baos, options);

        resource.getContents().clear();
        resourceSet.getResources().remove(resource);

        return baos.toString("UTF-8");
    }

}
