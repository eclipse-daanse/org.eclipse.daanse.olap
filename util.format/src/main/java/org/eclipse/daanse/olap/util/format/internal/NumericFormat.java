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
package org.eclipse.daanse.olap.util.format.internal;

import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_DECIMAL;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_E_MINUS_LOWER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_E_MINUS_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_E_PLUS_LOWER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_E_PLUS_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_SEMI;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_THOUSEP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.daanse.olap.util.format.FormatLocale;

/**
 * NumericFormat is an implementation of {@link BasicFormat} which prints
 * numbers with a given number of decimal places, leading zeroes, in exponential
 * notation, etc.
 *
 * It is implemented using {@link DaanseFloatingDecimal}.
 */
public final class NumericFormat extends JavaFormat {
    final FormatLocale locale;
    final int digitsLeftOfPoint;
    final int zeroesLeftOfPoint;
    final int digitsRightOfPoint;
    final int zeroesRightOfPoint;
    final int digitsRightOfExp;
    final int zeroesRightOfExp;

    /**
     * Number of decimal places to shift the number left before formatting it: 2
     * means multiply by 100; -3 means divide by 1000.
     */
    public int decimalShift;
    final char expChar;
    final boolean expSign;
    final boolean useDecimal;
    final boolean useThouSep;

    final ArrayList<Integer> cachedThousandSeparatorPositions;

    /**
     * Cache of parsed format strings and their thousand separator tokens length.
     * Used so we don't have to tokenize a format string over and over again.
     */
    private static final Map<String, ArrayList<Integer>> thousandSeparatorTokenMap = new ConcurrentHashMap<>();

    /**
     * Returns the format token as a string representation which corresponds to a
     * given token code.
     * 
     * @param code The code of the token to obtain.
     * @return The string representation of that token.
     */
    private static String getFormatToken(int code) {
        return FormatConstants.getFormatToken(code);
    }

    public NumericFormat(FormatLocale locale, int expFormat, int digitsLeftOfPoint, int zeroesLeftOfPoint,
            int digitsRightOfPoint, int zeroesRightOfPoint, int digitsRightOfExp, int zeroesRightOfExp,
            boolean useDecimal, boolean useThouSep, String formatString) {
        super(locale.locale());
        this.locale = locale;
        this.expChar = switch (expFormat) {
        case FORMAT_E_MINUS_UPPER, FORMAT_E_PLUS_UPPER -> 'E';
        case FORMAT_E_MINUS_LOWER, FORMAT_E_PLUS_LOWER -> 'e';
        default -> 0;
        };
        this.expSign = switch (expFormat) {
        case FORMAT_E_PLUS_UPPER, FORMAT_E_PLUS_LOWER -> true;
        default -> false;
        };
        this.digitsLeftOfPoint = digitsLeftOfPoint;
        this.zeroesLeftOfPoint = zeroesLeftOfPoint;
        this.digitsRightOfPoint = digitsRightOfPoint;
        this.zeroesRightOfPoint = zeroesRightOfPoint;
        this.digitsRightOfExp = digitsRightOfExp;
        this.zeroesRightOfExp = zeroesRightOfExp;
        this.useDecimal = useDecimal;
        this.useThouSep = useThouSep;
        this.decimalShift = 0; // set later

        // Check if we're dealing with a format macro token rather than
        // an actual format string.
        formatString = MacroToken.expand(locale, formatString);

        if (thousandSeparatorTokenMap.containsKey(formatString)) {
            cachedThousandSeparatorPositions = thousandSeparatorTokenMap.get(formatString);
        } else {
            // To provide backwards compatibility, we apply the old
            // formatting rules if there are less than 2 thousand
            // separators in the format string.
            String formatStringBuffer = formatString;

            // If the format includes a negative format part, we strip it.
            final int semiPos = formatStringBuffer.indexOf(getFormatToken(FORMAT_SEMI));
            if (semiPos > 0) {
                formatStringBuffer = formatStringBuffer.substring(0, semiPos);
            }

            final int nbThousandSeparators = countOccurrences(formatStringBuffer,
                    getFormatToken(FORMAT_THOUSEP).charAt(0));
            cachedThousandSeparatorPositions = new ArrayList<>();
            if (nbThousandSeparators > 1) {
                // Extract the whole part of the format string
                final int decimalPos = formatStringBuffer.indexOf(getFormatToken(FORMAT_DECIMAL));
                final int endIndex = decimalPos == -1 ? formatStringBuffer.length() : decimalPos;
                final String wholeFormat = formatStringBuffer.substring(0, endIndex);

                // Tokenize it so we can analyze it's structure
                final StringTokenizer st = new StringTokenizer(wholeFormat,
                        String.valueOf(getFormatToken(FORMAT_THOUSEP)));

                // We ignore the first token.
                // ie: #,###,###
                st.nextToken();

                // Now we build a list of the token lengths in
                // reverse order. The last one in the reversed
                // list will be re-applied if the number is
                // longer than the format string.
                while (st.hasMoreTokens()) {
                    cachedThousandSeparatorPositions.add(st.nextToken().length());
                }
            } else if (nbThousandSeparators == 1) {
                // Use old style formatting.
                cachedThousandSeparatorPositions.add(3);
            }
            thousandSeparatorTokenMap.put(formatString, cachedThousandSeparatorPositions);
        }
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.NUMERIC;
    }

