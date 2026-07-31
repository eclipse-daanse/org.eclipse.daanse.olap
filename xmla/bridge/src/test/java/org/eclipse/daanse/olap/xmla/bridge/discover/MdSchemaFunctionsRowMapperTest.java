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
package org.eclipse.daanse.olap.xmla.bridge.discover;

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
import org.eclipse.daanse.xmla.api.common.enums.InterfaceNameEnum;
import org.eclipse.daanse.xmla.api.common.enums.OriginEnum;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.ParameterInfo;
import org.junit.jupiter.api.Test;

class MdSchemaFunctionsRowMapperTest {

    private static FunctionMetaData sumMetaData() {
        return FunctionMetaDataR.of(new FunctionOperationAtom("Sum"),
                "Returns the sum of a numeric expression evaluated over a set.", DataType.NUMERIC,
                FunctionParameterR.param(DataType.SET),
                FunctionParameterR.param(DataType.NUMERIC).asOptional()
                        .describedAs("Expression evaluated per member."));
    }

    private static FunctionService serviceWith(FunctionMetaData... fmds) {
        FunctionService service = mock(FunctionService.class);
        when(service.getFunctionMetaDatas()).thenReturn(List.of(fmds));
        return service;
    }

    private static MdSchemaFunctionsRestrictions noRestrictions() {
        MdSchemaFunctionsRestrictions restrictions = mock(MdSchemaFunctionsRestrictions.class);
        when(restrictions.functionName()).thenReturn(Optional.empty());
        when(restrictions.origin()).thenReturn(Optional.empty());
        when(restrictions.interfaceName()).thenReturn(Optional.empty());
        when(restrictions.libraryName()).thenReturn(Optional.empty());
        return restrictions;
    }

    @Test
    void emitsFullRowWithParameterInfo() {
        List<MdSchemaFunctionsResponseRow> rows = MdSchemaFunctionsRowMapper.rows(serviceWith(sumMetaData()),
                new FunctionTextServiceImpl(), Locale.ENGLISH, noRestrictions());

        assertThat(rows).hasSize(1);
        MdSchemaFunctionsResponseRow row = rows.get(0);
        assertThat(row.functionName()).contains("Sum");
        assertThat(row.parameterList()).isEqualTo("«Set», [«Numeric Expression»]");
        assertThat(row.returnType()).contains(5); // DBTYPE_R8
        assertThat(row.origin()).contains(OriginEnum.MSOLAP);
        assertThat(row.interfaceName()).contains(InterfaceNameEnum.NUMERIC);
        assertThat(row.caption()).contains("Sum");

        assertThat(row.parameterInfo()).isPresent();
        List<ParameterInfo> parameterInfos = row.parameterInfo().get();
        assertThat(parameterInfos).hasSize(2);
        assertThat(parameterInfos.get(0).name()).isEqualTo("Set");
        assertThat(parameterInfos.get(0).optional()).isFalse();
        assertThat(parameterInfos.get(1).name()).isEqualTo("Numeric Expression");
        assertThat(parameterInfos.get(1).optional()).isTrue();
        assertThat(parameterInfos.get(1).description()).isEqualTo("Expression evaluated per member.");
    }

    @Test
    void appliesFunctionNameRestriction() {
        MdSchemaFunctionsRestrictions restrictions = noRestrictions();
        when(restrictions.functionName()).thenReturn(Optional.of("other"));

        List<MdSchemaFunctionsResponseRow> rows = MdSchemaFunctionsRowMapper.rows(serviceWith(sumMetaData()),
                new FunctionTextServiceImpl(), Locale.ENGLISH, restrictions);

        assertThat(rows).isEmpty();
    }

    @Test
    void appliesInterfaceNameRestriction() {
        MdSchemaFunctionsRestrictions restrictions = noRestrictions();
        when(restrictions.interfaceName()).thenReturn(Optional.of(InterfaceNameEnum.NUMERIC));

        List<MdSchemaFunctionsResponseRow> rows = MdSchemaFunctionsRowMapper.rows(serviceWith(sumMetaData()),
                new FunctionTextServiceImpl(), Locale.ENGLISH, restrictions);

        assertThat(rows).hasSize(1);
    }

    @Test
    void localizesDescriptionAndCaption() {
        FunctionTextServiceImpl textService = new FunctionTextServiceImpl();
        textService.addProvider(germanProvider());

        List<MdSchemaFunctionsResponseRow> rows = MdSchemaFunctionsRowMapper.rows(serviceWith(sumMetaData()),
                textService, Locale.of("de", "DE"), noRestrictions());

        MdSchemaFunctionsResponseRow row = rows.get(0);
        assertThat(row.description()).hasValueSatisfying(
                v -> assertThat(v).contains("Summe eines numerischen Ausdrucks"));
        assertThat(row.caption()).contains("Summe");
        // technical identity stays invariant
        assertThat(row.functionName()).contains("Sum");
    }

    @Test
    void mapsLcidToLocale() {
        assertThat(MdSchemaFunctionsRowMapper.localeOf(Optional.of(1031)).getLanguage()).isEqualTo("de");
        assertThat(MdSchemaFunctionsRowMapper.localeOf(Optional.empty())).isEqualTo(Locale.ENGLISH);
        assertThat(MdSchemaFunctionsRowMapper.localeOf(Optional.of(99999))).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void mapsReturnTypesToOleDb() {
        assertThat(MdSchemaFunctionsRowMapper.oleDbTypeOf(DataType.INTEGER)).isEqualTo(3);
        assertThat(MdSchemaFunctionsRowMapper.oleDbTypeOf(DataType.LOGICAL)).isEqualTo(11);
        assertThat(MdSchemaFunctionsRowMapper.oleDbTypeOf(DataType.STRING)).isEqualTo(130);
        assertThat(MdSchemaFunctionsRowMapper.oleDbTypeOf(DataType.SET)).isEqualTo(12);
        assertThat(MdSchemaFunctionsRowMapper.oleDbTypeOf(DataType.DATE_TIME)).isEqualTo(7);
    }

    private static FunctionTextProvider germanProvider() {
        return (key, locale) -> {
            if (!"de".equals(locale.getLanguage()) || !"Sum".equals(key)) {
                return Optional.empty();
            }
            return Optional.of(new FunctionTexts(
                    Optional.of("Liefert die Summe eines numerischen Ausdrucks über eine Menge."),
                    Optional.of("Summe"), Optional.empty(), Optional.empty(), Map.of()));
        };
    }
}
