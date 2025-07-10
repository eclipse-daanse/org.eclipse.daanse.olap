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

import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;

/**
 * The type of an expression which represents a Cube or Virtual Cube.
 *
 * @author jhyde
 * @since Feb 17, 2005
 */
public class CubeType implements Type {
    private final Cube cube;

    /**
     * Creates a type representing a cube.
     */
    public CubeType(Cube cube) {
        this.cube = cube;
    }

    /**
     * Returns the cube.
     *
     * @return Cube
     */
    public Cube getCube() {
        return cube;
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
	public Dimension getDimension() {
        return null;
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
	public int hashCode() {
        return cube.hashCode();
    }

    @Override
	public boolean equals(Object obj) {
        if (obj instanceof CubeType that) {
            return this.cube.equals(that.cube);
        } else {
            return false;
        }
    }

    @Override
	public Type computeCommonType(Type type, int[] conversionCount) {
        return this.equals(type)
            ? this
            : null;
    }

    @Override
	public boolean isInstance(Object value) {
        return value instanceof Cube;
    }

    @Override
	public int getArity() {
        // not meaningful; cube cannot be used in an expression
        throw new UnsupportedOperationException();
    }
}
