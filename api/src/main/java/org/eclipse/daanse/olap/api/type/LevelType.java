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
 * The type of an expression which represents a level.
 *
 * @author jhyde
 * @since Feb 17, 2005
 */
public class LevelType implements Type {
    private final Dimension dimension;
    private final Hierarchy hierarchy;
    private final Level level;
    private final String digest;

    public static final LevelType Unknown = new LevelType(null, null, null);

    /**
     * Creates a type representing a level.
     *
     * @param dimension Dimension which values of this type must belong to, or
     *   null if not known
     * @param hierarchy Hierarchy which values of this type must belong to, or
     *   null if not known
     * @param level Level which values of this type must belong to, or null if
     */
    public LevelType(Dimension dimension, Hierarchy hierarchy, Level level) {
        this.dimension = dimension;
        this.hierarchy = hierarchy;
        this.level = level;
        if (level != null) {
            assert hierarchy != null;
            assert
                level.getHierarchy() == hierarchy;
        }
        if (hierarchy != null) {
            assert dimension != null;
            assert hierarchy.getDimension() == dimension;
        }
        StringBuilder buf = new StringBuilder("LevelType<");
        if (level != null) {
            buf.append("level=").append(level.getUniqueName());
        } else if (hierarchy != null) {
            buf.append("hierarchy=").append(hierarchy.getUniqueName());
        } else if (dimension != null) {
            buf.append("dimension=").append(dimension.getUniqueName());
        }
        buf.append(">");
        this.digest = buf.toString();
    }

    public static LevelType forType(Type type) {
        return new LevelType(
            type.getDimension(),
            type.getHierarchy(),
            type.getLevel());
    }

    public static LevelType forLevel(Level level) {
        return new LevelType(
            level.getDimension(),
            level.getHierarchy(),
            level);
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
        return level;
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
        if (obj instanceof LevelType that) {
            return Objects.equals(this.level, that.level)
                && Objects.equals(this.hierarchy, that.hierarchy)
                && Objects.equals(this.dimension, that.dimension);
        }
        return false;
    }

    @Override
	public Type computeCommonType(Type type, int[] conversionCount) {
        if (!(type instanceof LevelType that)) {
            return null;
        }
        if (this.getLevel() != null
            && this.getLevel().equals(that.getLevel()))
        {
            return this;
        }
        if (this.getHierarchy() != null
            && this.getHierarchy().equals(that.getHierarchy()))
        {
            return new LevelType(
                this.getDimension(),
                this.getHierarchy(),
                null);
        }
        if (this.getDimension() != null
            && this.getDimension().equals(that.getDimension()))
        {
            return new LevelType(
                this.getDimension(),
                null,
                null);
        }
        return LevelType.Unknown;
    }

    @Override
	public boolean isInstance(Object value) {
        return value instanceof Level
            && (level == null
                || value.equals(level))
            && (hierarchy == null
                || ((Level) value).getHierarchy().equals(hierarchy))
            && (dimension == null
                || ((Level) value).getDimension().equals(dimension));
    }

    @Override
	public int getArity() {
        return 1;
    }
}
