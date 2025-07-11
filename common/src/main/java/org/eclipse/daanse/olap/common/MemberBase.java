/*
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

package org.eclipse.daanse.olap.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.api.MatchType;
import org.eclipse.daanse.olap.api.CatalogReader;
import org.eclipse.daanse.olap.api.Segment;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.OlapElement;
import org.eclipse.daanse.olap.api.formatter.MemberFormatter;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.fun.FunUtil;

//import mondrian.util.Bug;
/**
 * MemberBase is a partial implementation of {@link Member}.
 *
 * @author jhyde
 * @since 6 August, 2001
 */
public abstract class MemberBase
  extends OlapElementBase
  implements Member {

  protected Member parentMember;
  protected Level level;
  protected String uniqueName;

  /**
   * Combines member type and other properties, such as whether the member is the 'all' or 'null' member of its
   * hierarchy and whether it is a measure or is calculated, into an integer field.
   *
   * The fields are:
   * bits 0, 1, 2 ({@link #FLAG_TYPE_MASK}) are member type;
   * bit 3 ({@link #FLAG_HIDDEN}) is set if the member is hidden;
   * bit 4 ({@link #FLAG_ALL}) is set if this is the all member of its
   * hierarchy;
   * bit 5 ({@link #FLAG_NULL}) is set if this is the null member of its
   * hierarchy;
   * bit 6 ({@link #FLAG_CALCULATED}) is set if this is a calculated
   * member.
   * bit 7 ({@link #FLAG_MEASURE}) is set if this is a measure.
   *
   *
   * NOTE: jhyde, 2007/8/10. It is necessary to cache whether the member is 'all', 'calculated' or 'null' in the
   * member's state, because these properties are used so often. If we used a virtual method call - say we made each
   * subclass implement 'boolean isNull()' - it would be slower. We use one flags field rather than 4 boolean fields to
   * save space.
   */
  protected final int flags;

  private static final int FLAG_TYPE_MASK = 0x07;
  private static final int FLAG_HIDDEN = 0x08;
  private static final int FLAG_ALL = 0x10;
  private static final int FLAG_NULL = 0x20;
  private static final int FLAG_CALCULATED = 0x40;
  private static final int FLAG_MEASURE = 0x80;

  /**
   * Cached values of {@link org.eclipse.daanse.olap.api.element.Member.MemberType} enumeration. Without caching, get excessive calls to
   * {@link Object#clone}.
   */
  private static final MemberType[] MEMBER_TYPE_VALUES = MemberType.values();
    private final static String mdxMemberName = "member ''{0}''";

    protected MemberBase(
    Member parentMember,
    Level level,
    MemberType memberType ) {
    this.parentMember = parentMember;
    this.level = level;
    this.flags = memberType.ordinal()
      | ( memberType == MemberType.ALL ? FLAG_ALL : 0 )
      | ( memberType == MemberType.NULL ? FLAG_NULL : 0 )
      | ( computeCalculated( memberType ) ? FLAG_CALCULATED : 0 )
      | ( level.getHierarchy().getDimension().isMeasures()
      ? FLAG_MEASURE
      : 0 );
  }

  protected MemberBase() {
    this.flags = 0;
    this.level = null;
  }

  @Override
public String getQualifiedName() {
    return MessageFormat.format( mdxMemberName, getUniqueName() );
  }

  @Override
public abstract String getName();

  @Override
public String getUniqueName() {
    return uniqueName;
  }

  @Override
public String getCaption() {
    // if there is a member formatter for the members level,
    //  we will call this interface to provide the display string
    MemberFormatter mf = getLevel().getMemberFormatter();
    if ( mf != null ) {
      return mf.format( this );
    }

    // fallback if formatter is null
    final String caption = super.getCaption();
    return ( caption != null )
      ? caption
      : getName();
  }

  @Override
public String getParentUniqueName() {
    return parentMember == null
      ? null
      : parentMember.getUniqueName();
  }

  @Override
public Dimension getDimension() {
    return level.getDimension();
  }

  @Override
public Hierarchy getHierarchy() {
    return level.getHierarchy();
  }

  @Override
public Level getLevel() {
    return level;
  }

  @Override
public MemberType getMemberType() {
    return MEMBER_TYPE_VALUES[ flags & FLAG_TYPE_MASK ];
  }

  @Override
public String getDescription() {
    return (String) getPropertyValue( StandardProperty.DESCRIPTION_PROPERTY.getName() );
  }

  @Override
public boolean isMeasure() {
    return ( flags & FLAG_MEASURE ) != 0;
  }

  @Override
public boolean isAll() {
    return ( flags & FLAG_ALL ) != 0;
  }

  @Override
public boolean isNull() {
    return ( flags & FLAG_NULL ) != 0;
  }

  @Override
public boolean isCalculated() {
    return ( flags & FLAG_CALCULATED ) != 0;
  }

  @Override
public boolean isEvaluated() {
    // should just call isCalculated(), but called in tight loops
    // and too many subclass implementations for jit to inline properly?
    return ( flags & FLAG_CALCULATED ) != 0;
  }

  @Override
public OlapElement lookupChild(
    CatalogReader schemaReader,
    Segment childName,
    MatchType matchType ) {
    return schemaReader.lookupMemberChildByName(
      this, childName, matchType );
  }

  // implement Member
  @Override
public Member getParentMember() {
    return parentMember;
  }

  // implement Member
  @Override
public boolean isChildOrEqualTo( Member member ) {
    // REVIEW: Using uniqueName to calculate ancestry seems inefficient,
    //   because we can't afford to store every member's unique name, so
    //   we want to compute it on the fly
    //assert !Bug.BugSegregateRolapCubeMemberFixed;
    return ( member != null ) && isChildOrEqualTo( member.getUniqueName() );
  }

  /**
   * Returns whether this Member's unique name is equal to, a child of, or a descendent of a member whose
   * unique name is
   * uniqueName.
   */
  public boolean isChildOrEqualTo( String uniqueName ) {
    if ( uniqueName == null ) {
      return false;
    }

    return isChildOrEqualTo( this, uniqueName );
  }

  private static boolean isChildOrEqualTo( Member member, String uniqueName ) {
    while ( true ) {
      String thisUniqueName = member.getUniqueName();
      if ( thisUniqueName.equals( uniqueName ) ) {
        // found a match
        return true;
      }
      // try candidate's parentMember
      member = member.getParentMember();
      if ( member == null ) {
        // have reached root
        return false;
      }
    }
  }

  /**
   * Computes the value to be returned by {@link #isCalculated()}, so it can be cached in a variable.
   *
   * @param memberType Member type
   * @return Whether this member is calculated
   */
  protected boolean computeCalculated( final MemberType memberType ) {
    // If the member is not created from the "with member ..." MDX, the
    // calculated will be null. But it may be still a calculated measure
    // stored in the cube.
    return isCalculatedInQuery() || memberType == MemberType.FORMULA;
  }

  @Override
public int getSolveOrder() {
    return -1;
  }

  /**
   * Returns the expression by which this member is calculated. The expression is not null if and only if the
   * member is
   * not calculated.
   *
   *  (return ! = null) == (isCalculated())
   */
  @Override
public Expression getExpression() {
    return null;
  }

  // implement Member
  @Override
public List<Member> getAncestorMembers() {

      // if i see this member i am allowed to see the ancestors
    final CatalogReader schemaReader =
      getDimension().getCatalog().getCatalogReaderWithDefaultRole();
    final ArrayList<Member> ancestors = new ArrayList<>();
    schemaReader.getMemberAncestors( this, ancestors );
    return ancestors;
  }

  /**
   * Returns the ordinal of this member within its hierarchy. The default implementation returns -1.
   */
  @Override
public int getOrdinal() {
    return -1;
  }

  /**
   * Returns the order key of this member among its siblings. The default implementation returns null.
   */
  @Override
public Comparable getOrderKey() {
    return null;
  }

  @Override
public boolean isHidden() {
    return false;
  }

  @Override
public Member getDataMember() {
    return null;
  }

  @Override
public String getPropertyFormattedValue( String propertyName ) {
    return getPropertyValue( propertyName ).toString();
  }

  @Override
public boolean isParentChildPhysicalMember() {
    return false;
  }

  @Override
public boolean isParentChildLeaf() {
    return false;
  }

  @Override
public boolean isOnSameHierarchyChain( Member otherMember ) {
    return ( (MemberBase) otherMember ).isOnSameHierarchyChainInternal( this );
  }

  public boolean isOnSameHierarchyChainInternal( MemberBase otherMember ) {
    return FunUtil.isAncestorOf( otherMember, this, false ) || FunUtil.isAncestorOf( this, otherMember, false );
  }
}
