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
package org.eclipse.daanse.olap.function.def.vba.date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.type.DateTimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateCalcTest {

    private DateCalc dateCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = mock(Evaluator.class);
        dateCalc = new DateCalc(DateTimeType.INSTANCE);
    }

    @Test
    @DisplayName("Should return current date with time set to midnight")
    void shouldReturnCurrentDateAtMidnight() {
        Date result = dateCalc.evaluate(evaluator);

        assertThat(result).isNotNull();

        // Convert to calendar to check time components
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(calendar.get(Calendar.MINUTE)).isEqualTo(0);
        assertThat(calendar.get(Calendar.SECOND)).isEqualTo(0);
        assertThat(calendar.get(Calendar.MILLISECOND)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return today's date")
    @Disabled
    void shouldReturnTodaysDate() {
        Date result = dateCalc.evaluate(evaluator);

        LocalDate today = LocalDate.now();
        LocalDate resultDate = result.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        assertThat(resultDate).isEqualTo(today);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(dateCalc.getType()).isEqualTo(DateTimeType.INSTANCE);
    }

    @Test
    @DisplayName("Should be consistent across multiple calls")
    void shouldBeConsistentAcrossMultipleCalls() {
        Date first = dateCalc.evaluate(evaluator);
        Date second = dateCalc.evaluate(evaluator);

        // Both should represent the same day (ignoring millisecond differences)
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(first);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(second);

        assertThat(cal1.get(Calendar.YEAR)).isEqualTo(cal2.get(Calendar.YEAR));
        assertThat(cal1.get(Calendar.MONTH)).isEqualTo(cal2.get(Calendar.MONTH));
        assertThat(cal1.get(Calendar.DAY_OF_MONTH)).isEqualTo(cal2.get(Calendar.DAY_OF_MONTH));
        assertThat(cal1.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(cal2.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
    }
}