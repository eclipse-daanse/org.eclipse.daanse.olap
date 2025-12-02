/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.daanse.olap.check.runtime.impl.executors;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.CatalogReader;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.DimensionAttribute;
import org.eclipse.daanse.olap.check.model.check.DimensionAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.DimensionCheck;
import org.eclipse.daanse.olap.check.model.check.DimensionCheckResult;
import org.eclipse.daanse.olap.check.model.check.HierarchyCheck;
import org.eclipse.daanse.olap.check.model.check.HierarchyCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for DimensionCheck that verifies dimension existence and structure.
 */
public class DimensionCheckExecutor {

    private final DimensionCheck check;
    private final List<Dimension> dimensions;
    private final Cube cube;
    private final CatalogReader catalogReader;
    private final Connection connection;
    private final OlapCheckFactory factory;

    public DimensionCheckExecutor(DimensionCheck check, List<Dimension> dimensions, Cube cube,
                                   CatalogReader catalogReader, Connection connection, OlapCheckFactory factory) {
        this.check = check;
        this.dimensions = dimensions;
        this.cube = cube;
        this.catalogReader = catalogReader;
        this.connection = connection;
        this.factory = factory;
    }

    public DimensionCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Date start = new Date();

        DimensionCheckResult result = factory.createDimensionCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setDimensionName(check.getDimensionName());
        result.setStartTime(start);
        result.setSourceCheck(check);

        try {
            // Find the dimension
            Optional<Dimension> foundDimension = findDimension();

            if (foundDimension.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndTime(new Date());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            Dimension dimension = foundDimension.get();
            result.setDimensionUniqueName(dimension.getUniqueName());
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (DimensionAttributeCheck attrCheck : check.getDimensionAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, dimension);
                result.getAttributeResults().add(attrResult);
                if (attrResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute hierarchy checks
            List<Hierarchy> hierarchies = catalogReader.getDimensionHierarchies(dimension);
            for (HierarchyCheck hierarchyCheck : check.getHierarchyChecks()) {
                if (!hierarchyCheck.isEnabled()) {
                    continue;
                }

                HierarchyCheckExecutor hierExecutor = new HierarchyCheckExecutor(
                    hierarchyCheck, hierarchies, cube, catalogReader, connection, factory
                );
                HierarchyCheckResult hierResult = hierExecutor.execute();
                result.getHierarchyResults().add(hierResult);

                if (hierResult.getStatus() == CheckStatus.FAILURE) {
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

    private Optional<Dimension> findDimension() {
        String dimensionName = check.getDimensionName();
        String dimensionUniqueName = check.getDimensionUniqueName();

        return dimensions.stream()
            .filter(d -> {
                if (dimensionUniqueName != null && !dimensionUniqueName.isEmpty()) {
                    return dimensionUniqueName.equals(d.getUniqueName());
                }
                return dimensionName != null && dimensionName.equals(d.getName());
            })
            .findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(DimensionAttributeCheck attrCheck, Dimension dimension) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getDimensionAttributeValue(dimension, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches;
        if (attrCheck.getAttributeType() == DimensionAttribute.VISIBLE ||
            attrCheck.getAttributeType() == DimensionAttribute.IS_VIRTUAL) {
            Boolean expectedBool = attrCheck.getExpectedBoolean();
            Boolean actualBool = getDimensionBooleanAttribute(dimension, attrCheck.getAttributeType());
            matches = AttributeCheckHelper.compareBooleans(expectedBool, actualBool);
        } else if (attrCheck.getAttributeType() == DimensionAttribute.ORDINAL ||
                   attrCheck.getAttributeType() == DimensionAttribute.CARDINALITY) {
            Integer expectedInt = attrCheck.getExpectedInt();
            Integer actualInt = getDimensionIntAttribute(dimension, attrCheck.getAttributeType());
            matches = AttributeCheckHelper.compareInts(expectedInt, actualInt);
        } else {
            matches = AttributeCheckHelper.compareValues(
                attrCheck.getExpectedValue(),
                actualValue,
                attrCheck.getMatchMode(),
                attrCheck.isCaseSensitive()
            );
        }

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getDimensionAttributeValue(Dimension dimension, DimensionAttribute attributeType) {
        return switch (attributeType) {
            case NAME -> dimension.getName();
            case UNIQUE_NAME -> dimension.getUniqueName();
            case CAPTION -> dimension.getCaption();
            case DESCRIPTION -> dimension.getDescription();
            case VISIBLE -> String.valueOf(dimension.isVisible());
            case DIMENSION_TYPE -> dimension.getDimensionType() != null ? dimension.getDimensionType().name() : null;
            case ORDINAL -> "0"; // Ordinal not directly available on Dimension
            case IS_VIRTUAL -> "false";
            case CARDINALITY -> "0";
        };
    }

    private Boolean getDimensionBooleanAttribute(Dimension dimension, DimensionAttribute attributeType) {
        return switch (attributeType) {
            case VISIBLE -> dimension.isVisible();
            case IS_VIRTUAL -> false;
            default -> null;
        };
    }

    private Integer getDimensionIntAttribute(Dimension dimension, DimensionAttribute attributeType) {
        return switch (attributeType) {
            case ORDINAL -> 0; // Ordinal not directly available on Dimension
            case CARDINALITY -> 0;
            default -> null;
        };
    }
}
