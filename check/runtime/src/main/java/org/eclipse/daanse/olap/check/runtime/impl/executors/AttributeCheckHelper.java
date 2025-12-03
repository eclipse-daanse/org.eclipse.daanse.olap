/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.daanse.olap.check.runtime.impl.executors;

import org.eclipse.daanse.olap.check.model.check.MatchMode;

/**
 * Helper class for comparing attribute values with various match modes.
 */
public class AttributeCheckHelper {

    private AttributeCheckHelper() {
        // Utility class
    }

    public static boolean compareValues(String expected, String actual, MatchMode matchMode, boolean caseSensitive) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }

        return switch (matchMode) {
            case EQUALS -> caseSensitive ? expected.equals(actual) : expected.equalsIgnoreCase(actual);
            case CONTAINS -> caseSensitive ? actual.contains(expected) : actual.toLowerCase().contains(expected.toLowerCase());
            case STARTS_WITH -> caseSensitive ? actual.startsWith(expected) : actual.toLowerCase().startsWith(expected.toLowerCase());
            case ENDS_WITH -> caseSensitive ? actual.endsWith(expected) : actual.toLowerCase().endsWith(expected.toLowerCase());
            case REGEX -> actual.matches(expected);
            case NOT_EQUALS -> caseSensitive ? !expected.equals(actual) : !expected.equalsIgnoreCase(actual);
            case NOT_CONTAINS -> caseSensitive ? !actual.contains(expected) : !actual.toLowerCase().contains(expected.toLowerCase());
        };
    }

    public static boolean compareBooleans(Boolean expected, Boolean actual) {
        if (expected == null) {
            return true; // No expectation set
        }
        return expected.equals(actual);
    }

    public static boolean compareInts(Integer expected, Integer actual) {
        if (expected == null) {
            return true; // No expectation set
        }
        return expected.equals(actual);
    }
}
