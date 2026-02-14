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
package org.eclipse.daanse.olap.function.core.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.daanse.mdx.model.api.expression.operation.BracesOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.CaseOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.CastOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.EmptyOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.InfixOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.InternalOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.MethodOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.ParenthesesOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.PlainPropertyOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.PostfixOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.PrefixOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionParameter;
import org.eclipse.daanse.olap.api.function.FunctionService;
import org.eclipse.daanse.olap.function.core.FunctionPrinter;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

class FunctionMetaDataExportTest {

    private static final Path OUTPUT_DIR = Path.of("target/generated-r");

    @Test
    void exportFunctionMetaData(@InjectService FunctionService functionService) throws Exception {
        List<FunctionMetaData> allMetaData = functionService.getFunctionMetaDatas();
        assertThat(allMetaData).isNotEmpty();

        // Filter out internal
        List<FunctionMetaData> exported = allMetaData.stream()
                .filter(fmd -> !(fmd.operationAtom() instanceof EmptyOperationAtom))
                .filter(fmd -> !(fmd.operationAtom() instanceof InternalOperationAtom))
                .filter(fmd -> !(fmd.operationAtom() instanceof ParenthesesOperationAtom))
                .filter(fmd -> !(fmd.operationAtom() instanceof BracesOperationAtom)).toList();

        assertThat(exported).isNotEmpty();

        Files.createDirectories(OUTPUT_DIR);

        Map<String, FunctionGroup> functionGroups = groupFunctions(exported);

        Map<String, List<FunctionGroup>> categorized = categorize(functionGroups);

        for (Map.Entry<String, List<FunctionGroup>> entry : categorized.entrySet()) {
            String category = entry.getKey();
            List<FunctionGroup> functions = entry.getValue();
            String rCode = generateRFile(category, functions);
            Path rFile = OUTPUT_DIR.resolve("mdx-functions-" + category + ".R");
            Files.writeString(rFile, rCode);
        }
    }

    record ParameterInfo(DataType dataType, String name, String description, List<String> reservedWords) {
    }

    record Overload(List<ParameterInfo> parameters, String signature) {
    }

    static class FunctionGroup {
        String name;
        String operationAtomType;
        String description;
        DataType returnType;
        List<Overload> overloads = new ArrayList<>();

        String key() {
            return name + "|" + operationAtomType;
        }
    }

    private Map<String, FunctionGroup> groupFunctions(List<FunctionMetaData> metaDataList) {
        Map<String, FunctionGroup> groups = new LinkedHashMap<>();

        for (FunctionMetaData fmd : metaDataList) {
            String name = fmd.operationAtom().name();
            String atomType = operationAtomTypeName(fmd.operationAtom());
            String key = name + "|" + atomType;

            FunctionGroup group = groups.computeIfAbsent(key, k -> {
                FunctionGroup g = new FunctionGroup();
                g.name = name;
                g.operationAtomType = atomType;
                g.description = fmd.description();
                g.returnType = fmd.returnCategory();
                return g;
            });

            List<ParameterInfo> params = new ArrayList<>();
            FunctionParameter[] fmdParams = fmd.parameters();
            if (fmdParams != null) {
                for (FunctionParameter fp : fmdParams) {
                    params.add(new ParameterInfo(fp.dataType(), fp.name().orElse(null), fp.description().orElse(null),
                            fp.reservedWords().orElse(null)));
                }
            }

            String signature = FunctionPrinter.getSignature(fmd);
            group.overloads.add(new Overload(params, signature));
        }

        return groups;
    }

    private String operationAtomTypeName(OperationAtom atom) {
        return switch (atom) {
        case FunctionOperationAtom a -> "FunctionOperationAtom";
        case MethodOperationAtom a -> "MethodOperationAtom";
        case PlainPropertyOperationAtom a -> "PlainPropertyOperationAtom";
        case InfixOperationAtom a -> "InfixOperationAtom";
        case PrefixOperationAtom a -> "PrefixOperationAtom";
        case PostfixOperationAtom a -> "PostfixOperationAtom";
        case CaseOperationAtom a -> "CaseOperationAtom";
        case CastOperationAtom a -> "CastOperationAtom";
        default -> atom.getClass().getSimpleName();
        };
    }

