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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.DataTypeJdbc;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.DatabaseColumn;
import org.eclipse.daanse.olap.api.element.DatabaseSchema;
import org.eclipse.daanse.olap.api.element.DatabaseTable;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.element.MemberBase;
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

public class CSDLUtils {
    private static EdmFactory edmFactory = EdmFactory.eINSTANCE;
    private static BiFactory biFactory = BiFactory.eINSTANCE;

    private static final String csdlMd = """
            <Schema
                xmlns="http://schemas.microsoft.com/ado/2008/09/edm"
                xmlns:edm_annotation="http://schemas.microsoft.com/ado/2009/02/edm/annotation"
                xmlns:bi="http://schemas.microsoft.com/sqlbi/2010/10/edm/extensions" bi:Version="2.0" Namespace="Sandbox">
              <EntityContainer Name="Sandbox">
                <Documentation>
                  <Summary>%s</Summary>
                </Documentation>
                %s
                %s
                <bi:EntityContainer Caption="Sandbox" Culture="en-US"/>
             </EntityContainer>
             %s
            </Schema>
              """;

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

    private static final String csdlMd1 = """
            <Schema
                xmlns="http://schemas.microsoft.com/ado/2008/09/edm"
                xmlns:edm_annotation="http://schemas.microsoft.com/ado/2009/02/edm/annotation"
                xmlns:bi="http://schemas.microsoft.com/sqlbi/2010/10/edm/extensions" bi:Version="2.0" Namespace="Sandbox">
              <EntityContainer Name="Sandbox">
                <Documentation>
                  <Summary>%s</Summary>
                </Documentation>
                <EntitySet EntityType="Sandbox.Sales" Name="Sales">
                    <bi:EntitySet/>
                </EntitySet>
                <EntitySet EntityType="Sandbox.schule" Name="schule">
                    <bi:EntitySet/>
                </EntitySet>
                <EntitySet EntityType="Sandbox.ganztags_art" Name="ganztags_art">
                    <bi:EntitySet/>
                </EntitySet>
                <bi:Cube Name="SalesCube" Caption="Sales Cube">
                    <bi:Measures>
                       <bi:MeasureGroup Name="Sales">
                            <bi:Measure Name="Total Sales" Property="Amount" AggregateFunction="Sum" FormatString="#,0.00" />
                       </bi:MeasureGroup>
                    </bi:Measures>
                </bi:Cube>

                <bi:EntityContainer Caption="Sandbox" Culture="en-US"/>
              </EntityContainer>
              <EntityType Name="Sales">
                    <Key>
                        <PropertyRef Name="SalesId"/>
                    </Key>

                    <Property Name="SalesId" Type="Int64" Nullable="false"/>
                    <Property Name="DateKey" Type="Int32"/>
                    <Property Name="ProductKey" Type="Int32"/>
                    <Property Name="Amount" Type="Decimal"/>

                    <bi:EntityType Caption="Sales">
                        <bi:MeasureGroup Name="Sales">
                            <bi:Measure Name="Total Sales"
                                        Property="Amount"
                                        AggregateFunction="Sum"/>
                        </bi:MeasureGroup>
                    </bi:EntityType>
              </EntityType>
              <EntityType Name="schule">
                  <Key>
                      <PropertyRef Name="id"/>
                  </Key>
                  <Property Name="id" Nullable="false" Type="String">
                      <bi:Property/>
                  </Property>
                  <Property Name="schul_name" Nullable="false" Type="String">
                      <bi:Property/>
                  </Property>
                  <Property Name="schul_nummer" Nullable="false" Type="String">
                      <bi:Property/>
                  </Property>
                  <Property Name="ganztags_art_id" Nullable="false" Type="String">
                      <bi:Property/>
                  </Property>
                  <Property Name="traeger_id" Nullable="false" Type="String">
                      <bi:Property/>
                  </Property>
                  <Property Name="schul_art_id" Nullable="false" Type="String">
                      <bi:Property/>
                  </Property>
                  <bi:EntityType Caption="schule"/>
              </EntityType>
              <EntityType Name="ganztags_art">
                  <Key>
                      <PropertyRef Name="id"/>
                  </Key>
                  <Property Name="id" Nullable="false" Type="String">
                      <bi:Property/>
                  </Property>
                  <Property Name="schul_umfang" Nullable="false" Type="String">
                      <bi:Property/>
                  </Property>
                  <bi:EntityType Caption="ganztags_art"/>
              </EntityType>
            </Schema>
              """;

