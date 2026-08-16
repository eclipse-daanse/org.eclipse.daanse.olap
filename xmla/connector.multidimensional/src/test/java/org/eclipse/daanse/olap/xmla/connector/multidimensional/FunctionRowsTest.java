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
package org.eclipse.daanse.olap.xmla.connector.multidimensional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionService;
import org.eclipse.daanse.olap.api.function.FunctionTextProvider;
import org.eclipse.daanse.olap.api.function.FunctionTexts;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.text.FunctionTextServiceImpl;
import org.eclipse.daanse.xmla.model.rowset.multidimensional.MdschemaFunctionsRow;
import org.eclipse.daanse.xmla.model.rowset.multidimensional.ParameterInfo;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

/**
 * The bridge's {@code MdSchemaFunctionsRowMapperTest}, ported onto the
 * connector's rows.
 */
class FunctionRowsTest {

    private static FunctionMetaData sumMetaData() {
        return FunctionMetaDataR.of(new FunctionOperationAtom("Sum"),
                "Returns the sum of a numeric expression evaluated over a set.", DataType.NUMERIC,
                FunctionParameterR.param(DataType.SET), FunctionParameterR.param(DataType.NUMERIC).asOptional()
                        .describedAs("Expression evaluated per member."));
    }

    private static FunctionService serviceWith(FunctionMetaData... metaData) {
        FunctionService service = mock(FunctionService.class);
        when(service.getFunctionMetaDatas()).thenReturn(List.of(metaData));
        return service;
    }

    private static List<EObject> rows(FunctionService service, FunctionTextServiceImpl texts, Locale locale,
            Optional<String> functionName, Optional<String> interfaceName) {
        return FunctionRows.rows(service, texts, locale, functionName, Optional.empty(), interfaceName,
                Optional.empty());
    }

    @Test
    void emitsFullRowWithParameterInfo() {
        List<EObject> result = rows(serviceWith(sumMetaData()), new FunctionTextServiceImpl(), Locale.ENGLISH,
                Optional.empty(), Optional.empty());

        assertThat(result).hasSize(1);
        MdschemaFunctionsRow row = (MdschemaFunctionsRow) result.get(0);
        assertThat(row.getFunctionName()).isEqualTo("Sum");
        assertThat(row.getParameterList()).isEqualTo("«Set», [«Numeric Expression»]");
        assertThat(row.getReturnType()).isEqualTo(5);
        assertThat(row.getCaption()).isEqualTo("Sum");

        List<ParameterInfo> parameters = row.getParameterInfo();
        assertThat(parameters).hasSize(2);
        assertThat(parameters.get(0).getName()).isEqualTo("Set");
        assertThat(parameters.get(0).getOptional()).isFalse();
        assertThat(parameters.get(1).getName()).isEqualTo("Numeric Expression");
        assertThat(parameters.get(1).getOptional()).isTrue();
        assertThat(parameters.get(1).getDescription()).isEqualTo("Expression evaluated per member.");
    }

    @Test
    void appliesFunctionNameRestriction() {
        assertThat(rows(serviceWith(sumMetaData()), new FunctionTextServiceImpl(), Locale.ENGLISH, Optional.of("other"),
                Optional.empty())).isEmpty();
    }

    @Test
    void localizesDescriptionAndCaption() {
        FunctionTextServiceImpl texts = new FunctionTextServiceImpl();
        texts.addProvider(germanProvider());

        List<EObject> result = rows(serviceWith(sumMetaData()), texts, Locale.of("de", "DE"), Optional.empty(),
                Optional.empty());

        MdschemaFunctionsRow row = (MdschemaFunctionsRow) result.get(0);
        assertThat(row.getDescription()).contains("Summe eines numerischen Ausdrucks");
        assertThat(row.getCaption()).isEqualTo("Summe");
        assertThat(row.getFunctionName()).isEqualTo("Sum");
    }

    @Test
    void mapsLcidToLocale() {
        assertThat(FunctionRows.localeOf("1031").getLanguage()).isEqualTo("de");
        assertThat(FunctionRows.localeOf(null)).isEqualTo(Locale.ENGLISH);
        assertThat(FunctionRows.localeOf("99999")).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void mapsReturnTypesToOleDb() {
        assertThat(FunctionRows.oleDbTypeOf(DataType.INTEGER)).isEqualTo(3);
        assertThat(FunctionRows.oleDbTypeOf(DataType.LOGICAL)).isEqualTo(11);
        assertThat(FunctionRows.oleDbTypeOf(DataType.STRING)).isEqualTo(130);
        assertThat(FunctionRows.oleDbTypeOf(DataType.SET)).isEqualTo(12);
        assertThat(FunctionRows.oleDbTypeOf(DataType.DATE_TIME)).isEqualTo(7);
    }

    private static FunctionTextProvider germanProvider() {
        return (key, locale) -> {
            if (!"de".equals(locale.getLanguage()) || !"Sum".equals(key)) {
                return Optional.empty();
            }
            return Optional
                    .of(new FunctionTexts(Optional.of("Liefert die Summe eines numerischen Ausdrucks über eine Menge."),
                            Optional.of("Summe"), Optional.empty(), Optional.empty(), Map.of()));
        };
    }
}