    // --- Categorization ---

    private static final Map<String, Set<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    static {
        CATEGORY_KEYWORDS.put("set-operations",
                Set.of("Union", "Except", "CrossJoin", "Intersect", "Filter", "Distinct", "Exists", "NonEmpty",
                        "NonEmptyCrossJoin", "Hierarchize", "Generate", "Unorder", "NativizeSet", "Existing"));
        CATEGORY_KEYWORDS.put("member-navigation",
                Set.of("Parent", "Children", "Ancestors", "Ancestor", "Descendants", "Siblings", "FirstChild",
                        "LastChild", "FirstSibling", "LastSibling", "PrevMember", "NextMember", "Lead", "Lag", "Cousin",
                        "DataMember", "DefaultMember", "ValidMeasure", "StrToMember", "CurrentMember",
                        "CalculatedChild", "Ascendants"));
        CATEGORY_KEYWORDS.put("aggregate", Set.of("Sum", "Avg", "Count", "Min", "Max", "Median", "Aggregate"));
        CATEGORY_KEYWORDS.put("statistical",
                Set.of("Stdev", "StdevP", "Var", "VarP", "Correlation", "Covariance", "LinRegIntercept", "LinRegPoint",
                        "LinRegR2", "LinRegSlope", "LinRegVariance", "Percentile", "NthQuartile"));
        CATEGORY_KEYWORDS.put("operators", Set.of("+", "-", "*", "/", "AND", "OR", "NOT", "XOR", "=", "<>", "<", ">",
                "<=", ">=", "IS", "IS EMPTY", "IS NULL", "MATCHES", "NOT MATCHES"));
        CATEGORY_KEYWORDS.put("type-introspection",
                Set.of("Dimension", "Hierarchy", "Level", "Name", "UniqueName", "Unique_Name", "Caption", "Properties",
                        "Ordinal", "Value", "Dimensions", "Levels", "Members", "AllMembers"));
        CATEGORY_KEYWORDS.put("ranking", Set.of("Rank", "Order", "TopCount", "BottomCount", "TopPercent",
                "BottomPercent", "TopSum", "BottomSum"));
        CATEGORY_KEYWORDS.put("set-construction", Set.of("Head", "Tail", "Subset", "Item", "Extract",
                "AddCalculatedMembers", "StripCalculatedMembers", "SetToStr", "Range", "As"));
        CATEGORY_KEYWORDS.put("time", Set.of("PeriodsToDate", "ParallelPeriod", "OpeningPeriod", "ClosingPeriod",
                "LastPeriods", "Ytd", "Qtd", "Mtd", "Wtd"));
        CATEGORY_KEYWORDS.put("conditional",
                Set.of("IIf", "CoalesceEmpty", "_CaseTest", "_CaseMatch", "IsEmpty", "IsNull", "Is", "Cast"));
        CATEGORY_KEYWORDS.put("drill", Set.of("DrilldownLevel", "DrilldownLevelTop", "DrilldownLevelBottom",
                "DrilldownMember", "ToggleDrillState", "Drillthrough"));
        CATEGORY_KEYWORDS.put("string", Set.of("Len", "UCase", "LCase", "Format", "TupleToStr", "MemberToStr",
                "StrToSet", "StrToTuple", "SetToStr"));
        CATEGORY_KEYWORDS.put("vba",
                Set.of("Abs", "Atn", "CBool", "CByte", "CDate", "CDbl", "Chr", "ChrB", "ChrW", "CInt", "Cos", "Date",
                        "DateAdd", "DateDiff", "DatePart", "DateSerial", "DateValue", "Day", "DDB", "Exp", "Fix",
                        "FormatCurrency", "FormatDateTime", "FormatNumber", "FormatPercent", "FV", "Hex", "Hour",
                        "InStr", "InStrRev", "Int", "IPmt", "IRR", "IsArray", "IsDate", "IsError", "IsMissing",
                        "IsNull", "IsNumeric", "IsObject", "LCase", "Left", "Log", "LTrim", "Mid", "Minute", "MIRR",
                        "Month", "MonthName", "Now", "NPer", "NPV", "Oct", "Pmt", "PPmt", "PV", "Rate", "Replace",
                        "Right", "Round", "RTrim", "Second", "Sgn", "Sin", "SLN", "Space", "Sqr", "Str", "StrComp",
                        "String", "StrReverse", "SYD", "Tan", "Time", "Timer", "TimeSerial", "TimeValue", "Trim",
                        "TypeName", "Val", "Weekday", "WeekdayName", "Year", "Asc"));
        CATEGORY_KEYWORDS.put("excel", Set.of("ACos", "ACosh", "ASin", "ASinh", "ATan2", "ATanh", "Cosh", "Degrees",
                "Log10", "Mod", "Pi", "Power", "Radians", "Sinh", "SqrtPi", "Tanh"));
        CATEGORY_KEYWORDS.put("udf",
                Set.of("CurrentDateMember", "CurrentDateString", "InUdf", "LastNonEmpty", "Matches", "NullValue"));
    }

