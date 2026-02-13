/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2002-2017 Hitachi Vantara and others
 * All Rights Reserved.
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
package org.eclipse.daanse.olap.calc.base.type.tuplebase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.daanse.olap.api.calc.tuple.TupleList;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link TupleList} and common implementations.
 *
 * @author jhyde
 */
class TupleListTest {

    @Test
    void testTupleList() {
        assertTrue(TupleCollections.createList(1) instanceof UnaryTupleList);
        assertTrue(TupleCollections.createList(2) instanceof ArrayTupleList);
    }
}
