/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2005-2005 Julian Hyde
 * Copyright (C) 2005-2017 Hitachi Vantara
 * Copyright (C) 2022 Sergei Semenkov
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;

/**
 * Set type.
 *
 * @author jhyde
 * @since Feb 17, 2005
 */
public class SetType implements Type {

    private final Type elementType;
    private final String digest;

    /**
     * Creates a type representing a set of elements of a given type.
     *
     * @param elementType The type of the elements in the set, or null if not
     *   known
     */
    public SetType(Type elementType) {
        if (elementType != null) {
            assert elementType instanceof MemberType
                || elementType instanceof TupleType;
        }
        this.elementType = elementType;
        this.digest = "SetType<" + elementType + ">";
    }

    @Override
	public int hashCode() {
        return digest.hashCode();
    }

    @Override
	public boolean equals(Object obj) {
        if (obj instanceof SetType that) {
            return Objects.equals(this.elementType, that.elementType);
        } else {
            return false;
        }
    }

    @Override
	public String toString() {
        return digest;
    }

    /**
     * Returns the type of the elements of this set.
     *
     * @return the type of the elements in this set
     */
    public Type getElementType() {
        return elementType;
    }

    @Override
	public boolean usesDimension(Dimension dimension, boolean definitely) {
        if (elementType == null) {
            return definitely;
        }
        return elementType.usesDimension(dimension, definitely);
    }

    @Override
	public boolean usesHierarchy(Hierarchy hierarchy, boolean definitely) {
        if (elementType == null) {
            return definitely;
        }
        return elementType.usesHierarchy(hierarchy, definitely);
    }

    public List<Hierarchy> getHierarchies() {
        if(elementType instanceof TupleType tupleType) {
            return tupleType.getHierarchies();
        }
        else { //MemberType
            ArrayList<Hierarchy> result = new ArrayList<>();
            result.add(this.getHierarchy());
            return result;
        }
    }

    @Override
	public Dimension getDimension() {
        return elementType == null
            ? null
            : elementType.getDimension();
    }

    @Override
	public Hierarchy getHierarchy() {
        return elementType == null
            ? null
            : elementType.getHierarchy();
    }

    @Override
	public Level getLevel() {
        return elementType == null
            ? null
            : elementType.getLevel();
    }

    @Override
	public int getArity() {
        return elementType.getArity();
    }

    @Override
	public Type computeCommonType(Type type, int[] conversionCount) {
        if (!(type instanceof SetType that)) {
            return null;
        }
        final Type mostGeneralElementType =
            this.getElementType().computeCommonType(
                that.getElementType(), conversionCount);
        if (mostGeneralElementType == null) {
            return null;
        }
        return new SetType(mostGeneralElementType);
    }

    @Override
	public boolean isInstance(Object value) {
        if (!(value instanceof List list)) {
            return false;
        }
        for (Object o : list) {
            if (!elementType.isInstance(o)) {
                return false;
            }
        }
        return true;
    }
}
