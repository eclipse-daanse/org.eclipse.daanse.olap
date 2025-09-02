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
package org.eclipse.daanse.olap.odc.api;

import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;

/**
 * Interface for creating Office Data Connection (ODC) files from OLAP
 * connections. ODC files allow Microsoft Office applications (Excel, Access,
 * etc.) to connect to OLAP data sources and browse cubes, dimensions, measures,
 * and execute MDX queries.
 * 
 * This API supports different levels of ODC file creation: - Catalog-level
 * connections for browsing all cubes in a catalog - Cube-specific connections
 * for focused access to individual cubes - MDX-based connections for executing
 * specific queries
 */
public interface OdcCreator {

    /**
     * Creates an ODC file for catalog-level connection. This allows browsing all
     * cubes, dimensions, and measures within the catalog.
     * 
     * The generated ODC file will: - Connect at the catalog level - Allow cube
     * browsing and selection - Provide access to all dimensions and measures across
     * cubes - Use OLEDB provider with appropriate connection string
     * 
     * @param catalog The OLAP catalog containing cubes and metadata
     * @return ODC file content as HTML/XML string ready to save as .odc file
     * @throws IllegalArgumentException if catalog is null or invalid
     */
    String createCatalogOdc(Catalog catalog);

    /**
     * Creates an ODC file for a specific cube connection. This creates a focused
     * connection directly to a particular cube.
     * 
     * The generated ODC file will: - Connect directly to the specified cube -
     * Provide access to cube's dimensions and measures - Allow pivoting and
     * analysis within the cube context - Use OLEDB provider with cube-specific
     * connection string
     *
     * @param cube The cube to create ODC connection for
     * @return ODC file content as HTML/XML string ready to save as .odc file
     * @throws IllegalArgumentException if cube is null or invalid
     */
    String createCubeOdc(Cube cube);

    /**
     * Creates an ODC file for a cube with MDX query that selects all measures. This
     * is useful for reporting scenarios where you want to see all available
     * measures without any dimensional filtering.
     * 
     * The generated ODC file will: - Execute an MDX query selecting all measures
     * from the cube - Return a flat result set with all measure values - Use SQL
     * command type for MDX execution - Be optimized for reporting and data export
     * scenarios
     *
     * @param cube The cube to query for all measures
     * @return ODC file content as HTML/XML string ready to save as .odc file
     * @throws IllegalArgumentException if cube is null or has no measures
     */
    String createMdxOdcWithAllMeasures(Cube cube);

    /**
     * Creates an ODC file for a cube with a custom MDX query. This allows for
     * completely customized OLAP scenarios with specific dimensions, measures,
     * filters, and calculations.
     * 
     * The generated ODC file will: - Execute the provided MDX query - Return
     * results according to the query structure - Include custom title and
     * description - Use SQL command type for MDX execution
     *
     * @param cube        The cube being queried (for metadata and connection
     *                    context)
     * @param mdxQuery    The MDX query to execute (must be valid MDX syntax)
     * @param title       Title for the ODC file (displayed in Office applications)
     * @param description Description of the connection purpose and query
     * @return ODC file content as HTML/XML string ready to save as .odc file
     * @throws IllegalArgumentException if any parameter is null/empty or mdxQuery
     *                                  is invalid
     */
    String createMdxOdc(Cube cube, String mdxQuery, String title, String description);

}
