/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.function.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.tuple.TupleList;
import org.eclipse.daanse.olap.api.calc.tuple.TupleListCalc;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.query.component.Query;
import org.eclipse.daanse.olap.api.result.Result;
import org.eclipse.daanse.olap.function.TestCatalogs;
import org.eclipse.daanse.olap.provider.memory.MemoryContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * An MDX expression parsed, validated, compiled and evaluated end to end against the test
 * catalog, with no database anywhere.
 *
 * <p>This is the path the function contracts use for their dependency and result-shape
 * promises. It runs here already, which is why switching the contracts over is now a
 * question of migrating their expressions rather than of missing machinery.
 */
class MemoryEvaluationTest {

    private Connection connection;

    @BeforeEach
    void connect() {
        MemoryContext context = TestCatalogs.context();
        connection = context.getConnectionWithDefaultRole();
    }

    @Test
    @DisplayName("a query parses and validates against the catalog")
    void queryParsesAndValidates() {
        Query query = connection.parseQuery(
                "SELECT {[Geo].[All Geo].[North], [Geo].[All Geo].[South]} ON COLUMNS FROM [Test]");
        query.resolve();

        assertThat(query.getCube().getName()).isEqualTo("Test");
        assertThat(query.getAxes()).hasSize(1);
    }

    @Test
    @DisplayName("a set function compiles and evaluates to the members one can name by hand")
    void setFunctionEvaluates() {
        TupleList result = evaluateSet("Head([Geo].[Region].Members, 2)");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).get(0).getName()).isEqualTo("North");
        assertThat(result.get(1).get(0).getName()).isEqualTo("South");
    }

    @Test
    @DisplayName("children of a member come back in the order they were declared")
    void childrenEvaluate() {
        TupleList result = evaluateSet("[Geo].[All Geo].[South].Children");

        assertThat(result).hasSize(3);
        assertThat(result.stream().map(t -> t.get(0).getName()).toList())
                .containsExactly("C", "D", "E");
    }

    @Test
    @DisplayName("a time hierarchy answers its own navigation")
    void timeHierarchyEvaluates() {
        // Written in full: resolving [Time].[2023] by skipping the All member is a
        // convenience of the name resolver and a separate question from evaluation.
        TupleList result = evaluateSet("[Time].[All Time].[2023].Children");

        assertThat(result.stream().map(t -> t.get(0).getName()).toList())
                .containsExactly("Q1", "Q2", "Q3", "Q4");
    }

    private TupleList evaluateSet(String setExpression) {
        Query query = connection.parseQuery(
                "SELECT " + setExpression + " ON COLUMNS FROM [" + TestCatalogs.CUBE_NAME + "]");
        query.resolve();

        Calc<?> calc = query.compileExpression(query.getAxes()[0].getSet(), false, null);
        Evaluator evaluator = connection.getContext().createEvaluator(query.getStatement());
        return ((TupleListCalc) calc).evaluate(evaluator);
    }

    @Test
    @DisplayName("a whole query runs and its cells carry the values one can add up by hand")
    void wholeQueryRuns() {
        Result result = execute(
                "SELECT {[Geo].[All Geo].[North], [Geo].[All Geo].[South]} ON COLUMNS"
                        + " FROM [Test] WHERE [Measures].[Amount]");

        assertThat(result.getAxes()).hasSize(1);
        assertThat(result.getAxes()[0].getPositions()).hasSize(2);

        assertThat(number(result, 0)).isEqualTo(30L);
        assertThat(number(result, 1)).isEqualTo(120L);
    }

    @Test
    @DisplayName("a cell nobody wrote a value for is empty, and says so")
    void emptyCellIsEmpty() {
        Result result = execute(
                "SELECT {[Geo].[All Geo].[South].[C]} ON COLUMNS"
                        + " FROM [Test] WHERE [Measures].[Gaps]");

        assertThat(result.getCell(new int[] { 0 }).isNull()).isTrue();
        assertThat(result.getCell(new int[] { 0 }).getValue()).isNull();
    }

    @Test
    @DisplayName("a calculated member is computed, not looked up")
    void calculatedMemberIsComputed() {
        Result result = execute(
                "WITH MEMBER [Measures].[Twice] AS '[Measures].[Amount] * 2'"
                        + " SELECT {[Geo].[All Geo].[North]} ON COLUMNS"
                        + " FROM [Test] WHERE [Measures].[Twice]");

        assertThat(number(result, 0)).isEqualTo(60L);
    }

    private Result execute(String mdx) {
        Query query = connection.parseQuery(mdx);
        query.resolve();
        return connection.execute(query);
    }

    private static long number(Result result, int column) {
        Object value = result.getCell(new int[] { column }).getValue();
        return ((Number) value).longValue();
    }
}
