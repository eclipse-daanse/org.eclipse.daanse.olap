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
package org.eclipse.daanse.olap.function.def.ancestor;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedMemberCalc;
import org.eclipse.daanse.olap.fun.FunUtil;

public class AncestorNumericCalc extends AbstractProfilingNestedMemberCalc {

	public AncestorNumericCalc(Type type, MemberCalc memberCalc, IntegerCalc distanceCalc) {
		super(type,  memberCalc, distanceCalc );
	}

	@Override
	public Member evaluate(Evaluator evaluator) {
		Member member = getChildCalc(0, MemberCalc.class).evaluate(evaluator);
		Integer distance = getChildCalc(1, IntegerCalc.class).evaluate(evaluator);
		return FunUtil.ancestor(evaluator, member, distance, null);
	}
}