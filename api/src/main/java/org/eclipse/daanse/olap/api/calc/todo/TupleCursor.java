/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 * 
 * For more information please visit the Project: Hitachi Vantara - Mondrian
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


package org.eclipse.daanse.olap.api.calc.todo;

import java.util.List;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.element.Member;

/**
 * Cheap interface for iterating through the contents of a  TupleList.
 *
 * Stops short of the full  java.util.Iterator interface. If you want
 * that, see  TupleIterator.
 *
 * @author Julian Hyde
 */
public interface TupleCursor {
    void setContext(Evaluator evaluator);

    /**
     * Moves the iterator forward one position.
     *
     * Returns false only when end of data has been reached.
     *
     * Similar to calling the  java.util.Iterator methods
     *  java.util.Iterator#hasNext() followed by
     *  java.util.Iterator#next() but
     * does not construct an object, and is therefore cheaper.
     *
     * If you want to use an Iterator, see  TupleIterator.
     *
     * @return Whether was able to move forward a position
     */
    boolean forward();

    /**
     * Returns the tuple that this cursor is positioned on.
     *
     * This method never returns null, and may safely be called multiple
     * times (or not all) for each position in the iteration.
     *
     * Invalid to call this method when the cursor is has not been
     * positioned, for example, if  #forward() has not been called or
     * if the most recent call to {@code forward} returned {@code false}.
     *
     * @return Current tuple
     */
    List<Member> current();

    /**
     * Returns the number of members in each tuple.
     *
     * @return The number of members in each tuple
     */
    int getArity();

    Member member(int column);

    /**
     * Writes the member(s) of the next tuple to a given offset in an array.
     *
     * This method saves the overhead of a memory allocation when the
     * resulting tuple will be written immediately to an array. The effect of
     * {@code currentToArray(members, 0)} is the same as calling
     * {@code current().toArray(members)}.
     *
     * Before calling this method, you must position the iterator at a valid
     * position. Typically you would call hasNext followed by next; or forward.
     *
     * @param members Members
     * @param offset Offset in the array to write to
     */
    void currentToArray(Member[] members, int offset);
}
