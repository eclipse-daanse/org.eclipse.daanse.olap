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

import java.util.Map;
import java.util.Optional;

/**
 * Localized texts for one function, keyed by {@link FunctionMetaData#textKey()}.
 * Every field is optional; absent fields fall back to the next provider or the
 * inline default texts of the metadata.
 *
 * @param parameters localized parameter texts keyed by the canonical parameter
 *                   name ({@link FunctionParameter#name()})
 */
public record FunctionTexts(
        Optional<String> description,
        Optional<String> caption,
        Optional<String> example,
        Optional<String> remarks,
        Map<String, ParameterTexts> parameters) {

    public record ParameterTexts(Optional<String> displayName, Optional<String> description) {
    }
}
