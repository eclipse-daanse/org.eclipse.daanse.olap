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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.ResolvedFunctionTexts;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.junit.jupiter.api.Test;

class FunctionTextServiceImplTest {

    private static FunctionMetaData sum() {
        return FunctionMetaDataR.of(new FunctionOperationAtom("Sum"),
                "Returns the sum of a numeric expression evaluated over a set.", DataType.NUMERIC,
                FunctionParameterR.param(DataType.SET),
                FunctionParameterR.param(DataType.NUMERIC).asOptional());
    }

    @Test
    void withoutProvidersServesInlineTexts() {
        FunctionTextServiceImpl service = new FunctionTextServiceImpl();

        ResolvedFunctionTexts texts = service.resolve(sum(), Locale.of("de", "DE"));

        assertThat(texts.description()).startsWith("Returns the sum");
        assertThat(texts.caption()).isEqualTo("Sum");
        assertThat(texts.parameters().get("Numeric_Expression").displayName()).isEqualTo("Numeric Expression");
    }

    @Test
    void bundledGermanTextsResolveWithFallbackChain() {
        FunctionTextServiceImpl service = new FunctionTextServiceImpl();
        service.addProvider(new BundledFunctionTextProvider());

        ResolvedFunctionTexts texts = service.resolve(sum(), Locale.of("de", "DE"));

        assertThat(texts.description()).contains("Summe eines numerischen Ausdrucks");
        assertThat(texts.caption()).isEqualTo("Summe");
        assertThat(texts.example()).contains("Sum({[Product].Members}, [Measures].[Sales])");
        assertThat(texts.parameters().get("Numeric_Expression").displayName()).isEqualTo("Numerischer Ausdruck");
        assertThat(texts.parameters().get("Set").description())
                .hasValueSatisfying(v -> assertThat(v).contains("aggregiert"));
    }

    @Test
    void unknownLocaleFallsBackToInline() {
        FunctionTextServiceImpl service = new FunctionTextServiceImpl();
        service.addProvider(new BundledFunctionTextProvider());

        ResolvedFunctionTexts texts = service.resolve(sum(), Locale.of("fr", "FR"));

        assertThat(texts.description()).startsWith("Returns the sum");
    }
}