    private static final String csdlMd2 = """
            <Schema
                xmlns="http://schemas.microsoft.com/ado/2008/09/edm"
                xmlns:edm_annotation="http://schemas.microsoft.com/ado/2009/02/edm/annotation"
                xmlns:bi="http://schemas.microsoft.com/sqlbi/2010/10/edm/extensions" bi:Version="2.0" Namespace="SalesModel">
              <!-- ========================= -->
              <!-- ENTITY CONTAINER (CUBE)   -->
              <!-- ========================= -->
              <EntityContainer Name="Sales">
                  <!-- Dimension as an entity set -->
                  <EntitySet Name="Product" EntityType="SalesModel.Product" bi:Caption="Product" bi:CollectionCaption="Products" />
                  <!-- Measures entity set (this is your measure group) -->
                  <EntitySet Name="InternetSalesMeasures" EntityType="SalesModel.InternetSalesMeasures" bi:Caption="Internet Sales" bi:CollectionCaption="Internet Sales Measures" />
              </EntityContainer>
              
              <EntityType Name="InternetSalesMeasures">
                  <Key>
                      <!-- in multidimensional, the “measures” entity is often
                      keyless for query purposes; using a dummy key here -->
                      <PropertyRef Name="MeasureKey" />
                  </Key>
                  <Property Name="MeasureKey" Type="Int32" Nullable="false" />
                  <!-- Our cube measure: [Measures].[Sum of SalesAmount] -->
                  <Property Name="Sum_of_SalesAmount" Type="Decimal" Precision="19" Scale="4">
                      <Documentation>
                          <Summary>Total sales amount</Summary>
                      </Documentation>
                      <bi:Measure Caption="Sum of SalesAmount" ReferenceName="Sum of SalesAmount" FormatString="$#,0.00" />
                  </Property>
              </EntityType>
              
              <EntityType Name="Product">
                  <Key>
                      <PropertyRef Name="ProductKey" />
                  </Key>
                  <!-- Dimension attributes -->
                  <Property Name="ProductKey"   Type="Int32"   Nullable="false" />
                  <Property Name="ProductLine"  Type="String"  Nullable="true" />
                  <Property Name="ModelName"    Type="String"  Nullable="true" />

                  <!-- BI: this entity is a DIMENSION with a hierarchy -->
                  <bi:Hierarchy Name="Product_Hierarchy" Caption="Product Hierarchy" ReferenceName="Product Hierarchy">
                      <!-- level 1 -->
                      <bi:Level Name="ProductLine">
                          <bi:Source>
                              <bi:PropertyRef Name="ProductLine" />
                          </bi:Source>
                      </bi:Level>
                      <!-- level 2 -->
                      <bi:Level Name="ModelName">
                          <bi:Source>
                              <bi:PropertyRef Name="ModelName" />
                          </bi:Source>
                      </bi:Level>
                  </bi:Hierarchy>
              </EntityType>
            </Schema>
              """;
    
