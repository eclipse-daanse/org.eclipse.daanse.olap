/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2002-2005 Julian Hyde
 * Copyright (C) 2005-2020 Hitachi Vantara and others
 * Copyright (C) 2021 Sergei Semenkov
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

import static org.eclipse.daanse.olap.common.Util.newInternal;
import static org.eclipse.daanse.olap.fun.FunUtil.DOUBLE_NULL;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.Execution;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.todo.TupleCursor;
import org.eclipse.daanse.olap.api.calc.todo.TupleIterable;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.element.LimitedMember;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.type.ScalarType;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.DelegatingTupleList;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.TupleCollections;
import org.eclipse.daanse.olap.common.SystemWideProperties;
import org.eclipse.daanse.olap.common.Util;
import org.eclipse.daanse.olap.function.def.member.memberorderkey.MemberOrderKeyCalc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.daanse.olap.util.CancellationChecker;

@SuppressWarnings( "squid:S4274" )
public class Sorter {

  private static final String SORT_TIMING_NAME = "Sort";
  private static final String SORT_EVAL_TIMING_NAME = "EvalForSort";

  private static final Logger LOGGER = LoggerFactory.getLogger( Sorter.class );

  /**
   * For each member in a list, evaluates an expression and creates a map from members to values.
   *
   * If the list contains tuples, use
   * {@link #evaluateTuples(org.eclipse.daanse.olap.api.Evaluator, mondrian.calc.Calc, org.eclipse.daanse.olap.api.calc.todo.TupleList)}.
   *
   * @param evaluator  Evaluation context
   * @param exp        Expression to evaluate
   * @param memberIter Iterable over the collection of members
   * @param memberList List to be populated with members, or null
   * @param parentsToo If true, evaluate the expression for all ancestors of the members as well exp != null
   *                   exp.getType() instanceof ScalarType
   */
  static Map<Member, Object> evaluateMembers(
    Evaluator evaluator,
    Calc exp,
    Iterable<Member> memberIter,
    List<Member> memberList,
    boolean parentsToo ) {
    final int savepoint = evaluator.savepoint();
    try {
      assert exp.getType() instanceof ScalarType;
      Map<Member, Object> mapMemberToValue = new HashMap<>();
      for ( Member member : memberIter ) {
        if ( memberList != null ) {
          memberList.add( member );
        }
        while ( true ) {
          evaluator.setContext( member );
          Object result = exp.evaluate( evaluator );
          if ( result == null ) {
            result = Util.nullValue;
          }
          mapMemberToValue.put( member, result );
          if ( !parentsToo ) {
            break;
          }
          member = member.getParentMember();
          if ( member == null ) {
            break;
          }
          if ( mapMemberToValue.containsKey( member ) ) {
            break;
          }
        }
      }
      return mapMemberToValue;
    } finally {
      evaluator.restore( savepoint );
    }
  }

  /**
   * For each tuple in a list, evaluates an expression and creates a map from tuples to values.
   *
   * @param evaluator Evaluation context
   * @param exp       Expression to evaluate
   * @param tuples    List of tuples exp != null exp.getType() instanceof ScalarType
   */
  public static Map<List<Member>, Object> evaluateTuples(
    Evaluator evaluator,
    Calc exp,
    TupleList tuples ) {
    final int savepoint = evaluator.savepoint();

    try {
      assert exp.getType() instanceof ScalarType;
      final Map<List<Member>, Object> mapMemberToValue =
        new HashMap<>();
      for ( List<Member> tuple : tuples ) {
        evaluator.setContext( tuple );
        Object result = exp.evaluate( evaluator );
        if ( result == null ) {
          result = Util.nullValue;
        }
        mapMemberToValue.put( tuple, result );
      }
      return mapMemberToValue;
    } finally {
      evaluator.restore( savepoint );
    }
  }

