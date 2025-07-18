/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2002-2005 Julian Hyde
 * Copyright (C) 2005-2020 Hitachi Vantara and others
 * All Rights Reserved.
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


package org.eclipse.daanse.olap.fun.sort;

import java.util.List;
import java.util.Objects;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.element.Member;

class HierarchicalTupleComparator extends TupleExpMemoComparator {
  private final boolean desc;

  HierarchicalTupleComparator(
    Evaluator evaluator, Calc calc, int arity, boolean desc ) {
    super( evaluator, calc, arity );
    this.desc = desc;
  }

  @Override public int nonEqualCompare( List<Member> a1, List<Member> a2 ) {
    int c = 0;
    final int savepoint = evaluator.savepoint();
    try {
      for ( int i = 0; i < arity; i++ ) {
        Member m1 = a1.get( i );
        Member m2 = a2.get( i );
        c = compareHierarchicallyButSiblingsByValue( m1, m2 );
        if ( c != 0 ) {
          break;
        }
        // compareHierarchicallyButSiblingsByValue imposes a
        // total order
        assert m1.equals( m2 );
        evaluator.setContext( m1 );
      }
    } finally {
      evaluator.restore( savepoint );
    }
    return c;
  }

  protected int compareHierarchicallyButSiblingsByValue(
    Member m1,
    Member m2 ) {
    if ( Objects.equals( m1, m2 ) ) {
      return 0;
    }
    while ( true ) {
      int depth1 = m1.getDepth();
      int depth2 = m2.getDepth();
      if ( depth1 < depth2 ) {
        m2 = m2.getParentMember();
        if ( Objects.equals( m1, m2 ) ) {
          return -1;
        }
      } else if ( depth1 > depth2 ) {
        m1 = m1.getParentMember();
        if ( Objects.equals( m1, m2 ) ) {
          return 1;
        }
      } else {
        Member prev1 = m1;
        Member prev2 = m2;
        m1 = m1.getParentMember();
        m2 = m2.getParentMember();
        if ( Objects.equals( m1, m2 ) ) {
          // including case where both parents are null
          int c = compareByValue( prev1, prev2 );
          if ( c == 0 ) {
            c = Sorter.compareSiblingMembers( prev1, prev2 );
          }
          return desc ? -c : c;
        }
      }
    }
  }

  private int compareByValue( Member m1, Member m2 ) {
    int c;
    final int savepoint = evaluator.savepoint();
    try {
      evaluator.setContext( m1 );
      Object v1 = calc.evaluate( evaluator );
      evaluator.setContext( m2 );
      Object v2 = calc.evaluate( evaluator );
      c = Sorter.compareValues( v1, v2 );
      return c;
    } finally {
      // important to restore the evaluator state
      evaluator.restore( savepoint );
    }
  }
}
