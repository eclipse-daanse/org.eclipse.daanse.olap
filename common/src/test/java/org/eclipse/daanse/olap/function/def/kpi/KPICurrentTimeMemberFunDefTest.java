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
package org.eclipse.daanse.olap.function.def.kpi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.api.type.MemberType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KPICurrentTimeMemberFunDefTest {

    private KPICurrentTimeMemberFunDef funDef;

    @BeforeEach
    void setUp() {
        funDef = new KPICurrentTimeMemberFunDef();
    }

    @Test
    @DisplayName("Should have function name KPICurrentTimeMember")
    void shouldHaveCorrectFunctionName() {
        assertThat(funDef.getFunctionMetaData().operationAtom().name()).isEqualTo("KPICurrentTimeMember");
    }

    @Test
    @DisplayName("Should return MEMBER data type")
    void shouldReturnMemberDataType() {
        assertThat(funDef.getFunctionMetaData().returnCategory()).isEqualTo(DataType.MEMBER);
    }

    @Test
    @DisplayName("Should have one STRING parameter")
    void shouldHaveOneStringParameter() {
        DataType[] paramTypes = funDef.getFunctionMetaData().parameterDataTypes();
        assertThat(paramTypes).hasSize(1);
        assertThat(paramTypes[0]).isEqualTo(DataType.STRING);
    }

    @Test
    @DisplayName("Should compile call to KPICurrentTimeMemberCalc")
    void shouldCompileToKPICurrentTimeMemberCalc() {
        ResolvedFunCall call = mock(ResolvedFunCall.class);
        ExpressionCompiler compiler = mock(ExpressionCompiler.class);
        Expression arg = mock(Expression.class);
        StringCalc stringCalc = mock(StringCalc.class);

        when(call.getArg(0)).thenReturn(arg);
        when(call.getType()).thenReturn(MemberType.Unknown);
        when(compiler.compileString(arg)).thenReturn(stringCalc);

        Calc<?> result = funDef.compileCall(call, compiler);

        assertThat(result).isInstanceOf(KPICurrentTimeMemberCalc.class);
    }
}
