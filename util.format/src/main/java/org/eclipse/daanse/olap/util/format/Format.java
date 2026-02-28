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

import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_0;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_AMPM;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_BACKSLASH;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_DATESEP;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_DECIMAL;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_E_MINUS_LOWER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_E_MINUS_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_E_PLUS_LOWER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_E_PLUS_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_FILL_FROM_LEFT;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_GENERAL_DATE;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_GENERAL_NUMBER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_H;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_HH;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_HH_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_INTL_CURRENCY;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_LOWER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_LOWER_AM_SOLIDUS_PM;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_LOWER_A_SOLIDUS_P;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_M;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_MM;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_N;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_NN;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_NULL;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_PERCENT;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_POUND;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_QUOTE;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_SEMI;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_THOUSEP;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_TIMESEP;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_UPPER_AM_SOLIDUS_PM;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_UPPER_A_SOLIDUS_P;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.INTL_CURRENCY_SYMBOL;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormatSymbols;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.daanse.olap.util.format.internal.AlternateFormat;
import org.eclipse.daanse.olap.util.format.internal.BasicFormat;
import org.eclipse.daanse.olap.util.format.internal.CompoundFormat;
import org.eclipse.daanse.olap.util.format.internal.FormatAlternates;
import org.eclipse.daanse.olap.util.format.internal.FormatConstants;
import org.eclipse.daanse.olap.util.format.internal.FormatType;
import org.eclipse.daanse.olap.util.format.internal.JavaFormat;
import org.eclipse.daanse.olap.util.format.internal.LiteralFormat;
import org.eclipse.daanse.olap.util.format.internal.LruCache;
import org.eclipse.daanse.olap.util.format.internal.MacroToken;
import org.eclipse.daanse.olap.util.format.internal.NumericFormat;
import org.eclipse.daanse.olap.util.format.internal.StringCase;
import org.eclipse.daanse.olap.util.format.internal.StringFormat;
import org.eclipse.daanse.olap.util.format.internal.VbDateFormat;

/**
 * Format formats numbers, strings and dates according to the same specification
 * as Visual Basic's format() function. This function is described in more
 * detail <a href="http://www.apostate.com/programming/vb-format.html">here</a>.
 * We have made the following enhancements to this specification:
 *
 * <ul>
 * <li>if the international currency symbol (&#x00a4;) occurs in a format
 * string, it is translated to the locale's currency symbol.</li>
 *
 * <li>the format string "Currency" is translated to the locale's currency
 * format string. Negative currency values appear in parentheses.</li>
 *
 * <li>the string "USD" (abbreviation for U.S. Dollars) may occur in a format
 * string.</li>
 * </ul>
 *
 * One format object can be used to format multiple values, thereby amortizing
 * the time required to parse the format string. Example:
 *
 * <pre>
 * double[] values;
 * Format format = new Format("##,##0.###;(##,##0.###);;Nil");
 * for (int i = 0; i &lt; values.length; i++) {
 *     System.out.println("Value #" + i + " is " + format.format(values[i]));
 * }
 * </pre>
 *
 * @since 0.0.1
 * @author jhyde
 */
public class Format {
    private String formatString;
    private BasicFormat formatValue;
    private FormatLocale locale;

    /**
     * Maximum number of entries in the format cache used by
     * {@link #get(String, java.util.Locale)}.
     */
    public static final int CACHE_LIMIT = 1000;

    /**
     * Thread-safe LRU cache mapping (formatString, locale) pairs to {@link Format}
     * objects. When the cache exceeds {@link #CACHE_LIMIT} entries, the least
     * recently used entry is evicted automatically.
     */
    private record CacheKey(String formatString, Locale locale) {
    }

    private static final LruCache<CacheKey, Format> cache = new LruCache<>(CACHE_LIMIT);

    /**
     * Maps strings representing locales (for example, "en_US_Boston", "en_US",
     * "en", or "" for the default) to a {@link FormatLocale}. Thread-safe
     * replacement for HashMap.
     */
    private static final ConcurrentHashMap<String, FormatLocale> mapLocaleToFormatLocale = new ConcurrentHashMap<>();

