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
package org.eclipse.daanse.olap.xmla.connector.discover;

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
import org.eclipse.daanse.xmla.model.rowset.multidimensional.MdschemaFunctionsRow;
import org.eclipse.daanse.xmla.model.rowset.multidimensional.ParameterInfo;
import org.eclipse.daanse.xmla.model.rowset.multidimensional.RowsetMultidimensionalFactory;
import org.eclipse.emf.ecore.EObject;

/**
 * Maps the function metadata of a {@link FunctionService} onto the
 * MDSCHEMA_FUNCTIONS rowset per [MS-SSAS], including the nested PARAMETERINFO
 * rowset and localized display texts.
 * <p>
 * Ported from the bridge's {@code MdSchemaFunctionsRowMapper}; the rows are
 * model EObjects, and the nested parameters are containment children rather
 * than a record list a converter used to drop.
 */
public final class FunctionRows {

    private static final RowsetMultidimensionalFactory FACTORY = RowsetMultidimensionalFactory.eINSTANCE;

    /** LCID → Locale for the common XMLA LocaleIdentifier values. */
    private static final Map<Integer, Locale> LCIDS = Map.ofEntries(Map.entry(1033, Locale.of("en", "US")),
            Map.entry(2057, Locale.of("en", "GB")), Map.entry(1031, Locale.of("de", "DE")),
            Map.entry(1036, Locale.of("fr", "FR")), Map.entry(1049, Locale.of("ru", "RU")),
            Map.entry(1034, Locale.of("es", "ES")), Map.entry(3082, Locale.of("es", "ES")),
            Map.entry(1040, Locale.of("it", "IT")), Map.entry(1041, Locale.of("ja", "JP")),
            Map.entry(1046, Locale.of("pt", "BR")), Map.entry(2052, Locale.of("zh", "CN")),
            Map.entry(1043, Locale.of("nl", "NL")), Map.entry(1045, Locale.of("pl", "PL")),
            Map.entry(1029, Locale.of("cs", "CZ")), Map.entry(1035, Locale.of("fi", "FI")),
            Map.entry(1053, Locale.of("sv", "SE")), Map.entry(1030, Locale.of("da", "DK")),
            Map.entry(1038, Locale.of("hu", "HU")), Map.entry(1055, Locale.of("tr", "TR")));

    private FunctionRows() {
        // static access only
    }

    public static Locale localeOf(String localeIdentifier) {
        if (localeIdentifier == null || localeIdentifier.isEmpty()) {
            return Locale.ENGLISH;
        }
        Locale locale = LCIDS.get(Integer.valueOf(localeIdentifier));
        return locale == null ? Locale.ENGLISH : locale;
    }

    public static List<EObject> rows(FunctionService functionService, FunctionTextService textService, Locale locale,
            Optional<String> functionName, Optional<String> origin, Optional<String> interfaceName,
            Optional<String> libraryName) {
        List<EObject> result = new ArrayList<>();
        for (FunctionMetaData metaData : functionService.getFunctionMetaDatas()) {
            if (metaData.operationAtom() instanceof EmptyOperationAtom
                    || metaData.operationAtom() instanceof InternalOperationAtom
                    || metaData.operationAtom() instanceof ParenthesesOperationAtom) {
                continue;
            }
            if (functionName.isPresent() && !functionName.get().equalsIgnoreCase(metaData.operationAtom().name())) {
                continue;
            }
            if (origin.isPresent() && Integer.parseInt(origin.get()) != metaData.origin().getValue()) {
                continue;
            }
            if (interfaceName.isPresent()
                    && !interfaceName.get().equalsIgnoreCase(metaData.functionInterface().name())) {
                continue;
            }
            if (libraryName.isPresent() && !Optional.of(libraryName.get()).equals(metaData.libraryName())) {
                continue;
            }
            result.add(row(metaData, textService.resolve(metaData, locale)));
        }
        return result;
    }

    private static EObject row(FunctionMetaData metaData, ResolvedFunctionTexts texts) {
        String description = texts.description();
        if (description != null) {
            description = description.replace("\r", "");
        }
        FunctionParameter[] parameters = metaData.parameters();

        MdschemaFunctionsRow row = FACTORY.createMdschemaFunctionsRow();
        row.setFunctionName(metaData.operationAtom().name());
        if (description != null) {
            row.setDescription(description);
        }
        row.setParameterList(parameterList(parameters, texts));
        row.setReturnType(oleDbTypeOf(metaData.returnCategory()));
        row.setOrigin(metaData.origin().getValue());
        row.setInterfaceName(metaData.functionInterface().name());
        metaData.libraryName().ifPresent(row::setLibraryName);
        metaData.callingObject().map(DataType::getPrettyName).ifPresent(row::setObject);
        if (texts.caption() != null) {
            row.setCaption(texts.caption());
        }
        if (parameters != null) {
            for (FunctionParameter parameter : parameters) {
                row.getParameterInfo().add(parameterInfo(parameter, texts));
            }
        }
        if (metaData.visualCalculationsInfo() != 0) {
            row.setVisualCalculationsInfo(metaData.visualCalculationsInfo());
        }
        return row;
    }

    /**
     * Chevron style per SSAS convention: «Set», [«Numeric Expression»]; repeat
     * groups get an ellipsis.
     */
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
            boolean optional = parameter.optional() || parameter.skippable();
            if (optional) {
                buf.append('[');
            }
            buf.append('«').append(display).append('»');
            if (optional) {
                buf.append(']');
            }
            repeated |= parameter.repeatable();
        }
        if (repeated) {
            buf.append('…');
        }
        return buf.toString();
    }

    private static ParameterInfo parameterInfo(FunctionParameter parameter, ResolvedFunctionTexts texts) {
        String name = parameter.name().orElseGet(() -> parameter.dataType().getPrettyName());
        String description = Optional.ofNullable(texts.parameters().get(parameter.name().orElse(null)))
                .flatMap(ResolvedFunctionTexts.ResolvedParameterTexts::description).or(parameter::description)
                .orElse("");

        ParameterInfo info = FACTORY.createParameterInfo();
        info.setName(name.replace('_', ' '));
        info.setDescription(description);
        info.setOptional(parameter.optional());
        info.setRepeatable(parameter.repeatable());
        info.setRepeatgroup(parameter.repeatGroup());
        info.setSkippable(parameter.skippable() ? 1 : 0);
        return info;
    }

    private static String displayNameOf(FunctionParameter parameter, ResolvedFunctionTexts texts) {
        Optional<String> localized = Optional.ofNullable(texts.parameters().get(parameter.name().orElse(null)))
                .map(ResolvedFunctionTexts.ResolvedParameterTexts::displayName);
        return localized.orElseGet(() -> parameter.name().orElse(parameter.dataType().getPrettyName())).replace('_',
                ' ');
    }

    /** OLE DB DBTYPE codes per [MS-SSAS] RETURN_TYPE ("The OLE DB data type"). */
    static int oleDbTypeOf(DataType returnCategory) {
        if (returnCategory == null) {
            return 12; // DBTYPE_VARIANT
        }
        switch (returnCategory) {
        case INTEGER:
            return 3; // DBTYPE_I4
        case NUMERIC:
            return 5; // DBTYPE_R8
        case LOGICAL:
            return 11; // DBTYPE_BOOL
        case STRING:
        case SYMBOL:
            return 130; // DBTYPE_WSTR
        case DATE_TIME:
            return 7; // DBTYPE_DATE
        case EMPTY:
            return 0; // DBTYPE_EMPTY
        default:
            return 12; // DBTYPE_VARIANT (set, tuple, member, level, ...)
        }
    }
}
