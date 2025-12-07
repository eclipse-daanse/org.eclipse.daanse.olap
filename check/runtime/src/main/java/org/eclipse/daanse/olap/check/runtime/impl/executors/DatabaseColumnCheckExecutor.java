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
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.DatabaseColumnAttribute;
import org.eclipse.daanse.olap.check.model.check.DatabaseColumnAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.DatabaseColumnCheck;
import org.eclipse.daanse.olap.check.model.check.DatabaseColumnCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for DatabaseColumnCheck that verifies database column existence and
 * attributes.
 */
public class DatabaseColumnCheckExecutor {

    private final DatabaseColumnCheck check;
    private final List<DatabaseColumn> columns;
    private final OlapCheckFactory factory;

    public DatabaseColumnCheckExecutor(DatabaseColumnCheck check, List<DatabaseColumn> columns,
            OlapCheckFactory factory) {
        this.check = check;
        this.columns = columns;
        this.factory = factory;
    }

    public DatabaseColumnCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Date start = new Date();

        DatabaseColumnCheckResult result = factory.createDatabaseColumnCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setColumnName(check.getColumnName());
        result.setStartTime(start);
        result.setSourceCheck(check);

        try {
            // Find the column
            Optional<DatabaseColumn> foundColumn = findColumn();

            if (foundColumn.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndTime(new Date());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            DatabaseColumn column = foundColumn.get();
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (DatabaseColumnAttributeCheck attrCheck : check.getColumnAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, column);
                result.getAttributeResults().add(attrResult);
                if (attrResult.getStatus() == CheckStatus.FAILURE) {
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

    private Optional<DatabaseColumn> findColumn() {
        String columnName = check.getColumnName();

        return columns.stream().filter(c -> columnName != null && columnName.equals(c.getName())).findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(DatabaseColumnAttributeCheck attrCheck, DatabaseColumn column) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getColumnAttributeValue(column, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches;
        if (attrCheck.getAttributeType() == DatabaseColumnAttribute.NULLABLE) {
            Boolean expectedBool = attrCheck.getExpectedBoolean();
            Boolean actualBool = column.getNullable();
            matches = AttributeCheckHelper.compareBooleans(expectedBool, actualBool);
        } else if (attrCheck.getAttributeType() == DatabaseColumnAttribute.COLUMN_SIZE
                || attrCheck.getAttributeType() == DatabaseColumnAttribute.DECIMAL_DIGITS) {
            Integer expectedInt = attrCheck.getExpectedInt();
            Integer actualInt = getColumnIntAttribute(column, attrCheck.getAttributeType());
            matches = AttributeCheckHelper.compareInts(expectedInt, actualInt);
        } else {
            matches = AttributeCheckHelper.compareValues(attrCheck.getExpectedValue(), actualValue,
                    attrCheck.getMatchMode(), attrCheck.isCaseSensitive());
        }

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getColumnAttributeValue(DatabaseColumn column, DatabaseColumnAttribute attributeType) {
        return switch (attributeType) {
        case NAME -> column.getName();
        case TYPE -> column.getType() != null ? column.getType().name() : null;
        case NULLABLE -> String.valueOf(column.getNullable());
        case COLUMN_SIZE -> String.valueOf(column.getColumnSize());
        case DECIMAL_DIGITS -> String.valueOf(column.getDecimalDigits());
        };
    }

    private Integer getColumnIntAttribute(DatabaseColumn column, DatabaseColumnAttribute attributeType) {
        return switch (attributeType) {
        case COLUMN_SIZE -> column.getColumnSize();
        case DECIMAL_DIGITS -> column.getDecimalDigits();
        default -> null;
        };
    }
}
