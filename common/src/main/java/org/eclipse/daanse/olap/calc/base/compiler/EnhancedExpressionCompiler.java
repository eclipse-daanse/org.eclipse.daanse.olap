/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * 2002-2017 Hitachi Vantara.
 * 2006      jhyde
 * 
 * Contributors after Fork in 2023:
 *   Sergei Semenkov (2001)
 *   SmartCity Jena - initial
 */

package org.eclipse.daanse.olap.calc.base.compiler;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.operation.PlainPropertyOperationAtom;
import org.eclipse.daanse.olap.api.Validator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.calc.ResultStyle;
import org.eclipse.daanse.olap.api.calc.TupleCalc;
import org.eclipse.daanse.olap.api.calc.tuple.TupleListCalc;
import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.type.MemberType;
import org.eclipse.daanse.olap.api.type.TupleType;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.type.member.UnknownToMemberCalc;
import org.eclipse.daanse.olap.calc.base.type.tuple.MemberCalcToTupleCalc;
import org.eclipse.daanse.olap.calc.base.type.tuple.UnknownToTupleCalc;
import org.eclipse.daanse.olap.calc.base.type.tuplelist.CopyOfTupleListCalc;
import org.eclipse.daanse.olap.common.Util;

/**
 * Enhanced expression compiler. It can generate code to convert between scalar
 * types.
 *
 * @author jhyde
 * @since Sep 29, 2005
 */
public class EnhancedExpressionCompiler extends BaseExpressionCompiler {
    public EnhancedExpressionCompiler(Evaluator evaluator, Validator validator) {
        super(evaluator, validator);
    }

    public EnhancedExpressionCompiler(Evaluator evaluator, Validator validator, List<ResultStyle> resultStyles) {
        super(evaluator, validator, resultStyles);
    }

    @Override
    public TupleCalc compileTuple(Expression exp) {
        final Calc<?> calc = compile(exp);
        final Type type = exp.getType();
        return switch (type) {
            case org.eclipse.daanse.olap.api.type.DimensionType _, org.eclipse.daanse.olap.api.type.HierarchyType _ -> {
                final org.eclipse.daanse.olap.query.component.UnresolvedFunCallImpl unresolvedFunCall =
                        new org.eclipse.daanse.olap.query.component.UnresolvedFunCallImpl(
                                new PlainPropertyOperationAtom("DefaultMember"), new Expression[] { exp });
                yield compileTuple(unresolvedFunCall.accept(getValidator()));
            }
            case TupleType _ -> calc instanceof TupleCalc tc
                    ? tc
                    : new UnknownToTupleCalc(type, calc);
            case MemberType _ -> {
                final MemberCalc memberCalc = calc instanceof MemberCalc mc
                        ? mc
                        : new UnknownToMemberCalc(type, calc);
                yield new MemberCalcToTupleCalc(type, memberCalc);
            }
            default -> throw Util.newInternal("cannot cast " + exp);
        };
    }

    @Override
    public TupleListCalc compileList(Expression exp, boolean mutable) {
        final TupleListCalc tupleListCalc = super.compileList(exp, mutable);
        if (mutable && tupleListCalc.getResultStyle() == ResultStyle.LIST) {
            // Wrap the expression in an expression which creates a mutable
            // copy.

            return new CopyOfTupleListCalc(tupleListCalc);
        }
        return tupleListCalc;
    }

}
