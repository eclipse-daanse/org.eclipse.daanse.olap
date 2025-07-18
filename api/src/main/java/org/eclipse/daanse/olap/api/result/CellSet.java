/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.api.result;

import java.util.List;

import org.eclipse.daanse.olap.api.Statement;

public interface CellSet {

    CellSetMetaData getMetaData();

    List<CellSetAxis> getAxes();

    CellSetAxis getFilterAxis();

    Cell getCell(List<Integer> pos);

    Statement getStatement();

    void close();

    void execute();
}