    private Map<String, List<FunctionGroup>> categorize(Map<String, FunctionGroup> functionGroups) {
        Map<String, List<FunctionGroup>> result = new TreeMap<>();

        for (FunctionGroup g : functionGroups.values()) {
            String category = findCategory(g);
            result.computeIfAbsent(category, k -> new ArrayList<>()).add(g);
        }

        for (List<FunctionGroup> list : result.values()) {
            list.sort(Comparator.comparing(g -> g.name.toLowerCase()));
        }

        return result;
    }

    private String findCategory(FunctionGroup g) {
        for (Map.Entry<String, Set<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (entry.getValue().contains(g.name)) {
                return entry.getKey();
            }
        }
        if (g.operationAtomType.equals("InfixOperationAtom") || g.operationAtomType.equals("PrefixOperationAtom")
                || g.operationAtomType.equals("PostfixOperationAtom")) {
            return "operators";
        }
        return "other";
    }

    private String generateRFile(String category, List<FunctionGroup> functions) {
        StringBuilder sb = new StringBuilder();
        sb.append("#' @title MDX ").append(categoryTitle(category)).append(" (auto-generated)\n");
        sb.append("#' @description Auto-generated MDX function wrappers for ").append(category).append(".\n");
        sb.append("#' DO NOT EDIT - this file is generated by FunctionMetaDataExportTest.\n\n");

        for (FunctionGroup g : functions) {
            String rFunction = generateRFunction(g, category);
            if (rFunction != null) {
                sb.append(rFunction).append("\n");
            }
        }

        return sb.toString();
    }

    private String categoryTitle(String category) {
        return switch (category) {
        case "set-operations" -> "Set Operations";
        case "member-navigation" -> "Member Navigation";
        case "aggregate" -> "Aggregate Functions";
        case "statistical" -> "Statistical Functions";
        case "operators" -> "Operators";
        case "type-introspection" -> "Type Introspection Functions";
        case "ranking" -> "Ranking Functions";
        case "set-construction" -> "Set Construction Functions";
        case "time" -> "Time Functions";
        case "conditional" -> "Conditional Functions";
        case "drill" -> "Drill Functions";
        case "string" -> "String Functions";
        case "vba" -> "VBA Functions";
        case "excel" -> "Excel Functions";
        case "udf" -> "User-Defined Functions";
        default -> category + " Functions";
        };
    }

    private String generateRFunction(FunctionGroup g, String category) {
        // Skip internal/case functions that don't map well to R
        if (g.name.startsWith("_"))
            return null;

        return switch (g.operationAtomType) {
        case "FunctionOperationAtom" -> generateFunctionAtomR(g, category);
        case "MethodOperationAtom" -> generateMethodAtomR(g, category);
        case "PlainPropertyOperationAtom" -> generatePropertyAtomR(g, category);
        case "InfixOperationAtom" -> generateInfixAtomR(g, category);
        case "PrefixOperationAtom" -> generatePrefixAtomR(g, category);
        case "PostfixOperationAtom" -> generatePostfixAtomR(g, category);
        case "CastOperationAtom" -> generateCastAtomR(g, category);
        default -> null;
        };
    }

