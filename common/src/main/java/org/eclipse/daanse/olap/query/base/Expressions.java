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
package org.eclipse.daanse.olap.query.base;

import java.io.PrintWriter;
import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;

public class Expressions {
	public static void unparseExpressions(PrintWriter printWriter, Expression[] expressions, String start, String mid,
			String end) {
		printWriter.print(start);
		boolean first = true;
		for (Expression expression : expressions) {
			if (first) {
				first = false;
			} else {

				printWriter.print(mid);
			}
			expression.unparse(printWriter);
		}
		printWriter.print(end);
	}

	public static Expression[] cloneExpressions(Expression[] expressions) {
		return Stream.of(expressions).map(Expression::cloneExp).toArray(Expression[]::new);
	}

	public static DataType[] categoriesOf(Expression[] expressions) {
		return Stream.of(expressions).map(Expression::getCategory).toArray(DataType[]::new);
	}
	
    public static FunctionParameterR[] functionParameterOf(Expression[] expressions) {
        return Stream.of(expressions).map(e -> new FunctionParameterR(e.getCategory())).toArray(FunctionParameterR[]::new);
    }

    /**
     * Per-call parameters: the declared parameter each argument was bound to,
     * keeping name/description/flags, with the data type replaced by the actual
     * expression category. Falls back to bare category parameters when no
     * binding is available for an argument.
     */
    public static FunctionParameterR[] boundParametersOf(Expression[] expressions,
            org.eclipse.daanse.olap.api.function.FunctionMetaData matched,
            java.util.List<org.eclipse.daanse.olap.api.function.ArgumentBinding> bindings) {
        FunctionParameterR[] result = new FunctionParameterR[expressions.length];
        org.eclipse.daanse.olap.api.function.FunctionParameter[] declared = matched.parameters();
        for (org.eclipse.daanse.olap.api.function.ArgumentBinding binding : bindings) {
            int argIndex = binding.argumentIndex();
            if (argIndex < 0 || argIndex >= result.length) {
                continue;
            }
            org.eclipse.daanse.olap.api.function.FunctionParameter parameter = declared[binding.parameterIndex()];
            result[argIndex] = new FunctionParameterR(expressions[argIndex].getCategory(), parameter.name(),
                    parameter.description(), parameter.reservedWords(), parameter.optional(), parameter.repeatable(),
                    parameter.repeatGroup(), parameter.skippable());
        }
        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) {
                result[i] = new FunctionParameterR(expressions[i].getCategory());
            }
        }
        return result;
    }

}
