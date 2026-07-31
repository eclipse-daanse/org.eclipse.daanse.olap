/*
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.eclipse.daanse.olap.function.core;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionInterface;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionOrigin;

public record FunctionMetaDataR(OperationAtom operationAtom, String description, DataType returnCategory,
        FunctionParameterR[] parameters, FunctionInterface functionInterface, FunctionOrigin origin,
        Optional<String> libraryName, Optional<String> captionOverride, Optional<String> textKeyOverride,
        int directQueryPushable, int visualCalculationsInfo) implements FunctionMetaData {

    public FunctionMetaDataR(OperationAtom operationAtom, String description, DataType returnCategory,
            FunctionParameterR[] parameters) {
        this(operationAtom, description, returnCategory, parameters, FunctionInterface.derivedFrom(returnCategory),
                FunctionOrigin.MSOLAP, Optional.empty(), Optional.empty(), Optional.empty(), 0, 0);
    }

    public static FunctionMetaDataR of(OperationAtom operationAtom, String description, DataType returnCategory,
            FunctionParameterR... parameters) {
        return new FunctionMetaDataR(operationAtom, description, returnCategory, parameters);
    }

    @Override
    public DataType[] parameterDataTypes() {
        return Stream.of(parameters()).map(FunctionParameterR::dataType).toArray(DataType[]::new);
    }

    @Override
    public String caption() {
        return captionOverride.orElseGet(() -> operationAtom().name());
    }

    @Override
    public String textKey() {
        return textKeyOverride.orElseGet(() -> operationAtom().name());
    }

    public FunctionMetaDataR interfaceName(FunctionInterface fi) {
        return new FunctionMetaDataR(operationAtom, description, returnCategory, parameters, fi, origin, libraryName,
                captionOverride, textKeyOverride, directQueryPushable, visualCalculationsInfo);
    }

    public FunctionMetaDataR caption(String caption) {
        return new FunctionMetaDataR(operationAtom, description, returnCategory, parameters, functionInterface, origin,
                libraryName, Optional.ofNullable(caption), textKeyOverride, directQueryPushable,
                visualCalculationsInfo);
    }

    public FunctionMetaDataR origin(FunctionOrigin functionOrigin) {
        return new FunctionMetaDataR(operationAtom, description, returnCategory, parameters, functionInterface,
                functionOrigin, libraryName, captionOverride, textKeyOverride, directQueryPushable,
                visualCalculationsInfo);
    }

    public FunctionMetaDataR library(String library) {
        return new FunctionMetaDataR(operationAtom, description, returnCategory, parameters, functionInterface, origin,
                Optional.ofNullable(library), captionOverride, textKeyOverride, directQueryPushable,
                visualCalculationsInfo);
    }

    public FunctionMetaDataR withTextKey(String textKey) {
        return new FunctionMetaDataR(operationAtom, description, returnCategory, parameters, functionInterface, origin,
                libraryName, captionOverride, Optional.ofNullable(textKey), directQueryPushable,
                visualCalculationsInfo);
    }
}
