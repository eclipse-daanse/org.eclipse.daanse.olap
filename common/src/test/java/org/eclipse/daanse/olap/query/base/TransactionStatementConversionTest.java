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
*/
package org.eclipse.daanse.olap.query.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.daanse.mdx.model.api.TransactionKind;
import org.eclipse.daanse.mdx.model.api.TransactionStatement;
import org.eclipse.daanse.olap.api.Command;
import org.eclipse.daanse.olap.api.query.component.QueryComponent;
import org.eclipse.daanse.olap.api.query.component.TransactionCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * The last link of the writeback bracket: a parsed BEGIN/COMMIT/ROLLBACK has to
 * arrive at the connector as a {@link TransactionCommand}, because that is the
 * only thing that opens and closes a writeback scenario.
 */
class TransactionStatementConversionTest {

    private final QueryProviderImpl provider = new QueryProviderImpl();

    private static TransactionStatement statement(TransactionKind kind) {
        TransactionStatement statement = mock(TransactionStatement.class);
        when(statement.kind()).thenReturn(kind);
        return statement;
    }

    @ParameterizedTest
    @CsvSource({ "BEGIN,BEGIN", "COMMIT,COMMIT", "ROLLBACK,ROLLBACK" })
    void everyKindBecomesItsCommand(String kind, String expected) {
        QueryComponent component = provider.createQuery(null, statement(TransactionKind.valueOf(kind)),
                false);

        assertThat(component).isInstanceOf(TransactionCommand.class);
        assertThat(((TransactionCommand) component).getCommand()).isEqualTo(Command.valueOf(expected));
    }

    @Test
    void unparsesBackToSomethingThatParsesAgain() {
        StringWriter buffer = new StringWriter();
        PrintWriter writer = new PrintWriter(buffer);

        provider.createTransaction(statement(TransactionKind.BEGIN)).unparse(writer);
        writer.flush();

        assertThat(buffer.toString()).isEqualTo("BEGIN TRANSACTION");
    }
}
