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

import org.eclipse.daanse.olap.api.calc.BooleanCalc;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.type.MemberType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CaseTestNestedBooleanCalcTest {

	private Evaluator evaluator = mock(Evaluator.class);
	BooleanCalc bc = mock(BooleanCalc.class);
    private CaseTestNestedBooleanCalc caseTestNestedBooleanCalc;
    private BooleanCalc[] conditionCalcs = {bc};
    private Calc<Boolean> exprCalc = mock(BooleanCalc.class);
    private Calc<?>[] exprCalcs = {exprCalc};
    Calc<Boolean> defaultCalc = mock(BooleanCalc.class);
    Calc<?>[] calcs = {};

    @BeforeEach
    void setUp() {
        caseTestNestedBooleanCalc = new CaseTestNestedBooleanCalc(MemberType.Unknown, conditionCalcs, exprCalcs, defaultCalc, calcs);
    }

    @Test
    @DisplayName("Should return true")
    void shouldReturntrue() {
    	when(bc.evaluate(evaluator)).thenReturn(true);
    	when(exprCalc.evaluate(evaluator)).thenReturn(Boolean.TRUE);
        Boolean result = caseTestNestedBooleanCalc.evaluateInternal(evaluator);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false")
    void shouldReturnMemberWhenKpiFound() {
    	when(bc.evaluate(evaluator)).thenReturn(false);
    	when(defaultCalc.evaluate(evaluator)).thenReturn(Boolean.FALSE);
        Boolean result = caseTestNestedBooleanCalc.evaluateInternal(evaluator);
        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Should have MemberType as type")
    void shouldHaveMemberType() {
        assertThat(caseTestNestedBooleanCalc.getType()).isEqualTo(MemberType.Unknown);
    }
}
