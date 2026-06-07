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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.execution.Statement;
import org.eclipse.daanse.olap.api.query.component.DrillThrough;
import org.eclipse.daanse.olap.api.query.component.Query;
import org.eclipse.daanse.olap.api.query.component.QueryComponent;
import org.eclipse.daanse.olap.api.query.component.SqlQuery;
import org.eclipse.daanse.olap.api.result.Axis;
import org.eclipse.daanse.olap.api.result.Cell;
import org.eclipse.daanse.olap.api.result.CellSet;
import org.eclipse.daanse.olap.api.result.CellSetAxis;
import org.eclipse.daanse.olap.api.result.Position;
import org.eclipse.daanse.olap.api.result.Result;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.check.model.check.AxisCheck;
import org.eclipse.daanse.olap.check.model.check.AxisCheckResult;
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
            appendError(result, e);
        }

        result.setEndedAt(Instant.now());
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        return result;
    }

    private void executeMdxQuery(QueryCheckResult result, long startTime) {
        try {
            // Execute the MDX query
            QueryComponent queryComponent = connection.parseStatement(check.getQuery());
            if (queryComponent instanceof Query query) {
                Result mdxResult = connection.execute(query);
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

                // Execute axis checks
                for (AxisCheck axisCheck : check.getAxisChecks()) {
                    AxisCheckResult axisResult = executeAxisCheck(axisCheck, axes);
                    result.getAxisResults().add(axisResult);
                    if (axisResult.getStatus() == CheckStatus.FAILURE) {
                        result.setStatus(CheckStatus.FAILURE);
                    }
                }

            } else if (queryComponent instanceof DrillThrough drillThrough) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(drillThrough, Optional.empty(), null);
                checkResultSet(result, resultSet, startTime);
            } else {
                result.setStatus(CheckStatus.FAILURE);
            }
        } catch (Exception e) {
            result.setExecutedSuccessfully(false);
            result.setStatus(CheckStatus.FAILURE);
            appendError(result, e);
        }
    }

    private void executeSqlQuery(QueryCheckResult result, long startTime) {
        try {
            // Execute the Sql query
            QueryComponent queryComponent = connection.parseStatement(check.getQuery());
            if (queryComponent instanceof SqlQuery sqlQuery) {
                ResultSet resultSet = sqlQuery.execute();
                checkResultSet(result, resultSet, startTime);
            } else {
                result.setStatus(CheckStatus.FAILURE);
            }

        } catch (Exception e) {
            result.setExecutedSuccessfully(false);
            result.setStatus(CheckStatus.FAILURE);
            appendError(result, e);
        }
    }

    private void checkResultSet(QueryCheckResult result, ResultSet resultSet, long startTime) throws SQLException {
        List<List<Object>> res = getSqlResult(resultSet);
        resultSet.close();
        result.setExecutedSuccessfully(true);
        int rowCount = res.size();
        int columnCount = 0;
        if (res.size() > 0) {
            List<Object> row = res.get(0);
            if (row != null) {
                columnCount = row.size();
            }
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
            CellCheckResult cellResult = executeCellCheck(cellCheck, res);
            result.getCellResults().add(cellResult);
            if (cellResult.getStatus() == CheckStatus.FAILURE) {
                result.setStatus(CheckStatus.FAILURE);
            }
        }
    }

    private List<List<Object>> getSqlResult(ResultSet rs) throws SQLException {
        List<List<Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();

        // populate column defs
        for (int i = 0; i < columnCount; i++) {
            columns.add(md.getColumnLabel(i + 1));
        }

        // Populate data; assume that SqlStatement is already positioned
        // on first row (or isDone() is true), and assume that the
        // number of rows returned is limited.
        while (rs.next()) {
            List<Object> row = new ArrayList();
            for (int i = 0; i < columnCount; i++) {
                row.add(rs.getObject(i + 1));
            }
            rows.add(row);
        }

        return rows;
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
            appendCellError(result, e);
        }

        return result;
    }

    private CellCheckResult executeCellCheck(CellValueCheck cellCheck, List<List<Object>> res) {
        CellCheckResult result = factory.createCellCheckResult();
        result.setCheckName(cellCheck.getName());

        // Copy coordinates
        EList<Integer> coords = cellCheck.getCoordinates();
        result.getCoordinates().addAll(coords);
        result.setExpectedValue(cellCheck.getExpectedValue());

        try {
            int rowIndex = 0;
            int colIndex = 0;
            if (coords != null && coords.size() > 0) {
                rowIndex = coords.get(0);
            }
            ;
            if (coords != null && coords.size() > 1) {
                colIndex = coords.get(1);
            }
            ;

            List<Object> rowObject = res.size() > rowIndex ? res.get(rowIndex) : List.of();
            Object cellValue = rowObject.size() > colIndex ? rowObject.get(colIndex) : null;
            String actualValue = cellValue != null ? cellValue.toString() : null;

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
            appendCellError(result, e);
        }

        return result;
    }

    private AxisCheckResult executeAxisCheck(AxisCheck check, Axis[] axes) {
        AxisCheckResult result = factory.createAxisCheckResult();
        result.setCheckName(check.getName());
        result.setAxisIndex(check.getAxisIndex());
        result.setExpectedPositionCount(check.getExpectedPositionCount());
        result.setExpectedFirstMemberUniqueName(check.getExpectedFirstMemberUniqueName());
        result.setExpectedFirstMemberCaption(check.getExpectedFirstMemberCaption());

        int idx = check.getAxisIndex();
        if (idx < 0 || idx >= axes.length) {
            result.setStatus(CheckStatus.FAILURE);
            result.setAbsent(true);
            return result;
        }
        java.util.List<Position> positions = axes[idx].getPositions();
        int actualCount = positions.size();
        result.setActualPositionCount(actualCount);

        if (!positions.isEmpty() && !positions.get(0).isEmpty()) {
            Member first = positions.get(0).get(0);
            result.setActualFirstMemberUniqueName(first.getUniqueName());
            result.setActualFirstMemberCaption(first.getCaption());
        }

        boolean ok = true;
        if (check.getExpectedPositionCount() >= 0 && actualCount != check.getExpectedPositionCount()) {
            ok = false;
        }
        String expUn = check.getExpectedFirstMemberUniqueName();
        if (expUn != null && !expUn.isEmpty() && !expUn.equals(result.getActualFirstMemberUniqueName())) {
            ok = false;
        }
        String expCap = check.getExpectedFirstMemberCaption();
        if (expCap != null && !expCap.isEmpty() && !expCap.equals(result.getActualFirstMemberCaption())) {
            ok = false;
        }
        result.setStatus(ok ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private static void appendError(QueryCheckResult result, Throwable e) {
        String existing = result.getCheckDescription();
        String text = (existing == null || existing.isBlank() ? "" : existing + " | ") + summarize(e);
        result.setCheckDescription(text);
    }

    private static void appendCellError(CellCheckResult result, Throwable e) {
        String existing = result.getCheckName();
        result.setCheckName((existing == null ? "cell" : existing) + " (" + summarize(e) + ")");
    }

    private static String summarize(Throwable e) {
        StringBuilder sb = new StringBuilder();
        Throwable t = e;
        int depth = 0;
        while (t != null && depth < 4) {
            if (depth > 0) {
                sb.append(" <- ");
            }
            sb.append(t.getClass().getSimpleName());
            if (t.getMessage() != null) {
                sb.append(": ").append(t.getMessage().replace('\n', ' '));
            }
            t = t.getCause();
            depth++;
        }
        return sb.toString();
    }

}
