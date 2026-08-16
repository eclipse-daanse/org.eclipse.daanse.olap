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

import java.util.List;
import java.util.Set;

import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverSessionsProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.daanse.xmla.api.SimpleSessionHandler;
import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.eclipse.daanse.xmla.api.XmlaSessionHandler;
import org.eclipse.daanse.xmla.api.auth.AuthenticatedIdentity;
import org.eclipse.daanse.xmla.api.auth.Claims;
import org.eclipse.daanse.xmla.api.auth.NamedPrincipal;
import org.eclipse.daanse.xmla.model.rowset.server.DiscoverSessionsRow;
import org.eclipse.daanse.xmla.model.xmla.Discover;
import org.eclipse.daanse.xmla.model.xmla.RequestTypeEnum;
import org.eclipse.daanse.xmla.model.xmla.XmlaFactory;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** What the rowset reports, and to whom. */
class DiscoverSessionsProviderTest {

    private XmlaSessionHandler sessions;
    private DiscoverSessionsProvider provider;

    private static AuthenticatedIdentity identity(String name) {
        return new AuthenticatedIdentity(new NamedPrincipal(name), Set.of(), Claims.none());
    }

    @BeforeEach
    void wire() {
        sessions = new SimpleSessionHandler() {
        };
        provider = new DiscoverSessionsProvider(sessions);
    }

    private List<EObject> rowsFor(XmlaRequest caller) {
        return rowsFor(provider, caller);
    }

    private static List<EObject> rowsFor(DiscoverSessionsProvider provider, XmlaRequest caller) {
        Discover request = XmlaFactory.eINSTANCE.createDiscover();
        request.setRequestType(RequestTypeEnum.DISCOVER_SESSIONS);
        return provider.rows(RowsetScope.of(request, caller, null, Set.of()));
    }

    private static DiscoverSessionsRow only(List<EObject> rows) {
        assertThat(rows).hasSize(1);
        return (DiscoverSessionsRow) rows.get(0);
    }

    @Test
    void aCallerSeesTheSessionItIsUsing() {
        String id = sessions.beginSession(XmlaRequest.anonymous()).orElseThrow();

        DiscoverSessionsRow row = only(rowsFor(XmlaRequest.anonymous().withSession(id)));

        assertThat(row.getSessionId()).isEqualTo(id);
        assertThat(row.getSessionStartTime()).isNotNull();
        assertThat(row.getSessionIdleTimeMs()).isNotNull();
        assertThat(row.getSessionCommandCount()).isZero();
        assertThat(row.getSessionStatus()).isZero();
    }

    @Test
    void anAuthenticatedCallerSeesItsOwnSessionsEvenWithoutCarryingOne() {
        String mine = sessions.beginSession(XmlaRequest.anonymous()).orElseThrow();
        sessions.bindIdentity(mine, identity("alice"));

        DiscoverSessionsRow row = only(rowsFor(XmlaRequest.anonymous().withIdentity(identity("alice"))));

        assertThat(row.getSessionId()).isEqualTo(mine);
        assertThat(row.getSessionUserName()).isEqualTo("alice");
    }

    @Test
    void itDoesNotReportSomebodyElsesSessions() {
        String hers = sessions.beginSession(XmlaRequest.anonymous()).orElseThrow();
        sessions.bindIdentity(hers, identity("alice"));

        assertThat(rowsFor(XmlaRequest.anonymous().withIdentity(identity("mallory")))).isEmpty();
        assertThat(rowsFor(XmlaRequest.anonymous())).isEmpty();
    }

    @Test
    void anEndedSessionIsGoneFromTheAnswer() {
        String id = sessions.beginSession(XmlaRequest.anonymous()).orElseThrow();
        XmlaRequest caller = XmlaRequest.anonymous().withSession(id);
        assertThat(rowsFor(caller)).hasSize(1);

        sessions.endSession(id, caller);

        assertThat(rowsFor(caller)).isEmpty();
    }

    @Test
    void aColumnThisServerDoesNotMeasureStaysUnset() {
        // Answering 0 would be a statement; the model allows the silence.
        String id = sessions.beginSession(XmlaRequest.anonymous()).orElseThrow();

        DiscoverSessionsRow row = only(rowsFor(XmlaRequest.anonymous().withSession(id)));

        assertThat(row.getSessionCpuTimeMs()).isNull();
        assertThat(row.getSessionUsedMemory()).isNull();
        assertThat(row.getSessionSpid()).isNull();
        assertThat(row.getSessionConnectionId()).isNull();
    }

    @Test
    void aCommandShowsUpInTheCountAndTheStatus() {
        String id = sessions.beginSession(XmlaRequest.anonymous()).orElseThrow();
        XmlaRequest caller = XmlaRequest.anonymous().withSession(id);
        sessions.session(id).orElseThrow().commandStarted();

        assertThat(only(rowsFor(caller)).getSessionStatus()).isEqualTo(1);

        sessions.session(id).orElseThrow().commandEnded();

        DiscoverSessionsRow row = only(rowsFor(caller));
        assertThat(row.getSessionStatus()).isZero();
        assertThat(row.getSessionCommandCount()).isEqualTo(1);
        assertThat(row.getSessionLastCommandEndTime()).isNotNull();
    }

    @Test
    void aStatelessEndpointReportsNoSessionsRatherThanNoRowset() {
        assertThat(rowsFor(new DiscoverSessionsProvider(), XmlaRequest.anonymous())).isEmpty();
    }
}
