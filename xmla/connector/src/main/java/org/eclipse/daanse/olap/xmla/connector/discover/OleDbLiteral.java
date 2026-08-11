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
package org.eclipse.daanse.olap.xmla.connector.discover;

/**
 * The literals DISCOVER_LITERALS answers: quoting characters, invalid
 * characters, maximum lengths - what a query builder needs before it writes its
 * first identifier.
 * <p>
 * The enum values are DBLITERALENUM ordinals from OLEDB.H; the model documents
 * them only as prose, so the answered set is written out here, one constant per
 * DBLITERAL_* row.
 */
public enum OleDbLiteral {

    CATALOG_NAME(null, 24, ".", OleDbLiteral.NUMBERS, 2), CATALOG_SEPARATOR(".", 0, null, null, 3),
    COLUMN_ALIAS(null, -1, "'\"[]", OleDbLiteral.NUMBERS, 5), COLUMN_NAME(null, -1, ".", OleDbLiteral.NUMBERS, 6),
    CORRELATION_NAME(null, -1, "'\"[]", OleDbLiteral.NUMBERS, 7), CUBE_NAME(null, -1, ".", OleDbLiteral.NUMBERS, 21),
    DIMENSION_NAME(null, -1, ".", OleDbLiteral.NUMBERS, 22), HIERARCHY_NAME(null, -1, ".", OleDbLiteral.NUMBERS, 23),
    LEVEL_NAME(null, -1, ".", OleDbLiteral.NUMBERS, 24), MEMBER_NAME(null, -1, ".", OleDbLiteral.NUMBERS, 25),
    PROCEDURE_NAME(null, -1, ".", OleDbLiteral.NUMBERS, 14), PROPERTY_NAME(null, -1, ".", OleDbLiteral.NUMBERS, 26),
    QUOTE("[", -1, null, null, 15), QUOTE_SUFFIX("]", -1, null, null, 28),
    TABLE_NAME(null, -1, ".", OleDbLiteral.NUMBERS, 17), TEXT_COMMAND(null, -1, null, null, 18),
    USER_NAME(null, 0, null, null, 19);

    private static final String NUMBERS = "0123456789";

    private final String literalValue;
    private final int literalMaxLength;
    private final String literalInvalidChars;
    private final String literalInvalidStartingChars;
    private final int literalNameEnumValue;

    OleDbLiteral(String literalValue, int literalMaxLength, String literalInvalidChars,
            String literalInvalidStartingChars, int literalNameEnumValue) {
        this.literalValue = literalValue;
        this.literalMaxLength = literalMaxLength;
        this.literalInvalidChars = literalInvalidChars;
        this.literalInvalidStartingChars = literalInvalidStartingChars;
        this.literalNameEnumValue = literalNameEnumValue;
    }

    public String literalValue() {
        return literalValue;
    }

    public int literalMaxLength() {
        return literalMaxLength;
    }

    public String literalInvalidChars() {
        return literalInvalidChars;
    }

    public String literalInvalidStartingChars() {
        return literalInvalidStartingChars;
    }

    public int literalNameEnumValue() {
        return literalNameEnumValue;
    }
}
