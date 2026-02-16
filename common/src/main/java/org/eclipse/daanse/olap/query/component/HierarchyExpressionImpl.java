 /*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
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


package org.eclipse.daanse.olap.query.component;

import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.HierarchyCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.HierarchyExpression;
import org.eclipse.daanse.olap.api.query.component.visit.QueryComponentVisitor;
import org.eclipse.daanse.olap.api.type.HierarchyType;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.constant.ConstantHierarchyCalc;
import org.eclipse.daanse.olap.common.Util;
import org.eclipse.daanse.olap.query.component.expression.AbstractExpression;

/**
 * Usage of a {@link org.eclipse.daanse.olap.api.element.Hierarchy} as an MDX expression.
 *
 * @author jhyde
 * @since Sep 26, 2005
 */
public class HierarchyExpressionImpl extends AbstractExpression implements Expression, HierarchyExpression {
    private final Hierarchy hierarchy;

    /**
     * Creates a hierarchy expression.
     *
     * @param hierarchy Hierarchy
     *  hierarchy != null
     */
    public HierarchyExpressionImpl(Hierarchy hierarchy) {
        Util.assertPrecondition(hierarchy != null, "hierarchy != null");
        this.hierarchy = hierarchy;
    }

    /**
     * Returns the hierarchy.
     *
     *  return != null
     */
    @Override
    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    @Override
	public String toString() {
        return hierarchy.getUniqueName();
    }

    @Override
	public Type getType() {
        return HierarchyType.forHierarchy(hierarchy);
    }

    @Override
	public HierarchyExpressionImpl cloneExp() {
        return new HierarchyExpressionImpl(hierarchy);
    }

    @Override
	public DataType getCategory() {
        return DataType.HIERARCHY;
    }

    @Override
	public Expression accept(Validator validator) {
        return this;
    }

    @Override
	public HierarchyCalc accept(ExpressionCompiler compiler) {
        return ConstantHierarchyCalc.of(hierarchy);
    }

    @Override
	public Object accept(QueryComponentVisitor visitor) {
        return visitor.visitHierarchyExpression(this);
    }
}
