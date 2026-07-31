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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.EmptyOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.InternalOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.ParenthesesOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionParameter;
import org.eclipse.daanse.olap.api.function.FunctionService;
import org.eclipse.daanse.olap.api.function.FunctionTextService;
import org.eclipse.daanse.olap.api.function.ResolvedFunctionTexts;
import org.eclipse.daanse.xmla.api.common.enums.InterfaceNameEnum;
import org.eclipse.daanse.xmla.api.common.enums.OriginEnum;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsRestrictions;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.ParameterInfo;
import org.eclipse.daanse.xmla.model.record.discover.ParameterInfoR;
import org.eclipse.daanse.xmla.model.record.discover.mdschema.functions.MdSchemaFunctionsResponseRowR;

/**
 * Maps the function metadata of a {@link FunctionService} onto the
 * MDSCHEMA_FUNCTIONS rowset per [MS-SSAS], including the nested PARAMETERINFO
 * rowset and localized display texts.
 */
public final class MdSchemaFunctionsRowMapper {

    /** LCID → Locale for the common XMLA LocaleIdentifier values. */
    private static final Map<Integer, Locale> LCIDS = Map.ofEntries(
            Map.entry(1033, Locale.of("en", "US")), Map.entry(2057, Locale.of("en", "GB")),
            Map.entry(1031, Locale.of("de", "DE")), Map.entry(1036, Locale.of("fr", "FR")),
            Map.entry(1049, Locale.of("ru", "RU")), Map.entry(1034, Locale.of("es", "ES")),
            Map.entry(3082, Locale.of("es", "ES")), Map.entry(1040, Locale.of("it", "IT")),
            Map.entry(1041, Locale.of("ja", "JP")), Map.entry(1046, Locale.of("pt", "BR")),
            Map.entry(2052, Locale.of("zh", "CN")), Map.entry(1043, Locale.of("nl", "NL")),
            Map.entry(1045, Locale.of("pl", "PL")), Map.entry(1029, Locale.of("cs", "CZ")),
            Map.entry(1035, Locale.of("fi", "FI")), Map.entry(1053, Locale.of("sv", "SE")),
            Map.entry(1030, Locale.of("da", "DK")), Map.entry(1038, Locale.of("hu", "HU")),
            Map.entry(1055, Locale.of("tr", "TR")));

    private MdSchemaFunctionsRowMapper() {
    }

    public static Locale localeOf(Optional<Integer> localeIdentifier) {
        return localeIdentifier.map(LCIDS::get).orElse(Locale.ENGLISH);
    }

    public static List<MdSchemaFunctionsResponseRow> rows(FunctionService functionService,
            FunctionTextService textService, Locale locale, MdSchemaFunctionsRestrictions restrictions) {
        List<MdSchemaFunctionsResponseRow> result = new ArrayList<>();
        for (FunctionMetaData fm : functionService.getFunctionMetaDatas()) {
            if (fm.operationAtom() instanceof EmptyOperationAtom
                    || fm.operationAtom() instanceof InternalOperationAtom
                    || fm.operationAtom() instanceof ParenthesesOperationAtom) {
                continue;
            }
            if (!matches(fm, restrictions)) {
                continue;
            }
            result.add(toRow(fm, textService.resolve(fm, locale)));
        }
        return result;
    }

    private static boolean matches(FunctionMetaData fm, MdSchemaFunctionsRestrictions restrictions) {
        if (restrictions == null) {
            return true;
        }
        if (restrictions.functionName().isPresent()
                && !restrictions.functionName().get().equalsIgnoreCase(fm.operationAtom().name())) {
            return false;
        }
        if (restrictions.origin().isPresent()
                && restrictions.origin().get().getValue() != fm.origin().getValue()) {
            return false;
        }
        if (restrictions.interfaceName().isPresent()
                && !restrictions.interfaceName().get().name().equalsIgnoreCase(fm.functionInterface().name())) {
            return false;
        }
        if (restrictions.libraryName().isPresent()
                && !Optional.of(restrictions.libraryName().get()).equals(fm.libraryName())) {
            return false;
        }
        return true;
    }

