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
package org.eclipse.daanse.olap.function;

import java.util.Optional;

import org.eclipse.daanse.mdx.parser.cccx.CCCXMdxParserProvider;
import org.eclipse.daanse.olap.api.element.LevelType;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.function.services.standard.StandardFunctions;
import org.eclipse.daanse.olap.provider.memory.MapCellReader;
import org.eclipse.daanse.olap.provider.memory.MemoryCatalog;
import org.eclipse.daanse.olap.provider.memory.MemoryCatalogBuilder;
import org.eclipse.daanse.olap.provider.memory.MemoryContext;
import org.eclipse.daanse.olap.provider.memory.MemoryCube;
import org.eclipse.daanse.olap.provider.memory.MemoryHierarchy;
import org.eclipse.daanse.olap.provider.memory.MemoryMember;

/**
 * The catalog every contract in this module refers to.
 *
 * <p>It is small enough to check every expected value by hand and self-explanatory enough
 * that nobody has to look a foreign schema up:
 *
 * <pre>
 * Cube [Test]
 *
 * [Geo]        All Geo (150)
 *                North (30)   -&gt; A (10), B (20)
 *                South (120)  -&gt; C (30), D (40), E (50)
 *
 * [Time]       All Time
 *                2023         -&gt; Q1..Q4, each with its three months
 *                2024         -&gt; Q1 only, so a year can end early
 *
 * [Sparse]     All Sparse
 *                X            -&gt; X1
 *                Y                         (no child: a leaf on the middle level)
 *
 * [Measures]   [Amount]   numeric, densely populated
 *              [Gaps]     numeric, with NULL cells
 * </pre>
 *
 * <p>{@code Sum([Geo].[South].Children, [Measures].[Amount])} is {@code 120}, and anyone
 * can work that out without looking anything up.
 *
 * <p>There is no database behind this. The in-memory provider answers the method calls of
 * the OLAP interfaces directly, which is all an abstract function needs.
 */
public final class TestCatalogs {

    /** The name of the one cube. */
    public static final String CUBE_NAME = "Test";

    private static final String AMOUNT = "[Measures].[Amount]";
    private static final String GAPS = "[Measures].[Gaps]";

    /**
     * Whether the contracts evaluate against this catalogue.
     *
     * <p>They do. It is a single line so that the whole of stage B can be turned off in
     * one place if the provider ever regresses, but there is no reason to.
     *
     * <p>Earlier versions of this comment described the switch as off and cited the
     * failures that switching it on would produce. Those numbers are gone: the contracts
     * run, and what remains skipped are waivers with reasons of their own, not a blanket
     * one. Do not read a count out of this comment; run the suite.
     */
    private static final boolean CONTRACTS_USE_THIS_CATALOG = true;

    private static Connection connection;

    private TestCatalogs() {
    }

    /** A connection over the catalog above, shared by every contract in this module. */
    public static synchronized Optional<Connection> connection() {
        if (!CONTRACTS_USE_THIS_CATALOG) {
            return Optional.empty();
        }
        if (connection == null) {
            connection = context().getConnectionWithDefaultRole();
        }
        return Optional.of(connection);
    }

    /** The context over the catalog above. Built fresh each time. */
    public static MemoryContext context() {
        MemoryCatalogBuilder builder = MemoryCatalog.builder(CUBE_NAME);
        MemoryCube cube = builder.cube(CUBE_NAME);

        MemoryHierarchy geo = cube.dimension("Geo").hierarchy();
        geo.level("(All)").level("Region").level("City");
        MemoryMember allGeo = geo.member("All Geo");
        allGeo.child("North").children("A", "B");
        allGeo.child("South").children("C", "D", "E");

        MemoryHierarchy time = cube.dimension("Time",
                org.eclipse.daanse.olap.api.element.DimensionType.TIME_DIMENSION).hierarchy();
        // The level types matter: Ytd, ParallelPeriod and OpeningPeriod look for them,
        // and a REGULAR level makes a time dimension that no time function can use.
        time.level("(All)")
                .level("Year", LevelType.TIME_YEARS)
                .level("Quarter", LevelType.TIME_QUARTERS)
                .level("Month", LevelType.TIME_MONTHS);
        MemoryMember allTime = time.member("All Time");
        MemoryMember y2023 = allTime.child("2023");
        y2023.child("Q1").children("Jan", "Feb", "Mar");
        y2023.child("Q2").children("Apr", "May", "Jun");
        y2023.child("Q3").children("Jul", "Aug", "Sep");
        y2023.child("Q4").children("Oct", "Nov", "Dec");
        // 2024 is deliberately short: a year that stops after one quarter is what
        // ParallelPeriod, OpeningPeriod and LastPeriods need to be interesting.
        allTime.child("2024").child("Q1").children("Jan", "Feb", "Mar");

        MemoryHierarchy sparse = cube.dimension("Sparse").hierarchy();
        sparse.level("(All)").level("Group").level("Leaf");
        MemoryMember allSparse = sparse.member("All Sparse");
        allSparse.child("X").child("X1");
        allSparse.child("Y");

        MemoryHierarchy measures = cube.measures().hierarchy("Measures", false);
        measures.level("MeasuresLevel");
        measures.member("Amount");
        measures.member("Gaps");

        MemoryCatalog catalog = builder.build();

        MapCellReader cells = new MapCellReader()
                .at(10, "[Geo].[All Geo].[North].[A]", AMOUNT)
                .at(20, "[Geo].[All Geo].[North].[B]", AMOUNT)
                .at(30, "[Geo].[All Geo].[South].[C]", AMOUNT)
                .at(40, "[Geo].[All Geo].[South].[D]", AMOUNT)
                .at(50, "[Geo].[All Geo].[South].[E]", AMOUNT)
                // [Gaps] is deliberately incomplete: C and E have no value, so every NULL
                // promise has something real to look at.
                .at(30, "[Geo].[All Geo].[South].[D]", GAPS)
                .at(10, "[Geo].[All Geo].[North].[A]", GAPS);

        return MemoryContext.over(catalog, StandardFunctions.standard(),
                new CCCXMdxParserProvider(), cells);
    }
}
