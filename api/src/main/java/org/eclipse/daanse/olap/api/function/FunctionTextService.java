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

/**
 * Resolves localized function texts with a fallback chain: exact locale →
 * language → inline default texts of the metadata. Without any registered
 * {@link FunctionTextProvider} the result equals the inline texts.
 */
public interface FunctionTextService {

    ResolvedFunctionTexts resolve(FunctionMetaData functionMetaData, Locale locale);
}
