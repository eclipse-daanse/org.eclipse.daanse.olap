/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.function.core;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionParameter;

public record FunctionParameterR(DataType dataType, Optional<String> name, Optional<String> description,
        Optional<List<String>> reservedWords, boolean optional, boolean repeatable, int repeatGroup,
        boolean skippable) implements FunctionParameter {

    public FunctionParameterR(DataType dataType, Optional<String> name, Optional<String> description,
            Optional<List<String>> reservedWords) {
        this(dataType, name, description, reservedWords, false, false, 0, false);
    }

    public FunctionParameterR(DataType dataType) {
        this(dataType, Optional.empty(), Optional.empty(), Optional.empty());
    }

    public FunctionParameterR(DataType dataType, String name) {
        this(dataType, Optional.ofNullable(name), Optional.empty(), Optional.empty());
    }

    public FunctionParameterR(DataType dataType, String name, String description) {
        this(dataType, Optional.ofNullable(name), Optional.ofNullable(description), Optional.empty());
    }

    public FunctionParameterR(DataType dataType, String name, Optional<List<String>> reservedWords) {
        this(dataType, Optional.ofNullable(name), Optional.empty(), reservedWords);
    }

    /** Factory with the canonical vocabulary name derived from the data type. */
    public static FunctionParameterR param(DataType dataType) {
        return param(dataType, canonicalNameOf(dataType));
    }

    public static FunctionParameterR param(DataType dataType, String name) {
        return new FunctionParameterR(dataType, Optional.ofNullable(name), Optional.empty(), Optional.empty(), false,
                false, 0, false);
    }

    public FunctionParameterR asOptional() {
        return new FunctionParameterR(dataType, name, description, reservedWords, true, repeatable, repeatGroup,
                skippable);
    }

    public FunctionParameterR asSkippable() {
        return new FunctionParameterR(dataType, name, description, reservedWords, optional, repeatable, repeatGroup,
                true);
    }

    public FunctionParameterR repeatable(int group) {
        return new FunctionParameterR(dataType, name, description, reservedWords, optional, true, group, skippable);
    }

    public FunctionParameterR describedAs(String parameterDescription) {
        return new FunctionParameterR(dataType, name, Optional.ofNullable(parameterDescription), reservedWords,
                optional, repeatable, repeatGroup, skippable);
    }

    public FunctionParameterR reserved(String... words) {
        return new FunctionParameterR(dataType, name, description, Optional.of(List.of(words)), optional, repeatable,
                repeatGroup, skippable);
    }

    /** Canonical parameter name per data type, aligned with the MDX reference. */
    public static String canonicalNameOf(DataType dataType) {
        return switch (dataType) {
        case SET -> "Set";
        case MEMBER -> "Member";
        case TUPLE -> "Tuple";
        case NUMERIC -> "Numeric_Expression";
        case INTEGER -> "Index";
        case STRING -> "String_Expression";
        case LOGICAL -> "Logical_Expression";
        case LEVEL -> "Level";
        case HIERARCHY -> "Hierarchy";
        case DIMENSION -> "Dimension";
        case SYMBOL -> "Flag";
        case VALUE -> "Value_Expression";
        case DATE_TIME -> "DateTime";
        case CUBE -> "Cube";
        case ARRAY -> "Array";
        default -> "Expression";
        };
    }
}
