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
package org.eclipse.daanse.olap.function.core.text;

import java.util.Locale;
import java.util.Optional;

import org.eclipse.daanse.olap.api.function.FunctionTextProvider;
import org.eclipse.daanse.olap.api.function.FunctionTexts;
import org.osgi.service.component.annotations.Component;

/**
 * Ships the bundled translations (functions_&lt;lang&gt;.properties in this
 * package). Additional languages or sources register their own
 * {@link FunctionTextProvider}.
 */
@Component(service = FunctionTextProvider.class)
public class BundledFunctionTextProvider implements FunctionTextProvider {

    private final ResourceBundleFunctionTextProvider delegate = new ResourceBundleFunctionTextProvider(
            "org.eclipse.daanse.olap.function.core.text.functions", BundledFunctionTextProvider.class.getClassLoader());

    @Override
    public Optional<FunctionTexts> texts(String functionKey, Locale locale) {
        return delegate.texts(functionKey, locale);
    }
}
