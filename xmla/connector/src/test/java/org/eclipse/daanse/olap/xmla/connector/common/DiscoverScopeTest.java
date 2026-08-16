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
package org.eclipse.daanse.olap.xmla.connector.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** The two rules every discover implementation leans on. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiscoverScopeTest {

    @Mock
    private ContextListSupplyer contexts;
    @Mock
    private Catalog named;
    @Mock
    private Catalog other;

    private final XmlaRequest anonymous = XmlaRequest.anonymous();

    @Test
    void withoutARestrictionEveryCatalogTheCallerMaySeeIsInScope() {
        when(contexts.get(any())).thenReturn(List.of(named, other));

        assertThat(DiscoverScope.catalogs(contexts, Optional.empty(), anonymous)).containsExactly(named, other);
    }

    @Test
    void aNamedCatalogNarrowsTheScopeToIt() {
        when(contexts.tryGetFirstByName(any(), any())).thenReturn(Optional.of(named));

        assertThat(DiscoverScope.catalogs(contexts, Optional.of("foo"), anonymous)).containsExactly(named);
    }

    /**
     * The case worth stating: a restriction naming something the server does not
     * have must narrow to nothing. Falling back to every catalog would turn a
     * filter into its opposite — and a caller who may not see a catalog would be
     * shown it.
     */
    @Test
    void aNamedCatalogTheServerDoesNotHaveYieldsNothingRatherThanEverything() {
        when(contexts.tryGetFirstByName(any(), any())).thenReturn(Optional.empty());
        when(contexts.get(any())).thenReturn(List.of(named, other));

        assertThat(DiscoverScope.catalogs(contexts, Optional.of("absent"), anonymous)).isEmpty();
    }

    @Test
    void aHierarchyNamedLikeItsDimensionStandsAlone() {
        assertThat(DiscoverScope.hierarchyName("Time", "Time")).isEqualTo("Time");
    }

    @Test
    void anyOtherHierarchyIsQualifiedByItsDimension() {
        assertThat(DiscoverScope.hierarchyName("Weekly", "Time")).isEqualTo("Time.Weekly");
    }
}
