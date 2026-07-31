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
package org.eclipse.daanse.olap.api.function;

import org.eclipse.daanse.olap.api.DataType;

/**
 * Logical classification of a function as exposed through the XMLA
 * MDSCHEMA_FUNCTIONS INTERFACE_NAME column. The default classification is
 * derived from the return category via {@link #derivedFrom(DataType)};
 * functions whose semantic category differs (e.g. Filter returns a set but is
 * a FILTER function) declare an explicit value.
 */
public enum FunctionInterface {

    DATETIME,
    LOGICAL,
    FILTER,
    NAVIGATION,
    STATISTICAL,
    STRING,
    NUMERIC,
    SET,
    TUPLE,
    MEMBER,
    LEVEL,
    HIERARCHY,
    DIMENSION,
    ARRAY,
    SUBCUBE,
    METADATA,
    KPI,
    UDF,
    VALUE,
    OTHER;

    public static FunctionInterface derivedFrom(DataType returnCategory) {
        if (returnCategory == null) {
            return OTHER;
        }
        return switch (returnCategory) {
        case NUMERIC, INTEGER -> NUMERIC;
        case LOGICAL -> LOGICAL;
        case STRING -> STRING;
        case SET -> SET;
        case MEMBER -> MEMBER;
        case DATE_TIME -> DATETIME;
        case TUPLE -> TUPLE;
        case LEVEL -> LEVEL;
        case HIERARCHY -> HIERARCHY;
        case DIMENSION -> DIMENSION;
        case ARRAY -> ARRAY;
        case VALUE -> VALUE;
        default -> OTHER;
        };
    }
}
