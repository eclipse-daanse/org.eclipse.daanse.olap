/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 1998-2005 Julian Hyde
 * Copyright (C) 2005-2017 Hitachi Vantara and others
 * All Rights Reserved.
 *
 * ---- All changes after Fork in 2023 ------------------------
 * 
 * Project: Eclipse daanse
 * 
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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


package org.eclipse.daanse.olap.query.component;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for <code>Cell Property<code>.
 *
 * @author Shishir
 * @since 08 May, 2007
 */
class CellPropertyTest{
    private CellPropertyImpl cellProperty;

    @BeforeEach
    protected void setUp() throws Exception {

        cellProperty = new CellPropertyImpl(IdImpl.toList("Format_String"));
    }

    @Test
    void isNameEquals() {
        assertThat(cellProperty.isNameEquals("Format_String")).isTrue();
    }

    @Test
    void isNameEqualsDoesCaseInsensitiveMatch() {
        assertThat(cellProperty.isNameEquals("format_string")).isTrue();
    }

    @Test
    void isNameEqualsParameterShouldNotBeQuoted() {
        assertThat(cellProperty.isNameEquals("[Format_String]")).isFalse();
    }

}
