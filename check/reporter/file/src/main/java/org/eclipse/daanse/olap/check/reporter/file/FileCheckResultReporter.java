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
package org.eclipse.daanse.olap.check.reporter.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CatalogCheckResult;
import org.eclipse.daanse.olap.check.model.check.CellCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckExecutionResult;
import org.eclipse.daanse.olap.check.model.check.CheckFailure;
import org.eclipse.daanse.olap.check.model.check.CheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckSkipped;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.CubeCheckResult;
import org.eclipse.daanse.olap.check.model.check.DatabaseColumnCheckResult;
import org.eclipse.daanse.olap.check.model.check.DatabaseSchemaCheckResult;
import org.eclipse.daanse.olap.check.model.check.DatabaseTableCheckResult;
import org.eclipse.daanse.olap.check.model.check.DimensionCheckResult;
import org.eclipse.daanse.olap.check.model.check.DrillThroughActionCheckResult;
import org.eclipse.daanse.olap.check.model.check.HierarchyCheckResult;
import org.eclipse.daanse.olap.check.model.check.KPICheckResult;
import org.eclipse.daanse.olap.check.model.check.LevelCheckResult;
import org.eclipse.daanse.olap.check.model.check.MeasureCheckResult;
import org.eclipse.daanse.olap.check.model.check.MemberCheckResult;
import org.eclipse.daanse.olap.check.model.check.NamedSetCheckResult;
import org.eclipse.daanse.olap.check.model.check.PropertyCheckResult;
import org.eclipse.daanse.olap.check.model.check.QueryCheckResult;
import org.eclipse.daanse.olap.check.reporter.api.CheckResultReporter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = CheckResultReporter.class, configurationPid = "daanse.olap.check.reporter.file")
@Designate(factory = true, ocd = FileCheckResultReporterConfig.class)
public class FileCheckResultReporter implements CheckResultReporter {

    private static final Logger logger = LoggerFactory.getLogger(FileCheckResultReporter.class);
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private Path outputDir;

    @Activate
    public void activate(FileCheckResultReporterConfig config) throws IOException {
        this.outputDir = Path.of(config.output_dir());
        Files.createDirectories(outputDir);
    }

    @Override
    public void report(List<CheckExecutionResult> results, String suiteName) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Check Results: ").append(suiteName).append("\n\n");

        for (CheckExecutionResult result : results) {
            appendExecutionResult(sb, result);
        }

