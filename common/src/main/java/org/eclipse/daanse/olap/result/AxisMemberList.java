/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2001-2005 Julian Hyde
 * Copyright (C) 2005-2017 Hitachi Vantara and others
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
package org.eclipse.daanse.olap.result;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.daanse.olap.api.calc.tuple.TupleCursor;
import org.eclipse.daanse.olap.api.calc.tuple.TupleList;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.execution.Execution;
import org.eclipse.daanse.olap.api.execution.ExecutionContext;
import org.eclipse.daanse.olap.exceptions.ResourceLimitExceededException;
import org.eclipse.daanse.olap.util.CancellationChecker;

/**
 * Collection of members found on an axis.
 *
 *
 * The behavior depends on the mode (i.e. the kind of axis). If it collects, it generally eliminates duplicates. It
 * also has a mode where it only counts members, does not collect them.
 * 
 *
 *
 * This class does two things. First it collects all Members found during the Member-Determination phase. Second, it
 * counts how many Members are on each axis and forms the product, the totalCellCount which is checked against the
 * ResultLimit property value.
 * 
 */
final class AxisMemberList implements Iterable<Member> {
  private final List<Member> members;
  // Also store members by hierarchy for faster de-duplication and also reuse in RolapEvaluator
  private final Map<Hierarchy, Set<Member>> membersByHierarchy;
  private final int limit;
  private boolean isSlicer;
  private int totalCellCount;
  private int axisCount;
  private boolean countOnly;
  private final static String totalMembersLimitExceeded = "Total number of Members in result ({0,number}) exceeded limit ({1,number})";

    // The limit is handed in rather than looked up: this is a static nested
    // class, while its only caller sits in the result constructor where
    // the Context is unambiguous.
    AxisMemberList( int limit ) {
    this.countOnly = false;
    this.members = new ArrayList<>();
    this.membersByHierarchy = new HashMap<>();
    this.totalCellCount = 1;
    this.axisCount = 0;
    // Now that the axes are evaluated, make sure that the number of
    // cells does not exceed the result limit.
    this.limit = limit;
  }

  @Override
	public Iterator<Member> iterator() {
    return members.iterator();
  }

  void setSlicer( final boolean isSlicer ) {
    this.isSlicer = isSlicer;
  }

  boolean isEmpty() {
    return this.members.isEmpty();
  }

  void countOnly( boolean countOnly ) {
    this.countOnly = countOnly;
  }

  void checkLimit() {
    if ( this.limit > 0 ) {
      this.totalCellCount *= this.axisCount;
      if ( this.totalCellCount > this.limit ) {
        throw new ResourceLimitExceededException(MessageFormat.format(totalMembersLimitExceeded, String.valueOf(this.totalCellCount), String.valueOf(this.limit) ));
      }
      this.axisCount = 0;
    }
  }

  void clearAxisCount() {
    this.axisCount = 0;
  }

  void clearTotalCellCount() {
    this.totalCellCount = 1;
  }

  void clearMembers() {
    this.members.clear();
    this.membersByHierarchy.clear();
    this.axisCount = 0;
    this.totalCellCount = 1;
  }

  void mergeTupleList( TupleList list ) {
    mergeTupleIter( list.tupleCursor() );
  }

  void mergeTupleIter( TupleCursor cursor ) {
    int currentIteration = 0;
    Execution execution = ExecutionContext.current().getExecution();
    while ( cursor.forward() ) {
      CancellationChecker.checkCancelOrTimeout( currentIteration++, execution );
      mergeTuple( cursor );
    }
  }

  private Member getTopParent( Member m ) {
    while ( true ) {
      Member parent = m.getParentMember();
      if ( parent == null ) {
        return m;
      }
      m = parent;
    }
  }

  private void mergeTuple( final TupleCursor cursor ) {
    final int arity = cursor.getArity();
    for ( int i = 0; i < arity; i++ ) {
      mergeMember( cursor.member( i ) );
    }
  }

  private void mergeMember( final Member member ) {
    this.axisCount++;
    if ( !countOnly ) {
      if ( isSlicer ) {
        if ( !contains( member ) ) {
          addMember( member );
        }
      } else {
        if ( member.isNull() || member.isMeasure() || member.isCalculated() || member.isAll()) {
          return;
        }
        Member topParent = getTopParent( member );
        if ( !contains( topParent ) ) {
          addMember( topParent );
        }
      }
    }
  }

  private boolean contains( Member member ) {
    if ( !membersByHierarchy.containsKey( member.getHierarchy() ) ) {
      return false;
    }
    return membersByHierarchy.get( member.getHierarchy() ).contains( member );
  }

  private void addMember( Member member ) {
    members.add( member );
    Hierarchy hierarchy = member.getHierarchy();
    membersByHierarchy.computeIfAbsent(hierarchy, k -> new HashSet<>()).add( member );
  }

  public List<Member> getMembers() {
    return members;
  }

  public Map<Hierarchy, Set<Member>> getMembersByHierarchy() {
    return membersByHierarchy;
  }
}
