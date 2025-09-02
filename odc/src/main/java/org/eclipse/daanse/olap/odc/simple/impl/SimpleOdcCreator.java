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
*/
package org.eclipse.daanse.olap.odc.simple.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.daanse.odc.simple.model.CommandType;
import org.eclipse.daanse.odc.simple.model.ConnectionType;
import org.eclipse.daanse.odc.simple.model.DocumentProperties;
import org.eclipse.daanse.odc.simple.model.OdcFile;
import org.eclipse.daanse.odc.simple.model.OfficeDataConnection;
import org.eclipse.daanse.odc.writer.simple.OdcWriter;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.odc.api.OdcCreator;
import org.eclipse.daanse.olap.odc.simple.api.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.Designate;

/**
 * Simple implementation of OdcCreator that creates Office Data Connection files
 * for OLAP connections. This implementation supports creating ODC files for: -
 * Catalog-level connections (browse all cubes in a catalog) - Cube-specific
 * connections (connect to a specific cube) - MDX query connections with all
 * measures (useful for reporting scenarios)
 */
@Designate(ocd = CreatorConfig.class, factory = true)
@Component(configurationPid = Constants.CREATOR_PID, scope = ServiceScope.SINGLETON)
public class SimpleOdcCreator implements OdcCreator {

    private final OdcWriter odcWriter = new OdcWriter();

    private CreatorConfig config;

    @Activate
    public SimpleOdcCreator(CreatorConfig config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createCatalogOdc(Catalog catalog) {

        String connectionString = buildPseudoOleDbConnectionString(catalog);
        String catalogName = catalog.getName();
        String description = Optional.ofNullable(catalog.getDescription()).filter(d -> !d.trim().isEmpty())
                .orElse("OLAP Catalog Connection for " + catalogName);

        org.eclipse.daanse.odc.simple.model.Connection odcConnection = new org.eclipse.daanse.odc.simple.model.Connection(
                ConnectionType.OLEDB, connectionString, Optional.of(CommandType.Cube), List.of(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        DocumentProperties props = new DocumentProperties(Optional.of(description), Optional.of(catalogName),
                Optional.of("OLAP,Catalog,Browse"));

        OfficeDataConnection odc = OfficeDataConnection.of(odcConnection);
        OdcFile odcFile = new OdcFile(Optional.of(catalogName), props, odc, Optional.of(catalogName), Optional.empty(),
                Optional.empty());

        return odcWriter.write(odcFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createCubeOdc(Cube cube) {
        Catalog catalog = cube.getCatalog();

        String cubeName = cube.getName();
        String connectionString = buildPseudoOleDbConnectionString(catalog);
        String title = cubeName + " - " + catalog.getName();
        String description = "OLAP Cube Connection for " + cubeName;

        org.eclipse.daanse.odc.simple.model.Connection odcConnection = new org.eclipse.daanse.odc.simple.model.Connection(
                ConnectionType.OLEDB, connectionString, Optional.of(CommandType.Cube), List.of(), Optional.of(cubeName),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        DocumentProperties props = new DocumentProperties(Optional.of(description), Optional.of(title),
                Optional.of("OLAP,Cube," + cubeName));

        OfficeDataConnection odc = OfficeDataConnection.of(odcConnection);
        OdcFile odcFile = new OdcFile(Optional.of(title), props, odc, Optional.of(catalog.getName()), Optional.empty(),
                Optional.of(cubeName));

        return odcWriter.write(odcFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createMdxOdcWithAllMeasures(Cube cube) {
//        Catalog catalog = cube.getCatalog();
        java.lang.String cubeName = cube.getName();

        // Generate MDX to select all measures
        List<Member> measures = cube.getMeasures();
        String measuresSet = measures.stream().map(measure -> "[" + measure.getUniqueName() + "]")
                .collect(Collectors.joining(", "));

        String mdxQuery = "SELECT {" + measuresSet + "} ON COLUMNS FROM [" + cubeName + "]";

        return createMdxOdc(cube, mdxQuery, cubeName + " - All Measures",
                "MDX Query showing all measures from " + cubeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createMdxOdc(Cube cube, String query, String title, String description) {
        Catalog catalog = cube.getCatalog();
        String cubeName = cube.getName();
        String connectionString = buildPseudoOleDbConnectionString(catalog);

        // TODO: check sql type here
        org.eclipse.daanse.odc.simple.model.Connection odcConnection = new org.eclipse.daanse.odc.simple.model.Connection(
                ConnectionType.OLEDB, connectionString, Optional.of(CommandType.SQL), List.of(), Optional.of(query),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        DocumentProperties props = new DocumentProperties(Optional.of(description), Optional.of(title),
                Optional.of("OLAP,MDX," + cubeName));

        OfficeDataConnection odc = OfficeDataConnection.of(odcConnection);
        OdcFile odcFile = new OdcFile(Optional.of(title), props, odc, Optional.of(catalog.getName()), Optional.empty(),
                Optional.of(cubeName));

        return odcWriter.write(odcFile);
    }

    /**
     * Builds an OLEDB connection string from the OLAP connection. This extracts
     * necessary information from the DataSource and connection context.
     */
    private String buildPseudoOleDbConnectionString(Catalog catalog) {
        // For OLAP connections, we typically use MSOLAP provider
        StringBuilder connectionString = new StringBuilder("Provider=MSOLAP;");

        connectionString.append("Data Source=" + config.datasource() + ";");

        // Add catalog information
        connectionString.append("Initial Catalog=").append(catalog.getName()).append(";");

        return connectionString.toString();
    }

}
