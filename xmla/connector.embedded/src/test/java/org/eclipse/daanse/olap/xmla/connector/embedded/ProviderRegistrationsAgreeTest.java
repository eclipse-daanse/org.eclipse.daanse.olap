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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * The same mapping of request type to provider is written down three times, and
 * this is what checks that the three agree.
 * <ul>
 * <li>the {@code @Component} property on each provider — what OSGi actually
 * registers, and the only one that governs a running server;</li>
 * <li>{@code EmbeddedXmla.providers()} — what an embedded server registers,
 * where there is no OSGi to read the annotations;</li>
 * <li>{@code Providers} in this test tree — what the tests dispatch through.</li>
 * </ul>
 * Adding a rowset means touching all three, and forgetting one fails quietly:
 * an OSGi server answers the request while the embedded one faults, or the
 * tests pass over a provider nobody registers. There are 58 of them.
 * <p>
 * All three are read from source. The annotation has {@code CLASS} retention, so
 * it is invisible to reflection, and the component XML bnd derives from it is
 * written into the bundle at package time — after the tests have run. Reading
 * the text is what is left.
 * <p>
 * That the text says the same thing as the generated descriptors was checked
 * against a built bundle: 59 component XMLs, 57 of them carrying a request-type
 * property, and those 57 pairs equal to what the pattern below finds. If that
 * ever stops holding, this test is measuring the wrong thing and the comparison
 * is worth repeating.
 */
class ProviderRegistrationsAgreeTest {

    /** {@code property = RowsetProvider.PROPERTY_REQUEST_TYPE + "=DBSCHEMA_TABLES")} */
    private static final Pattern ANNOTATED = Pattern.compile("PROPERTY_REQUEST_TYPE\\s*\\+\\s*\"=(\\w+)\"");

    /** {@code providers.put("DBSCHEMA_TABLES", new DbschemaTablesProvider());} */
    private static final Pattern EMBEDDED = Pattern.compile("providers\\.put\\(\"(\\w+)\",\\s*new\\s+(\\w+)\\(\\)\\)");

    /** {@code BY_REQUEST_TYPE.put("DBSCHEMA_TABLES", DbschemaTablesProvider::new);} */
    private static final Pattern IN_TESTS = Pattern.compile("BY_REQUEST_TYPE\\.put\\(\"(\\w+)\",\\s*(\\w+)::new\\)");

    /**
     * The three families live in three bundles now, so the annotations are read
     * from three module directories. Relative to this module, as Maven runs tests.
     */
    private static final List<Path> PROVIDER_TREES = List.of(
            Path.of("../connector/src/main/java/org/eclipse/daanse/olap/xmla/connector/rowset/provider"),
            Path.of("../connector.relational/src/main/java/org/eclipse/daanse/olap/xmla/connector/rowset/provider"),
            Path.of("../connector.relational.oledb/src/main/java/org/eclipse/daanse/olap/xmla/connector/rowset/provider"),
            Path.of("../connector.multidimensional/src/main/java/org/eclipse/daanse/olap/xmla/connector/rowset/provider"));
    private static final Path EMBEDDED_XMLA = Path
            .of("src/main/java/org/eclipse/daanse/olap/xmla/connector/embedded/EmbeddedXmla.java");
    /**
     * Where the hand-bound session rowset is wired: the core's assembly entry
     * point, not the map this module supplies to it.
     */
    private static final Path ASSEMBLY = Path
            .of("../connector/src/main/java/org/eclipse/daanse/olap/xmla/connector/OlapXmlaConnector.java");

    private static final Path TEST_REGISTRY = Path
            .of("src/test/java/org/eclipse/daanse/olap/xmla/connector/embedded/Providers.java");

    private static String read(Path path) {
        try {
            assertThat(path).as("read relative to the module directory, as Maven runs tests").exists();
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Request type to provider class, taken from the {@code @Component} properties. */
    private static Map<String, String> annotated() {
        Map<String, String> found = new LinkedHashMap<>();
        for (Path root : PROVIDER_TREES) {
            try (Stream<Path> tree = Files.walk(root)) {
                tree.filter(path -> path.toString().endsWith("Provider.java")).sorted().forEach(path -> {
                    Matcher matcher = ANNOTATED.matcher(read(path));
                    if (matcher.find()) {
                        String className = path.getFileName().toString().replace(".java", "");
                        found.put(matcher.group(1), className);
                    }
                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return found;
    }

    private static Map<String, String> matches(Pattern pattern, Path source) {
        Map<String, String> found = new LinkedHashMap<>();
        Matcher matcher = pattern.matcher(read(source));
        while (matcher.find()) {
            found.put(matcher.group(1), matcher.group(2));
        }
        return found;
    }

    /**
     * The one provider not registered from the map: it reports the connector's own
     * sessions, so it is constructed with the connector and bound by hand.
     */
    private static final String BOUND_BY_HAND = "DISCOVER_SESSIONS";

    private static Map<String, String> annotatedExcept(String requestType) {
        Map<String, String> without = new LinkedHashMap<>(annotated());
        assertThat(without.remove(requestType)).as("%s is still an annotated provider", requestType).isNotNull();
        return without;
    }

    @Test
    void theEmbeddedServerRegistersWhatOsgiWould() {
        Map<String, String> osgi = annotated();

        assertThat(osgi).as("the annotations are the registration a running server uses").isNotEmpty();
        assertThat(read(ASSEMBLY)).as("%s is bound separately, not from the map", BOUND_BY_HAND)
                .contains("\"" + BOUND_BY_HAND + "\"");
        assertThat(matches(EMBEDDED, EMBEDDED_XMLA))
                .as("EmbeddedXmla must register every annotated provider, and no other")
                .containsExactlyInAnyOrderEntriesOf(annotatedExcept(BOUND_BY_HAND));
    }

    @Test
    void theTestsDispatchThroughWhatOsgiWould() {
        // DISCOVER_SESSIONS needs a session handler, so its own test builds it
        // directly rather than going through the registry.
        assertThat(matches(IN_TESTS, TEST_REGISTRY))
                .as("a provider the tests do not know is a provider the tests cannot cover")
                .containsExactlyInAnyOrderEntriesOf(annotatedExcept(BOUND_BY_HAND));
    }

    /**
     * Two providers claiming one request type is a registration OSGi resolves by
     * chance — the whiteboard keeps whichever service ranks higher, and nothing
     * says which that is.
     */
    @Test
    void noRequestTypeIsClaimedTwice() {
        Map<String, String> byType = annotated();
        long classes = 0;
        for (Path root : PROVIDER_TREES) {
            try (Stream<Path> tree = Files.walk(root)) {
                classes += tree.filter(path -> path.toString().endsWith("Provider.java")).count();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        assertThat(byType).as("one request type per provider class").hasSize((int) classes);
    }
}
