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

package org.eclipse.daanse.olap.util;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * Stack implementation based on {@link java.util.ArrayList}.
 *
 * More efficient than {@link java.util.Stack}, which extends
 * {@link java.util.Vector} and is
 * therefore synchronized whether you like it or not.
 *
 * @param <E> Element type
 *
 * @author jhyde
 */
public class ArrayStack<E> extends ArrayList<E> {
    /**
     * Default constructor.
     */
    public ArrayStack() {
        super();
    }

    /**
     * Copy Constructor
     * @param toCopy Instance of {@link ArrayStack} to copy.
     */
    public ArrayStack(ArrayStack<E> toCopy) {
        super();
        this.addAll(toCopy);
    }

    /**
     * Analogous to {@link java.util.Stack#push}.
     */
    public E push(E item) {
        add(item);
        return item;
    }

    /**
     * Analogous to {@link java.util.Stack#pop}.
     */
    public E pop() {
        int len = size();
        E obj = peek();
        remove(len - 1);
        return obj;
    }

    /**
     * Analogous to {@link java.util.Stack#peek}.
     */
    public E peek() {
        int len = size();
        if (len <= 0) {
            throw new EmptyStackException();
        }
        return get(len - 1);
    }
}
