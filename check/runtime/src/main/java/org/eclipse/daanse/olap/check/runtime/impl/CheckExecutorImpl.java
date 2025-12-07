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
package org.eclipse.daanse.olap.check.runtime.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.daanse.olap.api.CatalogReader;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.connection.ConnectionProps;
import org.eclipse.daanse.olap.check.model.check.CatalogCheck;
import org.eclipse.daanse.olap.check.model.check.CatalogCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckExecutionResult;
import org.eclipse.daanse.olap.check.model.check.CheckFailure;
import org.eclipse.daanse.olap.check.model.check.CheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.ConnectionConfig;
import org.eclipse.daanse.olap.check.model.check.CubeCheckResult;
import org.eclipse.daanse.olap.check.model.check.DimensionCheckResult;
import org.eclipse.daanse.olap.check.model.check.HierarchyCheckResult;
import org.eclipse.daanse.olap.check.model.check.LevelCheckResult;
import org.eclipse.daanse.olap.check.model.check.MemberCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;
import org.eclipse.daanse.olap.check.model.check.OlapCheckSuite;
import org.eclipse.daanse.olap.check.model.check.OlapConnectionCheck;
import org.eclipse.daanse.olap.check.runtime.api.CheckExecutor;
import org.eclipse.daanse.olap.check.runtime.impl.executors.CatalogCheckExecutor;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation of CheckExecutor that executes OLAP check suites and
 * connection checks. Creates connections from ConnectionConfig for each
 * OlapConnectionCheck. Stateless - receives everything as parameters.
 */
@Component(service = CheckExecutor.class)
public class CheckExecutorImpl implements CheckExecutor {

    private final OlapCheckFactory factory = OlapCheckFactory.eINSTANCE;

    @Override
    public List<CheckExecutionResult> execute(OlapCheckSuite suite, Context<?> context) {
        List<CheckExecutionResult> results = new ArrayList<>();
        for (OlapConnectionCheck connectionCheck : suite.getConnectionChecks()) {
            results.add(execute(connectionCheck, context));
        }
        return results;
    }

    private CheckExecutionResult execute(OlapConnectionCheck connectionCheck, Context<?> context) {
        CheckExecutionResult result = factory.createCheckExecutionResult();
        result.setName(connectionCheck.getName());
        result.setDescription(connectionCheck.getDescription());
        result.setStartTime(new Date());
        result.setSourceConnectionCheck(connectionCheck);

        // Create connection from ConnectionConfig
        Connection connection;
        try {
            connection = createConnection(connectionCheck.getConnectionConfig(), context);
        } catch (Exception e) {
            // Return failure if connection cannot be created
            result.setEndTime(new Date());
            result.setSuccess(false);
            result.setFailureCount(1);

            CheckFailure failure = factory.createCheckFailure();
            failure.setCheckName("ConnectionCreate");
            failure.setStatus(CheckStatus.FAILURE);
            failure.setMessage("Failed to create connection from config");
            failure.setException(e.getMessage());
            result.getCheckResults().add(failure);

            return result;
        }

        CatalogReader catalogReader = connection.getCatalogReader();
        int successCount = 0;
        int failureCount = 0;
        int skippedCount = 0;

        // Execute all catalog checks
        for (CatalogCheck catalogCheck : connectionCheck.getCatalogChecks()) {
            if (!catalogCheck.isEnabled()) {
                skippedCount++;
                continue;
            }

            CatalogCheckExecutor executor = new CatalogCheckExecutor(catalogCheck, catalogReader, connection, factory);
            CheckResult checkResult = executor.execute();
            result.getCheckResults().add(checkResult);

            // Count results
            if (checkResult.getStatus() == CheckStatus.SUCCESS) {
                successCount += countSuccesses(checkResult);
            } else if (checkResult.getStatus() == CheckStatus.FAILURE) {
                failureCount += countFailures(checkResult);
            } else {
                skippedCount++;
            }
        }

        result.setEndTime(new Date());
        result.setTotalExecutionTimeMs(result.getEndTime().getTime() - result.getStartTime().getTime());
        result.setSuccessCount(successCount);
        result.setFailureCount(failureCount);
        result.setSkippedCount(skippedCount);
        result.setSuccess(failureCount == 0);

        return result;
    }

    /**
     * Creates a Connection from the ConnectionConfig using the Context.
     */
    private Connection createConnection(ConnectionConfig config, Context<?> context) {
        if (config == null) {
            // Use default connection if no config specified
            return context.getConnectionWithDefaultRole();
        }

        List<String> roles = config.getRoles();
        String localeStr = config.getLocale();

        Locale locale = (localeStr != null && !localeStr.isEmpty()) ? Locale.forLanguageTag(localeStr)
                : Locale.getDefault();

        ConnectionProps props = new ConnectionProps(roles != null ? roles : List.of(), locale);

        return context.getConnection(props);
    }

    private int countSuccesses(CheckResult result) {
        int count = result.getStatus() == CheckStatus.SUCCESS ? 1 : 0;

        if (result instanceof CatalogCheckResult catalogResult) {
            for (CubeCheckResult cubeResult : catalogResult.getCubeResults()) {
                count += countSuccesses(cubeResult);
            }
        } else if (result instanceof CubeCheckResult cubeResult) {
            for (DimensionCheckResult dimResult : cubeResult.getDimensionResults()) {
                count += countSuccesses(dimResult);
            }
        } else if (result instanceof DimensionCheckResult dimResult) {
            for (HierarchyCheckResult hierResult : dimResult.getHierarchyResults()) {
                count += countSuccesses(hierResult);
            }
        } else if (result instanceof HierarchyCheckResult hierResult) {
            for (LevelCheckResult levelResult : hierResult.getLevelResults()) {
                count += countSuccesses(levelResult);
            }
        } else if (result instanceof LevelCheckResult levelResult) {
            for (MemberCheckResult memberResult : levelResult.getMemberResults()) {
                count += countSuccesses(memberResult);
            }
        }

        return count;
    }

    private int countFailures(CheckResult result) {
        int count = result.getStatus() == CheckStatus.FAILURE ? 1 : 0;

        if (result instanceof CatalogCheckResult catalogResult) {
            for (CubeCheckResult cubeResult : catalogResult.getCubeResults()) {
                count += countFailures(cubeResult);
            }
        } else if (result instanceof CubeCheckResult cubeResult) {
            for (DimensionCheckResult dimResult : cubeResult.getDimensionResults()) {
                count += countFailures(dimResult);
            }
        } else if (result instanceof DimensionCheckResult dimResult) {
            for (HierarchyCheckResult hierResult : dimResult.getHierarchyResults()) {
                count += countFailures(hierResult);
            }
        } else if (result instanceof HierarchyCheckResult hierResult) {
            for (LevelCheckResult levelResult : hierResult.getLevelResults()) {
                count += countFailures(levelResult);
            }
        } else if (result instanceof LevelCheckResult levelResult) {
            for (MemberCheckResult memberResult : levelResult.getMemberResults()) {
                count += countFailures(memberResult);
            }
        }

        return count;
    }
}
