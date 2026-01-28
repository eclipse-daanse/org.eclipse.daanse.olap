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
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.common.StandardProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.BiPackage;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntityContainer;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntitySet;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntityType;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.THierarchy;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TLevel;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TMeasure;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.AssociationSetType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EdmPackage;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EndType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EntityContainerType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EntitySetType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TAssociation;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TAssociationEnd;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityKeyElement;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TMultiplicity;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TNavigationProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TPropertyRef;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TSchema;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.util.EdmResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class CSDLUtils {
    private static final String NAMESPACE = "Sandbox";
    private static EdmFactory edmFactory = EdmFactory.eINSTANCE;
    private static BiFactory biFactory = BiFactory.eINSTANCE;

    private static final String csdlMd3 = """
            <Schema
                xmlns="http://schemas.microsoft.com/ado/2008/09/edm"
                xmlns:edm_annotation="http://schemas.microsoft.com/ado/2009/02/edm/annotation"
                xmlns:bi="http://schemas.microsoft.com/sqlbi/2010/10/edm/extensions" bi:Version="2.0" Namespace="Model">
              <EntityContainer Name="Model">
                <Documentation>
                  <Summary>%s</Summary>
                </Documentation>
                %s
                <bi:EntityContainer Caption="Model" Culture="en-US"/>
             </EntityContainer>
             %s
            </Schema>
              """;

    private static final String csdlMd4 = """
            <Schema
                xmlns="http://schemas.microsoft.com/ado/2008/09/edm"
                xmlns:edm_annotation="http://schemas.microsoft.com/ado/2009/02/edm/annotation"
                xmlns:bi="http://schemas.microsoft.com/sqlbi/2010/10/edm/extensions" bi:Version="2.0" Namespace="Model">
              <EntityContainer Name="Model">
                <bi:EntityContainer Caption="Model" Culture="en-US"/>
             </EntityContainer>
            </Schema>
              """;

    public static TSchema getCSDLModel(Catalog catalog, Optional<String> perspectiveName) {
        if (perspectiveName.isPresent()) {
            String cubeName = perspectiveName.get();
            Optional<Cube> oCube = catalog.getCubes().stream().filter(c -> cubeName.equals(c.getName())).findFirst();
            if (oCube.isPresent()) {
                Cube cube = oCube.get();
                TSchema schema = edmFactory.createTSchema();
                schema.setNamespace(NAMESPACE);
                schema.setAlias(NAMESPACE);

                EntityContainerType container = edmFactory.createEntityContainerType();
                container.setName(catalog.getName());

                TEntityContainer biContainer = biFactory.createTEntityContainer();
                biContainer.setCaption(cube.getName());
                // biContainer.setCulture("de-DE");
                container.setBiEntityContainer(biContainer);
                schema.getEntityContainer().add(container);

                List<? extends Dimension> dimensions = cube.getDimensions();
                Optional<? extends Dimension> oMeasureDimension = dimensions.stream().filter(d -> d.isMeasures()).findAny();
                if (dimensions != null) {
                    for (Dimension dimension : dimensions) {
                        EntitySetType entitySetDimension = edmFactory.createEntitySetType();
                        entitySetDimension.setName(dimension.getUniqueName());
                        entitySetDimension.setEntityType(NAMESPACE + "." + dimension.getUniqueName());

                        TEntitySet biEntitySetDimension = biFactory.createTEntitySet();
                        biEntitySetDimension.setCaption(dimension.getCaption());

                        entitySetDimension.setBiEntitySet(biEntitySetDimension);

                        container.getEntitySet().add(entitySetDimension);

                        org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType dimensionType = edmFactory
                                .createTEntityType();
                        dimensionType.setName(dimension.getUniqueName());

                        TEntityType entityType = biFactory.createTEntityType();
                        dimensionType.setBiEntityType(entityType);

                        if (dimension.isMeasures()) {
                            List<Member> measures = cube.getMeasures();
                            if (measures != null) {
                                for (Member member : measures) {
                                    boolean keyNeedFlag = true;

                                    if (keyNeedFlag) {
                                        TEntityKeyElement keyElement = edmFactory.createTEntityKeyElement();
                                        TPropertyRef propertyRef = edmFactory.createTPropertyRef();
                                        propertyRef.setName(member.getUniqueName());
                                        keyElement.getPropertyRef().add(propertyRef);
                                        dimensionType.setKey(keyElement);
                                        keyNeedFlag = false;
                                    }

                                    TEntityProperty valueProperty = edmFactory.createTEntityProperty();
                                    valueProperty.setName(member.getUniqueName());
                                    String type = getType(member.getPropertyValue(StandardProperty.DATATYPE.getName()));
                                    valueProperty.setType(type);
                                    valueProperty.setNullable(false);

                                    // add measures
                                    //TMeasure biValueMeasure = biFactory.createTMeasure();
                                    //biValueMeasure.setCaption(member.getCaption());
                                    //biValueMeasure.setReferenceName(member.getCaption());
                                    //biValueMeasure.setFormatString("\\$#,0.00;(\\$#,0.00);\\$#,0.00"); //TODO add format from member
                                    //valueProperty.setBiMeasure(biValueMeasure);

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
                                                    propertyRef.setName(l.getUniqueName());
                                                    keyElement.getPropertyRef().add(propertyRef);
                                                    dimensionType.setKey(keyElement);
                                                    keyNeedFlag = false;
                                                }
                                                TLevel tLevel = biFactory.createTLevel();
                                                tLevel.setName(l.getUniqueName());
                                                tLevel.setCaption(l.getCaption());
                                                tLevel.setReferenceName(l.getUniqueName());
                                                levels.add(tLevel);

                                                TEntityProperty levelProperty = edmFactory.createTEntityProperty();
                                                levelProperty.setName(l.getUniqueName());
                                                levelProperty.setType(getType(l.getDatatype())); 
                                                levelProperty.setNullable(false);
                                                dimensionType.getProperty().add(levelProperty);
                                            }
                                        }
                                    }
                                    THierarchy hierarchyTHierarchy = biFactory.createTHierarchy();
                                    hierarchyTHierarchy.setCaption(hierarchy.getCaption());
                                    hierarchyTHierarchy.setName(hierarchy.getUniqueName());
                                    hierarchyTHierarchy.setReferenceName(hierarchy.getUniqueName());

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

    private static TNavigationProperty getNavigationProperty(Dimension measureDimension, Dimension dimension) {
        TNavigationProperty navigationProperty = edmFactory.createTNavigationProperty();
        String name = getAssociationSetTypeName(measureDimension, dimension);
        navigationProperty.setName(dimension.getUniqueName()); //TODO add column name
        navigationProperty.setRelationship(NAMESPACE + "." + name);
        navigationProperty.setFromRole(measureDimension.getUniqueName()); //TODO add column name
        navigationProperty.setToRole(dimension.getUniqueName()); //TODO add column name
        return navigationProperty;
    }

    private static TAssociation getAssociation(Dimension measureDimension, Dimension dimension) {
        TAssociation association = edmFactory.createTAssociation();
        String name = getAssociationSetTypeName(measureDimension, dimension);
        association.setName(name);
        TAssociationEnd measureDimensionAssociationEnd = edmFactory.createTAssociationEnd();
        measureDimensionAssociationEnd.setRole(measureDimension.getUniqueName()); //TODO add name colunm to name
        measureDimensionAssociationEnd.setType(NAMESPACE + "." + measureDimension.getUniqueName());
        measureDimensionAssociationEnd.setMultiplicity(TMultiplicity.__);
        TAssociationEnd dimensionAssociationEnd = edmFactory.createTAssociationEnd();
        dimensionAssociationEnd.setRole(dimension.getUniqueName()); //TODO add name colunm to name
        dimensionAssociationEnd.setType(NAMESPACE + "." + dimension.getUniqueName());
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
        measureDimensionEndType.setEntitySet(measureDimension.getUniqueName());
        EndType dimensionEndType = edmFactory.createEndType();
        dimensionEndType.setEntitySet(dimension.getUniqueName());
        associationSetType.getEnd().add(measureDimensionEndType);
        associationSetType.getEnd().add(dimensionEndType);
        return associationSetType;
    }

    private static String getAssociationSetTypeName(Dimension measureDimension, Dimension dimension) {
        return removeSquareBrackets(measureDimension.getUniqueName()) + "_" + removeSquareBrackets(dimension.getUniqueName()); //TODO add name column to name
    }

    private static String removeSquareBrackets(String name) {
        return name.replace("[", "").replace("]", "").replace(".", "_");
    }

    private static String getType(Object propertyValue) {
        if (propertyValue != null && propertyValue instanceof String value) {
            switch (value) {
            case "UNDEFINED":
            case "String":
                return "String";
            case "NUMERIC":
                return "Numeric";
            case "Integer":
                return "Int32";
            case "Boolean":
                return "Boolean";
            case "Date":
                return "Date";
            case "Time":
                return "Time";
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
                return "Numeric";
            case INTEGER:
                return "Int32";
            case BIGINT:
                return "Int32";
            case SMALLINT:
                return "Int32";
            case BOOLEAN:
                return "Boolean";
            case DATE:
                return "Date";
            case TIME:
                return "Time";
            case TIMESTAMP:
                return "Timestamp";
            case DOUBLE:
                return "Double";
            case REAL:
                return "Double";
            case FLOAT:
                return "Double";
            case DECIMAL:
                return "Double";
            default:
                return "String";
            }
        }
        return "String";
    }

    public static String getCSDLModelAsString(TSchema schema) {
        try {
            return serializeToXml(schema);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getCSDL(Catalog catalog, Optional<String> perspectiveName) {
        if (perspectiveName.isPresent()) {
            String cubeName = perspectiveName.get();
            Optional<Cube> oCube = catalog.getCubes().stream().filter(c -> cubeName.equals(c.getName())).findFirst();
            if (oCube.isPresent()) {
                return String.format(csdlMd3, cubeName, getEntitySets(oCube.get()), getEntityTypes(oCube.get()));
            }
        }
        return String.format(csdlMd4);
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

    private static String getEntityTypes(Cube cube) {
        StringBuilder sb = new StringBuilder();
        boolean flag = true;
        for (Dimension dimension : cube.getDimensions()) {
            for (Hierarchy hierarchy : dimension.getHierarchies()) {
                if (flag) {
                    flag = false;
                } else {
                    sb.append(System.lineSeparator());
                }
                sb.append("<EntityType Name=\"").append(hierarchy.getUniqueName()).append("\">")
                        .append(System.lineSeparator());
                sb.append("    <Key>").append(System.lineSeparator()).append("        <PropertyRef Name=\"")
                        .append(hierarchy.getUniqueName()).append("\"/>").append(System.lineSeparator())
                        .append("    </Key>").append(System.lineSeparator());

                sb.append("    <Property Name=\"").append(hierarchy.getUniqueName()).append("\" Type=\"")
                        // .append(scldType(level.getLevelType().getType()))
                        .append("String").append("\" Nullable=\"false\">").append(System.lineSeparator())
                        .append("</Property>").append(System.lineSeparator());

                for (Level level : hierarchy.getLevels()) {
                    sb.append("    <Property Name=\"").append(level.getUniqueName()).append("\" Type=\"")
                            // .append(scldType(level.getLevelType().getType()))
                            .append("String").append("\" Nullable=\"false\">").append(System.lineSeparator())
                            .append("</Property>");
                }
                sb.append(System.lineSeparator());
                sb.append("    <bi:Hierarchy Name=\"").append(hierarchy.getUniqueName()).append("\" Caption=\"")
                        .append(hierarchy.getUniqueName()).append("\" ReferenceName=\"")
                        .append(hierarchy.getUniqueName()).append("\">");
                sb.append(System.lineSeparator());
                for (Level level : hierarchy.getLevels()) {
                    sb.append("        <bi:Level Name=\"");
                    sb.append(level.getUniqueName());
                    sb.append("\">");
                    sb.append(System.lineSeparator());
                    sb.append("            <bi:Source>");
                    sb.append(System.lineSeparator());
                    sb.append("                <bi:PropertyRef Name=\"");
                    sb.append(level.getUniqueName());
                    sb.append("\" />");
                    sb.append(System.lineSeparator());
                    sb.append("            </bi:Source>");
                    sb.append(System.lineSeparator());
                    sb.append("        </bi:Level>");
                    sb.append(System.lineSeparator());
                }
                sb.append("    </bi:Hierarchy>");
                sb.append(System.lineSeparator());
                sb.append("</EntityType>");

            }
        }
        for (Member member : cube.getMeasures()) {
            if (flag) {
                flag = false;
            } else {
                sb.append(System.lineSeparator());
            }
            sb.append("<EntityType Name=\"").append(member.getUniqueName()).append("\">")
                    .append(System.lineSeparator());
            sb.append("    <Key>").append(System.lineSeparator()).append("        <PropertyRef Name=\"")
                    .append(member.getUniqueName()).append("\"/>").append(System.lineSeparator()).append("    </Key>")
                    .append(System.lineSeparator());

            sb.append("    <Property Name=\"").append(member.getUniqueName()).append("\" Type=\"")
                    // .append(scldType(level.getLevelType().getType()))
                    .append("String").append("\" Nullable=\"false\">")
                    // .append(System.lineSeparator())
                    // .append(" <bi:Measure Caption=\"")
                    // .append(member.getUniqueName())
                    // .append("\" ReferenceName=\"")
                    // .append(member.getUniqueName())
                    // .append("\"/>")
                    .append(System.lineSeparator()).append("</Property>");
            sb.append(System.lineSeparator()).append("</EntityType>");
        }
        return sb.toString();
    }

    private static String getEntitySets(Cube cube) {
        StringBuilder sb = new StringBuilder();
        boolean flag = true;
        for (Dimension dimension : cube.getDimensions()) {
            for (Hierarchy hierarchy : dimension.getHierarchies()) {
                if (flag) {
                    flag = false;
                } else {
                    sb.append(System.lineSeparator());
                }
                sb.append("<EntitySet Name=\"").append(hierarchy.getUniqueName()).append("\" EntityType=\"Model.")
                        .append(hierarchy.getUniqueName()).append("\" bi:Caption=\"").append(hierarchy.getCaption())
                        .append("\">").append(System.lineSeparator()).append("</EntitySet>");
            }
        }
        if (cube.getMeasures() != null && cube.getMeasures().size() > 0) {
            for (Member member : cube.getMeasures()) {
                if (flag) {
                    flag = false;
                } else {
                    sb.append(System.lineSeparator());
                }
                sb.append("<EntitySet Name=\"").append(member.getUniqueName()).append("\" EntityType=\"Model.")
                        .append(member.getUniqueName()).append("\" bi:Caption=\"").append(member.getCaption())
                        .append("\">").append(System.lineSeparator()).append("</EntitySet>");
            }
        }
        return sb.toString();
    }

}
