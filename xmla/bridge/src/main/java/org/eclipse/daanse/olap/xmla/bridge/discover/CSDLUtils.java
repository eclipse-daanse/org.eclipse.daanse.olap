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

import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.common.StandardProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntityContainer;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntitySet;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TEntityType;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.THierarchy;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TLevel;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TMeasure;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EntityContainerType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EntitySetType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityKeyElement;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TPropertyRef;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TSchema;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class CSDLUtils {
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
                schema.setNamespace("Model");
                schema.setAlias("Model");

                EntityContainerType container = edmFactory.createEntityContainerType();
                container.setName(catalog.getName());

                TEntityContainer biContainer = biFactory.createTEntityContainer();
                biContainer.setCaption(cube.getName());
                biContainer.setCulture("de-DE");
                container.setBiEntityContainer(biContainer);

                
                List<Member> measures = cube.getMeasures();
                if (measures != null) {
                    for (Member member : measures) {
                        EntitySetType entitySet = edmFactory.createEntitySetType();
                        entitySet.setName(member.getUniqueName());
                        entitySet.setEntityType("Model." + member.getUniqueName());
                        TEntitySet biEntitySet = biFactory.createTEntitySet();
                        biEntitySet.setCaption(member.getCaption());
                        entitySet.setBiEntitySet(biEntitySet);
                        container.getEntitySet().add(entitySet);

                        org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType measureSumType = edmFactory.createTEntityType();
                        measureSumType.setName(member.getUniqueName());

                        TEntityType biMeasureSumType = biFactory.createTEntityType();
                        biMeasureSumType.setContents(member.getUniqueName());
                        measureSumType.setBiEntityType(biMeasureSumType);

                        TEntityProperty valueProperty = edmFactory.createTEntityProperty();
                        valueProperty.setName(member.getUniqueName());
                        String type = getType(member.getPropertyValue(StandardProperty.DATATYPE.getName()));
                        valueProperty.setType(type);
                        valueProperty.setNullable(false);

                        TMeasure biValueMeasure = biFactory.createTMeasure();
                        biValueMeasure.setCaption(member.getUniqueName());
                        biValueMeasure.setHidden(false);
                        valueProperty.setBiMeasure(biValueMeasure);

                        measureSumType.getProperty().add(valueProperty);
                        schema.getEntityType().add(measureSumType);
                    }
                }

                List<Hierarchy> hierarchies = cube.getHierarchies();
                if (hierarchies != null) {
                    for (Hierarchy hierarchy : hierarchies) {
                        EntitySetType entitySetHierarchy = edmFactory.createEntitySetType();
                        entitySetHierarchy.setName(hierarchy.getUniqueName());
                        entitySetHierarchy.setEntityType("Model." + hierarchy.getUniqueName());

                        TEntitySet biEntitySetHierarchy  = biFactory.createTEntitySet();
                        biEntitySetHierarchy.setCaption(hierarchy.getCaption());

                        entitySetHierarchy.setBiEntitySet(biEntitySetHierarchy);

                        container.getEntitySet().add(entitySetHierarchy);

                        org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType hierarchyType = edmFactory.createTEntityType();
                        hierarchyType.setName(hierarchy.getUniqueName());

                        List<TLevel> levels = new ArrayList<>();

                        List<? extends Level> ls = hierarchy.getLevels();
                        if (ls != null) {
                            for (Level l : ls) {
                                TLevel tLevel = biFactory.createTLevel();
                                tLevel.setName(l.getUniqueName());
                                tLevel.setCaption(l.getCaption());
                                tLevel.setReferenceName(l.getUniqueName());
                                levels.add(tLevel);
                                
                                TEntityProperty levelProperty = edmFactory.createTEntityProperty();
                                levelProperty.setName(l.getUniqueName());
                                levelProperty.setType("String"); //TODO need get type from level 
                                levelProperty.setNullable(false);
                                hierarchyType.getProperty().add(levelProperty);
                            }
                        }

                        THierarchy hierarchyTHierarchy = biFactory.createTHierarchy();
                        hierarchyTHierarchy.setCaption(hierarchy.getCaption());
                        hierarchyTHierarchy.setName(hierarchy.getUniqueName());
                        hierarchyTHierarchy.setReferenceName(hierarchy.getUniqueName());

                        hierarchyTHierarchy.getLevel().addAll(levels);

                        TEntityType hierarchyTEntityType = biFactory.createTEntityType();
                        hierarchyTEntityType.setContents(hierarchy.getUniqueName());
                        hierarchyTEntityType.getHierarchy().add(hierarchyTHierarchy);

                        hierarchyType.setBiEntityType(hierarchyTEntityType);

                        TEntityProperty hierarchyKeyProperty = edmFactory.createTEntityProperty();
                        hierarchyKeyProperty.setName(hierarchy.getUniqueName());
                        hierarchyKeyProperty.setType("Int32");
                        hierarchyKeyProperty.setNullable(false);

                        TPropertyRef hierarchyPropertyRef = edmFactory.createTPropertyRef();
                        hierarchyPropertyRef.setName(hierarchy.getUniqueName());

                        TEntityKeyElement key =  edmFactory.createTEntityKeyElement();
                        key.getPropertyRef().add(hierarchyPropertyRef);

                        hierarchyType.getProperty().add(hierarchyKeyProperty);

                        schema.getEntityType().add(hierarchyType);
                    }
                }
                return schema;
            }
        }
        return edmFactory.createTSchema();
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

    private  static String getEntityTypes(Cube cube) {
        StringBuilder sb = new StringBuilder();
        boolean flag = true;
        for (Dimension dimension : cube.getDimensions()) {
            for (Hierarchy hierarchy : dimension.getHierarchies()) {
                if (flag) {
                    flag = false;
                } else {
                    sb.append(System.lineSeparator());
                }
                sb.append("<EntityType Name=\"")
                .append(hierarchy.getUniqueName())
                .append("\">")
                .append(System.lineSeparator());
                sb.append("    <Key>")
                .append(System.lineSeparator())
                .append("        <PropertyRef Name=\"")
                .append(hierarchy.getUniqueName())
                .append("\"/>")
                .append(System.lineSeparator())
                .append("    </Key>")
                .append(System.lineSeparator());
                
                sb.append("    <Property Name=\"")
                .append(hierarchy.getUniqueName())
                .append("\" Type=\"")
                //.append(scldType(level.getLevelType().getType()))
                .append("String")
                .append("\" Nullable=\"false\">")
                .append(System.lineSeparator())
                .append("</Property>")
                .append(System.lineSeparator());

                for (Level level : hierarchy.getLevels()) {
                    sb.append("    <Property Name=\"")
                    .append(level.getUniqueName())
                    .append("\" Type=\"")
                    //.append(scldType(level.getLevelType().getType()))
                    .append("String")
                    .append("\" Nullable=\"false\">")
                    .append(System.lineSeparator())
                    .append("</Property>");
                }
                sb.append(System.lineSeparator());
                sb.append("    <bi:Hierarchy Name=\"")
                .append(hierarchy.getUniqueName())
                .append("\" Caption=\"")
                .append(hierarchy.getUniqueName())
                .append("\" ReferenceName=\"")
                .append(hierarchy.getUniqueName())
                .append("\">");
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
            sb.append("<EntityType Name=\"")
            .append(member.getUniqueName())
            .append("\">")
            .append(System.lineSeparator());
            sb.append("    <Key>")
            .append(System.lineSeparator())
            .append("        <PropertyRef Name=\"")
            .append(member.getUniqueName())
            .append("\"/>")
            .append(System.lineSeparator())
            .append("    </Key>")
            .append(System.lineSeparator());

            sb.append("    <Property Name=\"")
            .append(member.getUniqueName())
            .append("\" Type=\"")
            //.append(scldType(level.getLevelType().getType()))
            .append("String")
            .append("\" Nullable=\"false\">")
            //.append(System.lineSeparator())
            //.append("    <bi:Measure Caption=\"")
            //.append(member.getUniqueName())
            //.append("\" ReferenceName=\"")
            //.append(member.getUniqueName())
            //.append("\"/>")
            .append(System.lineSeparator())
            .append("</Property>");
            sb.append(System.lineSeparator())
            .append("</EntityType>");
        }
        return sb.toString();
    }

    private  static String getEntitySets(Cube cube) {
        StringBuilder sb = new StringBuilder();
        boolean flag = true;
        for (Dimension dimension : cube.getDimensions()) {
            for (Hierarchy hierarchy : dimension.getHierarchies()) {
                if (flag) {
                    flag = false;
                } else {
                    sb.append(System.lineSeparator());
                }
                sb.append("<EntitySet Name=\"")
                .append(hierarchy.getUniqueName())
                .append("\" EntityType=\"Model.")
                .append(hierarchy.getUniqueName())
                .append("\" bi:Caption=\"")
                .append(hierarchy.getCaption())
                .append("\">")
                .append(System.lineSeparator())
                .append("</EntitySet>");
            }
        }
        if (cube.getMeasures() != null && cube.getMeasures().size() > 0) {
            for (Member member : cube.getMeasures()) {
                if (flag) {
                    flag = false;
                } else {
                    sb.append(System.lineSeparator());
                }
                sb.append("<EntitySet Name=\"")
                .append(member.getUniqueName())
                .append("\" EntityType=\"Model.")
                .append(member.getUniqueName())
                .append("\" bi:Caption=\"")
                .append(member.getCaption())
                .append("\">")
                .append(System.lineSeparator())
                .append("</EntitySet>");
            }
        }
        return sb.toString();
    }

}
