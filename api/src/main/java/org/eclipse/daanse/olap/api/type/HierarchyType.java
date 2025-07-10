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

package org.eclipse.daanse.olap.api.type;

import java.util.Objects;

import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;

/**
 * The type of an expression which represents a hierarchy.
 *
 * @author jhyde
 * @since Feb 17, 2005
 */
public class HierarchyType implements Type {
    private final Dimension dimension;
    private final Hierarchy hierarchy;
    private final String digest;

    public static final HierarchyType Unknown = new HierarchyType(null, null);

    /**
     * Creates a type representing a hierarchy.
     *
     * @param dimension Dimension that values of this type must belong to, or
     *   null if the dimension is unknown
     * @param hierarchy Hierarchy that values of this type must belong to,
     *   null if the hierarchy is unknown
     */
    public HierarchyType(Dimension dimension, Hierarchy hierarchy) {
        this.dimension = dimension;
        this.hierarchy = hierarchy;
        StringBuilder buf = new StringBuilder("HierarchyType<");
        if (hierarchy != null) {
            buf.append("hierarchy=").append(hierarchy.getUniqueName());
        } else if (dimension != null) {
            buf.append("dimension=").append(dimension.getUniqueName());
        }
        buf.append(">");
        this.digest = buf.toString();
    }

    public static HierarchyType forHierarchy(Hierarchy hierarchy) {
        return new HierarchyType(hierarchy.getDimension(), hierarchy);
    }

    public static HierarchyType forType(Type type) {
        return new HierarchyType(type.getDimension(), type.getHierarchy());
    }

    @Override
	public boolean usesDimension(Dimension dimension, boolean definitely) {
        return this.dimension == dimension
            || (!definitely && this.dimension == null);
    }

    @Override
	public boolean usesHierarchy(Hierarchy hierarchy, boolean definitely) {
        return this.hierarchy == hierarchy
            || (!definitely
                && this.hierarchy == null
                && (this.dimension == null
                    || this.dimension == hierarchy.getDimension()));
    }

    @Override
	public Dimension getDimension() {
        return dimension;
    }

    @Override
	public Hierarchy getHierarchy() {
        return hierarchy;
    }

    @Override
	public Level getLevel() {
        return null;
    }

    @Override
	public String toString() {
        return digest;
    }

    @Override
	public int hashCode() {
        return digest.hashCode();
    }

    @Override
	public boolean equals(Object obj) {
        if (obj instanceof HierarchyType that) {
            return Objects.equals(this.hierarchy, that.hierarchy)
                && Objects.equals(this.dimension, that.dimension);
        }
        return false;
    }

    @Override
	public Type computeCommonType(Type type, int[] conversionCount) {
        if (!(type instanceof HierarchyType that)) {
            return null;
        }
        if (this.getHierarchy() != null
            && this.getHierarchy().equals(that.getHierarchy()))
        {
            return this;
        }
        if (this.getDimension() != null
            && this.getDimension().equals(that.getDimension()))
        {
            return new HierarchyType(
                this.getDimension(),
                null);
        }
        return HierarchyType.Unknown;
    }

    @Override
	public boolean isInstance(Object value) {
        return value instanceof Hierarchy hValue
            && (hierarchy == null
                || value.equals(hierarchy))
            && (dimension == null
                || hValue.getDimension().equals(dimension));
    }

    @Override
	public int getArity() {
        return 1;
    }
}
