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

package org.eclipse.daanse.olap.api.function;

import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.AmpersandQuotedPropertyOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.MethodOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.PlainPropertyOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.QuotedPropertyOperationAtom;
import org.eclipse.daanse.olap.api.DataType;

public interface FunctionMetaData {

    OperationAtom operationAtom();

    String description();

    DataType returnCategory();

    DataType[] parameterDataTypes();

    FunctionParameter[] parameters();

    /** Logical classification, MDSCHEMA_FUNCTIONS INTERFACE_NAME. */
    default FunctionInterface functionInterface() {
        return FunctionInterface.derivedFrom(returnCategory());
    }

    /** MDSCHEMA_FUNCTIONS ORIGIN. */
    default FunctionOrigin origin() {
        return FunctionOrigin.MSOLAP;
    }

    /** Implementing library, MDSCHEMA_FUNCTIONS LIBRARY_NAME; set for UDF libraries. */
    default Optional<String> libraryName() {
        return Optional.empty();
    }

    /** Display name, MDSCHEMA_FUNCTIONS CAPTION. */
    default String caption() {
        return operationAtom().name();
    }

    /** Bitmask, MDSCHEMA_FUNCTIONS DIRECTQUERY_PUSHABLE; 0 = not declared. */
    default int directQueryPushable() {
        return 0;
    }

    /** Bitmask, MDSCHEMA_FUNCTIONS VISUAL_CALCULATIONS_INFO; 0 = not declared. */
    default int visualCalculationsInfo() {
        return 0;
    }

    /**
     * Stable locale-independent key for localized texts. Type overloads that
     * need distinct texts override with a suffixed key.
     */
    default String textKey() {
        return operationAtom().name();
    }

    /**
     * MDSCHEMA_FUNCTIONS OBJECT column: the type of object this function is
     * called on — the first parameter's type for method and property syntax,
     * empty otherwise.
     */
    default Optional<DataType> callingObject() {
        OperationAtom atom = operationAtom();
        boolean onObject = atom instanceof MethodOperationAtom || atom instanceof PlainPropertyOperationAtom
                || atom instanceof QuotedPropertyOperationAtom
                || atom instanceof AmpersandQuotedPropertyOperationAtom;
        FunctionParameter[] params = parameters();
        if (onObject && params != null && params.length > 0) {
            return Optional.of(params[0].dataType());
        }
        return Optional.empty();
    }

    /** Minimum number of arguments: non-optional, non-skippable parameters, repeat groups counted once. */
    default int minArity() {
        int min = 0;
        int countedGroup = -1;
        for (FunctionParameter parameter : parameters()) {
            if (parameter.optional() || parameter.skippable()) {
                continue;
            }
            int group = parameter.repeatGroup();
            if (group > 0) {
                if (group == countedGroup) {
                    continue;
                }
                countedGroup = group;
            }
            min++;
        }
        return min;
    }

    /** Maximum number of arguments; {@link Integer#MAX_VALUE} if any parameter is repeatable. */
    default int maxArity() {
        FunctionParameter[] params = parameters();
        for (FunctionParameter parameter : params) {
            if (parameter.repeatable()) {
                return Integer.MAX_VALUE;
            }
        }
        return params.length;
    }
}
