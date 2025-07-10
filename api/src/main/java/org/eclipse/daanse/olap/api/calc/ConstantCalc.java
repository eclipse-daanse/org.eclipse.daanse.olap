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
package org.eclipse.daanse.olap.api.calc;

/**
 * Marker Interface for  Calc that returns the same value on each call of
 *  #evaluate(mondrian.olap.Evaluator) method.
 *  
 *  @param <E> parameter
 */
public interface ConstantCalc<E> extends Calc<E> {

}
