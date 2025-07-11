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
package org.eclipse.daanse.olap.function.def.var;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.calc.todo.TupleListCalc;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedDoubleCalc;
import org.eclipse.daanse.olap.calc.base.util.HirarchyDependsChecker;
import org.eclipse.daanse.olap.calc.base.value.CurrentValueUnknownCalc;
import org.eclipse.daanse.olap.fun.FunUtil;
import org.eclipse.daanse.olap.function.def.aggregate.AbstractAggregateFunDef;

public class VarPFunDef extends AbstractAggregateFunDef {

        public VarPFunDef(FunctionMetaData functionMetaData) {
            super(functionMetaData);
        }

        @Override
        public Calc compileCall( ResolvedFunCall call, ExpressionCompiler compiler) {
            final TupleListCalc tupleListCalc =
                compiler.compileList(call.getArg(0));
            final Calc calc =
                call.getArgCount() > 1
                ? compiler.compileScalar(call.getArg(1), true)
                : new CurrentValueUnknownCalc(call.getType());
            return new AbstractProfilingNestedDoubleCalc(call.getType(), new Calc[] {tupleListCalc, calc}) {
                @Override
                public Double evaluate(Evaluator evaluator) {
                    TupleList memberList = AbstractAggregateFunDef.evaluateCurrentList(tupleListCalc, evaluator);
                    final int savepoint = evaluator.savepoint();
                    try {
                        evaluator.setNonEmpty(false);
                        return
                            (Double) FunUtil.var(evaluator, memberList, calc, true);
                    } finally {
                        evaluator.restore(savepoint);
                    }
                }

                @Override
                public boolean dependsOn(Hierarchy hierarchy) {
                    return HirarchyDependsChecker.checkAnyDependsButFirst(getChildCalcs(), hierarchy);
                }
            };
        }
    }