  /**
   * Helper function to sort a list of members according to an expression.
   *
   * NOTE: This function does not preserve the contents of the validator.
   *
   * If you do not specify {@code memberList}, the method
   * will build its own member list as it iterates over {@code memberIter}. It is acceptable if {@code memberList} and
   * {@code memberIter} are the same list object.
   *
   * If you specify {@code memberList}, the list is sorted in place, and
   * memberList is returned.
   *
   * @param evaluator  Evaluator
   * @param memberIter Iterable over members
   * @param memberList List of members
   * @param exp        Expression to sort on
   * @param desc       Whether to sort descending
   * @param brk        Whether to break
   * @return sorted list (never null)
   */
  public static List<Member> sortMembers(
    Evaluator evaluator,
    Iterable<Member> memberIter,
    List<Member> memberList,
    Calc exp,
    boolean desc,
    boolean brk ) {
    if ( ( memberList != null ) && ( memberList.size() <= 1 ) ) {
      return memberList;
    }

    evaluator.getTiming().markStart( SORT_EVAL_TIMING_NAME );
    boolean timingEval = true;
    boolean timingSort = false;
    try {
      // REVIEW mberkowitz 1/09: test whether precomputing
      // values saves time.
      Map<Member, Object> mapMemberToValue;
      final boolean parentsToo = !brk;
      if ( memberList == null ) {
        memberList = new ArrayList<>();
        mapMemberToValue = evaluateMembers(
          evaluator, exp, memberIter, memberList, parentsToo );
      } else {
        mapMemberToValue = evaluateMembers(
          evaluator, exp, memberIter, null, parentsToo );
      }

      MemberComparator comp;
      if ( brk ) {
        comp = new MemberComparator.BreakMemberComparator( evaluator, exp, desc );
      } else {
        comp = new MemberComparator.HierarchicalMemberComparator( evaluator, exp, desc );
      }
      comp.preloadValues( mapMemberToValue );
      evaluator.getTiming().markEnd( SORT_EVAL_TIMING_NAME );
      timingEval = false;
      evaluator.getTiming().markStart( SORT_TIMING_NAME );
      timingSort = true;
      Collections.sort( memberList, comp.wrap() );
      return memberList;
    } finally {
      if ( timingEval ) {
        evaluator.getTiming().markEnd( SORT_EVAL_TIMING_NAME );
      } else if ( timingSort ) {
        evaluator.getTiming().markEnd( SORT_TIMING_NAME );
      }
    }
  }

