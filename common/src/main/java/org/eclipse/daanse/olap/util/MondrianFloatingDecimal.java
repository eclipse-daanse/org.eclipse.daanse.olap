 /*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 *
 * -----------------------------------------------------------------------------
 * Copied from the ICU project's DigitList class.
 *
 * Copyright (C) 1996-2011, International Business Machines Corporation and
 * others. All Rights Reserved.
 *
 * ---- All changes after Fork in 2023 ------------------------
 * 
 * Project: Eclipse daanse
 * 
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */

package org.eclipse.daanse.olap.util;

/**
 * Representation of a number as a list of digits.
 *
 * @author tkafalas
 */
class MondrianFloatingDecimal {
    boolean isExceptional;
    boolean isNegative;
    int decExponent;
    char digits[];
    int nDigits;
    private final DigitList digitList = new DigitList();
    private final DigitList expDigitList = new DigitList();

    private static final int MAX_SIGNIFICANT_DIGITS = 19;

    public MondrianFloatingDecimal(double d) {
        if (d < 0) {
            isNegative = true;
            d = -d;
        } else {
            isNegative = false;
        }
        digitList.set(d, MAX_SIGNIFICANT_DIGITS, true);
        nDigits = 0;
        for (int i = 0; i < digitList.digits.length; i++) {
            if (digitList.digits[i] != 0) {
                nDigits++;
            }
        }
        digits = toCharArray(digitList.digits);
        isExceptional = Double.isInfinite(d);
        decExponent = digitList.decimalAt;
    }

    @Override
	public String toString() {
        final StringBuilder s = new StringBuilder(MAX_SIGNIFICANT_DIGITS);
        if (nDigits == 0) {
            return "0";
        }
        if (isNegative) {
            s.append("-");
        }
        if (decExponent < -5) {
            s.append('.').append(digits).append("E-").append(decExponent);
        } else {
            if (decExponent < 0) {
                s.append('.');
                for (int i = 0; i < decExponent; i++) {
                    s.append('0');
                }
                s.append(digits);
            } else {
                if (decExponent < nDigits) {
                    s.append(digits, 0, decExponent).append(".")
                            .append(digits, decExponent, nDigits - decExponent);
                } else {
                    if (decExponent == nDigits) {
                        s.append(digits);
                    } else {
                        if (decExponent < nDigits + 10) {
                            s.append(digits);
                            for (int i = 0; i < decExponent - nDigits; i++) {
                                s.append('0');
                            }
                        } else {
                            s.append('.').append(digits).append("E")
                                .append(decExponent);
                        }
                    }
                }
            }
        }
        return s.toString();
    }

    /**
     * Appends {@link #decExponent} to result string. Returns i plus the
     * number of chars written.
     *
     * Implementation may assume that exponent has 3 or fewer digits.
     *
     * For example, given {@code decExponent} = 2,
     * {@code formatExponent(result, 5, true, 2)}
     * will write '0' into result[5]
     * and '2' into result[6] and return 7.
     *
     * @param result Result buffer
     * @param i Initial offset into result buffer
     * @param expSign Whether to print a '+' sign if exponent is positive
     *                (always prints '-' if negative)
     * @param minExpDigits Minimum number of digits to write
     * @return Offset into result buffer after writing chars
     */
    public int formatExponent(
        char[] result,
        int i,
        boolean expSign,
        int minExpDigits)
    {
        int useExp = nDigits == 0 ? 0 : decExponent - 1;
        expDigitList.set(Math.abs(useExp));
        if (useExp < 0 || expSign) {
            result[i++] = useExp < 0 ? '-' : '+';
        }
        if (minExpDigits > expDigitList.decimalAt) {
            for (int j = 0; j < minExpDigits - expDigitList.decimalAt; j++) {
                result[i++] = '0';
            }
        }
        for (int j = 0; j < expDigitList.decimalAt; j++) {
            result[i++] = ((char) expDigitList.digits[j]);
        }
        return i;
    }

    private char[] toCharArray(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chars[i] = (char) bytes[i];
        }
        return chars;
    }
}
