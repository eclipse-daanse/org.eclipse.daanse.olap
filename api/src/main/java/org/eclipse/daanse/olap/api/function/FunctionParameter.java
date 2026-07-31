/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
import java.util.Optional;

import org.eclipse.daanse.olap.api.DataType;

/**
 * Declared parameter of a function.
 *
 * <p>Flag semantics (matching the XMLA MDSCHEMA_FUNCTIONS PARAMETERINFO
 * rowset):
 * <ul>
 * <li>{@link #optional()} — the argument may be absent; all following
 * non-skippable parameters must then be absent too (trailing optional).</li>
 * <li>{@link #skippable()} — the argument may be omitted even when subsequent
 * arguments are present.</li>
 * <li>{@link #repeatGroup()} — parameters sharing a repeat group &gt; 0 repeat
 * together as a unit; a group whose first parameter is optional may occur zero
 * times. A single repeatable parameter forms its own group.</li>
 * </ul>
 */
public interface FunctionParameter {

    DataType dataType();

    Optional<String> name();

    Optional<String> description();

    Optional<List<String>> reservedWords();

    /** The argument may be absent (trailing optional). */
    default boolean optional() {
        return false;
    }

    /** Multiple values may be specified for this parameter. */
    default boolean repeatable() {
        return false;
    }

    /** Index of the repeat group; 0 = not repeated. */
    default int repeatGroup() {
        return 0;
    }

    /** The argument may be omitted even when later arguments are present. */
    default boolean skippable() {
        return false;
    }
}
