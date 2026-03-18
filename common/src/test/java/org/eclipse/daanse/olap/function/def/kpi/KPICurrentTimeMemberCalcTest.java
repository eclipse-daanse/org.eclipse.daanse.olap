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
package org.eclipse.daanse.olap.function.def.kpi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.catalog.CatalogReader;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.KPI;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.type.MemberType;
import org.eclipse.daanse.olap.common.ConfigConstants;
import org.eclipse.daanse.olap.fun.FunUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class KPICurrentTimeMemberCalcTest {

    private KPICurrentTimeMemberCalc kpiCurrentTimeMemberCalc;
    private StringCalc stringCalc;
    private Evaluator evaluator;
    private Cube cube;
    private KPI kpi;
    private Member expectedMember;
    private Catalog catalog;
    private Connection connection;
    private Context context;
    private CatalogReader catalogReader;

    @BeforeEach
    void setUp() {
        stringCalc = mock(StringCalc.class);
        evaluator = mock(Evaluator.class);
        cube = mock(Cube.class);
        kpi = mock(KPI.class);
        catalog = mock(Catalog.class);
        connection = mock(Connection.class);
        context = mock(Context.class);
        expectedMember = mock(Member.class);
        catalogReader = mock(CatalogReader.class);
        kpiCurrentTimeMemberCalc = new KPICurrentTimeMemberCalc(MemberType.Unknown, stringCalc);
        when(evaluator.getCube()).thenReturn(cube);
        when(cube.getCatalog()).thenReturn(catalog);
        when(catalog.getInternalConnection()).thenReturn(connection);
        when(connection.getContext()).thenReturn(context);
        when(context.getConfigValue(ConfigConstants.IGNORE_INVALID_MEMBERS, true, Boolean.class)).thenReturn(true);
        when(context.getConfigValue(ConfigConstants.IGNORE_INVALID_MEMBERS_DURING_QUERY, false, Boolean.class)).thenReturn(true);
        when(evaluator.getCatalogReader()).thenReturn(catalogReader);
        when(catalogReader.getCalculatedMember(any())).thenReturn(expectedMember);
    }

    @Test
    @DisplayName("Should return member when KPI is found")
    void shouldReturnMemberWhenKpiFound() {
        when(stringCalc.evaluate(evaluator)).thenReturn("TimeKpi");
        when(kpi.getName()).thenReturn("TimeKpi");
        when(kpi.getCurrentTimeMember()).thenReturn("[Time].[2024]");
        when(cube.getKPIs()).thenReturn((List) List.of(kpi));
        Member result = kpiCurrentTimeMemberCalc.evaluate(evaluator);
        assertThat(result).isSameAs(expectedMember);
    }

    @Test
    @DisplayName("Should throw exception when KPI is not found")
    void shouldThrowExceptionWhenKpiNotFound() {
        when(stringCalc.evaluate(evaluator)).thenReturn("NoSuchKpi");
        when(cube.getKPIs()).thenReturn((List) List.of());

        assertThatThrownBy(() -> kpiCurrentTimeMemberCalc.evaluate(evaluator))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("NoSuchKpi");
    }

    @Test
    @DisplayName("Should have MemberType as type")
    void shouldHaveMemberType() {
        assertThat(kpiCurrentTimeMemberCalc.getType()).isEqualTo(MemberType.Unknown);
    }
}
