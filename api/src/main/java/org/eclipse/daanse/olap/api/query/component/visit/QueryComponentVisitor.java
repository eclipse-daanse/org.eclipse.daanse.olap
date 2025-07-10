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
 */

package org.eclipse.daanse.olap.api.query.component.visit;

import org.eclipse.daanse.olap.api.query.component.DimensionExpression;
import org.eclipse.daanse.olap.api.query.component.Formula;
import org.eclipse.daanse.olap.api.query.component.HierarchyExpression;
import org.eclipse.daanse.olap.api.query.component.Id;
import org.eclipse.daanse.olap.api.query.component.LevelExpression;
import org.eclipse.daanse.olap.api.query.component.Literal;
import org.eclipse.daanse.olap.api.query.component.MemberExpression;
import org.eclipse.daanse.olap.api.query.component.NamedSetExpression;
import org.eclipse.daanse.olap.api.query.component.ParameterExpression;
import org.eclipse.daanse.olap.api.query.component.Query;
import org.eclipse.daanse.olap.api.query.component.QueryAxis;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.api.query.component.UnresolvedFunCall;

/**
 * Visitor to an Tree of QueryComponents that must implement
 *  Visitee.
 *
 */
public interface QueryComponentVisitor {
	/**
	 * Indicates that  Visitee must also call
	 *  QueryComponentVisitee#accept(QueryComponentVisitor)a on existing
	 * children
	 */
	boolean visitChildren();

	/**
	 * Visits a Query.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitQuery(Query query);

	/**
	 * Visits a QueryAxis.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitQueryAxis(QueryAxis queryAxis);

	/**
	 * Visits a Formula.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitFormula(Formula formula);

	/**
	 * Visits an UnresolvedFunCall.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 * 
	 */
	Object visitUnresolvedFunCall(UnresolvedFunCall call);

	/**
	 * Visits a ResolvedFunCall.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitResolvedFunCall(ResolvedFunCall call);

	/**
	 * Visits an Id.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitId(Id id);

	/**
	 * Visits a Parameter.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitParameterExpression(ParameterExpression parameterExpr);

	/**
	 * Visits a DimensionExpr.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitDimensionExpression(DimensionExpression dimensionExpr);

	/**
	 * Visits a HierarchyExpr.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitHierarchyExpression(HierarchyExpression hierarchyExpr);

	/**
	 * Visits a LevelExpr.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor) }
	 */
	Object visitLevelExpression(LevelExpression levelExpr);

	/**
	 * Visits a MemberExpr.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitMemberExpression(MemberExpression memberExpr);

	/**
	 * Visits a NamedSetExpr.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitNamedSetExpression(NamedSetExpression namedSetExpr);

	/**
	 * Visits a Literal.
	 *
	 *  QueryComponentVisitor#accept(QueryComponentVisitor)
	 */
	Object visitLiteral(Literal<?> literal);
}
