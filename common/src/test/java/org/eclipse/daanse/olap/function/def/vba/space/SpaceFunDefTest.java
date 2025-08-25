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
import static org.mockito.Mockito.mock;

import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionParameter;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpaceFunDefTest {

    private SpaceFunDef spaceFunDef;
    private ResolvedFunCall resolvedFunCall;
    private ExpressionCompiler expressionCompiler;
    private IntegerCalc integerCalc;
    private Expression expression;

    @BeforeEach
    void setUp() {
        spaceFunDef = new SpaceFunDef();
        resolvedFunCall = mock(ResolvedFunCall.class);
        expressionCompiler = mock(ExpressionCompiler.class);
        integerCalc = mock(IntegerCalc.class);
        expression = mock(Expression.class);
    }

    @Test
    void shouldHaveCorrectFunctionName() {
        FunctionMetaData metaData = spaceFunDef.getFunctionMetaData();
        assertThat(metaData.operationAtom().name()).isEqualTo("Space");
    }

    @Test
    void shouldHaveStringReturnType() {
        FunctionMetaData metaData = spaceFunDef.getFunctionMetaData();
        DataType returnType = metaData.returnCategory();

        assertThat(returnType).isEqualTo(DataType.STRING);
    }

    @Test
    void shouldHaveSingleIntegerParameter() {
        FunctionMetaData metaData = spaceFunDef.getFunctionMetaData();
        FunctionParameter[] parameters = metaData.parameters();

        assertThat(parameters).hasSize(1);
        assertThat(parameters[0].dataType()).isEqualTo(DataType.INTEGER);
        assertThat(parameters[0].name().orElse("")).isEqualTo("Number");
    }

}