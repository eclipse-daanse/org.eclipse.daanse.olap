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
package org.eclipse.daanse.olap.check.runtime.impl.executors;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.CatalogReader;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.DatabaseSchema;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.check.model.check.CatalogCheck;
import org.eclipse.daanse.olap.check.model.check.CatalogCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.CubeCheck;
import org.eclipse.daanse.olap.check.model.check.CubeCheckResult;
import org.eclipse.daanse.olap.check.model.check.DatabaseSchemaCheck;
import org.eclipse.daanse.olap.check.model.check.DatabaseSchemaCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;
import org.eclipse.daanse.olap.check.model.check.QueryCheck;
import org.eclipse.daanse.olap.check.model.check.QueryCheckResult;

/**
 * Executor for CatalogCheck that verifies catalog structure.
 */
public class CatalogCheckExecutor {

    private final CatalogCheck check;
    private final CatalogReader catalogReader;
    private final Connection connection;
    private final OlapCheckFactory factory;

    public CatalogCheckExecutor(CatalogCheck check, CatalogReader catalogReader, Connection connection,
            OlapCheckFactory factory) {
        this.check = check;
        this.catalogReader = catalogReader;
        this.connection = connection;
        this.factory = factory;
    }

    public CheckResult execute() {
        long startTime = System.currentTimeMillis();
        Date start = new Date();

        CatalogCheckResult result = factory.createCatalogCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setCatalogName(check.getCatalogName());
        result.setStartTime(start);
        result.setSourceCheck(check);

        try {
            // Get all cubes from catalog
            List<Cube> cubes = catalogReader.getCubes();

            // Mark catalog check as success (we have access)
            result.setStatus(CheckStatus.SUCCESS);

            // Execute cube checks
            for (CubeCheck cubeCheck : check.getCubeChecks()) {
                if (!cubeCheck.isEnabled()) {
                    continue;
                }

                CubeCheckExecutor cubeExecutor = new CubeCheckExecutor(cubeCheck, cubes, catalogReader, connection,
                        factory);
                CubeCheckResult cubeResult = cubeExecutor.execute();
                result.getCubeResults().add(cubeResult);

                // If any child fails, mark parent as failed
                if (cubeResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute database schema checks
            List<? extends DatabaseSchema> schemas = catalogReader.getDatabaseSchemas();
            for (DatabaseSchemaCheck schemaCheck : check.getDatabaseSchemaChecks()) {
                if (!schemaCheck.isEnabled()) {
                    continue;
                }

                DatabaseSchemaCheckExecutor schemaExecutor = new DatabaseSchemaCheckExecutor(schemaCheck, schemas,
                        factory);
                DatabaseSchemaCheckResult schemaResult = schemaExecutor.execute();
                result.getDatabaseSchemaResults().add(schemaResult);

                if (schemaResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute query checks (at catalog/connection level)
            for (QueryCheck queryCheck : check.getQueryChecks()) {
                if (!queryCheck.isEnabled()) {
                    continue;
                }

                QueryCheckExecutor queryExecutor = new QueryCheckExecutor(queryCheck, connection, factory);
                QueryCheckResult queryResult = queryExecutor.execute();
                result.getQueryResults().add(queryResult);

                if (queryResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute attribute checks
            // TODO: Add attribute check execution

        } catch (Exception e) {
            result.setStatus(CheckStatus.FAILURE);
            // Create a failure message
        }

        result.setEndTime(new Date());
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        return result;
    }
}