    /**
     * Locale for US English, also the default for English and for all locales.
     */
    static final FormatLocale locale_US = createLocale(Locale.US);

    // Values for variable numberState below.
    static final int NOT_IN_A_NUMBER = 0;
    static final int LEFT_OF_POINT = 1;
    static final int RIGHT_OF_POINT = 2;
    static final int RIGHT_OF_EXP = 3;

    /**
     * Constructs a Format in a specific locale.
     *
     * @param formatString the format string; see <a href=
     *                     "http://www.apostate.com/programming/vb-format.html">this
     *                     description</a> for more details
     * @param locale       The locale
     */
    public Format(String formatString, Locale locale) {
        this(formatString, getBestFormatLocale(locale));
    }

    /**
     * Constructs a Format in a specific locale.
     *
     * @param formatString the format string; see <a href=
     *                     "http://www.apostate.com/programming/vb-format.html">this
     *                     description</a> for more details
     * @param locale       The locale
     *
     * @see FormatLocale
     * @see #createLocale
     */
    public Format(String formatString, FormatLocale locale) {
        if (formatString == null) {
            formatString = "";
        }
        this.formatString = formatString;
        if (locale == null) {
            locale = locale_US;
        }
        this.locale = locale;

        List<BasicFormat> alternateFormatList = new ArrayList<>();
        FormatType[] formatType = { null };
        while (formatString.length() > 0) {
            formatString = parseFormatString(formatString, alternateFormatList, formatType);
        }

        // If the format string is empty, use a Java format.
        // Later entries in the formats list default to the first (e.g.
        // "#.00;;Nil"), but the first entry must be set.
        if (alternateFormatList.isEmpty() || alternateFormatList.getFirst() == null) {
            formatValue = new JavaFormat(locale.locale());
        } else if (alternateFormatList.size() == 1
                && (formatType[0] == FormatType.DATE || formatType[0] == FormatType.STRING)) {
            formatValue = alternateFormatList.getFirst();
        } else {
            formatValue = new AlternateFormat(FormatAlternates.of(alternateFormatList));
        }
    }

    /**
     * Constructs a Format in a specific locale, or retrieves one from the cache if
     * one already exists.
     *
     * If the number of entries in the cache exceeds {@link #CACHE_LIMIT}, the least
     * recently used entry is evicted.
     *
     * @param formatString the format string; see <a href=
     *                     "http://www.apostate.com/programming/vb-format.html">this
     *                     description</a> for more details
     * @param locale       the locale
     *
     * @return format for given format string in given locale
     */
    public static Format get(String formatString, Locale locale) {
        CacheKey key = new CacheKey(formatString, locale);
        return cache.getOrCompute(key, _ -> new Format(formatString, locale));
    }

    /**
     * Create a {@link FormatLocale} object characterized by the given properties.
     *
     * @param thousandSeparator  the character used to separate thousands in
     *                           numbers, or ',' by default. For example, 12345 is
     *                           '12,345 in English, '12.345 in French.
     * @param decimalPlaceholder the character placed between the integer and the
     *                           fractional part of decimal numbers, or '.' by
     *                           default. For example, 12.34 is '12.34' in English,
     *                           '12,34' in French.
     * @param dateSeparator      the character placed between the year, month and
     *                           day of a date such as '12/07/2001', or '/' by
     *                           default.
     * @param timeSeparator      the character placed between the hour, minute and
     *                           second value of a time such as '1:23:45 AM', or ':'
     *                           by default.
     * @param currencySymbol     the currency symbol, or '$' by default.
     * @param currencyFormat     the currency format string, or '$#,##0.00' by
     *                           default.
     * @param daysOfWeekShort    Short forms of the days of the week. The list is
     *                           1-based, because position {@link Calendar#SUNDAY}
     *                           (= 1) must hold Sunday, etc. The list must have 8
     *                           elements. For example {"", "Sun", "Mon", ...,
     *                           "Sat"}.
     * @param daysOfWeekLong     Long forms of the days of the week. The list is
     *                           1-based, because position {@link Calendar#SUNDAY}
     *                           must hold Sunday, etc. The list must have 8
     *                           elements. For example {"", "Sunday", ...,
     *                           "Saturday"}.
     * @param monthsShort        Short forms of the months of the year. The list is
     *                           0-based, because position {@link Calendar#JANUARY}
     *                           (= 0) holds January, etc. For example {"Jan", ...,
     *                           "Dec", ""}.
     * @param monthsLong         Long forms of the months of the year. The list is
     *                           0-based, because position {@link Calendar#JANUARY}
     *                           (= 0) holds January, etc. For example {"January",
     *                           ..., "December", ""}.
     * @param locale             if this is not null, register that the constructed
     *                           FormatLocale is the default for locale
     * @return the newly created FormatLocale
     */
    public static FormatLocale createLocale(char thousandSeparator, char decimalPlaceholder, String dateSeparator,
            String timeSeparator, String currencySymbol, String currencyFormat, List<String> daysOfWeekShort,
            List<String> daysOfWeekLong, List<String> monthsShort, List<String> monthsLong, Locale locale) {
        FormatLocale formatLocale = new FormatLocale(thousandSeparator, decimalPlaceholder, dateSeparator,
                timeSeparator, currencySymbol, currencyFormat, daysOfWeekShort, daysOfWeekLong, monthsShort, monthsLong,
                null, locale);
        if (locale != null) {
            registerFormatLocale(formatLocale, locale);
        }
        return formatLocale;
    }

