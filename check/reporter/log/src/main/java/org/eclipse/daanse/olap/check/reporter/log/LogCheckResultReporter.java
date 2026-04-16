/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.check.reporter.log;

import java.util.List;

import org.eclipse.daanse.olap.check.model.check.CatalogCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckExecutionResult;
import org.eclipse.daanse.olap.check.model.check.CheckFailure;
import org.eclipse.daanse.olap.check.model.check.CheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckSkipped;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.CubeCheckResult;
import org.eclipse.daanse.olap.check.model.check.DimensionCheckResult;
import org.eclipse.daanse.olap.check.model.check.HierarchyCheckResult;
import org.eclipse.daanse.olap.check.model.check.LevelCheckResult;
import org.eclipse.daanse.olap.check.reporter.api.CheckResultReporter;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = CheckResultReporter.class)
public class LogCheckResultReporter implements CheckResultReporter {

    private static final Logger logger = LoggerFactory.getLogger(LogCheckResultReporter.class);

    @Override
    public void report(List<CheckExecutionResult> results, String suiteName) {
        for (CheckExecutionResult result : results) {
            if (result.isSuccess()) {
                logger.info("Check suite '{}': {} passed, {} failed, {} skipped ({}ms)", suiteName,
                        result.getSuccessCount(), result.getFailureCount(), result.getSkippedCount(),
                        result.getTotalExecutionTimeMs());
            } else {
                logger.warn("Check suite '{}': {} passed, {} failed, {} skipped ({}ms)", suiteName,
                        result.getSuccessCount(), result.getFailureCount(), result.getSkippedCount(),
                        result.getTotalExecutionTimeMs());
            }

            for (CheckResult checkResult : result.getCheckResults()) {
                reportCheckResult(checkResult, "  ");
            }
        }
    }

    private void reportCheckResult(CheckResult result, String indent) {
        if (result.getStatus() == CheckStatus.FAILURE) {
            if (result instanceof CheckFailure failure) {
                logger.error("{}FAILED: {} - {} (expected='{}', actual='{}')", indent, failure.getCheckName(),
                        failure.getMessage(), failure.getExpectedValue(), failure.getActualValue());
            } else {
                logger.error("{}FAILED: {}", indent, result.getCheckName());
            }
        } else if (result.getStatus() == CheckStatus.SKIPPED) {
            if (result instanceof CheckSkipped skipped) {
                logger.info("{}SKIPPED: {} - {}", indent, skipped.getCheckName(), skipped.getReason());
            }
        }

        reportChildren(result, indent + "  ");
    }

    private void reportChildren(CheckResult result, String indent) {
        switch (result) {
        case CatalogCheckResult r -> {
            r.getCubeResults().forEach(c -> reportCheckResult(c, indent));
            r.getDatabaseSchemaResults().forEach(c -> reportCheckResult(c, indent));
            r.getQueryResults().forEach(c -> reportCheckResult(c, indent));
        }
        case CubeCheckResult r -> {
            r.getDimensionResults().forEach(c -> reportCheckResult(c, indent));
            r.getMeasureResults().forEach(c -> reportCheckResult(c, indent));
        }
        case DimensionCheckResult r -> r.getHierarchyResults().forEach(c -> reportCheckResult(c, indent));
        case HierarchyCheckResult r -> r.getLevelResults().forEach(c -> reportCheckResult(c, indent));
        case LevelCheckResult r -> r.getMemberResults().forEach(c -> reportCheckResult(c, indent));
        default -> {
        }
        }
    }
}
