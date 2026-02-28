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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.olap.util.format.FormatToken;

/**
 * Constants used by the format classes. Extracted from
 * {@link org.eclipse.daanse.olap.util.format.Format}.
 */
public class FormatConstants {

    private FormatConstants() {
        // utility class
    }

    /** Types of Format. */
    public static final int GENERAL = 0;
    public static final int DATE = 1;
    public static final int NUMERIC = 2;
    public static final int STRING = 4;

    /**
     * A Format is flagged SPECIAL if it needs special processing during parsing.
     */
    public static final int SPECIAL = 8;

    /** Values for {@link BasicFormat#code}. */
    public static final int FORMAT_NULL = 0;
    public static final int FORMAT_C = 3;
    public static final int FORMAT_D = 4;
    public static final int FORMAT_DD = 5;
    public static final int FORMAT_DDD = 6;
    public static final int FORMAT_DDDD = 7;
    public static final int FORMAT_DDDDD = 8;
    public static final int FORMAT_DDDDDD = 9;
    public static final int FORMAT_W = 10;
    public static final int FORMAT_WW = 11;
    public static final int FORMAT_M = 12;
    public static final int FORMAT_MM = 13;
    public static final int FORMAT_MMM_UPPER = 14;
    public static final int FORMAT_MMMM_UPPER = 15;
    public static final int FORMAT_Q = 16;
    public static final int FORMAT_Y = 17;
    public static final int FORMAT_YY = 18;
    public static final int FORMAT_YYYY = 19;
    public static final int FORMAT_H = 20;
    public static final int FORMAT_HH = 21;
    public static final int FORMAT_N = 22;
    public static final int FORMAT_NN = 23;
    public static final int FORMAT_S = 24;
    public static final int FORMAT_SS = 25;
    public static final int FORMAT_TTTTT = 26;
    public static final int FORMAT_UPPER_AM_SOLIDUS_PM = 27;
    public static final int FORMAT_LOWER_AM_SOLIDUS_PM = 28;
    public static final int FORMAT_UPPER_A_SOLIDUS_P = 29;
    public static final int FORMAT_LOWER_A_SOLIDUS_P = 30;
    public static final int FORMAT_AMPM = 31;
    public static final int FORMAT_0 = 32;
    public static final int FORMAT_POUND = 33;
    public static final int FORMAT_DECIMAL = 34;
    public static final int FORMAT_PERCENT = 35;
    public static final int FORMAT_THOUSEP = 36;
    public static final int FORMAT_TIMESEP = 37;
    public static final int FORMAT_DATESEP = 38;
    public static final int FORMAT_E_MINUS_UPPER = 39;
    public static final int FORMAT_E_PLUS_UPPER = 40;
    public static final int FORMAT_E_MINUS_LOWER = 41;
    public static final int FORMAT_E_PLUS_LOWER = 42;
    public static final int FORMAT_LITERAL = 43;
    public static final int FORMAT_BACKSLASH = 44;
    public static final int FORMAT_QUOTE = 45;
    public static final int FORMAT_CHARACTER_OR_SPACE = 46;
    public static final int FORMAT_CHARACTER_OR_NOTHING = 47;
    public static final int FORMAT_LOWER = 48;
    public static final int FORMAT_UPPER = 49;
    public static final int FORMAT_FILL_FROM_LEFT = 50;
    public static final int FORMAT_SEMI = 51;
    public static final int FORMAT_GENERAL_NUMBER = 52;
    public static final int FORMAT_GENERAL_DATE = 53;
    public static final int FORMAT_INTL_CURRENCY = 54;
    public static final int FORMAT_MMM_LOWER = 55;
    public static final int FORMAT_MMMM_LOWER = 56;
    public static final int FORMAT_USD = 57;
    public static final int FORMAT_MM_UPPER = 58;
    public static final int FORMAT_MMMMM_LOWER = 59;
    public static final int FORMAT_MMMMM_UPPER = 60;
    public static final int FORMAT_HH_UPPER = 61;
    public static final int FORMAT_M_UPPER = 62;

    public static final char INTL_CURRENCY_SYMBOL = '\u00a4';

