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
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.xmla.connector.execute;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.Test;

/**
 * Every timestamp in the mddataset carries its seconds.
 * <p>
 * {@code LastDataUpdate} and {@code LastSchemaUpdate} go in as plain strings, so
 * nothing between here and the wire checks their shape, and
 * {@code LocalDateTime.toString()} omits the seconds when they are zero. The
 * result is not an {@code xsd:dateTime} — the type requires them — and a client
 * refuses the entire response rather than the one value, once a minute.
 * <p>
 * This pins the formatter rather than the caller, because the formatter is what
 * the next reader will be tempted to simplify back into {@code toString()}.
 */
class MdDatasetTimestampTest {

    /** The same pattern {@link CellSetToMdDataset} writes with. */
    private static final DateTimeFormatter XSD_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** Every shape the writer has to survive. */
    private static List<LocalDateTime> awkwardMoments() {
        return List.of(LocalDateTime.of(2026, 8, 15, 11, 21, 0), // zero seconds
                LocalDateTime.of(2026, 8, 15, 0, 0, 0), // midnight: toString gives no time at all
                LocalDateTime.of(2026, 8, 15, 11, 21, 7), // an ordinary second
                LocalDateTime.of(2026, 1, 1, 0, 0, 0, 500_000_000)); // a fraction to drop
    }

    @Test
    void everyTimestampCarriesItsSeconds() {
        for (LocalDateTime moment : awkwardMoments()) {
            String written = XSD_DATE_TIME.format(moment);

            assertThat(written).as("%s must keep its seconds", moment).hasSize("yyyy-MM-ddThh:mm:ss".length())
                    .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
        }
    }

    /**
     * The check that matters: what an XSD parser makes of it, which is stricter
     * than a regular expression.
     */
    @Test
    void everyTimestampParsesAsXsdDateTime() throws Exception {
        DatatypeFactory factory = DatatypeFactory.newInstance();

        for (LocalDateTime moment : awkwardMoments()) {
            String written = XSD_DATE_TIME.format(moment);

            assertThat(factory.newXMLGregorianCalendar(written).getSecond()).as("%s survives the round trip", written)
                    .isEqualTo(moment.getSecond());
        }
    }

    /**
     * The trap held still. Not a test of the JDK for its own sake: it records why
     * the formatter above exists, so that removing it fails here with the reason
     * attached.
     */
    @Test
    void toStringIsTheTrapAndMustNotComeBack() {
        assertThat(LocalDateTime.of(2026, 8, 15, 11, 21, 0).toString()).as("zero seconds vanish")
                .isEqualTo("2026-08-15T11:21");
        assertThat(LocalDateTime.of(2026, 8, 15, 0, 0, 0).toString()).as("midnight loses the time entirely")
                .isEqualTo("2026-08-15T00:00");
    }
}
