/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.function.def.cast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.api.type.BooleanType;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CaseTestFunDefTest {

    private CaseTestFunDef funDef;
    private DataType dataType = DataType.STRING;
    private FunctionParameterR parameter1 = new FunctionParameterR(DataType.LOGICAL);
    private FunctionParameterR parameter2 = new FunctionParameterR(DataType.MEMBER);
    private FunctionParameterR parameter3 = new FunctionParameterR(DataType.MEMBER);;

    @BeforeEach
    void setUp() {
    	FunctionParameterR[] args = { parameter1, parameter2, parameter3 };
        funDef = new CaseTestFunDef(dataType, args);
    }

    @Test
    @DisplayName("Should have function name _CaseTest")
    void shouldHaveCorrectFunctionName() {
        assertThat(funDef.getFunctionMetaData().operationAtom().name()).isEqualTo("_CaseTest");
    }

    @Test
    @DisplayName("Should return MEMBER data type")
    void shouldReturnMemberDataType() {
        assertThat(funDef.getFunctionMetaData().returnCategory()).isEqualTo(DataType.STRING);
    }

    @Test
    @DisplayName("Should have 3 parameters")
    void shouldHaveOneStringParameter() {
        DataType[] paramTypes = funDef.getFunctionMetaData().parameterDataTypes();
        assertThat(paramTypes).hasSize(3);
        assertThat(paramTypes[0]).isEqualTo(DataType.LOGICAL);
        assertThat(paramTypes[1]).isEqualTo(DataType.MEMBER);
        assertThat(paramTypes[2]).isEqualTo(DataType.MEMBER);
    }

    @Test
    @DisplayName("Should compile call to CaseTestGenericCalc")
    void shouldCompileToCaseTestGenericCalc() {
        ResolvedFunCall call = mock(ResolvedFunCall.class);
        ExpressionCompiler compiler = mock(ExpressionCompiler.class);
        Expression expression1 = mock(Expression.class);
        Expression expression2 = mock(Expression.class);
        Expression expression3 = mock(Expression.class);
        Expression[] args = { expression1, expression2, expression3 };
        when(call.getArgs()).thenReturn(args);
        Calc<?> result = funDef.compileCall(call, compiler);
        assertThat(result).isInstanceOf(CaseTestGenericCalc.class);
    }

    @Test
    @DisplayName("Should compile call to CaseTestNestedBooleanCalc")
    void shouldCompileToCaseTestNestedBooleanCalc() {
        ResolvedFunCall call = mock(ResolvedFunCall.class);
        ExpressionCompiler compiler = mock(ExpressionCompiler.class);
        Expression expression1 = mock(Expression.class);
        Expression expression2 = mock(Expression.class);
        Expression expression3 = mock(Expression.class);
        Expression[] args = { expression1, expression2, expression3 };
        when(call.getArgs()).thenReturn(args);
        when(call.getType()).thenReturn(BooleanType.INSTANCE);
        Calc<?> result = funDef.compileCall(call, compiler);
        assertThat(result).isInstanceOf(CaseTestNestedBooleanCalc.class);
    }

}
