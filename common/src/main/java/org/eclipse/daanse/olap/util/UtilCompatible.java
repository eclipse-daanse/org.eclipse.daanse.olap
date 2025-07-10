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

import java.sql.Statement;

import org.eclipse.daanse.olap.common.Util;


public interface UtilCompatible {



    /**
     * Cancels and closes a SQL Statement object. If errors are encountered,
     * they should be logged under {@link Util}.
     * @param stmt The statement to close.
     */
    void cancelStatement(Statement stmt);


    /**
     * As {@link java.util.Arrays#binarySearch(Object[], int, int, Object)}, but
     * available pre-JDK 1.6.
     */
    <T extends Comparable<T>> int binarySearch(T[] ts, int start, int end, T t);



}
