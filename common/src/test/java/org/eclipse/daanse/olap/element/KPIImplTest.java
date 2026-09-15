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
package org.eclipse.daanse.olap.element;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KPIImplTest {

    @Test
    void beanRoundTripAndParentLink() {
        KPIImpl parent = new KPIImpl();
        parent.setName("Revenue");

        KPIImpl kpi = new KPIImpl();
        kpi.setName("Margin");
        kpi.setDescription("gross margin");
        kpi.setDisplayFolder("Finance");
        kpi.setValue("[Measures].[Margin]");
        kpi.setGoal("0.3");
        kpi.setStatus("[Measures].[Margin Status]");
        kpi.setTrend("[Measures].[Margin Trend]");
        kpi.setWeight("1");
        kpi.setCurrentTimeMember("[Time].[2026]");
        kpi.setStatusGraphic("Shapes");
        kpi.setTrendGraphic("Standard Arrow");
        kpi.setParentKpi(parent);

        assertThat(kpi.getName()).isEqualTo("Margin");
        assertThat(kpi.getDescription()).isEqualTo("gross margin");
        assertThat(kpi.getDisplayFolder()).isEqualTo("Finance");
        assertThat(kpi.getValue()).isEqualTo("[Measures].[Margin]");
        assertThat(kpi.getGoal()).isEqualTo("0.3");
        assertThat(kpi.getStatus()).isEqualTo("[Measures].[Margin Status]");
        assertThat(kpi.getTrend()).isEqualTo("[Measures].[Margin Trend]");
        assertThat(kpi.getWeight()).isEqualTo("1");
        assertThat(kpi.getCurrentTimeMember()).isEqualTo("[Time].[2026]");
        assertThat(kpi.getStatusGraphic()).isEqualTo("Shapes");
        assertThat(kpi.getTrendGraphic()).isEqualTo("Standard Arrow");
        assertThat(kpi.getParentKpi()).isSameAs(parent);
        assertThat(parent.getParentKpi()).isNull();
    }
}