    private ArrayList<Integer> getThousandSeparatorPositions() {
        // Defensive copy
        return new ArrayList<>(cachedThousandSeparatorPositions);
    }

    private int countOccurrences(final String s, final char c) {
        return (int) s.chars().filter(ch -> ch == c).count();
    }

    @Override
    public void format(double n, StringBuilder sb) {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(n);
        shift(fd, decimalShift);
        final int formatDigitsRightOfPoint = zeroesRightOfPoint + digitsRightOfPoint;
        if (n == 0.0 || (n < 0 && !shows(fd, formatDigitsRightOfPoint))) {
            // Underflow of negative number. Make it zero, so there is no
            // '-' sign.
            fd = new DaanseFloatingDecimal(0);
        }
        formatFd0(fd, sb, zeroesLeftOfPoint, locale.decimalPlaceholder(), zeroesRightOfPoint, formatDigitsRightOfPoint,
                expChar, expSign, zeroesRightOfExp, useThouSep ? locale.thousandSeparator() : '\0', useDecimal,
                getThousandSeparatorPositions());
    }

    @Override
    public boolean isApplicableTo(double n) {
        if (n >= 0) {
            return true;
        }
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(n);
        shift(fd, decimalShift);
        final int formatDigitsRightOfPoint = zeroesRightOfPoint + digitsRightOfPoint;
        return shows(fd, formatDigitsRightOfPoint);
    }

    private static boolean shows(DaanseFloatingDecimal fd, int formatDigitsRightOfPoint) {
        final int i0 = -fd.decExponent - formatDigitsRightOfPoint;
        if (i0 < 0) {
            return true;
        }
        if (i0 > 0) {
            return false;
        }
        return fd.digits[0] >= '5';
    }

    @Override
    public void format(long n, StringBuilder sb) {
        DaanseFloatingDecimal fd = new DaanseFloatingDecimal(n);
        shift(fd, decimalShift);
        formatFd0(fd, sb, zeroesLeftOfPoint, locale.decimalPlaceholder(), zeroesRightOfPoint,
                zeroesRightOfPoint + digitsRightOfPoint, expChar, expSign, zeroesRightOfExp,
                useThouSep ? locale.thousandSeparator() : '\0', useDecimal, getThousandSeparatorPositions());
    }

    static void shift(DaanseFloatingDecimal fd, int i) {
        if (fd.isExceptional || fd.nDigits == 1 && fd.digits[0] == '0') {
            // don't multiply zero
        } else {
            fd.decExponent += i;
        }
    }

