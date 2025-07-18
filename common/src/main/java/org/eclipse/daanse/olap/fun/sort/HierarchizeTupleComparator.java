/*
 *
 *  This software is subject to the terms of the Eclipse Public License v1.0
 *  Agreement, available at the following URL:
 *  http://www.eclipse.org/legal/epl-v10.html.
 *  You must accept the terms of that agreement to use this software.
 *
 *  Copyright (C) 2001-2005 Julian Hyde
 *  Copyright (C) 2005-2020 Hitachi Vantara and others
 *  All Rights Reserved.
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

import org.eclipse.daanse.olap.api.element.Member;

/**
 * Compares lists of {@link Member}s so as to convert them into hierarchical order. Applies lexicographic order to the
 * array.
 */
class HierarchizeTupleComparator extends TupleComparator {
  private final boolean post;

  HierarchizeTupleComparator( int arity, boolean post ) {
    super( arity );
    this.post = post;
  }

  @Override
public int compare( List<Member> a1, List<Member> a2 ) {
    for ( int i = 0; i < arity; i++ ) {
      Member m1 = a1.get( i );
      Member m2 = a2.get( i );
      int c = Sorter.compareHierarchically( m1, m2, post );
      if ( c != 0 ) {
        return c;
      }
    }
    return 0;
  }
}
