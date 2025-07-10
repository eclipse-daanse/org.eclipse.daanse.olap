/*
 * This software is subject to the terms of the Eclipse Public License v1.0
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

import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;

/**
 * Base class for types which represent scalar values.
 *
 * An instance of this class means a scalar value of unknown type.
 * Usually one of the derived classes  NumericType,
 *  StringType,  BooleanType is used instead.
 *
 * @author jhyde
 * @since Feb 17, 2005
 */
public class ScalarType implements Type {
    private final String digest;

	public static final ScalarType INSTANCE = new ScalarType();

    private ScalarType() {
        this("SCALAR");
    }

    protected ScalarType(String digest) {
        this.digest = digest;
    }

    @Override
	public int hashCode() {
        return digest.hashCode();
    }

    @Override
	public boolean equals(Object obj) {
        return obj != null
            && obj.getClass() == ScalarType.class;
    }

    @Override
	public String toString() {
        return digest;
    }

    @Override
	public boolean usesDimension(Dimension dimension, boolean definitely) {
        return false;
    }

    @Override
	public boolean usesHierarchy(Hierarchy hierarchy, boolean definitely) {
        return false;
    }

    @Override
	public Hierarchy getHierarchy() {
        return null;
    }

    @Override
	public Level getLevel() {
        return null;
    }

    @Override
	public Type computeCommonType(Type type, int[] conversionCount) {
        if (this.equals(type)) {
            return this;
        } else if (type instanceof NullType) {
            return this;
        } else if (this instanceof NullType
            && type instanceof ScalarType)
        {
            return type;
        } else if (this.getClass() == ScalarType.class
            && type instanceof ScalarType)
        {
            return this;
        } else if (type.getClass() == ScalarType.class) {
            return type;
        } else if (type instanceof ScalarType) {
            return new ScalarType();
        } else if (type instanceof MemberType) {
            return computeCommonType(
                ((MemberType) type).getValueType(),
                conversionCount);
        } else if (type instanceof TupleType) {
            return computeCommonType(
                ((TupleType) type).getValueType(),
                conversionCount);
        } else {
            return null;
        }
    }

    @Override
	public Dimension getDimension() {
        return null;
    }

    @Override
	public boolean isInstance(Object value) {
        // Somewhat pessimistic.
        return false;
    }

    @Override
	public int getArity() {
        return 1;
    }
}
