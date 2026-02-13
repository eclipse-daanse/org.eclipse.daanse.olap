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

import org.eclipse.daanse.olap.api.catalog.CatalogReader;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.LevelAttribute;
import org.eclipse.daanse.olap.check.model.check.LevelAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.LevelCheck;
import org.eclipse.daanse.olap.check.model.check.LevelCheckResult;
import org.eclipse.daanse.olap.check.model.check.MemberCheck;
import org.eclipse.daanse.olap.check.model.check.MemberCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for LevelCheck that verifies level existence and structure.
 */
public class LevelCheckExecutor {

    private final LevelCheck check;
    private final List<Level> levels;
    private final Cube cube;
    private final CatalogReader catalogReader;
    private final Connection connection;
    private final OlapCheckFactory factory;

    public LevelCheckExecutor(LevelCheck check, List<Level> levels, Cube cube, CatalogReader catalogReader,
            Connection connection, OlapCheckFactory factory) {
        this.check = check;
        this.levels = levels;
        this.cube = cube;
        this.catalogReader = catalogReader;
        this.connection = connection;
        this.factory = factory;
    }

    public LevelCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Instant start = Instant.now();

        LevelCheckResult result = factory.createLevelCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setLevelName(check.getLevelName());
        result.setStartedAt(start);
        result.setSourceCheck(check);

        try {
            // Find the level
            Optional<Level> foundLevel = findLevel();

            if (foundLevel.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndedAt(Instant.now());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            Level level = foundLevel.get();
            result.setLevelUniqueName(level.getUniqueName());
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (LevelAttributeCheck attrCheck : check.getLevelAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, level);
                result.getAttributeResults().add(attrResult);
                if (attrResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute member checks
            List<Member> members = catalogReader.getLevelMembers(level, true);
            for (MemberCheck memberCheck : check.getMemberChecks()) {
                if (!memberCheck.isEnabled()) {
                    continue;
                }

                MemberCheckExecutor memberExecutor = new MemberCheckExecutor(memberCheck, members, cube, catalogReader,
                        connection, factory);
                MemberCheckResult memberResult = memberExecutor.execute();
                result.getMemberResults().add(memberResult);

                if (memberResult.getStatus() == CheckStatus.FAILURE) {
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

    private Optional<Level> findLevel() {
        String levelName = check.getLevelName();
        String levelUniqueName = check.getLevelUniqueName();

        return levels.stream().filter(l -> {
            if (levelUniqueName != null && !levelUniqueName.isEmpty()) {
                return levelUniqueName.equals(l.getUniqueName());
            }
            return levelName != null && levelName.equals(l.getName());
        }).findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(LevelAttributeCheck attrCheck, Level level) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getLevelAttributeValue(level, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches;
        if (attrCheck.getAttributeType() == LevelAttribute.VISIBLE
                || attrCheck.getAttributeType() == LevelAttribute.IS_ALL
                || attrCheck.getAttributeType() == LevelAttribute.IS_UNIQUE) {
            Boolean expectedBool = attrCheck.getExpectedBoolean();
            Boolean actualBool = getLevelBooleanAttribute(level, attrCheck.getAttributeType());
            matches = AttributeCheckHelper.compareBooleans(expectedBool, actualBool);
        } else if (attrCheck.getAttributeType() == LevelAttribute.DEPTH
                || attrCheck.getAttributeType() == LevelAttribute.ORDINAL
                || attrCheck.getAttributeType() == LevelAttribute.CARDINALITY
                || attrCheck.getAttributeType() == LevelAttribute.MEMBERS_WITH_DATA) {
            Integer expectedInt = attrCheck.getExpectedInt();
            Integer actualInt = getLevelIntAttribute(level, attrCheck.getAttributeType());
            matches = AttributeCheckHelper.compareInts(expectedInt, actualInt);
        } else {
            matches = AttributeCheckHelper.compareValues(attrCheck.getExpectedValue(), actualValue,
                    attrCheck.getMatchMode(), attrCheck.isCaseSensitive());
        }

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getLevelAttributeValue(Level level, LevelAttribute attributeType) {
        return switch (attributeType) {
        case NAME -> level.getName();
        case UNIQUE_NAME -> level.getUniqueName();
        case CAPTION -> level.getCaption();
        case DESCRIPTION -> level.getDescription();
        case VISIBLE -> String.valueOf(level.isVisible());
        case DEPTH -> String.valueOf(level.getDepth());
        case LEVEL_TYPE -> level.getLevelType() != null ? level.getLevelType().name() : null;
        case IS_ALL -> String.valueOf(level.isAll());
        case IS_UNIQUE -> String.valueOf(level.isUnique());
        case ORDINAL -> String.valueOf(level.getDepth());
        case CARDINALITY -> String.valueOf(cube.getLevelCardinality(level, true, true));
        case MEMBERS_WITH_DATA -> "0";
        };
    }

    private Boolean getLevelBooleanAttribute(Level level, LevelAttribute attributeType) {
        return switch (attributeType) {
        case VISIBLE -> level.isVisible();
        case IS_ALL -> level.isAll();
        case IS_UNIQUE -> level.isUnique();
        default -> null;
        };
    }

    private Integer getLevelIntAttribute(Level level, LevelAttribute attributeType) {
        return switch (attributeType) {
        case DEPTH -> level.getDepth();
        case ORDINAL -> level.getDepth();
        case CARDINALITY -> cube.getLevelCardinality(level, true, true);
        case MEMBERS_WITH_DATA -> 0;
        default -> null;
        };
    }
}
