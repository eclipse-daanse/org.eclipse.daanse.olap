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

import java.time.Instant;

import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.result.Axis;
import org.eclipse.daanse.olap.api.result.Cell;
import org.eclipse.daanse.olap.api.result.Result;
import org.eclipse.daanse.olap.check.model.check.CellCheckResult;
import org.eclipse.daanse.olap.check.model.check.CellValueCheck;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;
import org.eclipse.daanse.olap.check.model.check.QueryCheck;
import org.eclipse.daanse.olap.check.model.check.QueryCheckResult;
import org.eclipse.daanse.olap.check.model.check.QueryLanguage;
import org.eclipse.emf.common.util.EList;

/**
 * Executor for QueryCheck that verifies query execution and results. Supports
 * MDX, SQL, and DAX query languages.
 */
public class QueryCheckExecutor {

    private final QueryCheck check;
    private final Connection connection;
    private final OlapCheckFactory factory;

    public QueryCheckExecutor(QueryCheck check, Connection connection, OlapCheckFactory factory) {
        this.check = check;
        this.connection = connection;
        this.factory = factory;
    }

    public QueryCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Instant start = Instant.now();

        QueryCheckResult result = factory.createQueryCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setQuery(check.getQuery());
        result.setQueryLanguage(check.getQueryLanguage());
        result.setStartedAt(start);
        result.setSourceCheck(check);

        try {
            QueryLanguage language = check.getQueryLanguage();
            if (language == null) {
                language = QueryLanguage.MDX; // Default to MDX
            }

            switch (language) {
                case MDX -> executeMdxQuery(result, startTime);
                case SQL -> executeSqlQuery(result, startTime);
                case DAX -> executeDaxQuery(result, startTime);
                default -> {
                    result.setExecutedSuccessfully(false);
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

        } catch (Exception e) {
            result.setExecutedSuccessfully(false);
            result.setStatus(CheckStatus.FAILURE);
        }

        result.setEndedAt(Instant.now());
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        return result;
    }

    private void executeMdxQuery(QueryCheckResult result, long startTime) {
        try {
            // Execute the MDX query
            Result mdxResult = connection.execute(connection.parseQuery(check.getQuery()));

            result.setExecutedSuccessfully(true);

            // Get axes for counting rows/columns
            Axis[] axes = mdxResult.getAxes();
            int rowCount = 0;
            int columnCount = 0;

            if (axes.length > 0) {
                columnCount = axes[0].getPositions().size();
            }
            if (axes.length > 1) {
                rowCount = axes[1].getPositions().size();
            }

            result.setRowCount(rowCount);
            result.setColumnCount(columnCount);
            result.setStatus(CheckStatus.SUCCESS);

            // Check expected row count (-1 means not specified)
            int expectedRowCount = check.getExpectedRowCount();
            if (expectedRowCount >= 0 && rowCount != expectedRowCount) {
                result.setStatus(CheckStatus.FAILURE);
            }

            // Check expected column count (-1 means not specified)
            int expectedColumnCount = check.getExpectedColumnCount();
            if (expectedColumnCount >= 0 && columnCount != expectedColumnCount) {
                result.setStatus(CheckStatus.FAILURE);
            }

            // Check execution time (-1 means not specified)
            long executionTime = System.currentTimeMillis() - startTime;
            long maxExecutionTime = check.getMaxExecutionTimeMs();
            if (maxExecutionTime > 0 && executionTime > maxExecutionTime) {
                result.setStatus(CheckStatus.FAILURE);
            }

            // Execute cell value checks
            for (CellValueCheck cellCheck : check.getCellChecks()) {
                CellCheckResult cellResult = executeCellCheck(cellCheck, mdxResult);
                result.getCellResults().add(cellResult);
                if (cellResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

        } catch (Exception e) {
            result.setExecutedSuccessfully(false);
            result.setStatus(CheckStatus.FAILURE);
        }
    }

    private void executeSqlQuery(QueryCheckResult result, long startTime) {
        // TODO: Implement SQL query execution
        // SQL queries would typically use JDBC to execute against the underlying
        // database
        result.setExecutedSuccessfully(false);
        result.setStatus(CheckStatus.FAILURE);
    }

    private void executeDaxQuery(QueryCheckResult result, long startTime) {
        // TODO: Implement DAX query execution
        // DAX queries would need to use appropriate DAX execution mechanism
        result.setExecutedSuccessfully(false);
        result.setStatus(CheckStatus.FAILURE);
    }

    private CellCheckResult executeCellCheck(CellValueCheck cellCheck, Result mdxResult) {
        CellCheckResult result = factory.createCellCheckResult();
        result.setCheckName(cellCheck.getName());

        // Copy coordinates
        EList<Integer> coords = cellCheck.getCoordinates();
        result.getCoordinates().addAll(coords);
        result.setExpectedValue(cellCheck.getExpectedValue());

        try {
            // Convert EList<Integer> to int[]
            int[] coordArray = new int[coords.size()];
            for (int i = 0; i < coords.size(); i++) {
                coordArray[i] = coords.get(i);
            }

            Cell cell = mdxResult.getCell(coordArray);

            Object cellValue = cell.getValue();
            String actualValue = cellValue != null ? cellValue.toString() : null;

            if (cellCheck.isCheckFormattedValue()) {
                actualValue = cell.getFormattedValue();
            }

            result.setActualValue(actualValue);

            // Compare values
            boolean matches;
            double expectedNumeric = cellCheck.getExpectedNumericValue();

            // Check if numeric comparison is needed (non-zero expectedNumericValue
            // indicates it's set)
            if (expectedNumeric != 0.0 || cellCheck.getExpectedValue() == null) {
                // Numeric comparison with tolerance
                Double actual = cellValue instanceof Number ? ((Number) cellValue).doubleValue() : null;
                double tolerance = cellCheck.getTolerance();

                matches = actual != null && Math.abs(expectedNumeric - actual) <= tolerance;
            } else {
                // String comparison
                matches = AttributeCheckHelper.compareValues(cellCheck.getExpectedValue(), actualValue,
                        cellCheck.getMatchMode(), true // cell values are case-sensitive by default
                );
            }

            result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);

        } catch (Exception e) {
            result.setStatus(CheckStatus.FAILURE);
        }

        return result;
    }
}