  public static boolean listEquals( List<Member> a1, List<Member> a2 ) {
    for ( int i = 0; i < a1.size(); i++ ) {
      if ( !Objects.equals( a1.get( i ), a2.get( i ) ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Sorts a list of members according to a list of SortKeySpecs. An in-place, Stable sort. Helper function for MDX
   * OrderSet function.
   *
   * NOTE: Does not preserve the contents of the validator.
   */
  public static List<Member> sortMembers(
    Evaluator evaluator,
    Iterable<Member> memberIter,
    List<Member> memberList,
    List<SortKeySpec> keySpecList ) {
    if ( ( memberList != null ) && ( memberList.size() <= 1 ) ) {
      return memberList;
    }
    if ( memberList == null ) {
      memberList = new ArrayList<>();
      for ( Member member : memberIter ) {
        memberList.add( member );
      }
      if ( memberList.size() <= 1 ) {
        return memberList;
      }
    }


	Comparator<Member> chain = (o1, o2) -> 0;

    for ( SortKeySpec key : keySpecList ) {
      boolean brk = key.getDirection().brk;
      MemberComparator comp;
      if ( brk ) {
        comp = new MemberComparator.BreakMemberComparator(
          evaluator, key.getKey(), key.getDirection().descending );
      } else {
        comp = new MemberComparator.HierarchicalMemberComparator(
          evaluator, key.getKey(), key.getDirection().descending );
      }
      comp.preloadValues( memberList );

      chain=chain.thenComparing(comp.wrap());


    }

    memberList.sort( chain );
    return memberList;
  }

  /**
   * Sorts a list of Tuples by the value of an applied expression. Stable sort.
   *
   * Helper function for MDX functions TopCount, TopSum, TopPercent,
   * BottomCount, BottomSum, BottomPercent, but not the MDX function Order.
   *
   * NOTE: This function does not preserve the contents of the validator.
   *
   * If you specify {@code tupleList}, the list is sorted in place, and
   * tupleList is returned.
   *
   * @param evaluator     Evaluator
   * @param tupleIterable Iterator over tuples
   * @param tupleList     List of tuples, if known, otherwise null
   * @param exp           Expression to sort on
   * @param desc          Whether to sort descending
   * @param brk           Whether to break
   * @param arity         Number of members in each tuple
   * @return sorted list (never null)
   */
  public static TupleList sortTuples(
    Evaluator evaluator,
    TupleIterable tupleIterable,
    TupleList tupleList,
    Calc exp,
    boolean desc,
    boolean brk,
    int arity ) {
    // NOTE: This method does not implement the iterable/list concept
    // as fully as sortMembers. This is because sortMembers evaluates all
    // sort expressions up front. There, it is efficient to unravel the
    // iterator and evaluate the sort expressions at the same time.
    List<List<Member>> tupleArrayList;
    if ( tupleList == null ) {
      final TupleCursor cursor = tupleIterable.tupleCursor();
      tupleArrayList = iterableToList( evaluator, cursor );
      if ( tupleArrayList.size() <= 1 ) {
        return new DelegatingTupleList(
          tupleIterable.getArity(),
          tupleArrayList );
      }
    } else {
      if ( tupleList.size() <= 1 ) {
        return tupleList;
      }
      tupleArrayList = tupleList;
    }

    @SuppressWarnings( { "unchecked" } )
    List<Member>[] tuples =
      tupleArrayList.toArray( new List[ tupleArrayList.size() ] );
    final DelegatingTupleList result =
      new DelegatingTupleList(
        tupleIterable.getArity(),
        Arrays.asList( tuples ) );

    Comparator<List<Member>> comparator;
    if ( brk ) {
      comparator =
        new TupleExpMemoComparator.BreakTupleComparator( evaluator, exp, arity );
      if ( desc ) {
        comparator = Collections.reverseOrder( comparator );
      }
    } else {
      comparator =
        new HierarchicalTupleComparator( evaluator, exp, arity, desc );

    }
    Arrays.sort( tuples, comparator );
    logTuples( tupleList, "Sorter.sortTuples" );
    return result;
  }

  private static TupleList iterableToList( Evaluator evaluator, TupleCursor cursor ) {
    TupleList tupleArrayList = TupleCollections.createList( cursor.getArity() );
    int currentIteration = 0;
    Execution execution = evaluator.getQuery().getStatement().getCurrentExecution();
    while ( cursor.forward() ) {
      CancellationChecker.checkCancelOrTimeout( currentIteration++, execution );
      tupleArrayList.addCurrent( cursor );
    }
    return tupleArrayList;
  }

  /**
   * Partially sorts a list of Members by the value of an applied expression.
   *
   * Avoids sorting the whole list, finds only the <i>n</i>top (or bottom)
   * valued Members, and returns them as a new List. Helper function for MDX functions TopCount and BottomCount.
   *
   * NOTE: Does not preserve the contents of the validator.
   *
   * @param list      a list of members
   * @param exp       a Calc applied to each member to find its sort-key
   * @param evaluator Evaluator
   * @param limit     maximum count of members to return.
   * @param desc      true to sort descending (and find TopCount), false to sort ascending (and find BottomCount).
   * @return the top or bottom members, as a new list.
   */
  public static List<Member> partiallySortMembers(
    Evaluator evaluator,
    List<Member> list,
    Calc exp,
    int limit,
    boolean desc ) {
    assert !list.isEmpty();
    assert limit <= list.size();
    evaluator.getTiming().markStart( SORT_EVAL_TIMING_NAME );
    boolean timingEval = true;
    boolean timingSort = false;
    try {
      MemberComparator comp =
        new MemberComparator.BreakMemberComparator( evaluator, exp, desc );
      Map<Member, Object> valueMap =
        evaluateMembers( evaluator, exp, list, null, false );
      evaluator.getTiming().markEnd( SORT_EVAL_TIMING_NAME );
      timingEval = false;
      evaluator.getTiming().markStart( SORT_TIMING_NAME );
      timingSort = true;
      comp.preloadValues( valueMap );
      return stablePartialSort( list, comp.wrap(), limit );
    } finally {
      if ( timingEval ) {
        evaluator.getTiming().markEnd( SORT_EVAL_TIMING_NAME );
      } else if ( timingSort ) {
        evaluator.getTiming().markEnd( SORT_TIMING_NAME );
      }
    }
  }

  /**
   * Helper function to sort a list of tuples according to a list of expressions and a list of sorting flags.
   *
   * NOTE: This function does not preserve the contents of the validator.
   */
  public static TupleList sortTuples(
    Evaluator evaluator,
    TupleIterable tupleIter,
    TupleList tupleList,
    List<SortKeySpec> keySpecList,
    int arity ) {
    if ( tupleList == null ) {
      tupleList = iterableToList( evaluator, tupleIter.tupleCursor() );
    }
    if ( tupleList.size() <= 1 ) {
      return tupleList;
    }

	Comparator<List<Member>> chain = (o1, o2) -> 0;
    for ( SortKeySpec key : keySpecList ) {
    	chain= applySortSpecToComparator( evaluator, arity, chain, key );
    }
    tupleList.sort( chain );
    logTuples( tupleList, "Sorter.sortTuples" );
    return tupleList;
  }

  /*
   * ONLY VisibleForTesting
   */
  static Comparator<List<Member>> applySortSpecToComparator( Evaluator evaluator, int arity, Comparator<List<Member>> chain,
                                         SortKeySpec key ) {
    boolean brk = key.getDirection().brk;
	boolean orderByKey = key.getKey() instanceof MemberOrderKeyCalc;
    boolean direction = key.getDirection().descending;
    if ( brk ) {
    	Comparator<List<Member>> comp =
        new TupleExpMemoComparator.BreakTupleComparator( evaluator, key.getKey(), arity );
		if (direction) {
			comp =  comp.reversed();
		}
     return chain.thenComparing(comp);
    } else if ( orderByKey ) {
    	Comparator<List<Member>>  comp =
        new HierarchicalTupleKeyComparator( evaluator, key.getKey(), arity );
		if (direction) {
			comp =  comp.reversed();
		}
		return chain.thenComparing(comp);
    } else {
    	Comparator<List<Member>>  comp =
        new HierarchicalTupleComparator( evaluator, key.getKey(), arity, direction );

    	return chain.thenComparing(comp); // ordering handled in the comparator.
    }
  }

  private static void logTuples( TupleList tupleList, String description ) {
    if ( LOGGER.isDebugEnabled() ) {
      StringBuilder sb = new StringBuilder(description).append(": ");
      if (tupleList != null) {
    	  for ( List<Member> tuple : tupleList ) {
    		  sb.append( "\n" );
    		  sb.append( tuple.toString() );
    	  }
      }
      LOGGER.debug( sb.toString() );
    }
  }

  /**
   * Partially sorts a list of Tuples by the value of an applied expression.
   *
   * Avoids sorting the whole list, finds only the <i>n</i> top (or bottom)
   * valued Tuples, and returns them as a new List. Helper function for MDX functions TopCount and BottomCount.
   *
   * NOTE: Does not preserve the contents of the validator. The returned
   * list is immutable.
   *
   * @param evaluator Evaluator
   * @param list      a list of tuples
   * @param exp       a Calc applied to each tuple to find its sort-key
   * @param limit     maximum count of tuples to return.
   * @param desc      true to sort descending (and find TopCount), false to sort ascending (and find BottomCount).
   * @return the top or bottom tuples, as a new list.
   */
  public static List<List<Member>> partiallySortTuples(
    Evaluator evaluator,
    TupleList list,
    Calc exp,
    int limit,
    boolean desc ) {
    assert !list.isEmpty();
    assert limit <= list.size();
    Comparator<List<Member>> comp =
      new TupleExpMemoComparator.BreakTupleComparator( evaluator, exp, list.getArity() );
    if ( desc ) {
      comp = Collections.reverseOrder( comp );
    }
    return stablePartialSort( list, comp, limit );
  }

  /**
   * Sorts a list of members into hierarchical order. The members must belong to the same dimension.
   *
   * @param memberList List of members
   * @param post       Whether to sort in post order; if false, sorts in pre order
   * @see #hierarchizeTupleList(org.eclipse.daanse.olap.api.calc.todo.TupleList, boolean)
   */
  public static void hierarchizeMemberList(
    List<Member> memberList,
    boolean post ) {
    if ( memberList.size() <= 1 ) {
      return;
    }

    Comparator<Member> comparator = new HierarchizeComparator( post );
    memberList.sort( comparator );
  }

  /**
   * Sorts a list of tuples into hierarchical order.
   *
   * @param tupleList List of tuples
   * @param post      Whether to sort in post order; if false, sorts in pre order
   * @see #hierarchizeMemberList(java.util.List, boolean)
   */
  public static TupleList hierarchizeTupleList(
    TupleList tupleList,
    boolean post ) {
    if ( tupleList.isEmpty() ) {
      TupleCollections.emptyList( tupleList.getArity() );
    }
    final TupleList fixedList = tupleList.fix();
    if ( tupleList.getArity() == 1 ) {
      hierarchizeMemberList( fixedList.slice( 0 ), post );
      return fixedList;
    }
    Comparator<List<Member>> comparator =
      new HierarchizeTupleComparator( fixedList.getArity(), post );

    fixedList.sort( comparator );

    logTuples( fixedList, "hierarchizeTupleList" );

    return fixedList;
  }

  /**
   * Compares double-precision values according to MDX semantics.
   *
   * MDX requires a total order:
   *
   * -inf &lt; NULL &lt; ... &lt; -1 &lt; ... &lt; 0 &lt; ... &lt; NaN &lt; +inf
   *
   * but this is different than Java semantics, specifically with regard to {@link Double#NaN}.
   */
  public static int compareValues( double d1, double d2 ) {
    if ( Double.isNaN( d1 ) ) {
      if ( d2 == Double.POSITIVE_INFINITY ) {
        return -1;
      } else if ( Double.isNaN( d2 ) ) {
        return 0;
      } else {
        return 1;
      }
    } else if ( Double.isNaN( d2 ) ) {
      if ( d1 == Double.POSITIVE_INFINITY ) {
        return 1;
      } else {
        return -1;
      }
    } else if ( d1 == d2 ) {
      return 0;
    } else if ( d1 == DOUBLE_NULL) {
      if ( d2 == Double.NEGATIVE_INFINITY ) {
        return 1;
      } else {
        return -1;
      }
    } else if ( d2 == DOUBLE_NULL) {
      if ( d1 == Double.NEGATIVE_INFINITY ) {
        return -1;
      } else {
        return 1;
      }
    } else if ( d1 < d2 ) {
      return -1;
    } else {
      return 1;
    }
  }

  /**
   * Compares two cell values.
   *
   * Nulls compare last, exceptions (including the
   * object which indicates the the cell is not in the cache yet) next, then numbers and strings are compared by value.
   *
   * @param value0 First cell value
   * @param value1 Second cell value
   * @return -1, 0, or 1, depending upon whether first cell value is less than, equal to, or greater than the second
   */
  public static int compareValues( Object value0, Object value1 ) {
    if ( value0 == value1 ) {
      return 0;
    }
    // null is less than anything else
    if ( value0 == null ) {
      return -1;
    }
    if ( value1 == null ) {
      return 1;
    }

    if ( value0 == Util.valueNotReadyException ) {
      // the left value is not in cache; continue as best as we can
      return -1;
    } else if ( value1 == Util.valueNotReadyException ) {
      // the right value is not in cache; continue as best as we can
      return 1;
    } else if ( value0 == Util.nullValue ) {
      return -1; // null == -infinity
    } else if ( value1 == Util.nullValue ) {
      return 1; // null == -infinity
    } else if ( value0 instanceof String ) {
      return ( (String) value0 ).compareToIgnoreCase( (String) value1 );
    } else if ( value0 instanceof Number ) {
      return Sorter.compareValues(
        ( (Number) value0 ).doubleValue(),
        ( (Number) value1 ).doubleValue() );
    } else if ( value0 instanceof Date ) {
      return ( (Date) value0 ).compareTo( (Date) value1 );
    } else if ( value0 instanceof OrderKey ) {
      return ( (OrderKey) value0 ).compareTo( value1 );
    } else {
      throw newInternal( "cannot compare " + value0 );
    }
  }


  /**
   * Converts a double (primitive) value to a Double. DoubleNull becomes null.
   */
  public static Double box( double d ) {
    return d == DOUBLE_NULL
      ? null
      : d;
  }



  /**
   * Compares a pair of members according to their positions in a prefix-order (or postfix-order, if {@code post} is
   * true) walk over a hierarchy.
   *
   * @param m1   First member
   * @param m2   Second member
   * @param post Whether to sortMembers in postfix order. If true, a parent will sortMembers immediately after its last
   *             child. If false, a parent will sortMembers immediately before its first child.
   * @return -1 if m1 collates before m2, 0 if m1 equals m2, 1 if m1 collates after m2
   */
  public static int compareHierarchically(
    Member m1,
    Member m2,
    boolean post ) {
    // Strip away the LimitedRollupMember wrapper, if it exists. The
    // wrapper does not implement equals and comparisons correctly. This
    // is safe this method has no side-effects: it just returns an int.
    m1 = unwrapLimitedRollupMember( m1 );
    m2 = unwrapLimitedRollupMember( m2 );

    if ( Objects.equals( m1, m2 ) ) {
      return 0;
    }

    while ( true ) {
      if (m1 == null) {
          return -1;
      }
      if (m2 == null) {
          return 1;
      }

      int depth1 = m1.getDepth();
      int depth2 = m2.getDepth();
      if ( depth1 < depth2 ) {
        m2 = m2.getParentMember();
        if ( Objects.equals( m1, m2 ) ) {
          return post ? 1 : -1;
        }
      } else if ( depth1 > depth2 ) {
        m1 = m1.getParentMember();
        if ( Objects.equals( m1, m2 ) ) {
          return post ? -1 : 1;
        }
      } else {
        Member prev1 = m1;
        Member prev2 = m2;
        m1 = unwrapLimitedRollupMember( m1.getParentMember() );
        m2 = unwrapLimitedRollupMember( m2.getParentMember() );
        if ( Objects.equals( m1, m2 ) ) {
          final int c = compareSiblingMembers( prev1, prev2 );
          // compareHierarchically needs to impose a total order
          // cannot return 0 for non-equal members
          assert c != 0
            : new StringBuilder("Members ").append(prev1).append(", ").append(prev2)
            .append(" are not equal, but compare returned 0.").toString();
          return c;
        }
      }
    }
  }

  private static Member unwrapLimitedRollupMember( Member m ) {
    if ( m instanceof LimitedMember lm) {
      return lm.getMember();
    }
    return m;
  }

  /**
   * Compares two members which are known to have the same parent.
   *
   * First, compare by ordinal. This is only valid now we know they're siblings, because ordinals are only unique within
   * a parent. If the dimension does not use ordinals, both ordinals will be -1.
   *
   * If the ordinals do not differ, compare using regular member
   * comparison.
   *
   * @param m1 First member
   * @param m2 Second member
   * @return -1 if m1 collates less than m2, 1 if m1 collates after m2, 0 if m1 == m2.
   */
  public static int compareSiblingMembers( Member m1, Member m2 ) {
    // calculated members collate after non-calculated
    final boolean calculated1 = m1.isCalculatedInQuery();
    final boolean calculated2 = m2.isCalculatedInQuery();
    if ( calculated1 ) {
      if ( !calculated2 ) {
        return 1;
      }
    } else {
      if ( calculated2 ) {
        return -1;
      }
    }
    if(
        SystemWideProperties.instance().CompareSiblingsByOrderKey
        &&
        m1.getLevel().getOrdinalExp() != null
    ) {
      final Comparable k1 = m1.getOrderKey();
      final Comparable k2 = m2.getOrderKey();
      if ( ( k1 != null ) && ( k2 != null ) && ( k1.getClass() == k2.getClass() ) ) {
        return k1.compareTo( k2 );
      } else {
        final String caption1 = m1.getCaption();
        final String caption2 = m2.getCaption();
        return caption1.compareTo( caption2 );
      }
    }
    else {
      final int ordinal1 = m1.getOrdinal();
      final int ordinal2 = m2.getOrdinal();
      return ( ordinal1 == ordinal2 )
        ? m1.compareTo( m2 )
        : ( ordinal1 < ordinal2 )
        ? -1
        : 1;
    }
  }


  /**
   * Partial Sort: sorts in place an array of Objects using a given Comparator, but only enough so that the N biggest
   * (or smallest) items are at the start of the array. Not a stable sort, unless the Comparator is so contrived.
   *
   * @param items will be partially-sorted in place
   * @param comp  a Comparator; null means use natural comparison
   */
  static <T> void partialSort( T[] items, Comparator<T> comp, int limit ) {
    if ( comp == null ) {
      //noinspection unchecked
      comp = (Comparator<T>) Comparator.naturalOrder();

    }
    new Quicksorter<>( items, comp ).partialSort( limit );
  }

  /**
   * Stable partial sort of a list. Returns the desired head of the list.
   */
  public static <T> List<T> stablePartialSort(
    final List<T> list, final Comparator<T> comp, int limit ) {
    return stablePartialSort( list, comp, limit, 0 );
  }

  /**
   * Stable partial sort of a list, using a specified algorithm.
   */
  public static <T> List<T> stablePartialSort(
    final List<T> list, final Comparator<T> comp, int limit, int algorithm ) {
    assert limit <= list.size();
    assert !list.isEmpty();
    while ( true ) {
      switch ( algorithm ) {
        case 0:
          float ratio = (float) limit / (float) list.size();
          if ( ratio <= .05 ) {
            algorithm = 4; // julian's algorithm
          } else if ( ratio <= .35 ) {
            algorithm = 2; // marc's algorithm
          } else {
            algorithm = 1; // array sort
          }
          break;
        case 1:
          return stablePartialSortArray( list, comp, limit );
        case 2:
          return stablePartialSortMarc( list, comp, limit );
        case 4:
          return stablePartialSortJulian( list, comp, limit );
        default:
          throw new IllegalStateException();
      }
    }
  }

  /**
   * Partial sort an array by sorting it and returning the first {@code limit} elements. Fastest approach if limit is a
   * significant fraction of the list.
   */
  public static <T> List<T> stablePartialSortArray(
    final List<T> list, final Comparator<T> comp, int limit ) {
    ArrayList<T> list2 = new ArrayList<>( list );
    list2.sort( comp );
    return list2.subList( 0, limit );
  }

  /**
   * Marc's original algorithm for stable partial sort of a list. Now superseded by {@link #stablePartialSortJulian}.
   */
  public static <T> List<T> stablePartialSortMarc(
    final List<T> list, final Comparator<T> comp, int limit ) {
    assert limit >= 0;

    // Load an array of pairs {list-item, list-index}.
    // List-index is a secondary sort key, to give a stable sort.
    // REVIEW Can we use a simple T[], with the index implied?
    // REVIEW When limit is big relative to list size, faster to
    // mergesort. Test for this.
    int n = list.size();            // O(n) to scan list
    @SuppressWarnings( { "unchecked" } ) final ObjIntPair<T>[] pairs = new ObjIntPair[ n ];

    int i = 0;
    for ( T item : list ) {           // O(n) to scan list
      pairs[ i ] = new ObjIntPair<>( item, i );
      ++i;
    }

    Comparator<ObjIntPair<T>> pairComp =
      ( x, y ) -> {
        int val = comp.compare( x.t, y.t );
        if ( val == 0 ) {
          val = x.i - y.i;
        }
        return val;
      };

    final int length = Math.min( limit, n );
    // O(n + limit * log(limit)) to quicksort
    partialSort( pairs, pairComp, length );

    // Use an abstract list to avoid doing a copy. The result is immutable.
    return new AbstractList<>() {
      @Override
      public T get( int index ) {
        return pairs[ index ].t;
      }

      @Override
      public int size() {
        return length;
      }
    };
  }


  /**
   * Julian's algorithm for stable partial sort. Improves Pedro's algorithm by using a heap (priority queue) for the top
   * {@code limit} items seen. The items on the priority queue have an ordinal field, so the queue can be used to
   * generate a list of stably sorted items. (Heap sort is not normally stable.)
   *
   * @param list  List to sort
   * @param comp  Comparator
   * @param limit Maximum number of items to return
   * @param <T>   Element type
   * @return Sorted list, containing at most limit items
   */
  public static <T> List<T> stablePartialSortJulian(
    final List<T> list, final Comparator<T> comp, int limit ) {
    final Comparator<ObjIntPair<T>> comp2 =
      ( o1, o2 ) -> {
        int c = comp.compare( o1.t, o2.t );
        if ( c == 0 ) {
          c = Integer.compare(o1.i,o2.i);
        }
        return -c;
      };
    int filled = 0;
    final PriorityQueue<ObjIntPair<T>> queue =
      new PriorityQueue<>( limit, comp2 );
    for ( T element : list ) {
      if ( filled < limit ) {
        queue.offer( new ObjIntPair<>( element, filled++ ) );
      } else {
        ObjIntPair<T> head = queue.element();
        if ( comp.compare( element, head.t ) <= 0 ) {
          ObjIntPair<T> item = new ObjIntPair<>( element, filled++ );
          if ( comp2.compare( item, head ) >= 0 ) {
            ObjIntPair<T> poll = queue.remove();
//            discard( poll );
            queue.offer( item );
          }
        }
      }
    }

    int n = queue.size();
    final Object[] elements = new Object[ n ];
    while ( n > 0 ) {
      elements[ --n ] = queue.poll().t;
    }
    assert queue.isEmpty();
    //noinspection unchecked
    return Arrays.asList( (T[]) elements );
  }


  /**
   * Enumeration of the flags allowed to the {@code ORDER} MDX function.
   */
  public enum SorterFlag {
    ASC( false, false ),
    DESC( true, false ),
    BASC( false, true ),
    BDESC( true, true );

    public final boolean descending;
    public final boolean brk;

    SorterFlag( boolean descending, boolean brk ) {
      this.descending = descending;
      this.brk = brk;
    }
    private static List<String> reservedWords=Stream.of(SorterFlag.values()).map(SorterFlag::name).toList();

    public static List<String> asReservedWords() {
    	return reservedWords;
    }

  }

  /**
   * Tuple consisting of an object and an integer.
   *
   * Similar to {@link org.eclipse.daanse.olap.util.Pair}, but saves boxing overhead of converting
   * {@code int} to {@link Integer}.
   */
  public static class ObjIntPair<T> {
    final T t;
    final int i;

    public ObjIntPair( T t, int i ) {
      this.t = t;
      this.i = i;
    }

    @Override
	public int hashCode() {
      return Util.hash( i, t );
    }

    @Override
	public boolean equals( Object obj ) {
      return this == obj
        || obj instanceof ObjIntPair
        && this.i == ( (ObjIntPair) obj ).i
        && Objects.equals( this.t, ( (ObjIntPair) obj ).t );
    }

    @Override
	public String toString() {
      return new StringBuilder("<").append(t).append(", ").append(i).append(">").toString();
    }
  }
}

