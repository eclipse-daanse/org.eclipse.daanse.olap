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

import org.eclipse.daanse.olap.api.element.KPI;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.KPIAttribute;
import org.eclipse.daanse.olap.check.model.check.KPIAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.KPICheck;
import org.eclipse.daanse.olap.check.model.check.KPICheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for KPICheck that verifies KPI existence and attributes.
 */
public class KPICheckExecutor {

    private final KPICheck check;
    private final List<? extends KPI> kpis;
    private final OlapCheckFactory factory;

    public KPICheckExecutor(KPICheck check, List<? extends KPI> kpis, OlapCheckFactory factory) {
        this.check = check;
        this.kpis = kpis;
        this.factory = factory;
    }

    public KPICheckResult execute() {
        long startTime = System.currentTimeMillis();
        Instant start = Instant.now();

        KPICheckResult result = factory.createKPICheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setKpiName(check.getKpiName());
        result.setStartedAt(start);
        result.setSourceCheck(check);

        try {
            // Find the KPI
            Optional<? extends KPI> foundKpi = findKpi();

            if (foundKpi.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndedAt(Instant.now());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            KPI kpi = foundKpi.get();
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (KPIAttributeCheck attrCheck : check.getKpiAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, kpi);
                result.getAttributeResults().add(attrResult);
                if (attrResult.getStatus() == CheckStatus.FAILURE) {
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

    private Optional<? extends KPI> findKpi() {
        String kpiName = check.getKpiName();

        return kpis.stream().filter(k -> kpiName != null && kpiName.equals(k.getName())).findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(KPIAttributeCheck attrCheck, KPI kpi) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getKpiAttributeValue(kpi, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches = AttributeCheckHelper.compareValues(attrCheck.getExpectedValue(), actualValue,
                attrCheck.getMatchMode(), attrCheck.isCaseSensitive());

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getKpiAttributeValue(KPI kpi, KPIAttribute attributeType) {
        return switch (attributeType) {
        case NAME -> kpi.getName();
        case DESCRIPTION -> kpi.getDescription();
        case DISPLAY_FOLDER -> kpi.getDisplayFolder();
        case VALUE -> kpi.getValue();
        case GOAL -> kpi.getGoal();
        case STATUS -> kpi.getStatus();
        case TREND -> kpi.getTrend();
        case WEIGHT -> kpi.getWeight();
        case CURRENT_TIME_MEMBER -> kpi.getCurrentTimeMember();
        case STATUS_GRAPHIC -> kpi.getStatusGraphic();
        case TREND_GRAPHIC -> kpi.getTrendGraphic();
        case PARENT_KPI_NAME -> {
            KPI parent = kpi.getParentKpi();
            yield parent != null ? parent.getName() : null;
        }
        };
    }
}
