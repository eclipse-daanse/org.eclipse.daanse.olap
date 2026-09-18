/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.query.base;

import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.Id;
import org.eclipse.daanse.olap.api.query.component.Literal;
import org.eclipse.daanse.olap.api.query.component.MemberExpression;
import org.eclipse.daanse.olap.api.type.MemberType;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.eclipse.daanse.olap.api.type.StringType;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.common.Util;

/**
 * Reads the name and the declared type of a {@code Parameter(...)} call out of its
 * argument list.
 *
 * <p>Both are pure functions over the arguments, so the query layer can use them while
 * collecting parameters, before any function definition has been resolved. They live here
 * rather than on the function definition so that the core does not have to know a concrete
 * {@code FunDef}; the definition delegates to this class.
 */
public final class ParameterExpressions {

    private ParameterExpressions() {
    }

    /**
     * Returns the parameter name, which must be a string constant.
     *
     * @throws org.eclipse.daanse.olap.api.exception.OlapRuntimeException if it is not
     */
    public static String getParameterName(Expression[] args) {
        if (args[0] instanceof Literal firstArgAsLiteral
                && args[0].getCategory() == DataType.STRING) {
            return (String) firstArgAsLiteral.getValue();
        }
        throw Util.newInternal("Parameter name must be a string constant");
    }

    /**
     * Returns an approximate type for a parameter, based upon the 1'th argument. Does not
     * use the default value expression, so this method can safely be used before the
     * expression has been validated.
     */
    public static Type getParameterType(Expression[] args) {
        if (args[1] instanceof Id id) {
            String[] names = id.toStringArray();
            if (names.length == 1) {
                final String name = names[0];
                if ("NUMERIC".equals(name)) {
                    return NumericType.INSTANCE;
                }
                if ("STRING".equals(name)) {
                    return StringType.INSTANCE;
                }
            }
        } else if (args[1] instanceof Literal literal) {
            if ("NUMERIC".equals(literal.getValue())) {
                return NumericType.INSTANCE;
            } else if ("STRING".equals(literal.getValue())) {
                return StringType.INSTANCE;
            }
        } else if (args[1] instanceof MemberExpression) {
            return new MemberType(null, null, null, null);
        }
        return StringType.INSTANCE;
    }
}
