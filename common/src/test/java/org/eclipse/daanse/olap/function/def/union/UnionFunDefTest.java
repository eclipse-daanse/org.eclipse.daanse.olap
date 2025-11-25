/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara.  All rights reserved.
 *
 * ---- All changes after Fork in 2023 ------------------------
 *
 * Project: Eclipse daanse
 *
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 */

package org.eclipse.daanse.olap.function.def.union;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.MetaData;
import org.eclipse.daanse.olap.api.element.Property;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.type.SetType;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.ArrayTupleList;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.UnaryTupleList;
import org.eclipse.daanse.olap.element.MemberBase;
import org.eclipse.daanse.olap.function.def.crossjoin.CrossJoinFunDef;
import org.eclipse.daanse.olap.function.def.crossjoin.CrossJoinTest;
import org.eclipse.daanse.olap.function.def.crossjoin.ImmutableListCalc;
import org.eclipse.daanse.olap.query.component.ResolvedFunCallImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

/**
 * Tests for UnionFunDef
 *
 * @author Yury Bakhmutski
 */
class UnionFunDefTest {

  /**
   * Test for MONDRIAN-2250 issue.
   * Tests that the result is independent on the hashCode.
   * For this purpose MemberForTest with rewritten hashCode is used.
   *
   * Tuples are gotten from customer attachments.
   */
  @Test
  void testMondrian2250() {
    Member[] dates = new Member[4];
    for (int i = 25; i < 29; i++) {
      dates[i - 25] =
          new MemberForTest("[Consumption Date.Calendar].[2014-07-" + i + "]");
    }
    List<Member> list = Arrays.asList(dates);
    UnaryTupleList unaryTupleList = new UnaryTupleList(list);

    Member consumptionMethod =
        new MemberForTest("[Consumption Method].[PVR]");
    Member measuresAverageTimeshift =
        new MemberForTest("[Measures].[Average Timeshift]");
    String[] hours = { "00", "14", "15", "16", "23" };
    Member[] times = new Member[5];
    for (int i = 0; i < hours.length; i++) {
      times[i] =
          new MemberForTest("[Consumption Time.Time].[" + hours[i] + ":00]");
    }

    int arity = 3;
    ArrayTupleList arrayTupleList = new ArrayTupleList(arity);
    for (Member time : times) {
      List<Member> currentList = new ArrayList(3);
      currentList.add(consumptionMethod);
      currentList.add(measuresAverageTimeshift);
      currentList.add(time);
      arrayTupleList.add(currentList);
    }

    CrossJoinFunDef crossJoinFunDef =
        new CrossJoinFunDef(new CrossJoinTest.NullFunDef().getFunctionMetaData());
    Expression[] expMock = new Expression[1];
    expMock[0] = mock(Expression.class);
    ResolvedFunCallImpl resolvedFunCall =
        new ResolvedFunCallImpl(mock(FunctionDefinition.class), expMock, mock(SetType.class));
    Calc[] calcs = new Calc[1];
    calcs[0] = Mockito.mock(Calc.class);
    ImmutableListCalc immutableListCalc =
        new ImmutableListCalc(
            resolvedFunCall, calcs, crossJoinFunDef.getCtag());

    TupleList listForUnion1 =
        immutableListCalc.makeList(unaryTupleList, arrayTupleList);

    List<Member> list2 = Arrays.asList(dates);
    UnaryTupleList unaryTupleList2 = new UnaryTupleList(list2);

    Member measuresTotalViewingTime =
        new MemberForTest("[Measures].[Total Viewing Time]");
    ArrayTupleList arrayTupleList2 = new ArrayTupleList(arity);
    for (Member time : times) {
      List<Member> currentList = new ArrayList(3);
      currentList.add(consumptionMethod);
      currentList.add(measuresTotalViewingTime);
      currentList.add(time);
      arrayTupleList2.add(currentList);
    }

    TupleList listForUnion2 =
        immutableListCalc.makeList(unaryTupleList2, arrayTupleList2);

    UnionCalc unionFunDefMock = mock(UnionCalc.class);
    doCallRealMethod().when(unionFunDefMock).union(
    		any(), any(), anyBoolean());

    TupleList tupleList =
        unionFunDefMock.union(listForUnion1, listForUnion2, false);
    System.out.println(tupleList);
    assertEquals(40, tupleList.size());
  }


  private class MemberForTest extends MemberBase {
    private String identifer;

    public MemberForTest(String identifer) {
      this.identifer = identifer;
    }

    @Override
    public String getUniqueName() {
      return identifer;
    }

    @Override
    public int hashCode() {
      return 31;
    }

	@Override
	public void setName(String name) {
	}

	@Override
	public boolean isCalculatedInQuery() {
		return false;
	}

	@Override
	public Object getPropertyValue(String propertyName) {
		return null;
	}

	@Override
	public Object getPropertyValue(String propertyName, boolean matchCase) {
		return null;
	}

	@Override
	public void setProperty(String name, Object value) {
		
	}

	@Override
	public Property[] getProperties() {
		return null;
	}

	@Override
	public int getDepth() {
		return 0;
	}

	@Override
	public int compareTo(Object arg0) {
		return 0;
	}

	@Override
	public MetaData getMetaData() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Object getCaptionValue() {
		return null;
	}

	@Override
	protected Logger getLogger() {
		return null;
	}
  }
}