    /** Formats a floating decimal to a given buffer. */
    static void formatFd0(DaanseFloatingDecimal fd, StringBuilder sb, int minDigitsLeftOfDecimal, char decimalChar, // '.'
                                                                                                                    // or
                                                                                                                    // ','
            int minDigitsRightOfDecimal, int maxDigitsRightOfDecimal, char expChar, // 'E' or 'e'
            boolean expSign, // whether to print '+' if exp is positive
            int minExpDigits, // minimum digits in exponent
            char thousandChar, // ',' or '.', or 0
            boolean useDecimal, List<Integer> thousandSeparatorPositions) {
        // char result[] = new char[nDigits + 10]; // crashes for 1.000.000,00
        // the result length does *not* depend from nDigits
        // it is : decExponent
        // +maxDigitsRightOfDecimal
        // + 10 (for decimal point and sign or -Infinity)
        // +decExponent/3 (for the thousand separators)
        // crashes e.g. for 1.1 and format '0000000000000'
        int resultLen = 10 + Math.max(Math.abs(fd.decExponent), minDigitsLeftOfDecimal) * 4 / 3
                + maxDigitsRightOfDecimal;
        char[] result = new char[resultLen];
        int i = formatFd1(fd, result, 0, minDigitsLeftOfDecimal, decimalChar, minDigitsRightOfDecimal,
                maxDigitsRightOfDecimal, expChar, expSign, minExpDigits, thousandChar, useDecimal,
                thousandSeparatorPositions);
        sb.append(result, 0, i);
    }

    /** Formats a floating decimal to a given char array. */
    private static int formatFd1(DaanseFloatingDecimal fd, char[] result, int i, int minDigitsLeftOfDecimal,
            char decimalChar, // '.' or ','
            int minDigitsRightOfDecimal, int maxDigitsRightOfDecimal, char expChar, // 'E' or 'e'
            boolean expSign, // whether to print '+' if exp is positive
            int minExpDigits, // minimum digits in exponent
            char thousandChar, // ',' or '.' or 0
            boolean useDecimal, List<Integer> thousandSeparatorPositions) {
        if (expChar != 0) {
            // Print the digits left of the 'E'.
            int oldExp = fd.decExponent;
            fd.decExponent = Math.min(minDigitsLeftOfDecimal, fd.nDigits);
            boolean oldIsNegative = fd.isNegative;
            fd.isNegative = false;
            i = formatFd2(fd, result, i, minDigitsLeftOfDecimal, decimalChar, minDigitsRightOfDecimal,
                    maxDigitsRightOfDecimal, '\0', useDecimal, thousandSeparatorPositions);
            fd.decExponent = oldExp;
            fd.isNegative = oldIsNegative;

            result[i++] = expChar;
            // Print the digits right of the 'E'.
            return fd.formatExponent(result, i, expSign, minExpDigits);
        } else {
            return formatFd2(fd, result, i, minDigitsLeftOfDecimal, decimalChar, minDigitsRightOfDecimal,
                    maxDigitsRightOfDecimal, thousandChar, useDecimal, thousandSeparatorPositions);
        }
    }

