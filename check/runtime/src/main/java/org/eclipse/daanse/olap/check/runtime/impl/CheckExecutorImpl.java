/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.daanse.olap.check.runtime.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.eclipse.daanse.olap.api.CatalogReader;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.check.model.check.CatalogCheck;
import org.eclipse.daanse.olap.check.model.check.CatalogCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckExecutionResult;
import org.eclipse.daanse.olap.check.model.check.CheckFailure;
import org.eclipse.daanse.olap.check.model.check.CheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.CubeCheck;
import org.eclipse.daanse.olap.check.model.check.CubeCheckResult;
import org.eclipse.daanse.olap.check.model.check.DimensionCheck;
import org.eclipse.daanse.olap.check.model.check.DimensionCheckResult;
import org.eclipse.daanse.olap.check.model.check.HierarchyCheck;
import org.eclipse.daanse.olap.check.model.check.HierarchyCheckResult;
import org.eclipse.daanse.olap.check.model.check.LevelCheck;
import org.eclipse.daanse.olap.check.model.check.LevelCheckResult;
import org.eclipse.daanse.olap.check.model.check.MemberCheck;
import org.eclipse.daanse.olap.check.model.check.MemberCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheck;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;
import org.eclipse.daanse.olap.check.model.check.OlapCheckModel;
import org.eclipse.daanse.olap.check.runtime.api.CheckExecutor;
import org.eclipse.daanse.olap.check.runtime.api.CheckModelLoader;
import org.eclipse.daanse.olap.check.runtime.impl.executors.CatalogCheckExecutor;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of CheckExecutor that executes OLAP check models.
 */
@Component(service = CheckExecutor.class)
public class CheckExecutorImpl implements CheckExecutor {

    private final OlapCheckFactory factory = OlapCheckFactory.eINSTANCE;

    @Reference
    private CheckModelLoader modelLoader;

    @Override
    public CheckExecutionResult execute(OlapCheckModel model, Connection connection) {
        CheckExecutionResult result = factory.createCheckExecutionResult();
        result.setName(model.getName());
        result.setDescription(model.getDescription());
        result.setStartTime(new Date());
        result.setSourceModel(model);

        CatalogReader catalogReader = connection.getCatalogReader();
        int successCount = 0;
        int failureCount = 0;
        int skippedCount = 0;

        // Execute all catalog checks
        for (CatalogCheck catalogCheck : model.getCatalogChecks()) {
            if (!catalogCheck.isEnabled()) {
                skippedCount++;
                continue;
            }

            CatalogCheckExecutor executor = new CatalogCheckExecutor(
                catalogCheck, catalogReader, connection, factory
            );
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

    @Override
    public CheckExecutionResult execute(URI modelUri, Connection connection) {
        try {
            OlapCheckModel model = modelLoader.loadFromXMI(modelUri);
            return execute(model, connection);
        } catch (IOException e) {
            // Return a failure result
            CheckExecutionResult result = factory.createCheckExecutionResult();
            result.setName("LoadFailure");
            result.setStartTime(new Date());
            result.setEndTime(new Date());
            result.setSuccess(false);
            result.setFailureCount(1);

            CheckFailure failure = factory.createCheckFailure();
            failure.setCheckName("ModelLoad");
            failure.setStatus(CheckStatus.FAILURE);
            failure.setMessage("Failed to load model from: " + modelUri);
            failure.setException(e.getMessage());
            result.getCheckResults().add(failure);

            return result;
        }
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
