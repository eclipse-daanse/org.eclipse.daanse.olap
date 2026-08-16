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
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Whether a caller may open a catalog, which decides whether a rowset names it.
 * <p>
 * A catalog offered to a client is one the client will go on to ask about, so
 * naming one it cannot open turns the rowset into a promise the next request
 * breaks. Roles are intersected with the ones the catalog itself declares.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CatalogScopeTest {

    @Mock
    private Context<?> catalog;

    private static XmlaRequest named(String user, Set<String> roles) {
        Principal principal = () -> user;
        return new XmlaRequest(principal, roles, Map.of(), "http://localhost/xmla", "127.0.0.1");
    }

    @Test
    void aCatalogWithoutRolesIsOpenToEveryone() {
        when(catalog.getAccessRoles()).thenReturn(List.of());

        assertThat(DiscoverScope.mayOpen(catalog, named("someone", Set.of()))).isTrue();
    }

    @Test
    void aNamedCallerNeedsOneOfTheRolesTheCatalogDeclares() {
        when(catalog.getAccessRoles()).thenReturn(List.of("Administrator", "California manager"));

        assertThat(DiscoverScope.mayOpen(catalog, named("probe", Set.of("Administrator")))).isTrue();
        assertThat(DiscoverScope.mayOpen(catalog, named("probe", Set.of("Admin"))))
                .as("a role the catalog does not declare leaves the intersection empty").isFalse();
    }

    /**
     * An endpoint that authenticates nobody still shows its catalogs: there is
     * nothing to intersect, and filtering would hide the whole server.
     */
    @Test
    void aCallerNobodyNamedSeesEverything() {
        when(catalog.getAccessRoles()).thenReturn(List.of("Administrator"));

        assertThat(DiscoverScope.mayOpen(catalog, XmlaRequest.anonymous())).isTrue();
        assertThat(DiscoverScope.mayOpen(catalog, null)).isTrue();
    }
}
