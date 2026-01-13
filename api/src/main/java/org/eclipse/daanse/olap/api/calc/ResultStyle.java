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

package org.eclipse.daanse.olap.api.calc;

import java.util.List;

/**
 * Enumeration of ways that a compiled expression can return its result to its
 * caller.
 *
 * @author jhyde
 */
public enum ResultStyle {
    /**
     * Indicates that caller will accept any applicable style.
     */
    ANY,

    /**
     * Indicates that the expression returns its result as a list which may safely
     * be modified by the caller.
     */
    MUTABLE_LIST,

    /**
     * Indicates that the expression returns its result as a list which must not be
     * modified by the caller.
     */
    LIST,

    /**
     * Indicates that the expression returns its result as an Iterable which must
     * not be modified by the caller.
     */
    ITERABLE,

    /**
     * Indicates that the expression results its result as an immutable value. This
     * is typical for expressions which return string, datetime and numeric values.
     */
    VALUE,

    /**
     * Indicates that the expression results its result as an immutable value which
     * will never be null. This is typical for expressions which return string,
     * datetime and numeric values.
     */
    VALUE_NOT_NULL;

    // ---------------------------------------------------------------
    // There follow a set of convenience constants for commonly-used
    // collections of result styles.

    public static final List<ResultStyle> ANY_LIST = List.of(ANY);

    public static final List<ResultStyle> ITERABLE_ONLY = List.of(ITERABLE);

    public static final List<ResultStyle> MUTABLELIST_ONLY = List.of(MUTABLE_LIST);

    public static final List<ResultStyle> LIST_ONLY = List.of(LIST);

    public static final List<ResultStyle> ITERABLE_ANY = List.of(ITERABLE, ANY);

    public static final List<ResultStyle> ITERABLE_LIST = List.of(ITERABLE, LIST);

    public static final List<ResultStyle> ITERABLE_MUTABLELIST = List.of(ITERABLE, MUTABLE_LIST);

    public static final List<ResultStyle> ITERABLE_LIST_MUTABLELIST = List.of(ITERABLE, LIST, MUTABLE_LIST);

    public static final List<ResultStyle> LIST_MUTABLELIST = List.of(LIST, MUTABLE_LIST);

    public static final List<ResultStyle> MUTABLELIST_LIST = List.of(MUTABLE_LIST, LIST);

    public static final List<ResultStyle> ITERABLE_LIST_MUTABLELIST_ANY = List.of(ITERABLE, LIST, MUTABLE_LIST, ANY);

    public static final List<ResultStyle> ITERABLE_MUTABLELIST_LIST = List.of(ITERABLE, MUTABLE_LIST, LIST);

    public static final List<ResultStyle> ANY_ONLY = List.of(ANY);
}
