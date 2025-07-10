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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.exception.OlapRuntimeException;



/**
 * Tuple type.
 *
 * @author jhyde
 * @since Feb 17, 2005
 */
public class TupleType implements Type {

    private final static String dupHierarchiesInTuple = "Tuple contains more than one member of hierarchy ''{0}''.";
    public final Type[] elementTypes;
    private final String digest;

    /**
     * Creates a type representing a tuple whose fields are the given types.
     *
     * @param elementTypes Array of types of the members in this tuple
     */
    public TupleType(Type[] elementTypes) {
        assert elementTypes != null;
        this.elementTypes = elementTypes.clone();

        final StringBuilder buf = new StringBuilder();
        buf.append("TupleType<");
        int k = 0;
        for (Type elementType : elementTypes) {
            if (k++ > 0) {
                buf.append(", ");
            }
            buf.append(elementType);
        }
        buf.append(">");
        digest = buf.toString();
    }

    @Override
	public String toString() {
        return digest;
    }

    @Override
	public boolean equals(Object obj) {
        if (obj instanceof TupleType that) {
            return Arrays.equals(this.elementTypes, that.elementTypes);
        } else {
            return false;
        }
    }

    @Override
	public int hashCode() {
        return digest.hashCode();
    }

    @Override
	public boolean usesDimension(Dimension dimension, boolean definitely) {
        for (Type elementType : elementTypes) {
            if (elementType.usesDimension(dimension, definitely)) {
                return true;
            }
        }
        return false;
    }

    @Override
	public boolean usesHierarchy(Hierarchy hierarchy, boolean definitely) {
        for (Type elementType : elementTypes) {
            if (elementType.usesHierarchy(hierarchy, definitely)) {
                return true;
            }
        }
        return false;
    }

    public List<Hierarchy> getHierarchies() {
        final List<Hierarchy> hierarchies =
            new ArrayList<>(elementTypes.length);
        for (Type elementType : elementTypes) {
            hierarchies.add(elementType.getHierarchy());
        }
        return hierarchies;
    }

    @Override
	public int getArity() {
        return elementTypes.length;
    }

    @Override
	public Dimension getDimension() {
        throw new UnsupportedOperationException();
    }

    @Override
	public Hierarchy getHierarchy() {
        throw new UnsupportedOperationException();
    }

    @Override
	public Level getLevel() {
        throw new UnsupportedOperationException();
    }

    public Type getValueType() {
        for (Type elementType : elementTypes) {
            if (elementType instanceof MemberType memberType) {
                Dimension dimension = memberType.getDimension();
                if (dimension != null && dimension.isMeasures()) {
                    return memberType.getValueType();
                }
            }
        }
        return ScalarType.INSTANCE;
    }

    @Override
	public Type computeCommonType(Type type, int[] conversionCount) {
        if (type instanceof ScalarType) {
            return getValueType().computeCommonType(type, conversionCount);
        }
        if (type instanceof MemberType) {
            return commonTupleType(
                new TupleType(new Type[]{type}),
                conversionCount);
        }
        if (!(type instanceof TupleType)) {
            return null;
        }
        return commonTupleType(type, conversionCount);
    }

    @Override
	public boolean isInstance(Object value) {
        if (!(value instanceof Object[] objects)) {
            return false;
        }
        if (objects.length != elementTypes.length) {
            return false;
        }
        for (int i = 0; i < objects.length; i++) {
            if (!elementTypes[i].isInstance(objects[i])) {
                return false;
            }
        }
        return true;
    }

    private Type commonTupleType(Type type, int[] conversionCount) {
        TupleType that = (TupleType) type;

        if (this.elementTypes.length < that.elementTypes.length) {
            return createCommonTupleType(that, conversionCount);
        }
        return that.createCommonTupleType(this, conversionCount);
    }

    private Type createCommonTupleType(TupleType that, int[] conversionCount) {
        final List<Type> elementTypes = new ArrayList<>();
        for (int i = 0; i < this.elementTypes.length; i++) {
            Type commonType = this.elementTypes[i].computeCommonType(
                that.elementTypes[i], conversionCount);
            elementTypes.add(commonType);
            if (commonType == null) {
                return null;
            }
        }
        if (elementTypes.size() < that.elementTypes.length) {
            for (int i = elementTypes.size();
                i < that.elementTypes.length; i++)
            {
                elementTypes.add(ScalarType.INSTANCE);
            }
        }
        return new TupleType(
            elementTypes.toArray(new Type[elementTypes.size()]));
    }

    /**
     * Checks that there are no duplicate dimensions in a list of member types.
     * If so, the member types will form a valid tuple type.
     * If not, throws  org.eclipse.daanse.olap.api.exception.OlapRuntimeException.
     *
     * @param memberTypes Array of member types
     */
    public static void checkHierarchies(MemberType[] memberTypes) {
        for (int i = 0; i < memberTypes.length; i++) {
            MemberType memberType = memberTypes[i];
            for (int j = 0; j < i; j++) {
                MemberType member1 = memberTypes[j];
                final Hierarchy hierarchy = memberType.getHierarchy();
                final Hierarchy hierarchy1 = member1.getHierarchy();
                if (hierarchy != null && hierarchy == hierarchy1) {
                    throw new OlapRuntimeException(MessageFormat.format(dupHierarchiesInTuple,
                        hierarchy.getUniqueName()));
                }
            }
        }
    }
}
