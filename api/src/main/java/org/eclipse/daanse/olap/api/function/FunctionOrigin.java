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

/**
 * Origin of a function as exposed through the XMLA MDSCHEMA_FUNCTIONS ORIGIN
 * column.
 */
public enum FunctionOrigin {

    /** Built-in multidimensional function. */
    MSOLAP(1),
    /** User-defined function. */
    UDF(2),
    /** Relational function. */
    RELATIONAL(3),
    /** Scalar function. */
    SCALAR(4);

    private final int value;

    FunctionOrigin(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
