/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2005-2005 Julian Hyde
 * Copyright (C) 2005-2017 Hitachi Vantara
 * All Rights Reserved.
 * 
 * For more information please visit the Project: Hitachi Vantara - Mondrian
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
 *   Stefan Bischof (bipolis.org) - initial
 */


package org.eclipse.daanse.olap.api.type;

import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;

/**
 * Type of an MDX expression.
 *
 * @author jhyde
 * @since Feb 17, 2005
 */
public interface Type {
    /**
     * Returns whether this type contains a given dimension.
     *
     * For example:
     *
     * DimensionType([Gender]) uses only the
     *     [Gender] dimension.
     * TupleType(MemberType([Gender]), MemberType([Store]))
     *     uses [Gender]  and [Store]
     *     dimensions.
     *
     *
     * The definitely parameter comes into play when the
     * dimensional information is incomplete. For example, when applied to
     * TupleType(MemberType(null), MemberType([Store])),
     * usesDimension([Gender], false) returns true because it
     * is possible that the expression returns a member of the
     * [Gender] dimension; but
     * usesDimension([Gender], true) returns true because it
     * is possible that the expression returns a member of the
     * [Gender] dimension.
     *
     * @param dimension Dimension
     * @param definitely If true, returns true only if this type definitely
     *    uses the dimension
     *
     * @return whether this Type uses the given Dimension
     */
    boolean usesDimension(Dimension dimension, boolean definitely);

    /**
     * Returns whether this type contains a given hierarchy.
     *
     * For example:
     *
     * HierarchyType([Customer].[Gender]) uses only the
     *     [Customer].[Gender] hierarchy.
     * TupleType(MemberType([Customer].[Gender]),
     *           MemberType([Store].[Store]))
     *     uses [Gender]  and [Store]
     *     dimensions.
     *
     *
     * The definitely parameter comes into play when the
     * dimensional information is incomplete. For example, when applied to
     * TupleType(MemberType([Customer]), MemberType([Store])),
     * usesDimension([Customer].[Gender], false) returns true
     * because the expression returns a member of one hierarchy of the
     * [Customer] dimension and that might be a member of the
     * [Customer].[Gender] hierarchy; but
     * usesDimension([Customer].[Gender], true) returns false
     * because might return a member of a different hierarchy, such as
     * [Customer].[State].
     *
     * @param hierarchy Hierarchy
     * @param definitely If true, returns true only if this type definitely
     *    uses the hierarchy
     *
     * @return whether this Type uses the given Hierarchy
     */
    boolean usesHierarchy(Hierarchy hierarchy, boolean definitely);

    /**
     * Returns the Dimension of this Type, or null if not known.
     * If not applicable, throws.
     *
     * @return the Dimension of this Type, or null if not known.
     */
    Dimension getDimension();

    /**
     * Returns the Hierarchy of this Type, or null if not known.
     * If not applicable, throws.
     *
     * @return the Hierarchy of this type, or null if not known
     */
    Hierarchy getHierarchy();

    /**
     * Returns the Level of this Type, or null if not known.
     * If not applicable, throws.
     *
     * @return the Level of this Type
     */
    Level getLevel();

    /**
     * Returns a Type which is more general than this and the given Type.
     * The type returned is broad enough to hold any value of either type,
     * but no broader. If there is no such type, returns null.
     *
     * Some examples:
     * The common type for StringType and NumericType is ScalarType.
     * The common type for NumericType and DecimalType(4, 2) is
     *     NumericType.
     * DimensionType and NumericType have no common type.
     *
     *
     * If conversionCount is not null, implicit conversions
     * such as HierarchyType to DimensionType are considered; the parameter
     * is incremented by the number of conversions performed.
     *
     * Some examples:
     * The common type for HierarchyType(hierarchy=Time.Weekly)
     *     and LevelType(dimension=Time), if conversions are allowed, is
     *     HierarchyType(dimension=Time).
     *
     *
     * One use of common types is to determine the types of the arguments
     * to the Iif function. For example, the call
     *
     * Iif(1 &gt; 2, [Measures].[Unit Sales],
     * 5)
     *
     * has type ScalarType, because DecimalType(-1, 0) is a subtype of
     * ScalarType, and MeasureType can be converted implicitly to ScalarType.
     *
     * @param type Type
     *
     * @param conversionCount Number of conversions; output parameter that is
     * incremented each time a conversion is performed; if null, conversions
     * are not considered
     *
     * @return More general type
     */
    Type computeCommonType(Type type, int[] conversionCount);

    /**
     * Returns whether a value is valid for a type.
     *
     * @param value Value
     * @return Whether value is valid for this type
     */
    boolean isInstance(Object value);

    /**
     * Returns the number of fields in a tuple type, or a set of tuples.
     * For most other types, in particular member type, returns 1.
     *
     * @return Arity of type
     */
    int getArity();
}
