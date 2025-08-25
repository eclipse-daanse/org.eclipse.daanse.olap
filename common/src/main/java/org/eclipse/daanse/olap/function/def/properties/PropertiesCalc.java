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
package org.eclipse.daanse.olap.function.def.properties;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedUnknownCalc;
import org.eclipse.daanse.olap.common.SystemWideProperties;
import org.eclipse.daanse.olap.common.Util;
import org.eclipse.daanse.olap.fun.MondrianEvaluationException;

public class PropertiesCalc extends AbstractProfilingNestedUnknownCalc {

    private MemberCalc memberCalc;
    private StringCalc stringCalc;

    protected PropertiesCalc(Type type, MemberCalc memberCalc, StringCalc stringCalc) {
        super(type);
        this.memberCalc = memberCalc;
        this.stringCalc = stringCalc;
    }

    @Override
    public Object evaluate(Evaluator evaluator) {
        Member member = memberCalc.evaluate(evaluator);
        String propertyKey = stringCalc.evaluate(evaluator);
        return properties(member, propertyKey);
    }

    private static Object properties(Member member, String s) {
        boolean matchCase = SystemWideProperties.instance().CaseSensitive;
        Object o = member.getPropertyValue(s, matchCase);
        if (o == null) {
            if (!Util.isValidProperty(s, member.getLevel())) {
                throw new MondrianEvaluationException(new StringBuilder("Property '").append(s)
                        .append("' is not valid for member '").append(member).append("'").toString());
            }
        }
        return o;
    }

    @Override
    public Calc<?>[] getChildCalcs() {
        return new Calc[] { memberCalc, stringCalc };
    }

}
