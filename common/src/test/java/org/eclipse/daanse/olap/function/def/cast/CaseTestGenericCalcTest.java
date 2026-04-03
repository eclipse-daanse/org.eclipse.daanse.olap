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
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.type.MemberType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CaseTestGenericCalcTest {

	private Evaluator evaluator = mock(Evaluator.class);
	BooleanCalc bc = mock(BooleanCalc.class);
    private CaseTestGenericCalc caseTestGenericCalc;
    private BooleanCalc[] conditionCalcs = {bc};
    private Calc<String> exprCalc = mock(StringCalc.class);
    private Calc<?>[] exprCalcs = {exprCalc};
    Calc<String> defaultCalc = mock(StringCalc.class);
    Calc<?>[] calcs = {};

    @BeforeEach
    void setUp() {
    	caseTestGenericCalc = new CaseTestGenericCalc(MemberType.Unknown, conditionCalcs, exprCalcs, defaultCalc, calcs);
    }

    @Test
    @DisplayName("Should return TestTrue")
    void shouldReturnTrue() {
    	when(bc.evaluate(evaluator)).thenReturn(true);
    	when(exprCalc.evaluate(evaluator)).thenReturn("TestTrue");
    	Object result = caseTestGenericCalc.evaluateInternal(evaluator);
        assertThat(result).isEqualTo("TestTrue");
    }

    @Test
    @DisplayName("Should return TestFalse")
    void shouldReturnFalse() {
    	when(bc.evaluate(evaluator)).thenReturn(false);
    	when(defaultCalc.evaluate(evaluator)).thenReturn("TestFalse");
    	Object result = caseTestGenericCalc.evaluateInternal(evaluator);
        assertThat(result).isEqualTo("TestFalse");
    }

    @Test
    @DisplayName("Should have MemberType as type")
    void shouldHaveMemberType() {
        assertThat(caseTestGenericCalc.getType()).isEqualTo(MemberType.Unknown);
    }
}
