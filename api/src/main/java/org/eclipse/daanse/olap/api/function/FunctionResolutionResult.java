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

import java.util.List;

/**
 * Successful match of a resolver: the chosen definition, the declared overload
 * that matched, the argument-to-parameter bindings and the conversions needed.
 */
public interface FunctionResolutionResult {

    FunctionDefinition definition();

    /** The declared overload that matched. */
    FunctionMetaData matchedMetaData();

    /** Per-argument trace; may be empty for hand-written resolvers. */
    List<ArgumentBinding> bindings();

    List<FunctionResolver.Conversion> conversions();

    /** Total conversion cost; used by the validator to arbitrate between resolvers. */
    default int cost() {
        int cost = 0;
        for (FunctionResolver.Conversion conversion : conversions()) {
            cost += conversion.getCost();
        }
        return cost;
    }
}
