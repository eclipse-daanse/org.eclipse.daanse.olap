 /*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2016-2017 Hitachi Vantara.
 * All Rights Reserved.
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

package org.eclipse.daanse.olap.format;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.jupiter.api.Test;

class FormatterCreateContextTest {

	@Test
    void elementDataShouldSupersedeAttributeData() {

        FormatterCreateContext context1 =
            new FormatterCreateContext.Builder("elementName")
            .formatterDef("elementClassName")
            .formatterAttr("attributeClassName")
            .build();
        FormatterCreateContext context2 =
            new FormatterCreateContext.Builder("elementName")
                .formatterAttr("attributeClassName")
                .build();
        FormatterCreateContext context3 =
            new FormatterCreateContext.Builder("elementName")
                .formatterDef(null)
                .formatterAttr("attributeClassName")
                .build();

        assertThat(context1.getFormatterClassName()).isEqualTo("elementClassName");
        assertThat(context2.getFormatterClassName()).isEqualTo("attributeClassName");
        assertThat(context3.getFormatterClassName()).isEqualTo("attributeClassName");
    }
}
