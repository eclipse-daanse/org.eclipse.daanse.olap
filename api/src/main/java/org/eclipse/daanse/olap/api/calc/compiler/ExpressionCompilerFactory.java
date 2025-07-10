/*
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/

package org.eclipse.daanse.olap.api.calc.compiler;

import java.util.List;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.Validator;
import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The  ExpressionCompilerFactory create a new  ExpressionCompiler
 * instance, each call of a
 *  #createExpressionCompiler(Evaluator, Validator) or
 *  #createExpressionCompiler(Evaluator, Validator, List)
 */
public interface ExpressionCompilerFactory {

	/**
	 * Create a new  ExpressionCompiler instance, each call.
	 *
	 * @param evaluator the  Evaluator that must be used from the
	 *                   ExpressionCompiler
	 * @param validator the  Validator that must be used from the
	 *                   ExpressionCompiler
	 * @return the new  ExpressionCompiler
	 */
	default ExpressionCompiler createExpressionCompiler(final Evaluator evaluator, final Validator validator) {
		return createExpressionCompiler(evaluator, validator, ResultStyle.ANY_LIST);
	}

	/**
	 * Create a new  ExpressionCompiler} instance, each call.
	 *
	 * @param evaluator    the  Evaluator that must be used from the
	 *                      ExpressionCompiler
	 * @param validator    the  Validator} that must be used from the
	 *                      ExpressionCompiler
	 * @param resultStyles the initial  ResultStyle array for the
	 *                      ExpressionCompiler
	 * @return the new  ExpressionCompiler
	 */
	ExpressionCompiler createExpressionCompiler(final Evaluator evaluator, final Validator validator,
			final List<ResultStyle> resultStyles);

}