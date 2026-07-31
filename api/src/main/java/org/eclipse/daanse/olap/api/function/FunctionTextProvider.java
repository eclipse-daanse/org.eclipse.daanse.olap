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

import java.util.Locale;
import java.util.Optional;

/**
 * Supplies localized {@link FunctionTexts} for function text keys. Multiple
 * providers may be registered (OSGi service, ranked); each language or source
 * can contribute its own provider.
 */
public interface FunctionTextProvider {

    /**
     * @param functionKey {@link FunctionMetaData#textKey()}
     * @param locale      requested locale, never null
     * @return texts for exactly this locale (no fallback inside the provider),
     *         empty if this provider has nothing for the key/locale pair
     */
    Optional<FunctionTexts> texts(String functionKey, Locale locale);
}
