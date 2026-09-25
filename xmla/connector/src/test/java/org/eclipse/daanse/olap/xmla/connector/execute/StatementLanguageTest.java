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
 *   dbulahov - initial
 */
package org.eclipse.daanse.olap.xmla.connector.execute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.connection.ConnectionProps;
import org.eclipse.daanse.olap.api.query.StatementLanguage;
import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.olap.xmla.connector.session.SessionScenarios;
import org.eclipse.daanse.xmla.api.XmlaCommandFailedException;
import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.eclipse.daanse.xmla.model.multipleresults.Results;
import org.eclipse.daanse.xmla.model.xmla.Execute;
import org.eclipse.daanse.xmla.model.xmla.Properties;
import org.eclipse.daanse.xmla.model.xmla.PropertyList;
import org.eclipse.daanse.xmla.model.xmla.RowsetCell;
import org.eclipse.daanse.xmla.model.xmla.RowsetResult;
import org.eclipse.daanse.xmla.model.xmla.RowsetRow;
import org.eclipse.daanse.xmla.model.xmla.Statement;
import org.eclipse.daanse.xmla.model.xmla.XmlaFactory;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A further statement language gets the statements it accepts, before MDX
 * parsing, and its result sets come back as rowsets.
 */
class StatementLanguageTest {

    private final Connection connection = mock(Connection.class);
    private final ContextListSupplyer contexts = mock(ContextListSupplyer.class);
    private final StatementLanguage language = mock(StatementLanguage.class);
    private OlapExecute execute;

    @BeforeEach
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void setUp() {
        Context context = mock(Context.class);
        when(context.getName()).thenReturn("Sales");
        when(context.getAccessRoles()).thenReturn(List.of());
        when(context.getConnection(any(ConnectionProps.class))).thenReturn(connection);
        when(contexts.getContexts()).thenReturn(List.of(context));
        when(contexts.getContext("Sales")).thenReturn(Optional.of(context));
        when(language.accepts(anyString())).thenAnswer(i -> ((String) i.getArgument(0)).startsWith("EVALUATE"));
        execute = new OlapExecute(contexts, new SessionScenarios(), null, null, List.of(language));
    }

    private static Execute request(String text, String cube) {
        Statement statement = XmlaFactory.eINSTANCE.createStatement();
        statement.setStatement(text);
        PropertyList propertyList = XmlaFactory.eINSTANCE.createPropertyList();
        propertyList.setCatalog("Sales");
        if (cube != null) {
            propertyList.setCube(cube);
        }
        Properties properties = XmlaFactory.eINSTANCE.createProperties();
        properties.setPropertyList(propertyList);
        Execute request = XmlaFactory.eINSTANCE.createExecute();
        request.setCommand(statement);
        request.setProperties(properties);
        return request;
    }

    /** A result set of one BIGINT column. */
    private static ResultSet resultSet(String column, Long... values) throws SQLException {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn(column);
        when(metaData.getColumnType(1)).thenReturn(Types.BIGINT);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        List<Boolean> more = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            more.add(true);
        }
        more.add(false);
        when(resultSet.next()).thenReturn(more.get(0), more.subList(1, more.size()).toArray(Boolean[]::new));
        if (values.length > 0) {
            when(resultSet.getObject(1)).thenReturn(values[0], (Object[]) java.util.Arrays.copyOfRange(values, 1,
                    values.length));
        }
        return resultSet;
    }

    private static List<String> values(RowsetResult rowset) {
        List<String> values = new ArrayList<>();
        for (RowsetRow row : rowset.getRows()) {
            for (RowsetCell cell : row.getCells()) {
                values.add(cell.getValue());
            }
        }
        return values;
    }

    @Test
    void oneResultSetIsARowset() throws Exception {
        ResultSet resultSet = resultSet("[Value]", 1L, 2L);
        when(language.execute(connection, "EVALUATE {1, 2}", Map.of("Catalog", "Sales", "Cube", "Budget")))
                .thenReturn(List.of(resultSet));

        EObject result = execute.execute(request("EVALUATE {1, 2}", "Budget"), XmlaRequest.anonymous());

        assertThat(result).isInstanceOf(RowsetResult.class);
        RowsetResult rowset = (RowsetResult) result;
        assertThat(rowset.getColumns()).extracting(c -> c.getField()).containsExactly("[Value]");
        assertThat(values(rowset)).containsExactly("1", "2");
        verify(connection, never()).parseStatement(anyString());
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    void severalResultSetsAreMultipleResults() throws Exception {
        ResultSet first = resultSet("[A]", 1L);
        ResultSet second = resultSet("[B]", 2L);
        when(language.execute(any(), anyString(), any())).thenReturn(List.of(first, second));

        EObject result = execute.execute(request("EVALUATE {1} EVALUATE {2}", null), XmlaRequest.anonymous());

        assertThat(result).isInstanceOf(Results.class);
        List<EObject> rowsets = ((Results) result).getResults();
        assertThat(rowsets).hasSize(2);
        assertThat(values((RowsetResult) rowsets.get(0))).containsExactly("1");
        assertThat(values((RowsetResult) rowsets.get(1))).containsExactly("2");
    }

    @Test
    void aFailureIsTheCommandsNotTheSessions() throws Exception {
        when(language.execute(any(), anyString(), any()))
                .thenThrow(new SQLSyntaxErrorException("the table 'Nope' does not exist", "42000"));

        assertThatThrownBy(() -> execute.execute(request("EVALUATE 'Nope'", null), XmlaRequest.anonymous()))
                .isInstanceOf(XmlaCommandFailedException.class).hasMessage("the table 'Nope' does not exist");
        verify(connection).close();
    }

    @Test
    void otherStatementsStillGoToTheMdxParser() throws Exception {
        when(connection.parseStatement("SELECT FROM [Sales]")).thenThrow(new IllegalStateException("parsed as MDX"));

        assertThatThrownBy(() -> execute.execute(request("SELECT FROM [Sales]", null), XmlaRequest.anonymous()))
                .hasMessage("parsed as MDX");
        verify(language, never()).execute(any(), anyString(), any());
    }

    @Test
    void passesOnlyTheGivenProperties() {
        assertThat(OlapExecute.languageProperties(null)).isEmpty();
        PropertyList properties = XmlaFactory.eINSTANCE.createPropertyList();
        properties.setCatalog("Sales");
        assertThat(OlapExecute.languageProperties(properties)).containsExactly(Map.entry("Catalog", "Sales"));
    }
}