        String fileName = suiteName.replace("/", "_").replace("\\", "_") + ".md";
        Path filePath = outputDir.resolve(fileName);
        try {
            Files.writeString(filePath, sb.toString());
            logger.info("Check results written to: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to write check results to: {}", filePath, e);
        }
    }

    private void appendExecutionResult(StringBuilder sb, CheckExecutionResult result) {
        String status = result.isSuccess() ? "PASSED" : "FAILED";

        sb.append("## ").append(result.getName() != null ? result.getName() : "Connection Check").append("\n\n");

        sb.append("| Property | Value |\n");
        sb.append("|----------|-------|\n");
        sb.append("| Status | **").append(status).append("** |\n");
        sb.append("| Passed | ").append(result.getSuccessCount()).append(" |\n");
        sb.append("| Failed | ").append(result.getFailureCount()).append(" |\n");
        sb.append("| Skipped | ").append(result.getSkippedCount()).append(" |\n");
        sb.append("| Duration | ").append(result.getTotalExecutionTimeMs()).append("ms |\n");

        if (result.getStartedAt() != null) {
            sb.append("| Started | ").append(TIMESTAMP_FMT.format(result.getStartedAt())).append(" |\n");
        }
        if (result.getEndedAt() != null) {
            sb.append("| Ended | ").append(TIMESTAMP_FMT.format(result.getEndedAt())).append(" |\n");
        }
        sb.append("\n");

        if (result.getFailureCount() > 0) {
            sb.append("### Failures\n\n");
            sb.append("| Check | Message | Expected | Actual |\n");
            sb.append("|-------|---------|----------|--------|\n");
            collectFailures(sb, result.getCheckResults());
            sb.append("\n");
        }

        if (result.getSkippedCount() > 0) {
            sb.append("### Skipped\n\n");
            sb.append("| Check | Reason |\n");
            sb.append("|-------|--------|\n");
            collectSkipped(sb, result.getCheckResults());
            sb.append("\n");
        }

        sb.append("### Details\n\n");
        for (CheckResult checkResult : result.getCheckResults()) {
            appendCheckResult(sb, checkResult, 0);
        }
        sb.append("\n");
    }

    private void collectFailures(StringBuilder sb, List<CheckResult> results) {
        for (CheckResult result : results) {
            if (result instanceof CheckFailure failure) {
                sb.append("| ").append(esc(failure.getCheckName()));
                sb.append(" | ").append(esc(failure.getMessage()));
                sb.append(" | ").append(esc(failure.getExpectedValue()));
                sb.append(" | ").append(esc(failure.getActualValue()));
                sb.append(" |\n");
            }
            collectFailuresFromChildren(sb, result);
        }
    }

    private void collectSkipped(StringBuilder sb, List<CheckResult> results) {
        for (CheckResult result : results) {
            if (result instanceof CheckSkipped skipped) {
                sb.append("| ").append(esc(skipped.getCheckName()));
                sb.append(" | ").append(skipped.getReason());
                sb.append(" |\n");
            }
            collectSkippedFromChildren(sb, result);
        }
    }

    private void appendCheckResult(StringBuilder sb, CheckResult result, int depth) {
        String prefix = "  ".repeat(depth);
        String icon = switch (result.getStatus()) {
        case SUCCESS -> "[x]";
        case FAILURE -> "[ ]";
        case SKIPPED -> "[-]";
        };

        sb.append(prefix).append("- ").append(icon).append(" ").append(result.getCheckName());

        if (result instanceof CheckFailure failure && failure.getMessage() != null) {
            sb.append(" - ").append(failure.getMessage());
        }
        sb.append("\n");

        appendChildResults(sb, result, depth + 1);
    }

    private void appendChildResults(StringBuilder sb, CheckResult result, int depth) {
        switch (result) {
        case CatalogCheckResult r -> {
            r.getCubeResults().forEach(c -> appendCheckResult(sb, c, depth));
            r.getDatabaseSchemaResults().forEach(c -> appendCheckResult(sb, c, depth));
            r.getQueryResults().forEach(c -> appendCheckResult(sb, c, depth));
        }
        case CubeCheckResult r -> {
            r.getDimensionResults().forEach(c -> appendCheckResult(sb, c, depth));
            r.getMeasureResults().forEach(c -> appendCheckResult(sb, c, depth));
        }
        case DimensionCheckResult r -> r.getHierarchyResults().forEach(c -> appendCheckResult(sb, c, depth));
        case HierarchyCheckResult r -> r.getLevelResults().forEach(c -> appendCheckResult(sb, c, depth));
        case LevelCheckResult r -> r.getMemberResults().forEach(c -> appendCheckResult(sb, c, depth));
        default -> {
        }
        }
    }

    private void collectFailuresFromChildren(StringBuilder sb, CheckResult result) {
        switch (result) {
        case CatalogCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Catalog : ").append(esc(r.getCatalogName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
                collectFailures(sb, list(r.getCubeResults()));
                collectFailures(sb, list(r.getDatabaseSchemaResults()));
                collectFailures(sb, list(r.getQueryResults()));
        	}
        }
        case CubeCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Cube : ").append(esc(r.getCubeName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
                collectFailures(sb, list(r.getDimensionResults()));
                collectFailures(sb, list(r.getMeasureResults()));
                collectFailures(sb, list(r.getDrillThroughActionResults()));
                collectFailures(sb, list(r.getNamedSetResults()));
                collectFailures(sb, list(r.getKpiResults()));
            }
        }
        case DatabaseSchemaCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Database : ").append(esc(r.getSchemaName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
                collectFailures(sb, list(r.getTableResults()));
            }
        }
        case DatabaseTableCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Table : ").append(esc(r.getTableName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
                collectFailures(sb, list(r.getColumnResults()));
            }
        }
        case DatabaseColumnCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Column : ").append(esc(r.getColumnName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
            }
        }
        case QueryCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append(esc(r.getQueryLanguage().getName())).append(": ").append(esc(r.getQuery()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getCellResults()));
            }
        }
        case DimensionCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Dimension : ").append(esc(r.getDimensionName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
                collectFailures(sb, list(r.getHierarchyResults()));
            }
        }
        case MeasureCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Measure : ").append(esc(r.getMeasureName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
            }
        }
        case DrillThroughActionCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Action : ").append(esc(r.getActionName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
            }
        }
        case NamedSetCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Named Set : ").append(esc(r.getNamedSetName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
            }
        }
        case KPICheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("KPI : ").append(esc(r.getKpiName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
            }
        }
        case HierarchyCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Hierarchy : ").append(esc(r.getHierarchyName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
                collectFailures(sb, list(r.getLevelResults()));
            }
        }
        case LevelCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Level : ").append(esc(r.getLevelName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
                collectFailures(sb, list(r.getMemberResults()));
            }
        }
        case MemberCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Member : ").append(esc(r.getMemberName()));
                sb.append(" | ").append("");
                sb.append(" | ").append("");
                sb.append(" |\n");
                collectFailures(sb, list(r.getAttributeResults()));
                collectFailures(sb, list(r.getPropertyResults()));
            }
        }
        case PropertyCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Property : ").append(esc(r.getPropertyName()));
                sb.append(" | ").append(r.getExpectedValue());
                sb.append(" | ").append(r.getActualValue());
                sb.append(" |\n");
            }
        }
        case AttributeCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("Attribute : ").append(esc(r.getAttributeName()));
                sb.append(" | ").append(r.getExpectedValue());
                sb.append(" | ").append(r.getActualValue());
                sb.append(" |\n");
            }
        }
        case CellCheckResult r -> {
            if (CheckStatus.FAILURE.equals(r.getStatus())) {
                sb.append("| ").append(esc(r.getCheckName()));
                sb.append(" | ").append("");
                sb.append(" | ").append(r.getExpectedValue());
                sb.append(" | ").append(r.getActualValue());
                sb.append(" |\n");
            }
        }
        default -> {
        }
        }
    }

    private void collectSkippedFromChildren(StringBuilder sb, CheckResult result) {
        switch (result) {
        case CatalogCheckResult r -> {
            collectSkipped(sb, list(r.getCubeResults()));
            collectSkipped(sb, list(r.getDatabaseSchemaResults()));
            collectSkipped(sb, list(r.getQueryResults()));
        }
        case CubeCheckResult r -> {
            collectSkipped(sb, list(r.getDimensionResults()));
            collectSkipped(sb, list(r.getMeasureResults()));
        }
        case DimensionCheckResult r -> collectSkipped(sb, list(r.getHierarchyResults()));
        case HierarchyCheckResult r -> collectSkipped(sb, list(r.getLevelResults()));
        case LevelCheckResult r -> collectSkipped(sb, list(r.getMemberResults()));
        default -> {
        }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends CheckResult> List<CheckResult> list(List<T> items) {
        return (List<CheckResult>) (List<?>) items;
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "\\|").replace("\n", " ");
    }
}
