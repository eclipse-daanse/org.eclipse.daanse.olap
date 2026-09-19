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
package org.eclipse.daanse.olap.xmla.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.ContextGroup;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.connection.ConnectionProps;
import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The bridge's {@code ContextsSupplyerImplTest}, and which roles a connection is
 * opened with: always the caller's own, with or without a session.
 */
@ExtendWith(MockitoExtension.class)
class ContextsSupplyerImplTest {

    private static final String CATALOG = "Sales";

    @Mock
    private ContextGroup contextGroup;

    @Test
    void anEmptyGroupAnswersAnEmptyList() {
        when(contextGroup.getValidContexts()).thenReturn(List.of());
        ContextsSupplyerImpl supplyer = new ContextsSupplyerImpl(contextGroup);
        assertThat(supplyer.getContexts()).isNotNull().isEmpty();
    }

    @Test
    void withoutASessionTheConnectionIsOpenedWithTheCallersRoles() {
        Context<?> context = context("reader", "admin");
        ContextsSupplyerImpl supplyer = supplyerOver(context);

        supplyer.getConnection(caller("reader"), CATALOG);

        assertThat(rolesOpenedWith(context, 1)).containsExactly(List.of("reader"));
    }

    @Test
    void aRoleTheCatalogDoesNotDefineIsNotPassedOn() {
        Context<?> context = context("reader", "admin");
        ContextsSupplyerImpl supplyer = supplyerOver(context);

        supplyer.getConnection(caller("reader", "somewhere-else"), CATALOG);

        assertThat(rolesOpenedWith(context, 1)).containsExactly(List.of("reader"));
    }

    @Test
    void aSessionThisSupplyerDoesNotKnowChangesNothingAboutTheRoles() {
        Context<?> context = context("reader", "admin");
        ContextsSupplyerImpl supplyer = supplyerOver(context);

        supplyer.getConnection(caller("reader").withSession("never-begun"), CATALOG);

        assertThat(rolesOpenedWith(context, 1)).containsExactly(List.of("reader"));
        assertThat(supplyer.getSessionCache()).isEmpty();
    }

    @Test
    void aMissingCallerIsRefusedAndOpensNothing() {
        Context<?> context = context("reader", "admin");
        ContextsSupplyerImpl supplyer = supplyerOver(context);

        assertThatThrownBy(() -> supplyer.getConnection(null, CATALOG)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> supplyer.get(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> supplyer.tryGetFirstByName(CATALOG, null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(context, never()).getConnection(any(ConnectionProps.class));
    }

    @Test
    void aSessionKeepsTheConnectionItOpened() {
        Context<?> context = context("reader", "admin");
        ContextsSupplyerImpl supplyer = supplyerOver(context);
        begin(supplyer, "s1");
        XmlaRequest caller = caller("reader").withSession("s1");

        Connection first = supplyer.getConnection(caller, CATALOG);
        Connection second = supplyer.getConnection(caller, CATALOG);

        assertThat(second).isSameAs(first);
        verify(context, times(1)).getConnection(any(ConnectionProps.class));
    }

    @Test
    void aSessionDoesNotServeAConnectionOpenedForOtherRoles() {
        Context<?> context = context("reader", "admin");
        ContextsSupplyerImpl supplyer = supplyerOver(context);
        begin(supplyer, "s1");

        Connection asAdmin = supplyer.getConnection(caller("admin").withSession("s1"), CATALOG);
        Connection asReader = supplyer.getConnection(caller("reader").withSession("s1"), CATALOG);

        assertThat(asReader).isNotSameAs(asAdmin);
        assertThat(rolesOpenedWith(context, 2)).containsExactly(List.of("admin"), List.of("reader"));
        // Both stay with the session, which is what closes them when it ends.
        assertThat(supplyer.getSessionCache().get("s1").values()).containsExactlyInAnyOrder(asAdmin, asReader);
    }

    @Test
    void anUnknownCatalogIsEmptyWhenAskedForAndAnErrorWhenDemanded() {
        ContextsSupplyerImpl supplyer = supplyerOver(context("reader"));

        assertThat(supplyer.tryGetFirstByName("not-here", caller("reader"))).isEmpty();
        assertThatThrownBy(() -> supplyer.getConnection(caller("reader"), "not-here"))
                .isInstanceOf(RuntimeException.class);
    }

    private ContextsSupplyerImpl supplyerOver(Context<?> context) {
        when(contextGroup.getValidContexts()).thenReturn(List.of(context));
        return new ContextsSupplyerImpl(contextGroup);
    }

    /** What {@code OlapXmlaConnector.onBeginSession} does. */
    private static void begin(ContextsSupplyerImpl supplyer, String sessionId) {
        supplyer.getSessionCache().put(sessionId, new ConcurrentHashMap<>());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Context<?> context(String... accessRoles) {
        Context context = mock(Context.class);
        lenient().when(context.getName()).thenReturn(CATALOG);
        lenient().when(context.getAccessRoles()).thenReturn(List.of(accessRoles));
        lenient().when(context.getConnection(any(ConnectionProps.class)))
                .thenAnswer(invocation -> mock(Connection.class));
        return context;
    }

    private static XmlaRequest caller(String... roles) {
        return new XmlaRequest(() -> "someone", Set.of(roles), Map.of(), "http://localhost/xmla", "127.0.0.1");
    }

    private static List<List<String>> rolesOpenedWith(Context<?> context, int times) {
        ArgumentCaptor<ConnectionProps> props = ArgumentCaptor.forClass(ConnectionProps.class);
        verify(context, times(times)).getConnection(props.capture());
        return props.getAllValues().stream().map(ConnectionProps::roles).toList();
    }
}
