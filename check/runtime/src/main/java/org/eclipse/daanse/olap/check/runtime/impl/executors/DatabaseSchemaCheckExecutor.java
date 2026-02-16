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
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.element.db.DatabaseSchema;
import org.eclipse.daanse.olap.api.element.db.DatabaseTable;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.DatabaseSchemaAttribute;
import org.eclipse.daanse.olap.check.model.check.DatabaseSchemaAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.DatabaseSchemaCheck;
import org.eclipse.daanse.olap.check.model.check.DatabaseSchemaCheckResult;
import org.eclipse.daanse.olap.check.model.check.DatabaseTableCheck;
import org.eclipse.daanse.olap.check.model.check.DatabaseTableCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for DatabaseSchemaCheck that verifies database schema existence and
 * structure.
 */
public class DatabaseSchemaCheckExecutor {

    private final DatabaseSchemaCheck check;
    private final List<? extends DatabaseSchema> schemas;
    private final OlapCheckFactory factory;

    public DatabaseSchemaCheckExecutor(DatabaseSchemaCheck check, List<? extends DatabaseSchema> schemas,
            OlapCheckFactory factory) {
        this.check = check;
        this.schemas = schemas;
        this.factory = factory;
    }

    public DatabaseSchemaCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Instant start = Instant.now();

        DatabaseSchemaCheckResult result = factory.createDatabaseSchemaCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setSchemaName(check.getSchemaName());
        result.setStartedAt(start);
        result.setSourceCheck(check);

        try {
            // Find the schema
            Optional<? extends DatabaseSchema> foundSchema = findSchema();

            if (foundSchema.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndedAt(Instant.now());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            DatabaseSchema schema = foundSchema.get();
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (DatabaseSchemaAttributeCheck attrCheck : check.getSchemaAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, schema);
                result.getAttributeResults().add(attrResult);
                if (attrResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute table checks
            List<DatabaseTable> tables = schema.getDbTables();
            for (DatabaseTableCheck tableCheck : check.getTableChecks()) {
                if (!tableCheck.isEnabled()) {
                    continue;
                }

                DatabaseTableCheckExecutor tableExecutor = new DatabaseTableCheckExecutor(tableCheck, tables, factory);
                DatabaseTableCheckResult tableResult = tableExecutor.execute();
                result.getTableResults().add(tableResult);

                if (tableResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

        } catch (Exception e) {
            result.setStatus(CheckStatus.FAILURE);
        }

        result.setEndedAt(Instant.now());
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        return result;
    }

    private Optional<? extends DatabaseSchema> findSchema() {
        String schemaName = check.getSchemaName();

        return schemas.stream().filter(s -> schemaName != null && schemaName.equals(s.getName())).findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(DatabaseSchemaAttributeCheck attrCheck, DatabaseSchema schema) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getSchemaAttributeValue(schema, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches = AttributeCheckHelper.compareValues(attrCheck.getExpectedValue(), actualValue,
                attrCheck.getMatchMode(), attrCheck.isCaseSensitive());

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getSchemaAttributeValue(DatabaseSchema schema, DatabaseSchemaAttribute attributeType) {
        return switch (attributeType) {
        case NAME -> schema.getName();
        };
    }
}