    private static MdSchemaFunctionsResponseRow toRow(FunctionMetaData fm, ResolvedFunctionTexts texts) {
        String description = texts.description();
        if (description != null) {
            description = description.replace("\r", "");
        }
        FunctionParameter[] parameters = fm.parameters();

        return new MdSchemaFunctionsResponseRowR(//
                Optional.ofNullable(fm.operationAtom().name()), //
                Optional.ofNullable(description), //
                parameterList(parameters, texts), //
                Optional.of(oleDbTypeOf(fm.returnCategory())), //
                Optional.of(originOf(fm)), //
                Optional.of(interfaceNameOf(fm)), //
                fm.libraryName(), //
                Optional.empty(), // DLL_NAME (unused per spec)
                Optional.empty(), // HELP_FILE (unused per spec)
                Optional.empty(), // HELP_CONTEXT (unused per spec)
                fm.callingObject().map(DataType::getPrettyName), //
                Optional.ofNullable(texts.caption()), //
                parameterInfo(parameters, texts), //
                Optional.empty(), // DIRECTQUERY_PUSHABLE (not declared by olap functions yet)
                fm.visualCalculationsInfo() == 0 ? Optional.empty() : Optional.of(fm.visualCalculationsInfo()));
    }

    /** Chevron style per SSAS convention: «Set», [«Numeric Expression»]; repeat groups get an ellipsis. */
    private static String parameterList(FunctionParameter[] parameters, ResolvedFunctionTexts texts) {
        if (parameters == null || parameters.length == 0) {
            return "(none)";
        }
        StringBuilder buf = new StringBuilder(64);
        boolean repeated = false;
        for (int i = 0; i < parameters.length; i++) {
            FunctionParameter parameter = parameters[i];
            if (i > 0) {
                buf.append(", ");
            }
            String display = displayNameOf(parameter, texts);
            boolean opt = parameter.optional() || parameter.skippable();
            if (opt) {
                buf.append('[');
            }
            buf.append('«').append(display).append('»');
            if (opt) {
                buf.append(']');
            }
            repeated |= parameter.repeatable();
        }
        if (repeated) {
            buf.append('…');
        }
        return buf.toString();
    }

    private static Optional<List<ParameterInfo>> parameterInfo(FunctionParameter[] parameters,
            ResolvedFunctionTexts texts) {
        if (parameters == null || parameters.length == 0) {
            return Optional.empty();
        }
        List<ParameterInfo> infos = new ArrayList<>(parameters.length);
        for (FunctionParameter parameter : parameters) {
            String name = parameter.name().orElseGet(() -> parameter.dataType().getPrettyName());
            String parameterDescription = Optional
                    .ofNullable(texts.parameters().get(parameter.name().orElse(null)))
                    .flatMap(ResolvedFunctionTexts.ResolvedParameterTexts::description)
                    .or(parameter::description).orElse("");
            infos.add(new ParameterInfoR(name.replace('_', ' '), parameterDescription, parameter.optional(),
                    parameter.repeatable(), parameter.repeatGroup(), parameter.skippable()));
        }
        return Optional.of(infos);
    }

    private static String displayNameOf(FunctionParameter parameter, ResolvedFunctionTexts texts) {
        Optional<String> localized = Optional.ofNullable(texts.parameters().get(parameter.name().orElse(null)))
                .map(ResolvedFunctionTexts.ResolvedParameterTexts::displayName);
        return localized.orElseGet(() -> parameter.name().orElse(parameter.dataType().getPrettyName()))
                .replace('_', ' ');
    }

    private static OriginEnum originOf(FunctionMetaData fm) {
        return switch (fm.origin()) {
        case MSOLAP -> OriginEnum.MSOLAP;
        case UDF -> OriginEnum.UDF;
        case RELATIONAL -> OriginEnum.RELATIONAL;
        case SCALAR -> OriginEnum.SCALAR;
        };
    }

    private static InterfaceNameEnum interfaceNameOf(FunctionMetaData fm) {
        try {
            return InterfaceNameEnum.valueOf(fm.functionInterface().name());
        } catch (IllegalArgumentException e) {
            return InterfaceNameEnum.OTHER;
        }
    }

    /** OLE DB DBTYPE codes per [MS-SSAS] RETURN_TYPE ("The OLE DB data type"). */
    static int oleDbTypeOf(DataType returnCategory) {
        if (returnCategory == null) {
            return 12; // DBTYPE_VARIANT
        }
        return switch (returnCategory) {
        case INTEGER -> 3; // DBTYPE_I4
        case NUMERIC -> 5; // DBTYPE_R8
        case LOGICAL -> 11; // DBTYPE_BOOL
        case STRING, SYMBOL -> 130; // DBTYPE_WSTR
        case DATE_TIME -> 7; // DBTYPE_DATE
        case EMPTY -> 0; // DBTYPE_EMPTY
        default -> 12; // DBTYPE_VARIANT (set, tuple, member, level, hierarchy, dimension, cube, value, ...)
        };
    }
}
