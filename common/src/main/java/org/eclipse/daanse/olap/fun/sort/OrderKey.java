/*
 *
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2001-2005 Julian Hyde
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

import org.eclipse.daanse.olap.api.element.Member;


public class OrderKey implements Comparable {
  final Member member;

  public OrderKey( Member member ) {
    super();
    this.member = member;
  }

  @Override
public int compareTo( Object o ) {
    assert o instanceof OrderKey;
    Member otherMember = ( (OrderKey) o ).member;
    final boolean thisCalculated = this.member.isCalculatedInQuery();
    final boolean otherCalculated = otherMember.isCalculatedInQuery();
    if ( thisCalculated ) {
      if ( !otherCalculated ) {
        return 1;
      }
    } else {
      if ( otherCalculated ) {
        return -1;
      }
    }
    final Comparable thisKey = this.member.getOrderKey();
    final Comparable otherKey = otherMember.getOrderKey();
    if ( ( thisKey != null ) && ( otherKey != null ) ) {
      return thisKey.compareTo( otherKey );
    } else {
      return this.member.compareTo( otherMember );
    }
  }
}