    /**
     * Creates a {@link FormatLocale} from a Java {@link Locale}, deriving all
     * formatting properties from the locale's default settings.
     *
     * @param locale the Java locale
     * @return the newly created FormatLocale
     */
    public static FormatLocale createLocale(Locale locale) {
        final DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols(locale);
        final DateFormatSymbols dateSymbols = new DateFormatSymbols(locale);

        final Date date = Date.from(LocalDate.of(1969, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());

        final java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT,
                locale);
        String dateSeparator = null;
        if (dateFormat instanceof SimpleDateFormat sdf) {
            dateSeparator = extractSeparatorFromPattern(sdf.toPattern());
        }
        if (dateSeparator == null) {
            final String dateValue = dateFormat.format(date);
            dateSeparator = dateValue.substring(2, 3);
        }

        final java.text.DateFormat timeFormat = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,
                locale);
        String timeSeparator = null;
        if (timeFormat instanceof SimpleDateFormat sdf) {
            timeSeparator = extractSeparatorFromPattern(sdf.toPattern());
        }
        if (timeSeparator == null) {
            final String timeValue = timeFormat.format(date);
            timeSeparator = timeValue.substring(2, 3);
        }

        // Deduce the locale's currency format.
        // For example, US is "$#,###.00"; France is "#,###-00FF".
        final NumberFormat currencyFormatObj = NumberFormat.getCurrencyInstance(locale);
        final String currencyValue = currencyFormatObj.format(123456.78);
        String currencyLeft = currencyValue.substring(0, currencyValue.indexOf("1"));
        String currencyRight = currencyValue.substring(currencyValue.indexOf("8") + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currencyLeft.length(); i++) {
            sb.append("\\");
            sb.append(currencyLeft.charAt(i));
        }
        int minimumIntegerDigits = currencyFormatObj.getMinimumIntegerDigits();
        for (int i = Math.max(minimumIntegerDigits, 4) - 1; i >= 0; --i) {
            sb.append(i < minimumIntegerDigits ? '0' : '#');
            if (i % 3 == 0 && i > 0) {
                sb.append(',');
            }
        }
        if (currencyFormatObj.getMaximumFractionDigits() > 0) {
            sb.append('.');
            appendTimes(sb, '0', currencyFormatObj.getMinimumFractionDigits());
            appendTimes(sb, '#',
                    currencyFormatObj.getMaximumFractionDigits() - currencyFormatObj.getMinimumFractionDigits());
        }
        for (int i = 0; i < currencyRight.length(); i++) {
            sb.append("\\");
            sb.append(currencyRight.charAt(i));
        }
        String currencyFormatString = sb.toString();

