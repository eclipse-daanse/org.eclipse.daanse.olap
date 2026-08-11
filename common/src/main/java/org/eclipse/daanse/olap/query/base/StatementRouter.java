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
package org.eclipse.daanse.olap.query.base;

import java.util.Locale;

/**
 * Decides what kind of statement a text is before any parser sees it.
 */
public final class StatementRouter {

    public enum Kind {
        DMV, MDX, SQL
    }

    private StatementRouter() {
        // static access only
    }

    public static Kind classify(String statement) {
        String text = withoutLeadingComments(statement);
        String upper = text.toUpperCase(Locale.ROOT);

        if (hasSystemTable(upper)) {
            return Kind.DMV;
        }
        if (upper.startsWith("WITH") || upper.startsWith("DRILLTHROUGH")
                || upper.startsWith("EXPLAIN") || upper.startsWith("REFRESH")
                || upper.startsWith("UPDATE") || upper.startsWith("CALL")
                || upper.startsWith("BEGIN") || upper.startsWith("COMMIT")
                || upper.startsWith("ROLLBACK")) {
            return Kind.MDX;
        }
        if (upper.startsWith("SELECT")) {
            // An MDX SELECT names axes (ON COLUMNS, ON 0) or a bracketed cube; a SQL SELECT
            // does neither.
            if (upper.contains(" ON ") || upper.contains("FROM [")) {
                return Kind.MDX;
            }
            return Kind.SQL;
        }
        if (upper.startsWith("INSERT") || upper.startsWith("DELETE")) {
            return Kind.SQL;
        }
        return Kind.MDX;
    }

    /**
     * {@code FROM $SYSTEM.} makes a DMV - looked for outside brackets and quotes, so a member
     * or literal spelling those characters does not reroute a query.
     */
    private static boolean hasSystemTable(String upper) {
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < upper.length(); i++) {
            char c = upper.charAt(i);
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                quote = c;
            } else if (c == '[') {
                depth++;
            } else if (c == ']' && depth > 0) {
                depth--;
            } else if (c == '$' && depth == 0 && upper.startsWith("$SYSTEM.", i)
                    && precededByFromOrParen(upper, i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The token before the {@code $} has to be FROM - or an opening parenthesis, which is the
     * {@code FROM SYSTEMRESTRICTSCHEMA($SYSTEM.…)} form - for this to be a DMV table
     * reference.
     */
    private static boolean precededByFromOrParen(String upper, int dollar) {
        int end = dollar;
        while (end > 0 && Character.isWhitespace(upper.charAt(end - 1))) {
            end--;
        }
        if (end > 0 && upper.charAt(end - 1) == '(') {
            return true;
        }
        return end >= 4 && upper.startsWith("FROM", end - 4)
                && (end == 4 || !Character.isLetterOrDigit(upper.charAt(end - 5)));
    }

    /** Leading whitespace and comments carry no meaning for the kind. */
    private static String withoutLeadingComments(String statement) {
        String text = statement == null ? "" : statement;
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                int close = text.indexOf("*/", i + 2);
                if (close < 0) {
                    return "";
                }
                i = close + 2;
                continue;
            }
            if ((c == '-' && i + 1 < text.length() && text.charAt(i + 1) == '-')
                    || (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/')) {
                int newline = text.indexOf('\n', i);
                if (newline < 0) {
                    return "";
                }
                i = newline + 1;
                continue;
            }
            break;
        }
        return text.substring(i);
    }
}
