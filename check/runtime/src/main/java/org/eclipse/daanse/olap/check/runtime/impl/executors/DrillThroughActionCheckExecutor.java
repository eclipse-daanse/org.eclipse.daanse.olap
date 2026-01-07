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

import org.eclipse.daanse.olap.api.element.DrillThroughAction;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.DrillThroughActionAttribute;
import org.eclipse.daanse.olap.check.model.check.DrillThroughActionAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.DrillThroughActionCheck;
import org.eclipse.daanse.olap.check.model.check.DrillThroughActionCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for DrillThroughActionCheck that verifies drill-through action
 * existence and attributes.
 */
public class DrillThroughActionCheckExecutor {

    private final DrillThroughActionCheck check;
    private final List<? extends DrillThroughAction> actions;
    private final OlapCheckFactory factory;

    public DrillThroughActionCheckExecutor(DrillThroughActionCheck check, List<? extends DrillThroughAction> actions,
            OlapCheckFactory factory) {
        this.check = check;
        this.actions = actions;
        this.factory = factory;
    }

    public DrillThroughActionCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Instant start = Instant.now();

        DrillThroughActionCheckResult result = factory.createDrillThroughActionCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setActionName(check.getActionName());
        result.setStartedAt(start);
        result.setSourceCheck(check);

        try {
            // Find the DrillThroughAction
            Optional<? extends DrillThroughAction> foundAction = findAction();

            if (foundAction.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndedAt(Instant.now());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            DrillThroughAction action = foundAction.get();
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (DrillThroughActionAttributeCheck attrCheck : check.getActionAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, action);
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

    private Optional<? extends DrillThroughAction> findAction() {
        String actionName = check.getActionName();

        return actions.stream().filter(a -> actionName != null && actionName.equals(a.getName())).findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(DrillThroughActionAttributeCheck attrCheck,
            DrillThroughAction action) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getActionAttributeValue(action, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches;
        if (attrCheck.getAttributeType() == DrillThroughActionAttribute.IS_DEFAULT) {
            Boolean expectedBool = attrCheck.getExpectedBoolean();
            Boolean actualBool = action.getIsDefault();
            matches = AttributeCheckHelper.compareBooleans(expectedBool, actualBool);
        } else {
            matches = AttributeCheckHelper.compareValues(attrCheck.getExpectedValue(), actualValue,
                    attrCheck.getMatchMode(), attrCheck.isCaseSensitive());
        }

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getActionAttributeValue(DrillThroughAction action, DrillThroughActionAttribute attributeType) {
        return switch (attributeType) {
        case NAME -> action.getName();
        case CAPTION -> action.getCaption();
        case DESCRIPTION -> action.getDescription();
        case IS_DEFAULT -> String.valueOf(action.getIsDefault());
        };
    }
}
