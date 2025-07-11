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
package org.eclipse.daanse.olap.function.def.lastperiods;

import org.eclipse.daanse.olap.api.Validator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.api.type.MemberType;
import org.eclipse.daanse.olap.api.type.SetType;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.function.def.AbstractFunctionDefinition;
import org.eclipse.daanse.olap.function.def.hierarchy.member.HierarchyCurrentMemberFixedCalc;
import org.eclipse.daanse.olap.util.type.TypeUtil;
import org.eclipse.daanse.olap.api.element.Hierarchy;

public class LastPeriodsFunDef extends AbstractFunctionDefinition {

    public LastPeriodsFunDef(FunctionMetaData functionMetaData) {
        super(functionMetaData);
    }

    @Override
    public Type getResultType(Validator validator, Expression[] args) {
        if (args.length == 1) {
            // If Member is not specified,
            // it is Time.CurrentMember.
            Hierarchy defaultTimeHierarchy = validator.getQuery().getCube()
                    .getTimeHierarchy(getFunctionMetaData().operationAtom().name());
            return new SetType(MemberType.forHierarchy(defaultTimeHierarchy));
        } else {
            Type type = args[1].getType();
            Type memberType = TypeUtil.toMemberOrTupleType(type);
            return new SetType(memberType);
        }
    }

    @Override
    public Calc<?> compileCall(ResolvedFunCall call, ExpressionCompiler compiler) {
        // Member defaults to [Time].currentmember
        Expression[] args = call.getArgs();
        final MemberCalc memberCalc;
        if (args.length == 1) {
            final Hierarchy timeHierarchy = compiler.getEvaluator().getCube()
                    .getTimeHierarchy(getFunctionMetaData().operationAtom().name());
            memberCalc = new HierarchyCurrentMemberFixedCalc(call.getType(), timeHierarchy);
        } else {
            memberCalc = compiler.compileMember(args[1]);
        }

        // Numeric Expression.
        final IntegerCalc indexValueCalc = compiler.compileInteger(args[0]);

        return new LastPeriodsCalc(call.getType(), memberCalc, indexValueCalc);
    }
}
