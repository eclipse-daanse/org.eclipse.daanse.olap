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
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.MemberAttribute;
import org.eclipse.daanse.olap.check.model.check.MemberAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.MemberCheck;
import org.eclipse.daanse.olap.check.model.check.MemberCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;
import org.eclipse.daanse.olap.check.model.check.PropertyCheck;
import org.eclipse.daanse.olap.check.model.check.PropertyCheckResult;

/**
 * Executor for MemberCheck that verifies member existence and properties.
 */
public class MemberCheckExecutor {

    private final MemberCheck check;
    private final List<Member> members;
    private final Cube cube;
    private final CatalogReader catalogReader;
    private final Connection connection;
    private final OlapCheckFactory factory;

    public MemberCheckExecutor(MemberCheck check, List<Member> members, Cube cube,
                                CatalogReader catalogReader, Connection connection, OlapCheckFactory factory) {
        this.check = check;
        this.members = members;
        this.cube = cube;
        this.catalogReader = catalogReader;
        this.connection = connection;
        this.factory = factory;
    }

    public MemberCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Date start = new Date();

        MemberCheckResult result = factory.createMemberCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setMemberName(check.getMemberName());
        result.setStartTime(start);
        result.setSourceCheck(check);

        try {
            // Find the member
            Optional<Member> foundMember = findMember();

            if (foundMember.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndTime(new Date());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            Member member = foundMember.get();
            result.setMemberUniqueName(member.getUniqueName());
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (MemberAttributeCheck attrCheck : check.getMemberAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, member);
                result.getAttributeResults().add(attrResult);
                if (attrResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute property checks
            for (PropertyCheck propertyCheck : check.getPropertyChecks()) {
                if (!propertyCheck.isEnabled()) {
                    continue;
                }

                PropertyCheckResult propResult = executePropertyCheck(propertyCheck, member);
                result.getPropertyResults().add(propResult);

                if (propResult.getStatus() == CheckStatus.FAILURE) {
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

    private Optional<Member> findMember() {
        String memberName = check.getMemberName();
        String memberUniqueName = check.getMemberUniqueName();

        return members.stream()
            .filter(m -> {
                if (memberUniqueName != null && !memberUniqueName.isEmpty()) {
                    return memberUniqueName.equals(m.getUniqueName());
                }
                return memberName != null && memberName.equals(m.getName());
            })
            .findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(MemberAttributeCheck attrCheck, Member member) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getMemberAttributeValue(member, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches;
        if (attrCheck.getAttributeType() == MemberAttribute.VISIBLE ||
            attrCheck.getAttributeType() == MemberAttribute.IS_CALCULATED ||
            attrCheck.getAttributeType() == MemberAttribute.IS_DATA_MEMBER) {
            Boolean expectedBool = attrCheck.getExpectedBoolean();
            Boolean actualBool = getMemberBooleanAttribute(member, attrCheck.getAttributeType());
            matches = AttributeCheckHelper.compareBooleans(expectedBool, actualBool);
        } else if (attrCheck.getAttributeType() == MemberAttribute.ORDINAL ||
                   attrCheck.getAttributeType() == MemberAttribute.DEPTH ||
                   attrCheck.getAttributeType() == MemberAttribute.PARENT_COUNT ||
                   attrCheck.getAttributeType() == MemberAttribute.CHILDREN_CARDINALITY) {
            Integer expectedInt = attrCheck.getExpectedInt();
            Integer actualInt = getMemberIntAttribute(member, attrCheck.getAttributeType());
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

    private String getMemberAttributeValue(Member member, MemberAttribute attributeType) {
        return switch (attributeType) {
            case NAME -> member.getName();
            case UNIQUE_NAME -> member.getUniqueName();
            case CAPTION -> member.getCaption();
            case DESCRIPTION -> member.getDescription();
            case VISIBLE -> String.valueOf(member.isVisible());
            case MEMBER_TYPE -> member.getMemberType() != null ? member.getMemberType().name() : null;
            case IS_CALCULATED -> String.valueOf(member.isCalculated());
            case IS_DATA_MEMBER -> String.valueOf(member.getDataMember() != null && member.getDataMember() != member);
            case PARENT_UNIQUE_NAME -> {
                Member parent = member.getParentMember();
                yield parent != null ? parent.getUniqueName() : null;
            }
            case PARENT_COUNT -> "1"; // Typically 1 for non-ragged hierarchies
            case CHILDREN_CARDINALITY -> "0"; // Would need to calculate
            case ORDINAL -> String.valueOf(member.getOrdinal());
            case DEPTH -> String.valueOf(member.getDepth());
        };
    }

    private Boolean getMemberBooleanAttribute(Member member, MemberAttribute attributeType) {
        return switch (attributeType) {
            case VISIBLE -> member.isVisible();
            case IS_CALCULATED -> member.isCalculated();
            case IS_DATA_MEMBER -> member.getDataMember() != null && member.getDataMember() != member;
            default -> null;
        };
    }

    private Integer getMemberIntAttribute(Member member, MemberAttribute attributeType) {
        return switch (attributeType) {
            case ORDINAL -> member.getOrdinal();
            case DEPTH -> member.getDepth();
            case PARENT_COUNT -> 1;
            case CHILDREN_CARDINALITY -> 0;
            default -> null;
        };
    }

    private PropertyCheckResult executePropertyCheck(PropertyCheck propertyCheck, Member member) {
        long startTime = System.currentTimeMillis();
        Date start = new Date();

        PropertyCheckResult result = factory.createPropertyCheckResult();
        result.setCheckName(propertyCheck.getName());
        result.setCheckDescription(propertyCheck.getDescription());
        result.setPropertyName(propertyCheck.getPropertyName());
        result.setStartTime(start);
        result.setSourceCheck(propertyCheck);

        try {
            // Get property value from member
            Object actualValueObj = member.getPropertyValue(propertyCheck.getPropertyName());
            String actualValue = actualValueObj != null ? actualValueObj.toString() : null;
            result.setActualValue(actualValue);

            // Compare with expected value
            String expectedValue = propertyCheck.getExpectedValue();
            result.setExpectedValue(expectedValue);

            boolean matches = AttributeCheckHelper.compareValues(
                expectedValue,
                actualValue,
                propertyCheck.getMatchMode(),
                propertyCheck.isCaseSensitive()
            );
            result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);

        } catch (Exception e) {
            result.setStatus(CheckStatus.FAILURE);
        }

        result.setEndTime(new Date());
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        return result;
    }
}
