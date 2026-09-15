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
 */
package org.eclipse.daanse.olap.testkit.function.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionParameter;
import org.eclipse.daanse.olap.api.function.FunctionService;
import org.eclipse.daanse.olap.function.services.standard.StandardFunctions;
import org.eclipse.daanse.olap.testkit.function.SignatureText;

/**
 * Writes functions.properties from the descriptions currently living in Java.
 * Run once, review, commit. Afterwards the properties file is the source of truth and
 * FunctionMetaData.description() is only the fallback for runtime-registered functions.
 */
class GenerateBaseTextBundle {

    public static void main(String[] args) throws Exception {
        FunctionService service = StandardFunctions.standard();
        SortedMap<String, SortedMap<String, String>> entriesByFunction = new TreeMap<>();

        for (FunctionMetaData metaData : service.getFunctionMetaDatas()) {
            String key = metaData.textKey();
            SortedMap<String, String> entries = entriesByFunction.computeIfAbsent(key, k -> new TreeMap<>());
            entries.putIfAbsent(key + ".description", metaData.description());
            entries.putIfAbsent(key + ".caption", metaData.caption());
            entries.putIfAbsent(key + ".example", SignatureText.ofDeclaration(metaData));
            for (FunctionParameter parameter : metaData.parameters()) {
                parameter.name().ifPresent(name -> parameter.description().ifPresent(
                        d -> entries.putIfAbsent(key + ".param." + name + ".description", d)));
            }
        }

        StringBuilder buf = new StringBuilder("""
                # Base (English) function texts for MDSCHEMA_FUNCTIONS and formula editors.
                # Key scheme: <textKey>.description|caption|example|remarks
                #             <textKey>.param.<ParameterName>.displayName|description
                #
                # Generated once from FunctionMetaData.description(); this file is now the
                # source of truth. Translations live in functions_<language>.properties.

                """);
        entriesByFunction.values().forEach(entries -> {
            entries.forEach((k, v) -> buf.append(k).append('=')
                    .append(v.replaceAll("\\s*\\R\\s*", " ").trim()).append('\n'));
            buf.append('\n');
        });

        Files.writeString(Path.of("common/src/main/resources/org/eclipse/daanse/olap/"
                + "function/core/text/functions_en.properties"), buf.toString());
    }
}