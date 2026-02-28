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
package org.eclipse.daanse.olap.util.format;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.daanse.olap.util.format.internal.DaanseFloatingDecimal;
import org.junit.jupiter.api.Test;

class DaanseFloatingDecimalTest {

    @Test
    void testZero() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(0.0);
        assertThat(fd.isNegative).isFalse();
        assertThat(fd.isExceptional).isFalse();
        assertThat(fd.toString()).isEqualTo("0");
    }

    @Test
    void testPositiveInteger() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(123.0);
        assertThat(fd.isNegative).isFalse();
        assertThat(fd.isExceptional).isFalse();
        assertThat(fd.decExponent).isEqualTo(3);
        assertThat(fd.nDigits).isGreaterThan(0);
    }

    @Test
    void testNegativeNumber() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(-42.5);
        assertThat(fd.isNegative).isTrue();
        assertThat(fd.isExceptional).isFalse();
    }

    @Test
    void testVerySmallNumber() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(0.000001);
        assertThat(fd.isNegative).isFalse();
        assertThat(fd.isExceptional).isFalse();
        assertThat(fd.decExponent).isLessThanOrEqualTo(0);
    }

    @Test
    void testVeryLargeNumber() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(1e15);
        assertThat(fd.isNegative).isFalse();
        assertThat(fd.isExceptional).isFalse();
        assertThat(fd.decExponent).isEqualTo(16);
    }

    @Test
    void testPositiveInfinity() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(Double.POSITIVE_INFINITY);
        assertThat(fd.isExceptional).isTrue();
        assertThat(fd.isNegative).isFalse();
    }

    @Test
    void testNegativeInfinity() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(Double.NEGATIVE_INFINITY);
        assertThat(fd.isExceptional).isTrue();
        assertThat(fd.isNegative).isTrue();
    }

    @Test
    void testOne() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(1.0);
        assertThat(fd.isNegative).isFalse();
        assertThat(fd.decExponent).isEqualTo(1);
        assertThat(fd.nDigits).isGreaterThan(0);
        assertThat(fd.digits[0]).isEqualTo('1');
    }

    @Test
    void testFractionalNumber() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(3.14);
        assertThat(fd.isNegative).isFalse();
        assertThat(fd.decExponent).isEqualTo(1);
        assertThat(fd.nDigits).isGreaterThan(1);
    }

    @Test
    void testFormatExponentPositive() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(100.0);
        char[] result = new char[20];
        int end = fd.formatExponent(result, 0, true, 2);
        String exp = new String(result, 0, end);
        assertThat(exp).isEqualTo("+02");
    }

    @Test
    void testFormatExponentNegative() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(0.01);
        char[] result = new char[20];
        int end = fd.formatExponent(result, 0, true, 2);
        String exp = new String(result, 0, end);
        assertThat(exp).startsWith("-");
    }

    @Test
    void testFormatExponentNoSign() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(100.0);
        char[] result = new char[20];
        int end = fd.formatExponent(result, 0, false, 2);
        String exp = new String(result, 0, end);
        // No '+' sign for positive exponent when expSign is false
        assertThat(exp).doesNotStartWith("+");
    }

    @Test
    void testToStringPositive() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(12345.67);
        String s = fd.toString();
        assertThat(s).isNotNull();
        assertThat(s).doesNotStartWith("-");
    }

    @Test
    void testToStringNegative() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(-12345.67);
        String s = fd.toString();
        assertThat(s).startsWith("-");
    }

    @Test
    void testNegativeZero() {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(-0.0);
        // -0.0 should be treated as non-negative for display
        assertThat(fd.isNegative).isFalse();
    }
}
