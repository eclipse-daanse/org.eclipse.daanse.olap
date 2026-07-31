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
 * Binding of one call argument to one declared parameter of the matched
 * overload. With repeat groups the same parameter index can be bound by
 * several arguments.
 */
public record ArgumentBinding(int argumentIndex, int parameterIndex, DataType argumentCategory,
        DataType parameterCategory, int conversionCost) {
}
