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

import org.eclipse.daanse.xmla.model.xmla.PropertyList;
import org.eclipse.daanse.xmla.model.xmla.XmlaFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * The Content property is one switch with two halves — the schema and the rows
 * — and only the schema half was wired. {@code Content=Schema} therefore
 * answered with every row: the expensive answer to a request that asked for the
 * cheap one. ADOMD sets it for the duration of a schema lookup and for
 * {@code CommandBehavior.SchemaOnly}, so it is not a hypothetical value.
 */
class ContentPropertyTest {

    private static PropertyList withContent(String content) {
        PropertyList properties = XmlaFactory.eINSTANCE.createPropertyList();
        if (content != null) {
            properties.setContent(content);
        }
        return properties;
    }

    @ParameterizedTest(name = "Content={0} → schema {1}, rows {2}")
    @CsvSource({ "SchemaData, true,  true", "Schema,     true,  false", "Data,       false, true",
            "Metadata,   true,  false", "None,       false, false" })
    void theFiveContentValues(String content, boolean schema, boolean rows) {
        PropertyList properties = withContent(content);

        assertThat(OlapExecute.schemaIncluded(properties)).as("schema").isEqualTo(schema);
        assertThat(OlapExecute.dataIncluded(properties)).as("rows").isEqualTo(rows);
    }

    @Test
    void anAbsentContentMeansEverything() {
        assertThat(OlapExecute.schemaIncluded(null)).isTrue();
        assertThat(OlapExecute.dataIncluded(null)).isTrue();
        assertThat(OlapExecute.schemaIncluded(withContent(null))).isTrue();
        assertThat(OlapExecute.dataIncluded(withContent(null))).isTrue();
    }

    /** Clients have been seen to send it lower case; the comparison ignores case. */
    @Test
    void theComparisonIgnoresCase() {
        assertThat(OlapExecute.dataIncluded(withContent("schema"))).isFalse();
        assertThat(OlapExecute.schemaIncluded(withContent("data"))).isFalse();
    }
}
