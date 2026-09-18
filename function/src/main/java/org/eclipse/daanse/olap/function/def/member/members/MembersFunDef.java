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
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.function.def.member.members;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.exception.OlapRuntimeException;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.AbstractFunctionDefinition;

public class MembersFunDef extends AbstractFunctionDefinition {

    // Members(<String Expression>)
    static FunctionOperationAtom functionOperationAtom = new FunctionOperationAtom("Members");
    static FunctionMetaData functionMetaData = new FunctionMetaDataR(functionOperationAtom,
            // The description said "Returns the last child of the parent of a member", which
            // is LastChild and has nothing to do with this function. What is said here is
            // only what the signature already states, because what this function is meant to
            // do beyond that is written down nowhere. The parameter description, text key and
            // caption come from main.
            "Returns the member named by a string expression.", DataType.MEMBER,
            new FunctionParameterR[] {
                    FunctionParameterR.param(DataType.STRING, "String").describedAs("String Expression") })
            .withTextKey("Members.String.Function").caption("Members Function");

    public MembersFunDef() {
        super(functionMetaData);
    }

    /**
     * Refuses the call, by name.
     *
     * <p>This function is registered and resolves, but has never been implemented. It used
     * to throw a bare {@code UnsupportedOperationException}, which tells a caller nothing
     * and is indistinguishable from an accident. It is not implemented here either: what
     * {@code Members(<String>)} is supposed to return, beyond a member, is written down in
     * no place this repository can point at, and guessing it would be worse than refusing.
     */
    @Override
    public Calc<?> compileCall(ResolvedFunCall call, ExpressionCompiler compiler) {
        throw new OlapRuntimeException(
                "Members(<String Expression>) is declared and resolves, but is not implemented."
                        + " Use StrToMember to look a member up by its unique name.");
    }
}
