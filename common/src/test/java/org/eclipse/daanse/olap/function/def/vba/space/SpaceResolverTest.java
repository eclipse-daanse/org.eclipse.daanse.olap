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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolutionResult;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.junit.jupiter.api.BeforeEach;
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
        when(validator.canConvert(anyInt(), eq(expression), any(), any())).thenReturn(true);

        Expression[] args = { expression };
        Optional<FunctionResolutionResult> result = spaceResolver.resolve(args, validator);

        assertThat(result).isPresent();
        assertThat(result.get().definition()).isInstanceOf(SpaceFunDef.class);
        assertThat(result.get().matchedMetaData()).isNotNull();
        assertThat(result.get().bindings()).hasSize(1);
    }

    @Test
    void shouldNotResolveWhenValidatorCannotConvert() {
        when(validator.canConvert(anyInt(), eq(expression), any(), any())).thenReturn(false);

        Expression[] args = { expression };
        Optional<FunctionResolutionResult> result = spaceResolver.resolve(args, validator);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotResolveWithWrongArgumentCount() {
        when(validator.canConvert(anyInt(), any(Expression.class), any(), any())).thenReturn(true);

        assertThat(spaceResolver.resolve(new Expression[] {}, validator)).isEmpty();
        assertThat(spaceResolver.resolve(new Expression[] { expression, expression }, validator)).isEmpty();
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