    static final String DISPLAY_THE_MONTH_AS_A_FULL_MONTH_NAME_JANUARY_DECEMBER = "Display the month as a full month name (January - December).";
    static final String SCIENTIFIC_FORMAT = "Scientific format";

    private static final Map<Integer, String> tempMap = new HashMap<>();

    public static final Map<Integer, String> formatTokenToFormatString;

    private static FormatToken nfe(int code, int flags, String token, String purpose, String description) {
        tempMap.put(code, token);
        return new FormatToken(code, flags, token);
    }

    public static final FormatToken[] tokens = {
            nfe(FORMAT_NULL, NUMERIC, null, "No formatting", "Display the number with no formatting."),
            nfe(FORMAT_C, DATE, "C", null,
                    "Display the date as ddddd and display the time as t t t t t, in "
                            + "that order. Display only date information if there is no "
                            + "fractional part to the date serial number; display only time "
                            + "information if there is no integer portion."),
            nfe(FORMAT_D, DATE, "d", null, "Display the day as a number without a leading zero (1 - 31)."),
            nfe(FORMAT_DD, DATE, "dd", null, "Display the day as a number with a leading zero (01 - 31)."),
            nfe(FORMAT_DDD, DATE, "Ddd", null, "Display the day as an abbreviation (Sun - Sat)."),
            nfe(FORMAT_DDDD, DATE, "dddd", null, "Display the day as a full name (Sunday - Saturday)."),
            nfe(FORMAT_DDDDD, DATE, "ddddd", null,
                    "Display the date as a complete date (including day, month, and "
                            + "year), formatted according to your system's short date format "
                            + "setting. The default short date format is m/d/yy."),
            nfe(FORMAT_DDDDDD, DATE, "dddddd", null,
                    "Display a date serial number as a complete date (including day, "
                            + "month, and year) formatted according to the long date setting "
                            + "recognized by your system. The default long date format is mmmm " + "dd, yyyy."),
            nfe(FORMAT_W, DATE, "w", null,
                    "Display the day of the week as a number (1 for Sunday through 7 " + "for Saturday)."),
            nfe(FORMAT_WW, DATE, "ww", null, "Display the week of the year as a number (1 - 53)."),
            nfe(FORMAT_M, DATE | SPECIAL, "m", null,
                    "Display the month as a number without a leading zero (1 - 12). If "
                            + "m immediately follows h or hh, the minute rather than the month " + "is displayed."),
            nfe(FORMAT_M_UPPER, DATE, "M", null, "Display the month as a number without a leading zero (1 - 12)."),
            nfe(FORMAT_MM, DATE | SPECIAL, "mm", null,
                    "Display the month as a number with a leading zero (01 - 12). If m "
                            + "immediately follows h or hh, the minute rather than the month " + "is displayed."),
            nfe(FORMAT_MM_UPPER, DATE, "MM", null, "Display the month as a number with a leading zero (01 - 12)."),
            nfe(FORMAT_MMM_LOWER, DATE, "mmm", null, "Display the month as an abbreviation (Jan - Dec)."),
            nfe(FORMAT_MMMM_LOWER, DATE, "mmmm", null, DISPLAY_THE_MONTH_AS_A_FULL_MONTH_NAME_JANUARY_DECEMBER),
            nfe(FORMAT_MMM_UPPER, DATE, "MMM", null, "Display the month as an abbreviation (Jan - Dec)."),
            nfe(FORMAT_MMMM_UPPER, DATE, "MMMM", null, DISPLAY_THE_MONTH_AS_A_FULL_MONTH_NAME_JANUARY_DECEMBER),
            nfe(FORMAT_Q, DATE, "q", null, "Display the quarter of the year as a number (1 - 4)."),
            nfe(FORMAT_Y, DATE, "y", null, "Display the day of the year as a number (1 - 366)."),
            nfe(FORMAT_YY, DATE, "yy", null, "Display the year as a 2-digit number (00 - 99)."),
            nfe(FORMAT_YYYY, DATE, "yyyy", null, "Display the year as a 4-digit number (100 - 9999)."),
            nfe(FORMAT_H, DATE, "h", null, "Display the hour as a number without leading zeros (0 - 23)."),
            nfe(FORMAT_HH, DATE, "hh", null, "Display the hour as a number with leading zeros (00 - 23)."),
            nfe(FORMAT_N, DATE, "n", null, "Display the minute as a number without leading zeros (0 - 59)."),
            nfe(FORMAT_NN, DATE, "nn", null, "Display the minute as a number with leading zeros (00 - 59)."),
            nfe(FORMAT_S, DATE, "s", null, "Display the second as a number without leading zeros (0 - 59)."),
            nfe(FORMAT_SS, DATE, "ss", null, "Display the second as a number with leading zeros (00 - 59)."),
            nfe(FORMAT_TTTTT, DATE, "ttttt", null,
                    "Display a time as a complete time (including hour, minute, and "
                            + "second), formatted using the time separator defined by the time "
                            + "format recognized by your system. A leading zero is displayed "
                            + "if the leading zero option is selected and the time is before "
                            + "10:00 A.M. or P.M. The default time format is h:mm:ss."),
            nfe(FORMAT_UPPER_AM_SOLIDUS_PM, DATE, "AM/PM", null,
                    "Use the 12-hour clock and display an uppercase AM with any hour "
                            + "before noon; display an uppercase PM with any hour between noon and 11:59 P.M."),
            nfe(FORMAT_LOWER_AM_SOLIDUS_PM, DATE, "am/pm", null,
                    "Use the 12-hour clock and display a lowercase AM with any hour "
                            + "before noon; display a lowercase PM with any hour between noon " + "and 11:59 P.M."),
            nfe(FORMAT_UPPER_A_SOLIDUS_P, DATE, "A/P", null,
                    "Use the 12-hour clock and display an uppercase A with any hour "
                            + "before noon; display an uppercase P with any hour between noon " + "and 11:59 P.M."),
            nfe(FORMAT_LOWER_A_SOLIDUS_P, DATE, "a/p", null,
                    "Use the 12-hour clock and display a lowercase A with any hour "
                            + "before noon; display a lowercase P with any hour between noon " + "and 11:59 P.M."),
            nfe(FORMAT_AMPM, DATE, "AMPM", null,
                    "Use the 12-hour clock and display the AM string literal as "
                            + "defined by your system with any hour before noon; display the "
                            + "PM string literal as defined by your system with any hour "
                            + "between noon and 11:59 P.M. AMPM can be either uppercase or "
                            + "lowercase, but the case of the string displayed matches the "
                            + "string as defined by your system settings. The default format " + "is AM/PM."),
            nfe(FORMAT_0, NUMERIC | SPECIAL, "0", "Digit placeholder",
                    "Display a digit or a zero. If the expression has a digit in the "
                            + "position where the 0 appears in the format string, display it; "
                            + "otherwise, display a zero in that position. If the number has "
                            + "fewer digits than there are zeros (on either side of the "
                            + "decimal) in the format expression, display leading or trailing "
                            + "zeros. If the number has more digits to the right of the "
                            + "decimal separator than there are zeros to the right of the "
                            + "decimal separator in the format expression, round the number to "
                            + "as many decimal places as there are zeros. If the number has "
                            + "more digits to the left of the decimal separator than there are "
                            + "zeros to the left of the decimal separator in the format "
                            + "expression, display the extra digits without modification."),
            nfe(FORMAT_POUND, NUMERIC | SPECIAL, "#", "Digit placeholder",
                    "Display a digit or nothing. If the expression has a digit in the "
                            + "position where the # appears in the format string, display it; "
                            + "otherwise, display nothing in that position.  This symbol works "
                            + "like the 0 digit placeholder, except that leading and trailing "
                            + "zeros aren't displayed if the number has the same or fewer "
                            + "digits than there are # characters on either side of the "
                            + "decimal separator in the format expression."),
            nfe(FORMAT_DECIMAL, NUMERIC | SPECIAL, ".", "Decimal placeholder",
                    "In some locales, a comma is used as the decimal separator. The "
                            + "decimal placeholder determines how many digits are displayed to "
                            + "the left and right of the decimal separator. If the format "
                            + "expression contains only number signs to the left of this "
                            + "symbol, numbers smaller than 1 begin with a decimal separator. "
                            + "If you always want a leading zero displayed with fractional "
                            + "numbers, use 0 as the first digit placeholder to the left of "
                            + "the decimal separator instead. The actual character used as a "
                            + "decimal placeholder in the formatted output depends on the "
                            + "Number Format recognized by your system."),
            nfe(FORMAT_PERCENT, NUMERIC, "%", "Percent placeholder",
                    "The expression is multiplied by 100. The percent character (%) is "
                            + "inserted in the position where it appears in the format " + "string."),
            nfe(FORMAT_THOUSEP, NUMERIC | SPECIAL, ",", "Thousand separator",
                    "In some locales, a period is used as a thousand separator. The "
                            + "thousand separator separates thousands from hundreds within a "
                            + "number that has four or more places to the left of the decimal "
                            + "separator. Standard use of the thousand separator is specified "
                            + "if the format contains a thousand separator surrounded by digit "
                            + "placeholders (0 or #). Two adjacent thousand separators or a "
                            + "thousand separator immediately to the left of the decimal "
                            + "separator (whether or not a decimal is specified) means \"scale "
                            + "the number by dividing it by 1000, rounding as needed.\"  You "
                            + "can scale large numbers using this technique. For example, you "
                            + "can use the format string \"##0,,\" to represent 100 million as "
                            + "100. Numbers smaller than 1 million are displayed as 0. Two "
                            + "adjacent thousand separators in any position other than "
                            + "immediately to the left of the decimal separator are treated "
                            + "simply as specifying the use of a thousand separator. The "
                            + "actual character used as the thousand separator in the "
                            + "formatted output depends on the Number Format recognized by " + "your system."),
            nfe(FORMAT_TIMESEP, DATE | SPECIAL, ":", "Time separator",
                    "In some locales, other characters may be used to represent the "
                            + "time separator. The time separator separates hours, minutes, "
                            + "and seconds when time values are formatted. The actual "
                            + "character used as the time separator in formatted output is "
                            + "determined by your system settings."),
            nfe(FORMAT_DATESEP, DATE | SPECIAL, "/", "Date separator",
                    "In some locales, other characters may be used to represent the "
                            + "date separator. The date separator separates the day, month, "
                            + "and year when date values are formatted. The actual character "
                            + "used as the date separator in formatted output is determined by "
                            + "your system settings."),
            nfe(FORMAT_E_MINUS_UPPER, NUMERIC | SPECIAL, "E-", SCIENTIFIC_FORMAT,
                    "If the format expression contains at least one digit placeholder "
                            + "(0 or #) to the right of E-, E+, e-, or e+, the number is "
                            + "displayed in scientific format and E or e is inserted between "
                            + "the number and its exponent. The number of digit placeholders "
                            + "to the right determines the number of digits in the exponent. "
                            + "Use E- or e- to place a minus sign next to negative exponents. "
                            + "Use E+ or e+ to place a minus sign next to negative exponents "
                            + "and a plus sign next to positive exponents."),
            nfe(FORMAT_E_PLUS_UPPER, NUMERIC | SPECIAL, "E+", SCIENTIFIC_FORMAT, "See E-."),
            nfe(FORMAT_E_MINUS_LOWER, NUMERIC | SPECIAL, "e-", SCIENTIFIC_FORMAT, "See E-."),
            nfe(FORMAT_E_PLUS_LOWER, NUMERIC | SPECIAL, "e+", SCIENTIFIC_FORMAT, "See E-."),
            nfe(FORMAT_LITERAL, GENERAL, "-", "Display a literal character",
                    "To display a character other than one of those listed, precede it "
                            + "with a backslash (\\) or enclose it in double quotation marks " + "(\" \")."),
            nfe(FORMAT_LITERAL, GENERAL, "+", "Display a literal character", "See -."),
            nfe(FORMAT_LITERAL, GENERAL, "$", "Display a literal character", "See -."),
            nfe(FORMAT_LITERAL, GENERAL, "(", "Display a literal character", "See -."),
            nfe(FORMAT_LITERAL, GENERAL, ")", "Display a literal character", "See -."),
            nfe(FORMAT_LITERAL, GENERAL, " ", "Display a literal character", "See -."),
            nfe(FORMAT_BACKSLASH, GENERAL | SPECIAL, "\\", "Display the next character in the format string",
                    "Many characters in the format expression have a special meaning "
                            + "and can't be displayed as literal characters unless they are "
                            + "preceded by a backslash. The backslash itself isn't displayed. "
                            + "Using a backslash is the same as enclosing the next character "
                            + "in double quotation marks. To display a backslash, use two "
                            + "backslashes (\\).  Examples of characters that can't be "
                            + "displayed as literal characters are the date- and "
                            + "time-formatting characters (a, c, d, h, m, n, p, q, s, t, w, y, "
                            + "and /:), the numeric-formatting characters (#, 0, %, E, e, "
                            + "comma, and period), and the string-formatting characters (@, &, " + "<, >, and !)."),
            nfe(FORMAT_QUOTE, GENERAL | SPECIAL, "\"", "Display the string inside the double quotation marks",
                    "To include a string in format from within code, you must use "
                            + "Chr(34) to enclose the text (34 is the character code for a "
                            + "double quotation mark)."),
            nfe(FORMAT_CHARACTER_OR_SPACE, STRING, "@", "Character placeholder",
                    "Display a character or a space. If the string has a character in "
                            + "the position where the @ appears in the format string, display "
                            + "it; otherwise, display a space in that position. Placeholders "
                            + "are filled from right to left unless there is an ! character in "
                            + "the format string. See below."),
            nfe(FORMAT_CHARACTER_OR_NOTHING, STRING, "&", "Character placeholder",
                    "Display a character or nothing. If the string has a character in "
                            + "the position where the & appears, display it; otherwise, "
                            + "display nothing. Placeholders are filled from right to left "
                            + "unless there is an ! character in the format string. See " + "below."),
            nfe(FORMAT_LOWER, STRING | SPECIAL, "<", "Force lowercase", "Display all characters in lowercase format."),
            nfe(FORMAT_UPPER, STRING | SPECIAL, ">", "Force uppercase", "Display all characters in uppercase format."),
            nfe(FORMAT_FILL_FROM_LEFT, STRING | SPECIAL, "!", "Force left to right fill of placeholders",
                    "The default is to fill from right to left."),
            nfe(FORMAT_SEMI, GENERAL | SPECIAL, ";", "Separates format strings for different kinds of values",
                    "If there is one section, the format expression applies to all "
                            + "values. If there are two sections, the first section applies "
                            + "to positive values and zeros, the second to negative values. If "
                            + "there are three sections, the first section applies to positive "
                            + "values, the second to negative values, and the third to zeros. "
                            + "If there are four sections, the first section applies to "
                            + "positive values, the second to negative values, the third to "
                            + "zeros, and the fourth to Null values."),
            nfe(FORMAT_INTL_CURRENCY, NUMERIC | SPECIAL, INTL_CURRENCY_SYMBOL + "", null,
                    "Display the locale's currency symbol."),
            nfe(FORMAT_USD, GENERAL, "USD", null, "Display USD (U.S. Dollars)."),
            nfe(FORMAT_GENERAL_NUMBER, NUMERIC | SPECIAL, "General Number", null, "Shows numbers as entered."),
            nfe(FORMAT_GENERAL_DATE, DATE | SPECIAL, "General Date", null,
                    "Shows date and time if expression contains both. If expression is "
                            + "only a date or a time, the missing information is not " + "displayed."),
            nfe(FORMAT_MMMMM_LOWER, DATE, "mmmmm", null, DISPLAY_THE_MONTH_AS_A_FULL_MONTH_NAME_JANUARY_DECEMBER),
            nfe(FORMAT_MMMMM_UPPER, DATE, "MMMMM", null, DISPLAY_THE_MONTH_AS_A_FULL_MONTH_NAME_JANUARY_DECEMBER),
            nfe(FORMAT_HH_UPPER, DATE, "HH", null, "Display the hour as a number with leading zeros (00 - 23)."), };

    static {
        formatTokenToFormatString = Collections.unmodifiableMap(tempMap);
    }

    public static List<FormatToken> getTokenList() {
        return List.of(tokens);
    }

    /**
     * Returns the format token as a string representation which corresponds to a
     * given token code.
     * 
     * @param code The code of the token to obtain.
     * @return The string representation of that token.
     */
    public static String getFormatToken(int code) {
        return formatTokenToFormatString.get(code);
    }
}
