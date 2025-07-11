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
package org.eclipse.daanse.olap.calc.base.constant;

import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.type.MemberType;
import org.eclipse.daanse.olap.calc.base.AbstractProfilingConstantCalc;

public class ConstantMemberCalc extends AbstractProfilingConstantCalc<Member> implements MemberCalc {

	public ConstantMemberCalc(MemberType type, Member value) {
		super(value, type);
	}

	public static ConstantMemberCalc of(Member member) {
		return new ConstantMemberCalc(MemberType.forMember(member), member);
	}

}