    static int formatFd2(DaanseFloatingDecimal fd, char[] result, int i, int minDigitsLeftOfDecimal, char decimalChar, // '.'
                                                                                                                       // or
                                                                                                                       // ','
            int minDigitsRightOfDecimal, int maxDigitsRightOfDecimal, char thousandChar, // ',' or '.' or 0
            boolean useDecimal, List<Integer> thousandSeparatorPositions) {
        if (fd.isNegative) {
            result[i++] = '-';
        }
        if (fd.isExceptional) {
            System.arraycopy(fd.digits, 0, result, i, fd.nDigits);
            return i + fd.nDigits;
        }
        // Build a new array of digits, padded with 0s at either end. For
        // example, here is the array we would build for 1234.56.
        //
        // | 0 0 1 2 3 . 4 5 6 0 0 |
        // | |- nDigits=6 -----------------------| |
        // | |- decExponent=3 -| |
        // |- minDigitsLeftOfDecimal=5 --| |
        // | |- minDigitsRightOfDecimal=5 --|
        // |- wholeDigits=5 -------------|- fractionDigits=5 -----------|
        // |- totalDigits=10 -------------------------------------------|
        // | |- maxDigitsRightOfDecimal=5 --|
        int wholeDigits = Math.max(fd.decExponent, minDigitsLeftOfDecimal);
        int fractionDigits = Math.max(fd.nDigits - fd.decExponent, minDigitsRightOfDecimal);
        int totalDigits = wholeDigits + fractionDigits;
        char[] digits2 = new char[totalDigits];
        Arrays.fill(digits2, '0');
        for (int j = 0; j < fd.nDigits; j++) {
            digits2[wholeDigits - fd.decExponent + j] = fd.digits[j];
        }

        // Now round. Suppose that we want to round 1234.56 to 1 decimal
        // place (that is, maxDigitsRightOfDecimal = 1). Then lastDigit
        // initially points to '5'. We find out that we need to round only
        // when we see that the next digit ('6') is non-zero.
        //
        // | 0 0 1 2 3 . 4 5 6 0 0 |
        // | | ^ | |
        // | maxDigitsRightOfDecimal=1 |
        int lastDigit = wholeDigits + maxDigitsRightOfDecimal;
        if (lastDigit < totalDigits) {
            // We need to truncate -- also round if the trailing digits are
            // 5000... or greater.
            int m = totalDigits;
            if (digits2.length >= lastDigit && lastDigit != 0) {
                while (digits2[lastDigit - 1] < '0' || digits2[lastDigit - 1] > '9') {
                    // BACKLOG-15504
                    lastDigit--;
                }
            }
            while (true) {
                m--;
                if (m < 0) {
                    // The entire number was 9s. Re-allocate, so we can
                    // prepend a '1'.
                    wholeDigits++;
                    totalDigits++;
                    lastDigit++;
                    char[] old = digits2;
                    digits2 = new char[totalDigits];
                    digits2[0] = '1';
                    System.arraycopy(old, 0, digits2, 1, old.length);
                    break;
                } else if (m == lastDigit) {
                    char d = digits2[m];
                    digits2[m] = '0';
                    if (d < '5' || d == ':') {
                        break; // no need to round
                    }
                } else if (m > lastDigit) {
                    digits2[m] = '0';
                } else if (digits2[m] == '9') {
                    digits2[m] = '0';
                    // do not break - we have to carry
                } else {
                    digits2[m]++;
                    break; // nothing to carry
                }
            }
        }

        // Find the first non-zero digit and the last non-zero digit.
        int firstNonZero = wholeDigits;
        int firstTrailingZero = 0;
        for (int j = 0; j < totalDigits; j++) {
            if (digits2[j] != '0') {
                if (j < firstNonZero) {
                    firstNonZero = j;
                }
                firstTrailingZero = j + 1;
            }
        }

        int firstDigitToPrint = firstNonZero;
        if (firstDigitToPrint > wholeDigits - minDigitsLeftOfDecimal) {
            firstDigitToPrint = wholeDigits - minDigitsLeftOfDecimal;
        }
        int lastDigitToPrint = firstTrailingZero;
        if (lastDigitToPrint > wholeDigits + maxDigitsRightOfDecimal) {
            lastDigitToPrint = wholeDigits + maxDigitsRightOfDecimal;
        }
        if (lastDigitToPrint < wholeDigits + minDigitsRightOfDecimal) {
            lastDigitToPrint = wholeDigits + minDigitsRightOfDecimal;
        }

        if (thousandChar != '\0' && !thousandSeparatorPositions.isEmpty()) {
            // Now print the number. That will happen backwards, so we
            // store it temporarily and then invert.
            ArrayList<Character> formattedWholeDigits = new ArrayList<>();
            // We need to keep track of how many digits we printed in the
            // current token.
            int nbInserted = 0;
            for (int j = wholeDigits - 1; j >= firstDigitToPrint; j--) {
                // Check if we need to insert another thousand separator
                if (nbInserted % thousandSeparatorPositions.getLast() == 0 && nbInserted > 0) {
                    formattedWholeDigits.add(thousandChar);
                    nbInserted = 0;
                    // The last format token is kept because we re-apply it
                    // until the end of the digits.
                    if (thousandSeparatorPositions.size() > 1) {
                        thousandSeparatorPositions.removeLast();
                    }
                }
                // Insert the next digit.
                formattedWholeDigits.add(digits2[j]);
                nbInserted++;
            }
            // We're done. Invert the print out and add it to
            // the result array.
            while (!formattedWholeDigits.isEmpty()) {
                result[i++] = formattedWholeDigits.removeLast();
            }
        } else {
            // There are no thousand separators. Just put the
            // digits in the results array.
            for (int j = firstDigitToPrint; j < wholeDigits; j++) {
                result[i++] = digits2[j];
            }
        }

        if (wholeDigits < lastDigitToPrint || (useDecimal && wholeDigits == lastDigitToPrint)) {
            result[i++] = decimalChar;
        }
        for (int j = wholeDigits; j < lastDigitToPrint; j++) {
            result[i++] = digits2[j];
        }
        return i;
    }
}
