/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   dbulahov - initial
 */
package org.eclipse.daanse.olap.xmla.connector.discover.csdl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CsdlFormatStringsTest {

    @Test
    void namedNumberFormatsAreTheDotNetFormatsTheyStandFor() {
        assertThat(CsdlFormatStrings.of("Currency")).hasValue("\\$#,0.00;(\\$#,0.00);\\$#,0.00");
        assertThat(CsdlFormatStrings.of("currency")).hasValue("\\$#,0.00;(\\$#,0.00);\\$#,0.00");
        assertThat(CsdlFormatStrings.of("Standard")).hasValue("#,0.00");
        assertThat(CsdlFormatStrings.of("Percent")).hasValue("0.00%");
        assertThat(CsdlFormatStrings.of("Yes/No")).hasValue("\"Yes\";\"Yes\";\"No\"");
    }

    @Test
    void otherFormatsStay() {
        assertThat(CsdlFormatStrings.of("#,##0.00")).hasValue("#,##0.00");
        assertThat(CsdlFormatStrings.of("Short Date")).hasValue("Short Date");
    }

    @Test
    void generalNumberAndBlankAreNone() {
        assertThat(CsdlFormatStrings.of("General Number")).isEmpty();
        assertThat(CsdlFormatStrings.of(" ")).isEmpty();
        assertThat(CsdlFormatStrings.of(null)).isEmpty();
    }
}
