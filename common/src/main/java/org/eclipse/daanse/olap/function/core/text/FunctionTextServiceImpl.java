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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionParameter;
import org.eclipse.daanse.olap.api.function.FunctionTextProvider;
import org.eclipse.daanse.olap.api.function.FunctionTextService;
import org.eclipse.daanse.olap.api.function.FunctionTexts;
import org.eclipse.daanse.olap.api.function.ResolvedFunctionTexts;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ServiceScope;

@Component(service = FunctionTextService.class, scope = ServiceScope.SINGLETON)
public class FunctionTextServiceImpl implements FunctionTextService {

    private final List<FunctionTextProvider> providers = new ArrayList<>();

    @Reference(service = FunctionTextProvider.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public synchronized void addProvider(FunctionTextProvider provider) {
        providers.add(provider);
    }

    public synchronized void removeProvider(FunctionTextProvider provider) {
        providers.remove(provider);
    }

    @Override
    public ResolvedFunctionTexts resolve(FunctionMetaData functionMetaData, Locale locale) {
        List<FunctionTexts> layers = layersFor(functionMetaData.textKey(), locale);

        String description = firstText(layers, FunctionTexts::description).orElse(functionMetaData.description());
        String caption = firstText(layers, FunctionTexts::caption).orElse(functionMetaData.caption());
        Optional<String> example = firstText(layers, FunctionTexts::example);
        Optional<String> remarks = firstText(layers, FunctionTexts::remarks);

        Map<String, ResolvedFunctionTexts.ResolvedParameterTexts> parameters = new HashMap<>();
        for (FunctionParameter parameter : functionMetaData.parameters()) {
            Optional<String> parameterName = parameter.name();
            if (parameterName.isEmpty()) {
                continue;
            }
            String key = parameterName.get();
            Optional<String> displayName = firstParameterText(layers, key, FunctionTexts.ParameterTexts::displayName);
            Optional<String> parameterDescription = firstParameterText(layers, key,
                    FunctionTexts.ParameterTexts::description);
            parameters.put(key, new ResolvedFunctionTexts.ResolvedParameterTexts(
                    displayName.orElseGet(() -> key.replace('_', ' ')),
                    parameterDescription.or(parameter::description)));
        }
        return new ResolvedFunctionTexts(description, caption, example, remarks, Map.copyOf(parameters));
    }

    /** Layers ordered by precedence: exact locale, then language-only, per provider registration order. */
    private synchronized List<FunctionTexts> layersFor(String functionKey, Locale locale) {
        List<FunctionTexts> layers = new ArrayList<>();
        List<Locale> chain = new ArrayList<>();
        if (locale != null) {
            chain.add(locale);
            if (!locale.getCountry().isEmpty() || !locale.getVariant().isEmpty()) {
                chain.add(Locale.of(locale.getLanguage()));
            }
        }
        for (Locale l : chain) {
            for (FunctionTextProvider provider : providers) {
                provider.texts(functionKey, l).ifPresent(layers::add);
            }
        }
        return layers;
    }

    private static Optional<String> firstText(List<FunctionTexts> layers,
            Function<FunctionTexts, Optional<String>> accessor) {
        for (FunctionTexts layer : layers) {
            Optional<String> value = accessor.apply(layer);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    private static Optional<String> firstParameterText(List<FunctionTexts> layers, String parameterKey,
            Function<FunctionTexts.ParameterTexts, Optional<String>> accessor) {
        for (FunctionTexts layer : layers) {
            FunctionTexts.ParameterTexts parameterTexts = layer.parameters().get(parameterKey);
            if (parameterTexts != null) {
                Optional<String> value = accessor.apply(parameterTexts);
                if (value.isPresent()) {
                    return value;
                }
            }
        }
        return Optional.empty();
    }
}
