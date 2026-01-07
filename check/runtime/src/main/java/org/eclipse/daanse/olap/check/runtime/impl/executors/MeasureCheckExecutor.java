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

import org.eclipse.daanse.olap.api.CatalogReader;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.MeasureAttribute;
import org.eclipse.daanse.olap.check.model.check.MeasureAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.MeasureCheck;
import org.eclipse.daanse.olap.check.model.check.MeasureCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for MeasureCheck that verifies measure existence and attributes.
 */
public class MeasureCheckExecutor {

    private final MeasureCheck check;
    private final List<Member> measures;
    private final Cube cube;
    private final CatalogReader catalogReader;
    private final Connection connection;
    private final OlapCheckFactory factory;

    public MeasureCheckExecutor(MeasureCheck check, List<Member> measures, Cube cube, CatalogReader catalogReader,
            Connection connection, OlapCheckFactory factory) {
        this.check = check;
        this.measures = measures;
        this.cube = cube;
        this.catalogReader = catalogReader;
        this.connection = connection;
        this.factory = factory;
    }

    public MeasureCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Instant start = Instant.now();

        MeasureCheckResult result = factory.createMeasureCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setMeasureName(check.getMeasureName());
        result.setStartedAt(start);
        result.setSourceCheck(check);

        try {
            // Find the measure
            Optional<Member> foundMeasure = findMeasure();

            if (foundMeasure.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndedAt(Instant.now());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            Member measure = foundMeasure.get();
            result.setMeasureUniqueName(measure.getUniqueName());
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (MeasureAttributeCheck attrCheck : check.getMeasureAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, measure);
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

    private Optional<Member> findMeasure() {
        String measureName = check.getMeasureName();
        String measureUniqueName = check.getMeasureUniqueName();

        return measures.stream().filter(m -> {
            if (measureUniqueName != null && !measureUniqueName.isEmpty()) {
                return measureUniqueName.equals(m.getUniqueName());
            }
            return measureName != null && measureName.equals(m.getName());
        }).findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(MeasureAttributeCheck attrCheck, Member measure) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getMeasureAttributeValue(measure, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches;
        if (attrCheck.getAttributeType() == MeasureAttribute.VISIBLE) {
            Boolean expectedBool = attrCheck.getExpectedBoolean();
            Boolean actualBool = measure.isVisible();
            matches = AttributeCheckHelper.compareBooleans(expectedBool, actualBool);
        } else {
            matches = AttributeCheckHelper.compareValues(attrCheck.getExpectedValue(), actualValue,
                    attrCheck.getMatchMode(), attrCheck.isCaseSensitive());
        }

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getMeasureAttributeValue(Member measure, MeasureAttribute attributeType) {
        return switch (attributeType) {
        case NAME -> measure.getName();
        case UNIQUE_NAME -> measure.getUniqueName();
        case CAPTION -> measure.getCaption();
        case DESCRIPTION -> measure.getDescription();
        case VISIBLE -> String.valueOf(measure.isVisible());
        case AGGREGATOR -> getPropertyString(measure, "AGGREGATOR");
        case DATA_TYPE -> getPropertyString(measure, "DATA_TYPE");
        case EXPRESSION -> getPropertyString(measure, "EXPRESSION");
        case FORMAT_STRING -> getPropertyString(measure, "FORMAT_STRING");
        case MEASURE_GROUP_NAME -> getPropertyString(measure, "MEASUREGROUP_NAME");
        };
    }

    private String getPropertyString(Member member, String propertyName) {
        Object value = member.getPropertyValue(propertyName);
        return value != null ? value.toString() : null;
    }
}