    private static final String csdlMd7 = """
                            <Schema xmlns="http://schemas.microsoft.com/ado/2008/09/edm" xmlns:bi="http://schemas.microsoft.com/sqlbi/2010/10/edm/extensions" xmlns:edm_annotation="http://schemas.microsoft.com/ado/2009/02/edm/annotation" Namespace="Model" bi:Version="2.0">
                                <EntityContainer Name="Model">
                                    <Documentation>
                                        <Summary>Pädagogisches Personal an Jenaer Schulen</Summary>
                                    </Documentation>
                                    <EntitySet EntityType="Model.Measures" Name="Measures" bi:Caption="Measures"></EntitySet>
                                    <EntitySet EntityType="Model.Anzahl Personen" Name="Anzahl Personen" bi:Caption="Anzahl Personen"></EntitySet>
                                    <EntitySet EntityType="Model.Fact Count" Name="Fact Count" bi:Caption="Fact Count"></EntitySet>
                                    <bi:EntityContainer Caption="Model" Culture="en-US"/>
                                </EntityContainer>
                                <EntityType Name="Measures">
                                    <Key>
                                        <PropertyRef Name="Measures"/>
                                    </Key>
                                    <Property Name="Measures" Nullable="false" Type="String">
                                        <bi:Property/>
                                    </Property>
                                    <Property Name="MeasuresLevel" Nullable="false" Type="String"></Property>
                                </EntityType>
                                <EntityType Name="Anzahl Personen">
                                    <Key>
                                        <PropertyRef Name="Anzahl_Personen"/>
                                    </Key>
                                    <Property Name="Anzahl_Personen" Nullable="false" Type="String"></Property>
                                </EntityType>
                                <EntityType Name="Fact Count">
                                    <Key>
                                        <PropertyRef Name="Fact_Count"/>
                                    </Key>
                                    <Property Name="Fact_Count" Nullable="false" Type="String"></Property>
                                </EntityType>
                            </Schema>
                      """;

    
    public static TSchema getCSDLModel(Catalog catalog, Optional<String> perspectiveName) {
        //return String.format(csdlMd, catalog.getName(), getTables(catalog), getCubes(catalog), getTableColumns(catalog));
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
                        valueProperty.setType("Int32");
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
                                levelProperty.setType("String");
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

    public static String getCSDL(Catalog catalog, Optional<String> perspectiveName) {
        //return String.format(csdlMd, catalog.getName(), getTables(catalog), getCubes(catalog), getTableColumns(catalog));
        if (perspectiveName.isPresent()) {
            String cubeName = perspectiveName.get();
            Optional<Cube> oCube = catalog.getCubes().stream().filter(c -> cubeName.equals(c.getName())).findFirst();
            if (oCube.isPresent()) {
                return String.format(csdlMd3, cubeName, getEntitySets(oCube.get()), getEntityTypes(oCube.get()));
                //return csdlMd7;
            }
        }
        return String.format(csdlMd4);
    }
    
    private  static String getTables(Catalog catalog) {
        StringBuilder sb = new StringBuilder();
        boolean flag = true;
        for (DatabaseSchema databaseSchema : catalog.getDatabaseSchemas()) {
            for ( DatabaseTable databaseTable: databaseSchema.getDbTables()) {
                if (flag) {
                    flag = false;
                } else {
                    sb.append(System.lineSeparator());
                }
                sb.append("<EntitySet Name=\"")
                .append(databaseTable.getName())
                .append("\" EntityType=\"Sandbox.")
                .append(databaseTable.getName())
                .append("\">")
                .append(System.lineSeparator())
                .append("    <bi:EntitySet/>")
                .append(System.lineSeparator())
                .append("</EntitySet>");
            }
        }
        return sb.toString();
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

    private static String getTableColumns(Catalog catalog) {
        StringBuilder sb = new StringBuilder();
        boolean flag = true;
        for (DatabaseSchema databaseSchema : catalog.getDatabaseSchemas()) {
            for ( DatabaseTable databaseTable: databaseSchema.getDbTables()) {
                if (flag) {
                    flag = false;
                } else {
                    sb.append(System.lineSeparator());
                }
                sb.append("<EntityType Name=\"")
                .append(databaseTable.getName())
                .append("\">")
                .append(System.lineSeparator());
                boolean flag1 = true;
                for (DatabaseColumn databaseColumn : databaseTable.getDbColumns()) {
                    if (flag1) {
                        flag1 = false;
                        sb.append("    <Key>")
                        .append(System.lineSeparator())
                        .append("        <PropertyRef Name=\"")
                        .append(databaseColumn.getName())
                        .append("\"/>")
                        .append(System.lineSeparator())
                        .append("    </Key>")
                        .append(System.lineSeparator());
                    } else {
                        sb.append(System.lineSeparator());
                    }
                    sb.append("    <Property Name=\"")
                    .append(databaseColumn.getName())
                    .append("\" Type=\"")
                    .append(scldType(databaseColumn.getType()))
                    .append("\" Nullable=\"false\">")
                    .append(System.lineSeparator())
                    .append("        <bi:Property/>")
                    .append(System.lineSeparator())
                    .append("</Property>");
                }
                sb.append(System.lineSeparator())
                .append("    <bi:EntityType Caption=\"")
                .append(databaseTable.getName())
                .append("\"/>")
                .append(System.lineSeparator())
                .append("</EntityType>");
            }
        }
        return sb.toString();
    }

    private static String scldType(DataTypeJdbc type) {
        if (type != null) {
            if (DataTypeJdbc.VARCHAR.equals(type)) {
                return "String";
            }
            if (DataTypeJdbc.INTEGER.equals(type) || DataTypeJdbc.BIGINT.equals(type) || DataTypeJdbc.SMALLINT.equals(type)) {
                return "Int64";
            }
            if (DataTypeJdbc.SMALLINT.equals(type)) {
                return "Int32";
            }
            return type.getValue();
        }
        return "String";
    }
}


