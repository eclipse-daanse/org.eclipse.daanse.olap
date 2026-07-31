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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

import org.eclipse.daanse.olap.api.function.FunctionTextProvider;
import org.eclipse.daanse.olap.api.function.FunctionTexts;

/**
 * {@link FunctionTextProvider} backed by {@link ResourceBundle} properties.
 *
 * <p>Key scheme: {@code <textKey>.description|caption|example|remarks} and
 * {@code <textKey>.param.<parameterName>.displayName|description}.
 *
 * <p>Register an instance per bundle (as an OSGi service or via the
 * {@link FunctionTextServiceImpl}); locale fallback is handled by the service,
 * therefore this provider only answers for the exact bundle locale.
 */
public class ResourceBundleFunctionTextProvider implements FunctionTextProvider {

    private static final String PARAM_SEGMENT = ".param.";

    private final String baseName;
    private final ClassLoader classLoader;

    public ResourceBundleFunctionTextProvider(String baseName, ClassLoader classLoader) {
        this.baseName = baseName;
        this.classLoader = classLoader;
    }

    @Override
    public Optional<FunctionTexts> texts(String functionKey, Locale locale) {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(baseName, locale, classLoader,
                    ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        } catch (MissingResourceException e) {
            return Optional.empty();
        }
        if (!matches(bundle.getLocale(), locale)) {
            return Optional.empty();
        }

        Optional<String> description = read(bundle, functionKey + ".description");
        Optional<String> caption = read(bundle, functionKey + ".caption");
        Optional<String> example = read(bundle, functionKey + ".example");
        Optional<String> remarks = read(bundle, functionKey + ".remarks");

        Map<String, FunctionTexts.ParameterTexts> parameters = new HashMap<>();
        String prefix = functionKey + PARAM_SEGMENT;
        for (String key : bundle.keySet()) {
            if (!key.startsWith(prefix)) {
                continue;
            }
            String rest = key.substring(prefix.length());
            int dot = rest.lastIndexOf('.');
            if (dot <= 0) {
                continue;
            }
            String parameterName = rest.substring(0, dot);
            parameters.computeIfAbsent(parameterName,
                    p -> new FunctionTexts.ParameterTexts(read(bundle, prefix + p + ".displayName"),
                            read(bundle, prefix + p + ".description")));
        }

        if (description.isEmpty() && caption.isEmpty() && example.isEmpty() && remarks.isEmpty()
                && parameters.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new FunctionTexts(description, caption, example, remarks, Map.copyOf(parameters)));
    }

    private static boolean matches(Locale bundleLocale, Locale requested) {
        if (bundleLocale.equals(requested)) {
            return true;
        }
        return bundleLocale.getLanguage().equals(requested.getLanguage()) && bundleLocale.getCountry().isEmpty()
                && requested.getCountry().isEmpty();
    }

    private static Optional<String> read(ResourceBundle bundle, String key) {
        return bundle.containsKey(key) ? Optional.of(bundle.getString(key)) : Optional.empty();
    }
}
