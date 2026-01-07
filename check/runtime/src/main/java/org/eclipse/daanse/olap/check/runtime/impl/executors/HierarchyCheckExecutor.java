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
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.HierarchyAttribute;
import org.eclipse.daanse.olap.check.model.check.HierarchyAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.HierarchyCheck;
import org.eclipse.daanse.olap.check.model.check.HierarchyCheckResult;
import org.eclipse.daanse.olap.check.model.check.LevelCheck;
import org.eclipse.daanse.olap.check.model.check.LevelCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for HierarchyCheck that verifies hierarchy existence and structure.
 */
public class HierarchyCheckExecutor {

    private final HierarchyCheck check;
    private final List<Hierarchy> hierarchies;
    private final Cube cube;
    private final CatalogReader catalogReader;
    private final Connection connection;
    private final OlapCheckFactory factory;

    public HierarchyCheckExecutor(HierarchyCheck check, List<Hierarchy> hierarchies, Cube cube,
            CatalogReader catalogReader, Connection connection, OlapCheckFactory factory) {
        this.check = check;
        this.hierarchies = hierarchies;
        this.cube = cube;
        this.catalogReader = catalogReader;
        this.connection = connection;
        this.factory = factory;
    }

    public HierarchyCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Instant start = Instant.now();

        HierarchyCheckResult result = factory.createHierarchyCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setHierarchyName(check.getHierarchyName());
        result.setStartedAt(start);
        result.setSourceCheck(check);

        try {
            // Find the hierarchy
            Optional<Hierarchy> foundHierarchy = findHierarchy();

            if (foundHierarchy.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndedAt(Instant.now());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            Hierarchy hierarchy = foundHierarchy.get();
            result.setHierarchyUniqueName(hierarchy.getUniqueName());
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (HierarchyAttributeCheck attrCheck : check.getHierarchyAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, hierarchy);
                result.getAttributeResults().add(attrResult);
                if (attrResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute level checks
            List<Level> levels = catalogReader.getHierarchyLevels(hierarchy);
            for (LevelCheck levelCheck : check.getLevelChecks()) {
                if (!levelCheck.isEnabled()) {
                    continue;
                }

                LevelCheckExecutor levelExecutor = new LevelCheckExecutor(levelCheck, levels, cube, catalogReader,
                        connection, factory);
                LevelCheckResult levelResult = levelExecutor.execute();
                result.getLevelResults().add(levelResult);

                if (levelResult.getStatus() == CheckStatus.FAILURE) {
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

    private Optional<Hierarchy> findHierarchy() {
        String hierarchyName = check.getHierarchyName();
        String hierarchyUniqueName = check.getHierarchyUniqueName();

        return hierarchies.stream().filter(h -> {
            if (hierarchyUniqueName != null && !hierarchyUniqueName.isEmpty()) {
                return hierarchyUniqueName.equals(h.getUniqueName());
            }
            return hierarchyName != null && hierarchyName.equals(h.getName());
        }).findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(HierarchyAttributeCheck attrCheck, Hierarchy hierarchy) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getHierarchyAttributeValue(hierarchy, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches;
        if (attrCheck.getAttributeType() == HierarchyAttribute.VISIBLE
                || attrCheck.getAttributeType() == HierarchyAttribute.HAS_ALL) {
            Boolean expectedBool = attrCheck.getExpectedBoolean();
            Boolean actualBool = getHierarchyBooleanAttribute(hierarchy, attrCheck.getAttributeType());
            matches = AttributeCheckHelper.compareBooleans(expectedBool, actualBool);
        } else if (attrCheck.getAttributeType() == HierarchyAttribute.CARDINALITY) {
            Integer expectedInt = attrCheck.getExpectedInt();
            Integer actualInt = getHierarchyIntAttribute(hierarchy, attrCheck.getAttributeType());
            matches = AttributeCheckHelper.compareInts(expectedInt, actualInt);
        } else {
            matches = AttributeCheckHelper.compareValues(attrCheck.getExpectedValue(), actualValue,
                    attrCheck.getMatchMode(), attrCheck.isCaseSensitive());
        }

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getHierarchyAttributeValue(Hierarchy hierarchy, HierarchyAttribute attributeType) {
        return switch (attributeType) {
        case NAME -> hierarchy.getName();
        case UNIQUE_NAME -> hierarchy.getUniqueName();
        case CAPTION -> hierarchy.getCaption();
        case DESCRIPTION -> hierarchy.getDescription();
        case VISIBLE -> String.valueOf(hierarchy.isVisible());
        case HAS_ALL -> String.valueOf(hierarchy.hasAll());
        case ALL_MEMBER_NAME -> {
            Member allMember = hierarchy.getAllMember();
            yield allMember != null ? allMember.getName() : null;
        }
        case ALL_MEMBER_UNIQUE_NAME -> {
            Member allMember = hierarchy.getAllMember();
            yield allMember != null ? allMember.getUniqueName() : null;
        }
        case DEFAULT_MEMBER -> {
            Member defaultMember = hierarchy.getDefaultMember();
            yield defaultMember != null ? defaultMember.getUniqueName() : null;
        }
        case DISPLAY_FOLDER -> hierarchy.getDisplayFolder();
        case ORIGIN -> ""; // Not typically available
        case CARDINALITY -> "0"; // Would need to calculate
        };
    }

    private Boolean getHierarchyBooleanAttribute(Hierarchy hierarchy, HierarchyAttribute attributeType) {
        return switch (attributeType) {
        case VISIBLE -> hierarchy.isVisible();
        case HAS_ALL -> hierarchy.hasAll();
        default -> null;
        };
    }

    private Integer getHierarchyIntAttribute(Hierarchy hierarchy, HierarchyAttribute attributeType) {
        return switch (attributeType) {
        case CARDINALITY -> 0; // Would need to calculate
        default -> null;
        };
    }
}
