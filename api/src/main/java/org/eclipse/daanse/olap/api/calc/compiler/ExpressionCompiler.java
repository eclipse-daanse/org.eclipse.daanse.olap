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


package org.eclipse.daanse.olap.api.calc.compiler;

import java.util.List;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.Parameter;
import org.eclipse.daanse.olap.api.Validator;
import org.eclipse.daanse.olap.api.calc.BooleanCalc;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.DateTimeCalc;
import org.eclipse.daanse.olap.api.calc.DimensionCalc;
import org.eclipse.daanse.olap.api.calc.DoubleCalc;
import org.eclipse.daanse.olap.api.calc.HierarchyCalc;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.LevelCalc;
import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.calc.ResultStyle;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.calc.TupleCalc;
import org.eclipse.daanse.olap.api.calc.todo.TupleIteratorCalc;
import org.eclipse.daanse.olap.api.calc.todo.TupleListCalc;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.type.Type;

/**
 * Mediates the compilation of an expression ( org.eclipse.daanse.olap.api.query.component.Expression)
 * into a compiled expression ( Calc).
 *
 * @author jhyde
 * @since Sep 28, 2005
 */
public interface ExpressionCompiler {

    /**
     * Returns the evaluator to be used for evaluating expressions during the
     * compilation process.
     */
    Evaluator getEvaluator();

    /**
     * Returns the validator which was used to validate this expression.
     *
     * @return validator
     */
    Validator getValidator();

    /**
     * Compiles an expression.
     *
     * @param exp Expression
     * @return Compiled expression
     */
    Calc<?> compile(Expression exp);

    /**
     * Compiles an expression to a given result type.
     *
     * If resultType is not null, casts the expression to that
     * type. Throws an exception if that conversion is not allowed by the
     * type system.
     *
     * The preferredResultStyles parameter specifies a list
     * of desired result styles. It must not be null, but may be empty.
     *
     * @param exp Expression
     *
     * @param resultType Desired result type, or null to use expression's
     *                   current type
     *
     * @param preferredResultStyles List of result types, in descending order
     *                   of preference. Never null.
     *
     * @return Compiled expression, or null if none can satisfy
     */
    Calc compileAs(
            Expression exp,
            Type resultType,
            List<ResultStyle> preferredResultStyles);

    /**
     * Compiles an expression which yields a  Member result.
     */
    MemberCalc compileMember(Expression exp);

    /**
     * Compiles an expression which yields a  Level result.
     */
    LevelCalc compileLevel(Expression exp);

    /**
     * Compiles an expression which yields a  Dimension result.
     */
    DimensionCalc compileDimension(Expression exp);

    /**
     * Compiles an expression which yields a  Hierarchy result.
     */
    HierarchyCalc compileHierarchy(Expression exp);

    /**
     * Compiles an expression which yields an int result.
     * The expression is implicitly converted into a scalar.
     */
    IntegerCalc compileInteger(Expression exp);

    /**
     * Compiles an expression which yields a  String result.
     * The expression is implicitly converted into a scalar.
     */
    StringCalc compileString(Expression exp);

    /**
     * Compiles an expression which yields a  java.util.Date result.
     * The expression is implicitly converted into a scalar.
     */
    DateTimeCalc compileDateTime(Expression exp);

    /**
     * Compiles an expression which yields an immutable  TupleList
     * result.
     *
     * Always equivalent to  #compileList(exp, false).
     */
    TupleListCalc compileList(Expression exp);

    /**
     * Compiles an expression which yields  TupleList result.
     *
     * Such an expression is generally a list of  Member objects or a
     * list of tuples (each represented by a  Member array).
     *
     * See  #compileList(org.eclipse.daanse.olap.api.query.component.Expression).
     *
     * @param exp Expression
     * @param mutable Whether resulting list is mutable
     */
    TupleListCalc compileList(Expression exp, boolean mutable);

    /**
     * Compiles an expression which yields an immutable  Iterable result.
     *
     * @param exp Expression
     * @return Calculator which yields an Iterable
     */
    TupleIteratorCalc compileIter(Expression exp);

    /**
     * Compiles an expression which yields a boolean result.
     *
     * @param exp Expression
     * @return Calculator which yields a boolean
     */
    BooleanCalc compileBoolean(Expression exp);

    /**
     * Compiles an expression which yields a double result.
     *
     * @param exp Expression
     * @return Calculator which yields a double
     */
    DoubleCalc compileDouble(Expression exp);

    /**
     * Compiles an expression which yields a tuple result.
     *
     * @param exp Expression
     * @return Calculator which yields a tuple
     */
    TupleCalc compileTuple(Expression exp);

    /**
     * Compiles an expression to yield a scalar result.
     *
     * If the expression yields a member or tuple, the calculator will
     * automatically apply that member or tuple to the current dimensional
     * context and return the value of the current measure.
     *
     * @param exp Expression
     * @param specific Whether to try to use the specific compile method for
     *   scalar types. For example, if specific is true and
     *   exp is a string expression, calls
     *    #compileString(org.eclipse.daanse.olap.api.query.component.Expression)
     * @return Calculation which returns the scalar value of the expression
     */
    Calc<?> compileScalar(Expression exp, boolean specific);

    /**
     * Implements a parameter, returning a unique slot which will hold the
     * parameter's value.
     *
     * @param parameter Parameter
     * @return Slot
     */
    ParameterSlot registerParameter(Parameter parameter);

    /**
     * Returns a list of the  ResultStyles
     * acceptable to the caller.
     */
    List<ResultStyle> getAcceptableResultStyles();

}
