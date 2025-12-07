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

import org.eclipse.daanse.olap.api.element.DatabaseColumn;
import org.eclipse.daanse.olap.api.element.DatabaseTable;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.DatabaseColumnCheck;
import org.eclipse.daanse.olap.check.model.check.DatabaseColumnCheckResult;
import org.eclipse.daanse.olap.check.model.check.DatabaseTableAttribute;
import org.eclipse.daanse.olap.check.model.check.DatabaseTableAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.DatabaseTableCheck;
import org.eclipse.daanse.olap.check.model.check.DatabaseTableCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for DatabaseTableCheck that verifies database table existence and
 * structure.
 */
public class DatabaseTableCheckExecutor {

    private final DatabaseTableCheck check;
    private final List<DatabaseTable> tables;
    private final OlapCheckFactory factory;

    public DatabaseTableCheckExecutor(DatabaseTableCheck check, List<DatabaseTable> tables, OlapCheckFactory factory) {
        this.check = check;
        this.tables = tables;
        this.factory = factory;
    }

    public DatabaseTableCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Date start = new Date();

        DatabaseTableCheckResult result = factory.createDatabaseTableCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setTableName(check.getTableName());
        result.setStartTime(start);
        result.setSourceCheck(check);

        try {
            // Find the table
            Optional<DatabaseTable> foundTable = findTable();

            if (foundTable.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndTime(new Date());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            DatabaseTable table = foundTable.get();
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (DatabaseTableAttributeCheck attrCheck : check.getTableAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, table);
                result.getAttributeResults().add(attrResult);
                if (attrResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute column checks
            List<DatabaseColumn> columns = table.getDbColumns();
            for (DatabaseColumnCheck columnCheck : check.getColumnChecks()) {
                if (!columnCheck.isEnabled()) {
                    continue;
                }

                DatabaseColumnCheckExecutor columnExecutor = new DatabaseColumnCheckExecutor(columnCheck, columns,
                        factory);
                DatabaseColumnCheckResult columnResult = columnExecutor.execute();
                result.getColumnResults().add(columnResult);

                if (columnResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

        } catch (Exception e) {
            result.setStatus(CheckStatus.FAILURE);
        }

        result.setEndTime(new Date());
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        return result;
    }

    private Optional<DatabaseTable> findTable() {
        String tableName = check.getTableName();

        return tables.stream().filter(t -> tableName != null && tableName.equals(t.getName())).findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(DatabaseTableAttributeCheck attrCheck, DatabaseTable table) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getTableAttributeValue(table, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches = AttributeCheckHelper.compareValues(attrCheck.getExpectedValue(), actualValue,
                attrCheck.getMatchMode(), attrCheck.isCaseSensitive());

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getTableAttributeValue(DatabaseTable table, DatabaseTableAttribute attributeType) {
        return switch (attributeType) {
        case NAME -> table.getName();
        case DESCRIPTION -> table.getDescription();
        };
    }
}
