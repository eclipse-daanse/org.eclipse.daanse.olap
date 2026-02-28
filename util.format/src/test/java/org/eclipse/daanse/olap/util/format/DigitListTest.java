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

import java.math.BigInteger;

import org.eclipse.daanse.olap.util.format.internal.DigitList;
import org.junit.jupiter.api.Test;

class DigitListTest {

    @Test
    void testSetDoubleZero() {
        DigitList dl = new DigitList();
        dl.set(0.0, 19, true);
        assertThat(dl.count).isEqualTo(0);
    }

    @Test
    void testSetDoublePositive() {
        DigitList dl = new DigitList();
        dl.set(123.456, 19, true);
        assertThat(dl.decimalAt).isEqualTo(3);
        assertThat(dl.count).isGreaterThan(0);
    }

    @Test
    void testSetDoubleSmallFraction() {
        DigitList dl = new DigitList();
        dl.set(0.001, 19, true);
        assertThat(dl.decimalAt).isLessThanOrEqualTo(0);
    }

    @Test
    void testSetLongZero() {
        DigitList dl = new DigitList();
        dl.set(0L);
        assertThat(dl.count).isEqualTo(0);
        assertThat(dl.decimalAt).isEqualTo(0);
    }

    @Test
    void testSetLongPositive() {
        DigitList dl = new DigitList();
        dl.set(12345L);
        assertThat(dl.decimalAt).isEqualTo(5);
        assertThat(dl.count).isGreaterThan(0);
        // First digit should be '1'
        assertThat((char) dl.digits[0]).isEqualTo('1');
    }

    @Test
    void testSetLongMinValue() {
        DigitList dl = new DigitList();
        dl.set(Long.MIN_VALUE);
        assertThat(dl.count).isEqualTo(19);
        assertThat(dl.decimalAt).isEqualTo(19);
    }

    @Test
    void testSetBigInteger() {
        DigitList dl = new DigitList();
        dl.set(BigInteger.valueOf(9999), 0);
        assertThat(dl.decimalAt).isEqualTo(4);
        assertThat(dl.count).isGreaterThan(0);
    }

    @Test
    void testRoundUp() {
        DigitList dl = new DigitList();
        dl.set(1.55, 19, true);
        dl.round(2); // keep 2 significant digits
        assertThat(dl.count).isLessThanOrEqualTo(2);
    }

    @Test
    void testRoundNoChange() {
        DigitList dl = new DigitList();
        dl.set(100L);
        int countBefore = dl.count;
        dl.round(19); // round to 19 digits, should not change anything
        assertThat(dl.count).isEqualTo(countBefore);
    }

    @Test
    void testAppend() {
        DigitList dl = new DigitList();
        dl.append('1');
        dl.append('2');
        dl.append('3');
        assertThat(dl.count).isEqualTo(3);
        assertThat((char) dl.digits[0]).isEqualTo('1');
        assertThat((char) dl.digits[1]).isEqualTo('2');
        assertThat((char) dl.digits[2]).isEqualTo('3');
    }

    @Test
    void testSetLargeDouble() {
        DigitList dl = new DigitList();
        dl.set(1e18, 19, false);
        assertThat(dl.decimalAt).isGreaterThan(0);
    }

    @Test
    void testSetDoubleFixedPoint() {
        DigitList dl = new DigitList();
        dl.set(0.123456789, 5, true);
        // With fixed-point, maximumDigits is max fractional digits
        assertThat(dl.count).isGreaterThan(0);
    }
}
