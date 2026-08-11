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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.eclipse.daanse.olap.api.result.Scenario;
import org.eclipse.daanse.xmla.api.XmlaRefusedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Where a writeback transaction's pending values live, and how long. */
class SessionScenariosTest {

    private SessionScenarios scenarios;
    private Scenario scenario;

    @BeforeEach
    void wire() {
        scenarios = new SessionScenarios();
        scenario = mock(Scenario.class);
    }

    @Test
    void aTransactionOnASessionIsFoundAgainOnTheNextRequest() {
        scenarios.begin("a-session", scenario);

        assertThat(scenarios.of("a-session")).isSameAs(scenario);
        assertThat(scenarios.require("a-session")).isSameAs(scenario);
    }

    @Test
    void aSessionWithNoTransactionHasNoScenario() {
        assertThat(scenarios.of("a-session")).isNull();
    }

    @Test
    void aTransactionWithoutASessionIsRefused() {
        // It used to be accepted and filed under a null key, so every caller without a
        // session accumulated into the same scenario as every other.
        assertThatThrownBy(() -> scenarios.begin(null, scenario)).isInstanceOf(XmlaRefusedException.class);
        assertThatThrownBy(() -> scenarios.require(null)).isInstanceOf(XmlaRefusedException.class);
    }

    @Test
    void askingForATransactionThatWasNeverOpenedIsRefused() {
        // The predecessor threw a bare RuntimeException here, which reached the client
        // as a server error rather than as the mistake it is.
        assertThatThrownBy(() -> scenarios.require("a-session")).isInstanceOf(XmlaRefusedException.class);
    }

    @Test
    void endingATransactionKeepsTheSessionAndDropsTheValues() {
        scenarios.begin("a-session", scenario);

        scenarios.clear("a-session");

        assertThat(scenarios.of("a-session")).isNull();
    }

    @Test
    void oneSessionsValuesAreNotAnothers() {
        Scenario hers = mock(Scenario.class);
        scenarios.begin("mine", scenario);
        scenarios.begin("hers", hers);

        assertThat(scenarios.of("mine")).isSameAs(scenario);
        assertThat(scenarios.of("hers")).isSameAs(hers);
    }

    @Test
    void clearingSomethingThatIsNotThereIsNotAFailure() {
        scenarios.clear("never-opened");
        scenarios.clear(null);
    }
}
