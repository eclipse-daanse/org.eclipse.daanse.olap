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
 *   SmartCity Jena, Stefan Bischof - initial
 *
 */
package org.eclipse.daanse.olap.api;

import java.util.stream.Stream;

public enum DataTypeJdbc {
    VARCHAR("Varchar"),

    NUMERIC("Numeric"),

    INTEGER("Integer"), DECIMAL("Decimal"),

    FLOAT("Float"),

    REAL("Real"),

    BIGINT("BigInt"),

    SMALLINT("SmallInt"),

    DOUBLE("Double"),

    BOOLEAN("Boolean"),

    DATE("Date"),

    TIME("Time"),

    TIMESTAMP("Timestamp");

    private String value;

    DataTypeJdbc(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static final java.util.Map<String, DataTypeJdbc> SQL_NAME_ALIASES = sqlNameAliases();

    private static java.util.Map<String, DataTypeJdbc> sqlNameAliases() {
        java.util.Map<String, DataTypeJdbc> m = new java.util.HashMap<>();
        // Character types — every flavour maps to VARCHAR.
        m.put("VARCHAR",                  VARCHAR);
        m.put("CHAR",                     VARCHAR);
        m.put("CHARACTER",                VARCHAR);
        m.put("CHARACTER VARYING",        VARCHAR);
        m.put("CHARACTER LARGE OBJECT",   VARCHAR);
        m.put("LONGVARCHAR",              VARCHAR);
        m.put("NVARCHAR",                 VARCHAR);
        m.put("NCHAR",                    VARCHAR);
        m.put("NATIONAL CHARACTER",       VARCHAR);
        m.put("NATIONAL CHARACTER VARYING", VARCHAR);
        m.put("CLOB",                     VARCHAR);
        m.put("NCLOB",                    VARCHAR);
        // Integral.
        m.put("INT",                      INTEGER);
        m.put("INTEGER",                  INTEGER);
        m.put("BIGINT",                   BIGINT);
        m.put("SMALLINT",                 SMALLINT);
        m.put("TINYINT",                  SMALLINT);
        // Numeric (non-integral).
        m.put("NUMERIC",                  NUMERIC);
        m.put("DECIMAL",                  DECIMAL);
        m.put("FLOAT",                    FLOAT);
        m.put("REAL",                     REAL);
        m.put("DOUBLE",                   DOUBLE);
        m.put("DOUBLE PRECISION",         DOUBLE);
        // Boolean / bit.
        m.put("BOOLEAN",                  BOOLEAN);
        m.put("BIT",                      BOOLEAN);
        // Temporal.
        m.put("DATE",                     DATE);
        m.put("TIME",                     TIME);
        m.put("TIME WITH TIMEZONE",       TIME);
        m.put("TIMESTAMP",                TIMESTAMP);
        m.put("TIMESTAMP WITH TIMEZONE",  TIMESTAMP);
        return m;
    }

    public static DataTypeJdbc fromValue(String v) {
        if (v == null) return NUMERIC;
        String key = v.trim().toUpperCase().replaceAll("\\s+", " ");
        DataTypeJdbc byAlias = SQL_NAME_ALIASES.get(key);
        if (byAlias != null) return byAlias;
        return Stream.of(DataTypeJdbc.values())
            .filter(e -> e.getValue().equalsIgnoreCase(v))
            .findFirst()
            .orElse(NUMERIC);
    }
    
    
}
