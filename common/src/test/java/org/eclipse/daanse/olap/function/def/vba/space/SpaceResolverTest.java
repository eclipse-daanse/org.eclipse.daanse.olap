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
package org.eclipse.daanse.olap.function.def.vba.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.Validator;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolver.Conversion;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SpaceResolverTest {

    private SpaceResolver spaceResolver;
    private Validator validator;
    private Expression expression;

    @BeforeEach
    void setUp() {
        spaceResolver = new SpaceResolver();
        validator = mock(Validator.class);
        expression = mock(Expression.class);
    }

    @Test
    void shouldResolveWithValidIntegerArgument() {
        List<Conversion> conversions = List.of();
        when(validator.canConvert(anyInt(), eq(expression), any(), eq(conversions))).thenReturn(true);

        Expression[] args = { expression };
        FunctionDefinition result = spaceResolver.resolve(args, validator, conversions);

        assertThat(result).isInstanceOf(SpaceFunDef.class);
    }

    @Test
    void shouldHaveCorrectFunctionOperationAtom() {
        FunctionMetaData metaData = spaceResolver.getRepresentativeFunctionMetaDatas().get(0);

        assertThat(metaData.operationAtom().name()).isEqualTo("Space");
    }

    @Test
    void shouldHaveStringReturnType() {
        FunctionMetaData metaData = spaceResolver.getRepresentativeFunctionMetaDatas().get(0);
        DataType returnType = metaData.returnCategory();

        assertThat(returnType).isEqualTo(DataType.STRING);
    }

    @Test
    void shouldRequireExactlyOneParameter() {
        FunctionMetaData metaData = spaceResolver.getRepresentativeFunctionMetaDatas().get(0);
        assertThat(metaData.parameters()).hasSize(1);
    }

    @Test
    void shouldRequireIntegerParameter() {
        FunctionMetaData metaData = spaceResolver.getRepresentativeFunctionMetaDatas().get(0);
        DataType parameterType = metaData.parameters()[0].dataType();

        assertThat(parameterType).isEqualTo(DataType.INTEGER);
    }

    @Test
    @Disabled
    void shouldFailWithNoArguments() {
        List<Conversion> conversions = List.of();
        Expression[] args = {};

        assertThatExceptionOfType(Exception.class).isThrownBy(() -> spaceResolver.resolve(args, validator, conversions));
    }

    @Test
    @Disabled
    void shouldFailWithTooManyArguments() {
        List<Conversion> conversions = List.of();
        Expression[] args = { expression, expression };
        when(validator.canConvert(anyInt(), any(Expression.class), any(), eq(conversions))).thenReturn(true);

        assertThatExceptionOfType(Exception.class).isThrownBy(() -> spaceResolver.resolve(args, validator, conversions));
    }

    @Test
    @Disabled
    void shouldFailWhenValidatorCannotConvert() {
        List<Conversion> conversions = List.of();
        when(validator.canConvert(anyInt(), eq(expression), any(), eq(conversions))).thenReturn(false);

        Expression[] args = { expression };

        assertThatExceptionOfType(Exception.class).isThrownBy(() -> spaceResolver.resolve(args, validator, conversions));
    }

    @Test
    void shouldExtendParametersCheckingFunctionDefinitionResolver() {
        assertThat(spaceResolver).isInstanceOf(
                org.eclipse.daanse.olap.function.core.resolver.ParametersCheckingFunctionDefinitionResolver.class);
    }

    @Test
    void shouldHaveRepresentativeFunctionMetaDatas() {
        assertThat(spaceResolver.getRepresentativeFunctionMetaDatas()).hasSize(1);
        assertThat(spaceResolver.getRepresentativeFunctionMetaDatas().get(0)).isNotNull();
    }
}