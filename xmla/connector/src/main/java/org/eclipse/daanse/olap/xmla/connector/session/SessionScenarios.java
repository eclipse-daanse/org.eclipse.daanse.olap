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
package org.eclipse.daanse.olap.xmla.connector.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.daanse.olap.api.result.Scenario;
import org.eclipse.daanse.xmla.api.XmlaRefusedException;

/**
 * The pending writeback values of each session.
 * <p>
 * A writeback transaction spans requests - {@code BEGIN}, some {@code UPDATE}s,
 * then {@code COMMIT} or {@code ROLLBACK} - so the scenario holding those
 * values has to outlive one request. Binding it to the session is what makes
 * ending a session release it along with everything else the session held.
 * <p>
 * <strong>That binding is this project's decision, not the protocol's.</strong>
 * A session and a scenario are two different things. The session is XMLA's
 * ([MS-SSAS] 3.1.3), and the only state the specification calls session-scoped
 * is metadata - calculated members, named sets, KPIs, session cubes - never
 * data. The scenario is the OLAP engine's, from Mondrian; XMLA has no such
 * concept. Where the specification names a handle for pending writeback at all
 * it is the {@code ResultId} of the
 * {@code KeepResult}/{@code Result}/{@code ClearResult} headers
 * (3.1.4.3.2.1.1.26), and it never ties that handle's lifetime to a session -
 * it says only that it lasts until the client sends {@code ClearResult}.
 * <p>
 * So the protocol leaves this open, and the session is the only bracket on
 * offer. A deployment that wants the two decoupled - a scenario that survives
 * its session, or several per session - is not fighting the specification, only
 * this choice.
 */
public final class SessionScenarios {

    private final Map<String, Scenario> held = new ConcurrentHashMap<>();

    /** The scenario this session is accumulating into, or {@code null} for none. */
    public Scenario of(String sessionId) {
        return sessionId == null ? null : held.get(sessionId);
    }

    /**
     * Opens a transaction on this session.
     *
     * @throws XmlaRefusedException without a session - the values would have
     *                              nowhere to live until the commit, and every
     *                              caller without one would be accumulating into
     *                              the same place
     */
    public void begin(String sessionId, Scenario scenario) {
        held.put(requireSession(sessionId), scenario);
    }

    /**
     * The scenario of an open transaction.
     *
     * @throws XmlaRefusedException when there is no session, or no transaction open
     *                              on it
     */
    public Scenario require(String sessionId) {
        Scenario scenario = held.get(requireSession(sessionId));
        if (scenario == null) {
            throw new XmlaRefusedException(XmlaRefusedException.Side.CLIENT,
                    "no writeback transaction is open on this session");
        }
        return scenario;
    }

    /** Ends the transaction, keeping the session itself. */
    public void clear(String sessionId) {
        if (sessionId != null) {
            held.remove(sessionId);
        }
    }

    private static String requireSession(String sessionId) {
        if (sessionId == null) {
            throw new XmlaRefusedException(XmlaRefusedException.Side.CLIENT,
                    "a writeback transaction needs a session; send BeginSession first");
        }
        return sessionId;
    }
}
