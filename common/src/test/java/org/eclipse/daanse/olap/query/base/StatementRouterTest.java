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
package org.eclipse.daanse.olap.query.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.daanse.olap.query.base.StatementRouter.Kind.DMV;
import static org.eclipse.daanse.olap.query.base.StatementRouter.Kind.MDX;
import static org.eclipse.daanse.olap.query.base.StatementRouter.Kind.SQL;

import org.junit.jupiter.api.Test;

class StatementRouterTest {

    @Test
    void aSystemTableMakesADmv() {
        assertThat(StatementRouter.classify("SELECT * FROM $SYSTEM.DBSCHEMA_CATALOGS"))
                .isEqualTo(DMV);
        assertThat(StatementRouter.classify(
                "select CATALOG_NAME from $system.DBSCHEMA_CATALOGS where CATALOG_NAME = 'x'"))
                .isEqualTo(DMV);
        assertThat(StatementRouter.classify("SELECT\n  *\nFROM\n  $SYSTEM.MDSCHEMA_CUBES"))
                .isEqualTo(DMV);
    }

    @Test
    void systemRestrictSchemaIsADmvToo() {
        assertThat(StatementRouter.classify(
                "SELECT * FROM SYSTEMRESTRICTSCHEMA($SYSTEM.MDSCHEMA_MEMBERS,"
                        + " [CATALOG_NAME] = 'FoodMart')")).isEqualTo(DMV);
        assertThat(StatementRouter.classify(
                "Select * from SYSTEMRESTRICTSCHEMA ( $System.Discover_csdl_metadata,"
                        + " [CATALOG_NAME] = 'x' )")).isEqualTo(DMV);
    }

    @Test
    void aSystemSpelledInsideNamesOrLiteralsDoesNotReroute() {
        assertThat(StatementRouter.classify(
                "SELECT [FROM $SYSTEM.Fake] ON COLUMNS FROM [Cube]")).isEqualTo(MDX);
        assertThat(StatementRouter.classify(
                "SELECT [Measures].[X] ON 0 FROM [Cube] WHERE [Dim].[from $SYSTEM.y]"))
                .isEqualTo(MDX);
    }

    @Test
    void mdxReadsAsMdx() {
        assertThat(StatementRouter.classify(
                "SELECT [Measures].Members ON COLUMNS FROM [Cube]")).isEqualTo(MDX);
        assertThat(StatementRouter.classify(
                "WITH MEMBER [Measures].[m] AS 1 SELECT FROM [Cube]")).isEqualTo(MDX);
        assertThat(StatementRouter.classify(
                "DRILLTHROUGH SELECT FROM [Cube]")).isEqualTo(MDX);
        assertThat(StatementRouter.classify("REFRESH CUBE [Cube]")).isEqualTo(MDX);
        assertThat(StatementRouter.classify("BEGIN TRANSACTION")).isEqualTo(MDX);
        assertThat(StatementRouter.classify("SELECT FROM [Cube]")).isEqualTo(MDX);
    }

    @Test
    void sqlReadsAsSql() {
        assertThat(StatementRouter.classify(
                "SELECT name, id FROM customers WHERE id = 1")).isEqualTo(SQL);
        assertThat(StatementRouter.classify("select count(*) from sales")).isEqualTo(SQL);
    }

    @Test
    void leadingCommentsAndWhitespaceCarryNoMeaning() {
        assertThat(StatementRouter.classify(
                "  /* a comment */ SELECT * FROM $SYSTEM.MDSCHEMA_CUBES")).isEqualTo(DMV);
        assertThat(StatementRouter.classify(
                "-- line comment\nSELECT [M].[x] ON 0 FROM [Cube]")).isEqualTo(MDX);
        assertThat(StatementRouter.classify(
                "// slashes too\nSELECT a FROM b")).isEqualTo(SQL);
    }

    @Test
    void theUnclassifiableDefaultsToMdxSoTheParserStatesTheError() {
        assertThat(StatementRouter.classify("")).isEqualTo(MDX);
        assertThat(StatementRouter.classify(null)).isEqualTo(MDX);
        assertThat(StatementRouter.classify("GARBAGE IN")).isEqualTo(MDX);
        assertThat(StatementRouter.classify("/* only a comment */")).isEqualTo(MDX);
    }
}
