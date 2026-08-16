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
package org.eclipse.daanse.olap.xmla.connector.embedded;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Where the relational family is cut in two, and why.
 * <p>
 * One bundle answers with rows, the other advertises and answers empty. What
 * sorts a provider is whether anything implements it — not where the rowset
 * comes from. The point of the cut is that the empty half costs nothing to
 * leave out: it holds no implementation and depends on nothing but the API. A
 * provider that grows an implementation has to move, and this is what says so.
 * <p>
 * Four of the seven implemented ones — CATALOGS, COLUMNS, PROVIDER_TYPES and
 * TABLES — are the only DBSCHEMA rowsets a recorded Analysis Services
 * advertises among its seventy, and the only four [MS-SSAS] names. The
 * remaining three are OLE DB rowsets filled because the catalog knows the
 * answer.
 */
class RelationalSplitTest {

    private static final Path IMPLEMENTED = Path.of(
            "../connector.relational/src/main/java/org/eclipse/daanse/olap/xmla/connector/rowset/provider/relational");
    private static final Path EMPTY = Path.of(
            "../connector.relational.oledb/src/main/java/org/eclipse/daanse/olap/xmla/connector/rowset/provider/relational/oledb");

    /** The one call that tells a filled provider from an empty one. */
    private static final String IMPLEMENTATION = "DbSchemaDiscover";

    private static List<Path> sources(Path root) {
        try (Stream<Path> tree = Files.walk(root)) {
            return tree.filter(path -> path.toString().endsWith("Provider.java")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void everyProviderInTheEmptyBundleIsActuallyEmpty() {
        List<Path> empty = sources(EMPTY);

        assertThat(empty).isNotEmpty();
        for (Path path : empty) {
            assertThat(read(path)).as("%s answers empty, so it must not reach for an implementation",
                    path.getFileName()).doesNotContain(IMPLEMENTATION);
        }
    }

    @Test
    void everyProviderInTheOtherBundleAnswersWithRows() {
        List<Path> implemented = sources(IMPLEMENTED);

        assertThat(implemented).isNotEmpty();
        for (Path path : implemented) {
            assertThat(read(path)).as("%s sits with the implemented ones, so it must use one", path.getFileName())
                    .contains(IMPLEMENTATION);
        }
    }

    /**
     * The empty bundle exists to be omitted. That only holds while it depends on
     * nothing an omission would take away — so it must not import from the bundle
     * beside it.
     */
    @Test
    void theEmptyBundleLeansOnNothingItWouldTakeWithIt() {
        for (Path path : sources(EMPTY)) {
            assertThat(read(path)).as("%s must not depend on the implemented half", path.getFileName())
                    .doesNotContain("connector.relational.DbSchemaDiscover")
                    .doesNotContain("import org.eclipse.daanse.olap.xmla.connector.relational.");
        }
    }
}
