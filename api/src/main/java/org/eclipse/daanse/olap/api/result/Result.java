/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2001-2005 Julian Hyde
 * Copyright (C) 2005-2017 Hitachi Vantara and others
 * All Rights Reserved.
 *
 * Contributors:
 *  SmartCity Jena - refactor, clean API
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


package org.eclipse.daanse.olap.api.result;

import java.io.PrintWriter;

import org.eclipse.daanse.olap.api.Connection;
import org.eclipse.daanse.olap.api.Execution;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.query.component.Query;

/**
 * A Result is the result of running an MDX query. See
 * Connection#execute}.
 *
 * @author jhyde
 * @since 6 August, 2001
 */
public interface Result {
    /** Returns the query which generated this result. */
    Query getQuery();
    /** Returns the non-slicer axes. */
    Axis[] getAxes();
    /** Returns the slicer axis. */
    Axis getSlicerAxis();
    /** Returns the cell at a given set of coordinates. For example, in a result
     * with 4 columns and 6 rows, the top-left cell has coordinates [0, 0],
     * and the bottom-right cell has coordinates [3, 5]. */
    Cell getCell(int[] pos);
    void print(PrintWriter pw);
    void close();

    Execution getExecution();

    Member[] getCellMembers(int[] coordinates);
}
