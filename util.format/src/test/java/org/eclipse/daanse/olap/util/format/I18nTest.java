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
 */
package org.eclipse.daanse.olap.util.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Locale;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test suite for internalization and localization.
 */
class I18nTest {

    @Nested
    class NumberAndCurrencyFormatting {

        @Test
        void spanishThousandsAndDecimalSeparators() {
            Locale spanish = Locale.of("es", "ES");
            Format numFormat = new Format("#,000.00", spanish);
            assertThat(numFormat.format(Double.valueOf(123456.789))).isEqualTo("123.456,79");
        }

        @Test
        void spanishCurrency() {
            Locale spanish = Locale.of("es", "ES");
            Format currencyFormat = new Format("Currency", spanish);
            assertThat(currencyFormat.format(Double.valueOf(1234567.789))).isEqualTo("1.234.567,79\u00A0\u20AC");
        }
    }

    @Nested
    class DateFormatting {

        private final LocalDateTime ldt = LocalDateTime.of(2005, 1, 22, 0, 0, 0);

        @Test
        void spanishMediumDate() {
            Locale spanish = Locale.of("es", "ES");
            Format dateFormat = new Format("Medium Date", spanish);
            assertThat(dateFormat.format(ldt)).isEqualTo("22-ene-05");
        }

        @Test
        void germanLongDate() {
            Locale german = Locale.of("de", "DE");
            Format dateFormat = new Format("Long Date", german);
            assertThat(dateFormat.format(ldt)).isEqualTo("Samstag, Januar 22, 2005");
        }
    }

    @Nested
    class AmPmFormatting {

        @ParameterizedTest(name = "hour {0} → {1}")
        @CsvSource({ "0, AM",
            "1, AM",
            "2, AM",
            "3, AM",
            "4, AM",
            "5, AM",
            "6, AM",
            "7, AM",
            "8, AM",
            "9, AM",
            "10, AM",
            "11, AM",
            "12, PM",
            "13, PM",
            "14, PM",
            "15, PM",
            "16, PM",
            "17, PM",
            "18, PM",
            "19, PM",
            "20, PM",
            "21, PM",
            "22, PM",
            "23, PM"
        })
        void ampmFormat(int hour, String expected) {
            Format f = new Format("h:nn AMPM", Locale.US);
            LocalDateTime ldt = LocalDateTime.of(2005, 1, 22, hour, 30, 0);
            assertThat(f.format(ldt)).endsWith(expected);
        }
    }
}
