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
 * Fully resolved texts for one function and locale: description and caption are
 * always present (falling back to the inline metadata texts), parameter texts
 * are keyed by canonical parameter name.
 */
public record ResolvedFunctionTexts(
        String description,
        String caption,
        Optional<String> example,
        Optional<String> remarks,
        Map<String, ResolvedParameterTexts> parameters) {

    public record ResolvedParameterTexts(String displayName, Optional<String> description) {
    }
}