        // If the locale passed is only a language, Java cannot
        // resolve the currency symbol and will instead return
        // u00a4 (The international currency symbol). For those cases,
        // we use the default system locale currency symbol.
        String currencySymbol = decimalSymbols.getCurrencySymbol();
        if (currencySymbol.equals(INTL_CURRENCY_SYMBOL + "")) {
            final DecimalFormatSymbols defaultDecimalSymbols = new DecimalFormatSymbols(Locale.getDefault());
            currencySymbol = defaultDecimalSymbols.getCurrencySymbol();
        }

        FormatLocale formatLocale = new FormatLocale(decimalSymbols.getGroupingSeparator(),
                decimalSymbols.getDecimalSeparator(), dateSeparator, timeSeparator, currencySymbol,
                currencyFormatString, List.of(dateSymbols.getShortWeekdays()), List.of(dateSymbols.getWeekdays()),
                List.of(dateSymbols.getShortMonths()), List.of(dateSymbols.getMonths()),
                List.of(dateSymbols.getAmPmStrings()), locale);
        if (locale != null) {
            registerFormatLocale(formatLocale, locale);
        }
        return formatLocale;
    }

    private static void appendTimes(StringBuilder sb, char c, int i) {
        while (i-- > 0) {
            sb.append(c);
        }
    }

    private static String extractSeparatorFromPattern(String pattern) {
        boolean inQuote = false;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '\'') {
                if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '\'') {
                    i++; // skip escaped quote
                } else {
                    inQuote = !inQuote;
                }
                continue;
            }
            if (inQuote) {
                continue;
            }
            if (!Character.isLetter(c)) {
                return String.valueOf(c);
            }
        }
        return null;
    }

    /**
     * Returns the {@link FormatLocale} which precisely matches {@link Locale}, if
     * any, or null if there is none.
     *
     * @param locale the Java locale to look up
     * @return the matching FormatLocale, or null
     */
    public static FormatLocale getFormatLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.US;
        }
        String key = locale.toString();
        return mapLocaleToFormatLocale.get(key);
    }

    /**
     * Returns the best {@link FormatLocale} for a given {@link Locale}. Never
     * returns null, even if locale is null.
     *
     * @param locale the Java locale
     * @return the best matching FormatLocale
     */
    public static FormatLocale getBestFormatLocale(Locale locale) {
        if (locale == null) {
            return locale_US;
        }
        String key = locale.toString();
        FormatLocale formatLocale = mapLocaleToFormatLocale.get(key);
        if (formatLocale != null) {
            return formatLocale;
        }
        // Compute outside of computeIfAbsent to avoid recursive update,
        // since createLocale() calls registerFormatLocale() which puts
        // into the same map.
        formatLocale = getFormatLocaleUsingFactory(locale);
        if (formatLocale == null) {
            formatLocale = locale_US;
        }
        mapLocaleToFormatLocale.putIfAbsent(key, formatLocale);
        return mapLocaleToFormatLocale.get(key);
    }

    private static FormatLocale getFormatLocaleUsingFactory(Locale locale) {
        FormatLocale formatLocale;
        // Lookup full locale, e.g. "en-US-Boston"
        if (!locale.getVariant().equals("")) {
            formatLocale = createLocale(locale);
            if (formatLocale != null) {
                return formatLocale;
            }
            locale = Locale.of(locale.getLanguage(), locale.getCountry());
        }
        // Lookup language and country, e.g. "en-US"
        if (!locale.getCountry().equals("")) {
            formatLocale = createLocale(locale);
            if (formatLocale != null) {
                return formatLocale;
            }
            locale = Locale.of(locale.getLanguage());
        }
        // Lookup language, e.g. "en"
        formatLocale = createLocale(locale);
        if (formatLocale != null) {
            return formatLocale;
        }
        return null;
    }

    /**
     * Registers a {@link FormatLocale} to a given {@link Locale}. Returns the
     * previous mapping.
     *
     * @param formatLocale the FormatLocale to register
     * @param locale       the Java locale to register it for
     * @return the previous FormatLocale for this locale, or null
     */
    public static FormatLocale registerFormatLocale(FormatLocale formatLocale, Locale locale) {
        String key = locale.toString(); // e.g. "en_us_Boston"
        return mapLocaleToFormatLocale.put(key, formatLocale);
    }

    /**
     * Returns the list of format tokens.
     *
     * @return an immutable list of FormatToken
     */
    public static List<FormatToken> getTokenList() {
        return FormatConstants.getTokenList();
    }

    /**
     * Returns the format token as a string representation which corresponds to a
     * given token code.
     * 
     * @param code The code of the token to obtain.
     * @return The string representation of that token.
     */
    public static String getFormatToken(int code) {
        return FormatConstants.getFormatToken(code);
    }

    /**
     * Formats a value according to the format string.
     *
     * @param o the value to format
     * @return the formatted string
     */
    public String format(Object o) {
        StringBuilder sb = new StringBuilder();
        format(o, sb);
        return sb.toString();
    }

    /**
     * Returns the format string.
     *
     * @return the format string
     */
    public String getFormatString() {
        return formatString;
    }

    /**
     * Dispatches the value to the appropriate format method based on its type.
     */
    private StringBuilder format(Object o, StringBuilder sb) {
        switch (o) {
        case null -> formatValue.formatNull(sb);
        case Double d -> formatValue.format(d.doubleValue(), sb);
        case Float f -> formatValue.format(f.doubleValue(), sb);
        case Integer i -> formatValue.format(i.longValue(), sb);
        case Long l -> formatValue.format(l.longValue(), sb);
        case Short s -> formatValue.format(s.longValue(), sb);
        case Byte b -> formatValue.format(b.longValue(), sb);
        case BigDecimal bd -> formatValue.format(bd, sb);
        case BigInteger bi -> formatValue.format(bi.longValue(), sb);
        case String str -> formatValue.format(str, sb);
        case LocalDateTime ldt -> formatValue.format(ldt, sb);
        case LocalDate ld -> formatValue.format(ld, sb);
        case Instant instant -> formatValue.format(instant, sb);
        // includes java.sql.Date, java.sql.Time and java.sql.Timestamp
        case java.util.Date d -> formatValue.format(d, sb);
        case Calendar c -> formatValue.format(c, sb);
        default -> sb.append(o);
        }
        return sb;
    }

    /**
     * Reads formatString up to the first semi-colon, or to the end if there are no
     * semi-colons. Adds a format to alternateFormatList, and returns the remains of
     * formatString.
     */
    private String parseFormatString(String formatString, List<BasicFormat> alternateFormatList,
            FormatType[] formatTypeOut) {
        // Cache the original value
        final String originalFormatString = formatString;

        // Where we are in a numeric format.
        int numberState = NOT_IN_A_NUMBER;
        StringBuilder ignored = new StringBuilder();
        String prevIgnored = null;
        boolean haveSeenNumber = false;
        int digitsLeftOfPoint = 0;
        int digitsRightOfPoint = 0;
        int digitsRightOfExp = 0;
        int zeroesLeftOfPoint = 0;
        int zeroesRightOfPoint = 0;
        int zeroesRightOfExp = 0;
        boolean useDecimal = false;
        boolean useThouSep = false;
        boolean fillFromRight = true;

        // Whether to print numbers in decimal or exponential format. Valid
        // values are FORMAT_NULL, FORMAT_E_PLUS_LOWER, FORMAT_E_MINUS_LOWER,
        // FORMAT_E_PLUS_UPPER, FORMAT_E_MINUS_UPPER.
        int expFormat = FORMAT_NULL;

        // Look for the format string in the table of named formats.
        formatString = MacroToken.expand(locale, formatString);

        // Add a semi-colon to the end of the string so the end of the string
        // looks like the end of an alternate.
        if (!formatString.endsWith(";")) {
            formatString = formatString + ";";
        }

        // Scan through the format string for format elements.
        List<BasicFormat> formatList = new ArrayList<>();
        List<Integer> thousands = new ArrayList<>();
        int decimalShift = 0;
        loop: while (formatString.length() > 0) {
            BasicFormat format = null;
            String newFormatString;
            final FormatToken token = findToken(formatString, formatTypeOut[0]);
            if (token != null) {
                String matched = token.tokenValue();
                newFormatString = formatString.substring(matched.length());
                if (token.isSpecial()) {
                    switch (token.code()) {
                    case FORMAT_SEMI:
                        break loop;

                    case FORMAT_POUND:
                        switch (numberState) {
                        case NOT_IN_A_NUMBER:
                            numberState = LEFT_OF_POINT;
                            // fall through
                        case LEFT_OF_POINT:
                            digitsLeftOfPoint++;
                            break;
                        case RIGHT_OF_POINT:
                            digitsRightOfPoint++;
                            break;
                        case RIGHT_OF_EXP:
                            digitsRightOfExp++;
                            break;
                        default:
                            throw new IllegalStateException("Unexpected format code");
                        }
                        break;

                    case FORMAT_0:
                        switch (numberState) {
                        case NOT_IN_A_NUMBER:
                            numberState = LEFT_OF_POINT;
                            // fall through
                        case LEFT_OF_POINT:
                            zeroesLeftOfPoint++;
                            break;
                        case RIGHT_OF_POINT:
                            zeroesRightOfPoint++;
                            break;
                        case RIGHT_OF_EXP:
                            zeroesRightOfExp++;
                            break;
                        default:
                            throw new IllegalStateException("Unexpected format code");
                        }
                        break;

                    case FORMAT_M:
                    case FORMAT_MM: {
                        // "m" and "mm" mean minute if immediately after
                        // "h" or "hh"; month otherwise.
                        boolean theyMeantMinute = false;
                        int j = formatList.size() - 1;
                        while (j >= 0) {
                            BasicFormat prevFormat = formatList.get(j);
                            if (prevFormat instanceof LiteralFormat) {
                                // ignore boilerplate
                                j--;
                            } else if (prevFormat.code == FORMAT_H || prevFormat.code == FORMAT_HH
                                    || prevFormat.code == FORMAT_HH_UPPER) {
                                theyMeantMinute = true;
                                break;
                            } else {
                                theyMeantMinute = false;
                                break;
                            }
                        }
                        if (theyMeantMinute) {
                            format = new VbDateFormat((token.code() == FORMAT_M ? FORMAT_N : FORMAT_NN), matched,
                                    locale, false);
                        } else {
                            format = makeFormat(token, locale);
                        }
                        break;
                    }

                    case FORMAT_DECIMAL: {
                        if (numberState == LEFT_OF_POINT) {
                            decimalShift = fixThousands(thousands, formatString, decimalShift);
                        }
                        numberState = RIGHT_OF_POINT;
                        useDecimal = true;
                        break;
                    }

                    case FORMAT_THOUSEP: {
                        if (numberState == LEFT_OF_POINT) {
                            // e.g. "#,##"
                            useThouSep = true;
                            thousands.add(formatString.length());
                        } else {
                            // e.g. "ddd, mmm dd, yyy"
                            format = makeFormat(token, locale);
                        }
                        break;
                    }

                    case FORMAT_TIMESEP: {
                        format = new LiteralFormat(locale.timeSeparator());
                        break;
                    }

                    case FORMAT_DATESEP: {
                        format = new LiteralFormat(locale.dateSeparator());
                        break;
                    }

                    case FORMAT_BACKSLASH: {
                        // Display the next character in the format string.
                        String s;
                        if (formatString.length() == 1) {
                            // Backslash is the last character in the
                            // string.
                            s = "";
                            newFormatString = "";
                        } else {
                            s = formatString.substring(1, 2);
                            newFormatString = formatString.substring(2);
                        }
                        format = new LiteralFormat(s);
                        break;
                    }

                    case FORMAT_E_MINUS_UPPER:
                    case FORMAT_E_PLUS_UPPER:
                    case FORMAT_E_MINUS_LOWER:
                    case FORMAT_E_PLUS_LOWER: {
                        if (numberState == LEFT_OF_POINT) {
                            decimalShift = fixThousands(thousands, formatString, decimalShift);
                        }
                        numberState = RIGHT_OF_EXP;
                        expFormat = token.code();
                        if (zeroesLeftOfPoint == 0 && zeroesRightOfPoint == 0) {
                            // We need a mantissa, so that format(123.45,
                            // "E+") gives "1E+2", not "0E+2" or "E+2".
                            zeroesLeftOfPoint = 1;
                        }
                        break;
                    }

                    case FORMAT_QUOTE: {
                        // Display the string inside the double quotation
                        // marks.
                        String s;
                        int j = formatString.indexOf("\"", 1);
                        if (j == -1) {
                            // The string did not contain a closing quote.
                            // Use the whole string.
                            s = formatString.substring(1);
                            newFormatString = "";
                        } else {
                            // Take the string inside the quotes.
                            s = formatString.substring(1, j);
                            newFormatString = formatString.substring(j + 1);
                        }
                        format = new LiteralFormat(s);
                        break;
                    }

                    case FORMAT_UPPER: {
                        format = new StringFormat(StringCase.UPPER, ">", locale.locale());
                        break;
                    }

                    case FORMAT_LOWER: {
                        format = new StringFormat(StringCase.LOWER, "<", locale.locale());
                        break;
                    }

                    case FORMAT_FILL_FROM_LEFT: {
                        fillFromRight = false;
                        break;
                    }

                    case FORMAT_GENERAL_NUMBER: {
                        format = new JavaFormat(locale.locale());
                        break;
                    }

                    case FORMAT_GENERAL_DATE: {
                        format = new JavaFormat(locale.locale());
                        break;
                    }

                    case FORMAT_INTL_CURRENCY: {
                        format = new LiteralFormat(locale.currencySymbol());
                        break;
                    }

                    default:
                        throw new IllegalStateException("Unexpected format code");
                    }
                    if (formatTypeOut[0] == null) {
                        formatTypeOut[0] = token.formatType();
                    }
                    if (format == null) {
                        // If the special-case code does not set format,
                        // we should not create a format element. (The
                        // token probably caused some flag to be set.)
                        ignored.append(matched);
                    } else {
                        prevIgnored = ignored.toString();
                        ignored.setLength(0);
                    }
                } else {
                    format = makeFormat(token, locale);
                }
            } else {
                // None of the standard format elements matched. Make the
                // current character into a literal.
                format = new LiteralFormat(formatString.substring(0, 1));
                newFormatString = formatString.substring(1);
            }

            if (format != null) {
                if (numberState != NOT_IN_A_NUMBER) {
                    // Having seen a few number tokens, we're looking at a
                    // non-number token. Create the number first.
                    if (numberState == LEFT_OF_POINT) {
                        decimalShift = fixThousands(thousands, formatString, decimalShift);
                    }
                    NumericFormat numericFormat = new NumericFormat(locale, expFormat, digitsLeftOfPoint,
                            zeroesLeftOfPoint, digitsRightOfPoint, zeroesRightOfPoint, digitsRightOfExp,
                            zeroesRightOfExp, useDecimal, useThouSep, originalFormatString);
                    formatList.add(numericFormat);
                    numberState = NOT_IN_A_NUMBER;
                    haveSeenNumber = true;
                }

                formatList.add(format);
                if (formatTypeOut[0] == null) {
                    formatTypeOut[0] = format.getFormatType();
                }
            }

            formatString = newFormatString;
        }

        if (numberState != NOT_IN_A_NUMBER) {
            // We're still in a number. Create a number format.
            if (numberState == LEFT_OF_POINT) {
                decimalShift = fixThousands(thousands, formatString, decimalShift);
            }
            NumericFormat numericFormat = new NumericFormat(locale, expFormat, digitsLeftOfPoint, zeroesLeftOfPoint,
                    digitsRightOfPoint, zeroesRightOfPoint, digitsRightOfExp, zeroesRightOfExp, useDecimal, useThouSep,
                    originalFormatString);
            formatList.add(numericFormat);
            numberState = NOT_IN_A_NUMBER;
            haveSeenNumber = true;
        }

        if (formatString.startsWith(";")) {
            formatString = formatString.substring(1);
        }

        // If they used some symbol like 'AM/PM' in the format string, tell all
        // date formats to use twelve hour clock. Likewise, figure out the
        // multiplier implied by their use of "%" or ",".
        boolean twelveHourClock = false;
        // User 24 hour format if HH is used in the format String. This follows
        // Java's date format convention.
        boolean isFormatHH = false;
        for (int i = 0; i < formatList.size(); i++) {
            switch (formatList.get(i).code) {
            case FORMAT_HH_UPPER:
                isFormatHH = true;
                break;
            case FORMAT_UPPER_AM_SOLIDUS_PM:
            case FORMAT_LOWER_AM_SOLIDUS_PM:
            case FORMAT_UPPER_A_SOLIDUS_P:
            case FORMAT_LOWER_A_SOLIDUS_P:
            case FORMAT_AMPM:
                twelveHourClock = true && !isFormatHH;
                break;

            case FORMAT_PERCENT:
                // If "%" occurs, the number should be multiplied by 100.
                decimalShift += 2;
                break;

            case FORMAT_THOUSEP:
                // If there is a thousands separator (",") immediately to the
                // left of the point, or at the end of the number, divide the
                // number by 1000. (Or by 1000^n if there are more than one.)
                if (haveSeenNumber && i + 1 < formatList.size()) {
                    final BasicFormat nextFormat = formatList.get(i + 1);
                    if (nextFormat.code != FORMAT_THOUSEP && nextFormat.code != FORMAT_0
                            && nextFormat.code != FORMAT_POUND) {
                        for (int j = i; j >= 0 && formatList.get(j).code == FORMAT_THOUSEP; j--) {
                            decimalShift -= 3;
                            formatList.remove(j); // ignore
                            --i;
                        }
                    }
                }
                break;

            default:
            }
        }

        if (twelveHourClock) {
            for (int i = 0; i < formatList.size(); i++) {
                if (formatList.get(i) instanceof VbDateFormat dateFormat) {
                    dateFormat.setTwelveHourClock(true);
                }
            }
        }

        if (decimalShift != 0) {
            for (int i = 0; i < formatList.size(); i++) {
                if (formatList.get(i) instanceof NumericFormat numericFormat) {
                    numericFormat.decimalShift = decimalShift;
                }
            }
        }

        // Merge adjacent literal formats.
        //
        // Must do this AFTER adjusting for percent. Otherwise '%' and following
        // '|' might be merged into a plain literal, and '%' would lose its
        // special powers.
        for (int i = 0; i < formatList.size(); ++i) {
            if (i > 0 && formatList.get(i) instanceof LiteralFormat literalFormat
                    && formatList.get(i - 1) instanceof LiteralFormat literalFormat1) {
                formatList.set(i - 1, new LiteralFormat(literalFormat1.getLiteral() + literalFormat.getLiteral()));
                formatList.remove(i);
                --i;
            }
        }

        // Create a CompoundFormat containing all of the format elements.
        // This is the end of an alternate - or of the whole format string.
        // Push the current list of formats onto the list of alternates.

        BasicFormat alternateFormat;
        switch (formatList.size()) {
        case 0:
            alternateFormat = null;
            break;
        case 1:
            alternateFormat = formatList.getFirst();
            break;
        default:
            alternateFormat = new CompoundFormat(formatList);
            break;
        }
        alternateFormatList.add(alternateFormat);
        return formatString;
    }

    /**
     * Creates a BasicFormat from a FormatToken. This replaces the Token.makeFormat
     * method from the original inner class.
     */
    private static BasicFormat makeFormat(FormatToken token, FormatLocale locale) {
        if (token.isDate()) {
            return new VbDateFormat(token.code(), token.tokenValue(), locale, false);
        } else if (token.isNumeric()) {
            return new LiteralFormat(token.code(), token.tokenValue());
        } else {
            return new LiteralFormat(token.tokenValue());
        }
    }

    private FormatToken findToken(String formatString, FormatType formatType) {
        FormatToken[] tokens = FormatConstants.tokens;
        for (int i = tokens.length - 1; i > 0; i--) {
            final FormatToken token = tokens[i];
            if (formatString.startsWith(token.tokenValue()) && token.compatibleWith(formatType)) {
                return token;
            }
        }
        return null;
    }

    private int fixThousands(List<Integer> thousands, String formatString, int shift) {
        int offset = formatString.length() + 1;
        for (int i = thousands.size() - 1; i >= 0; i--) {
            Integer integer = thousands.get(i);
            thousands.set(i, integer - offset);
            ++offset;
        }
        while (!thousands.isEmpty() && thousands.get(thousands.size() - 1) == 0) {
            shift -= 3;
            thousands.remove(thousands.size() - 1);
        }
        return shift;
    }
}
