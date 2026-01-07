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

import org.eclipse.daanse.olap.api.element.NamedSet;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.NamedSetAttribute;
import org.eclipse.daanse.olap.check.model.check.NamedSetAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.NamedSetCheck;
import org.eclipse.daanse.olap.check.model.check.NamedSetCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for NamedSetCheck that verifies named set existence and attributes.
 */
public class NamedSetCheckExecutor {

    private final NamedSetCheck check;
    private final List<? extends NamedSet> namedSets;
    private final OlapCheckFactory factory;

    public NamedSetCheckExecutor(NamedSetCheck check, List<? extends NamedSet> namedSets, OlapCheckFactory factory) {
        this.check = check;
        this.namedSets = namedSets;
        this.factory = factory;
    }

    public NamedSetCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Instant start = Instant.now();

        NamedSetCheckResult result = factory.createNamedSetCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setNamedSetName(check.getNamedSetName());
        result.setStartedAt(start);
        result.setSourceCheck(check);

        try {
            // Find the NamedSet
            Optional<? extends NamedSet> foundNamedSet = findNamedSet();

            if (foundNamedSet.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndedAt(Instant.now());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            NamedSet namedSet = foundNamedSet.get();
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (NamedSetAttributeCheck attrCheck : check.getNamedSetAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, namedSet);
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

    private Optional<? extends NamedSet> findNamedSet() {
        String namedSetName = check.getNamedSetName();

        return namedSets.stream().filter(ns -> namedSetName != null && namedSetName.equals(ns.getName())).findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(NamedSetAttributeCheck attrCheck, NamedSet namedSet) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getNamedSetAttributeValue(namedSet, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches;
        if (attrCheck.getAttributeType() == NamedSetAttribute.IS_DYNAMIC) {
            Boolean expectedBool = attrCheck.getExpectedBoolean();
            Boolean actualBool = namedSet.isDynamic();
            matches = AttributeCheckHelper.compareBooleans(expectedBool, actualBool);
        } else {
            matches = AttributeCheckHelper.compareValues(attrCheck.getExpectedValue(), actualValue,
                    attrCheck.getMatchMode(), attrCheck.isCaseSensitive());
        }

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getNamedSetAttributeValue(NamedSet namedSet, NamedSetAttribute attributeType) {
        return switch (attributeType) {
        case NAME -> namedSet.getName();
        case CAPTION -> namedSet.getCaption();
        case DESCRIPTION -> namedSet.getDescription();
        case DISPLAY_FOLDER -> namedSet.getDisplayFolder();
        case IS_DYNAMIC -> String.valueOf(namedSet.isDynamic());
        case EXPRESSION -> namedSet.getExp() != null ? namedSet.getExp().toString() : null;
        };
    }
}