    // FunctionOperationAtom: Name(arg1, arg2, ...)
    private String generateFunctionAtomR(FunctionGroup g, String category) {
        String rName = toRName(g, category);
        MergedParams merged = mergeOverloads(g);

        StringBuilder sb = new StringBuilder();
        appendRoxygen(sb, g, merged, category);
        sb.append(rName).append(" <- function(");
        sb.append(rParamList(merged));
        sb.append(") {\n");
        sb.append(rFunctionBody(g.name, merged));
        sb.append("}\n");

        return sb.toString();
    }

    // MethodOperationAtom: object.Method(arg1, ...)
    private String generateMethodAtomR(FunctionGroup g, String category) {
        String rName = toRName(g, category);
        MergedParams merged = mergeOverloads(g);

        if (merged.params.isEmpty())
            return null;

        StringBuilder sb = new StringBuilder();
        appendRoxygen(sb, g, merged, category);
        sb.append(rName).append(" <- function(");
        sb.append(rParamList(merged));
        sb.append(") {\n");

        String receiverParam = merged.params.get(0).rName;
        sb.append("  args <- \"\"");
        if (merged.params.size() > 1) {
            sb.append("\n");
            for (int i = 1; i < merged.params.size(); i++) {
                MergedParam p = merged.params.get(i);
                if (p.optional) {
                    sb.append("  if (!is.null(").append(p.rName).append(")) {\n");
                    sb.append("    args <- if (nchar(args) > 0) paste0(args, \", \", ").append(p.rName)
                            .append(") else ").append(p.rName).append("\n");
                    sb.append("  }\n");
                } else {
                    sb.append("  args <- if (nchar(args) > 0) paste0(args, \", \", ").append(p.rName).append(") else ")
                            .append(p.rName).append("\n");
                }
            }
        }
        sb.append("  paste0(").append(receiverParam).append(", \".").append(g.name).append("(\", args, \")\")\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String generatePropertyAtomR(FunctionGroup g, String category) {
        String rName = toRName(g, category);
        MergedParams merged = mergeOverloads(g);

        if (merged.params.isEmpty())
            return null;
        String receiverParam = merged.params.get(0).rName;

        StringBuilder sb = new StringBuilder();
        appendRoxygen(sb, g, merged, category);
        sb.append(rName).append(" <- function(").append(receiverParam).append(") {\n");
        sb.append("  paste0(").append(receiverParam).append(", \".").append(g.name).append("\")\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String generateInfixAtomR(FunctionGroup g, String category) {
        String rName = toRName(g, category);
        MergedParams merged = mergeOverloads(g);

        if (merged.params.size() < 2)
            return null;

        StringBuilder sb = new StringBuilder();
        appendRoxygen(sb, g, merged, category);
        sb.append(rName).append(" <- function(").append(merged.params.get(0).rName).append(", ")
                .append(merged.params.get(1).rName).append(") {\n");
        sb.append("  paste0(\"(\", ").append(merged.params.get(0).rName).append(", \" ").append(g.name).append(" \", ")
                .append(merged.params.get(1).rName).append(", \")\")\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String generatePrefixAtomR(FunctionGroup g, String category) {
        String rName = toRName(g, category);
        MergedParams merged = mergeOverloads(g);

        if (merged.params.isEmpty())
            return null;
        String param = merged.params.get(0).rName;

        StringBuilder sb = new StringBuilder();
        appendRoxygen(sb, g, merged, category);
        sb.append(rName).append(" <- function(").append(param).append(") {\n");
        sb.append("  paste0(\"").append(g.name).append(" \", ").append(param).append(")\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String generatePostfixAtomR(FunctionGroup g, String category) {
        String rName = toRName(g, category);
        MergedParams merged = mergeOverloads(g);

        if (merged.params.isEmpty())
            return null;
        String param = merged.params.get(0).rName;

        StringBuilder sb = new StringBuilder();
        appendRoxygen(sb, g, merged, category);
        sb.append(rName).append(" <- function(").append(param).append(") {\n");
        sb.append("  paste0(").append(param).append(", \" ").append(g.name).append("\")\n");
        sb.append("}\n");

        return sb.toString();
    }

    // CastOperationAtom: CAST(expr AS type)
    private String generateCastAtomR(FunctionGroup g, String category) {
        StringBuilder sb = new StringBuilder();
        sb.append("#' MDX CAST function\n");
        sb.append("#'\n");
        sb.append("#' Casts an expression to a specified type.\n");
        sb.append("#'\n");
        sb.append("#' @param expression An MDX expression.\n");
        sb.append("#' @param type The target type.\n");
        sb.append("#' @return MDX string.\n");
        sb.append("#' @family ").append(category).append("\n");
        sb.append("#' @export\n");
        sb.append("mdx_cast <- function(expression, type) {\n");
        sb.append("  paste0(\"CAST(\", expression, \" AS \", type, \")\")\n");
        sb.append("}\n");

        return sb.toString();
    }

    record MergedParam(String rName, DataType dataType, boolean optional, String description,
            List<String> reservedWords) {
    }

    record MergedParams(List<MergedParam> params) {
    }

    private MergedParams mergeOverloads(FunctionGroup g) {
        if (g.overloads.isEmpty())
            return new MergedParams(List.of());

        List<Overload> sorted = g.overloads.stream().sorted(Comparator.comparingInt(o -> o.parameters().size()))
                .toList();

        int minParams = sorted.get(0).parameters().size();
        int maxParams = sorted.get(sorted.size() - 1).parameters().size();

        List<MergedParam> result = new ArrayList<>();
        Set<String> usedNames = new java.util.HashSet<>();

        for (int i = 0; i < maxParams; i++) {
            boolean optional = i >= minParams;

            DataType dataType = DataType.UNKNOWN;
            String name = null;
            String description = null;
            List<String> reservedWords = null;

            for (Overload ov : sorted) {
                if (i < ov.parameters().size()) {
                    ParameterInfo pi = ov.parameters().get(i);
                    if (dataType == DataType.UNKNOWN || dataType == DataType.EMPTY) {
                        dataType = pi.dataType();
                    }
                    if (name == null && pi.name() != null) {
                        name = pi.name();
                    }
                    if (description == null && pi.description() != null) {
                        description = pi.description();
                    }
                    if (reservedWords == null && pi.reservedWords() != null) {
                        reservedWords = pi.reservedWords();
                    }
                }
            }

            // Generate R parameter name
            String rName = deriveRParamName(name, dataType, i, usedNames);
            usedNames.add(rName);

            result.add(new MergedParam(rName, dataType, optional, description, reservedWords));
        }

        return new MergedParams(result);
    }

    private String deriveRParamName(String metadataName, DataType dataType, int position, Set<String> usedNames) {
        String candidate;
        if (metadataName != null && !metadataName.isBlank()) {
            candidate = toSnakeCase(metadataName);
        } else {
            candidate = switch (dataType) {
            case MEMBER -> "member";
            case SET -> "set_expr";
            case NUMERIC -> "numeric_expr";
            case STRING -> "string_expr";
            case LEVEL -> "level";
            case HIERARCHY -> "hierarchy";
            case DIMENSION -> "dimension";
            case LOGICAL -> "logical_expr";
            case TUPLE -> "tuple";
            case SYMBOL -> "flag";
            case INTEGER -> "integer_expr";
            case DATE_TIME -> "datetime_expr";
            case VALUE -> "value_expr";
            case CUBE -> "cube";
            default -> "arg" + (position + 1);
            };
        }

        if (usedNames.contains(candidate)) {
            candidate = candidate + "_" + (position + 1);
        }

        if (Set.of("if", "else", "for", "while", "repeat", "function", "in", "next", "break", "TRUE", "FALSE", "NULL",
                "NA", "Inf", "NaN").contains(candidate)) {
            candidate = candidate + "_val";
        }

        return candidate;
    }

    private String rParamList(MergedParams merged) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < merged.params.size(); i++) {
            if (i > 0)
                sb.append(", ");
            MergedParam p = merged.params.get(i);
            sb.append(p.rName);
            if (p.optional) {
                sb.append(" = NULL");
            }
        }
        return sb.toString();
    }

    private String rFunctionBody(String funcName, MergedParams merged) {
        StringBuilder sb = new StringBuilder();

        if (merged.params.isEmpty()) {
            sb.append("  paste0(\"").append(funcName).append("()\")\n");
            return sb.toString();
        }

        int firstOptional = -1;
        for (int i = 0; i < merged.params.size(); i++) {
            if (merged.params.get(i).optional) {
                firstOptional = i;
                break;
            }
        }

        if (firstOptional == -1) {
            sb.append("  paste0(\"").append(funcName).append("(\", ");
            for (int i = 0; i < merged.params.size(); i++) {
                if (i > 0)
                    sb.append(", \", \", ");
                sb.append(merged.params.get(i).rName);
            }
            sb.append(", \")\")\n");
        } else {
            sb.append("  args <- paste0(");
            for (int i = 0; i < firstOptional; i++) {
                if (i > 0)
                    sb.append(", \", \", ");
                sb.append(merged.params.get(i).rName);
            }
            sb.append(")\n");

            for (int i = firstOptional; i < merged.params.size(); i++) {
                MergedParam p = merged.params.get(i);
                sb.append("  if (!is.null(").append(p.rName).append(")) args <- paste0(args, \", \", ").append(p.rName)
                        .append(")\n");
            }

            sb.append("  paste0(\"").append(funcName).append("(\", args, \")\")\n");
        }

        return sb.toString();
    }

    private void appendRoxygen(StringBuilder sb, FunctionGroup g, MergedParams merged, String category) {
        sb.append("#' MDX ").append(g.name).append(" function\n");
        sb.append("#'\n");
        if (g.description != null && !g.description.isBlank()) {
            String desc = g.description.replace("\r", "").replace("\n", " ");
            sb.append("#' ").append(desc).append("\n");
        }
        sb.append("#'\n");

        for (MergedParam p : merged.params) {
            sb.append("#' @param ").append(p.rName).append(" ");
            if (p.description != null && !p.description.isBlank()) {
                sb.append(p.description);
            } else {
                sb.append("A ").append(p.dataType.getPrittyName()).append(" expression");
                if (p.optional)
                    sb.append(" (optional)");
            }
            sb.append(".\n");
        }

        sb.append("#' @return MDX string returning ").append(g.returnType.getPrittyName()).append(".\n");
        sb.append("#' @family ").append(category).append("\n");
        sb.append("#' @export\n");
    }

    private String toRName(FunctionGroup g, String category) {
        String base = toSnakeCase(g.name);

        if (g.operationAtomType.equals("PlainPropertyOperationAtom")
                || g.operationAtomType.equals("MethodOperationAtom")) {

            if (!g.overloads.isEmpty()) {
                DataType receiverType = g.overloads.get(0).parameters().isEmpty() ? null
                        : g.overloads.get(0).parameters().get(0).dataType();

                if (receiverType != null && needsTypePrefix(g.name)) {
                    String typePrefix = receiverType.getName();
                    return "mdx_" + typePrefix + "_" + base;
                }
            }
        }

        return "mdx_" + base;
    }

    private boolean needsTypePrefix(String name) {
        return Set.of("Name", "UniqueName", "Unique_Name", "Caption", "Properties", "Dimension", "Hierarchy", "Level",
                "Ordinal", "Count", "Members", "AllMembers", "CurrentMember", "DefaultMember").contains(name);
    }

    private String toSnakeCase(String camelCase) {
        if (camelCase == null)
            return "unknown";

        // Handle special characters in operator names
        return switch (camelCase) {
        case "+" -> "plus";
        case "-" -> "minus";
        case "*" -> "multiply";
        case "/" -> "divide";
        case "AND" -> "and";
        case "OR" -> "or";
        case "NOT" -> "not";
        case "XOR" -> "xor";
        case "=" -> "equal";
        case "<>" -> "not_equal";
        case "<" -> "less";
        case ">" -> "greater";
        case "<=" -> "less_equal";
        case ">=" -> "greater_equal";
        case "IS" -> "is";
        case "IS EMPTY" -> "is_empty";
        case "IS NULL" -> "is_null";
        default -> {
            String sanitized = camelCase.replace(" ", "_").replaceAll("[^a-zA-Z0-9_]", "_");
            // CamelCase to snake_case
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sanitized.length(); i++) {
                char c = sanitized.charAt(i);
                if (Character.isUpperCase(c) && i > 0 && sanitized.charAt(i - 1) != '_'
                        && !Character.isUpperCase(sanitized.charAt(i - 1))) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            }
            yield sb.toString().replaceAll("_+", "_").replaceAll("^_|_$", "");
        }
        };
    }
}
