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
package org.eclipse.daanse.olap.xmla.connector.execute;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.daanse.lcid.api.LcidService;
import org.eclipse.daanse.mdx.model.api.select.Allocation;
import org.eclipse.daanse.olap.api.Command;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.cache.CacheControl;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.connection.ConnectionProps;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Measure;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.query.component.CalculatedFormula;
import org.eclipse.daanse.olap.api.query.component.DmvQuery;
import org.eclipse.daanse.olap.api.query.component.DrillThrough;
import org.eclipse.daanse.olap.api.query.component.SqlQuery;
import org.eclipse.daanse.olap.api.query.component.Formula;
import org.eclipse.daanse.olap.api.query.component.Query;
import org.eclipse.daanse.olap.api.query.component.QueryComponent;
import org.eclipse.daanse.olap.api.query.component.Refresh;
import org.eclipse.daanse.olap.api.query.component.TransactionCommand;
import org.eclipse.daanse.olap.api.query.component.Update;
import org.eclipse.daanse.olap.api.query.component.UpdateClause;
import org.eclipse.daanse.olap.api.result.AllocationPolicy;
import org.eclipse.daanse.olap.api.result.Cell;
import org.eclipse.daanse.olap.api.result.CellSet;
import org.eclipse.daanse.olap.api.result.CellSetAxis;
import org.eclipse.daanse.olap.api.result.Scenario;
import org.eclipse.daanse.olap.xmla.connector.session.SessionScenarios;
import org.eclipse.daanse.olap.common.StandardProperty;
import org.eclipse.daanse.olap.query.component.QueryPrintWriter;
import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.model.io.RowsetCatalog;
import org.eclipse.daanse.xmla.model.xmla.Batch;
import org.eclipse.daanse.xmla.model.xmla.Cancel;
import org.eclipse.daanse.xmla.model.xmla.ClearCache;
import org.eclipse.daanse.xmla.model.xmla.Execute;
import org.eclipse.daanse.xmla.model.io.RowsetResults;
import org.eclipse.daanse.xmla.model.xmla.PropertyList;
import org.eclipse.daanse.xmla.model.xmla.Discover;
import org.eclipse.daanse.xmla.model.xmla.Parameter;
import org.eclipse.daanse.xmla.model.xmla.RequestTypeEnum;
import org.eclipse.daanse.xmla.model.xmla.Statement;
import org.eclipse.daanse.xmla.model.xmla.XmlaFactory;
import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute: an MDX statement against a connection, answered as a model
 * {@code MdDataset}.
 * <p>
 * Ported from the bridge's {@code OlapExecuteService}, statement kind by
 * statement kind. The MDX query path — parse, scenario, run,
 * {@link CellSetToMdDataset} — keeps the bridge's rules: a client that names no
 * catalog gets the only one when there is only one (Power BI sends an empty
 * catalog then), the connection carries the caller's roles and locale, a
 * writeback session's scenario is applied to the fact before the query runs,
 * and {@code Content=Data} omits the default slicer info the way SSAS does.
 * <p>
 * The writeback family — {@code BEGIN/COMMIT/ROLLBACK}, {@code UPDATE CUBE},
 * {@code REFRESH}, calculated formulas — and the commands {@code Alter},
 * {@code Cancel} and {@code ClearCache} answer as the bridge answered them. The
 * kinds whose answer is a rowset rather than a dataset — DMV, drill-through,
 * SQL, {@code Format=Tabular} — name themselves when asked for, and the codec
 * writes them as the tabular Execute answer and an empty result would read as
 * success.
 */
public class OlapExecute {

    private static final Logger LOGGER = LoggerFactory.getLogger(OlapExecute.class);

    /**
     * How a DMV query reaches the discover implementations without knowing them.
     */
    public interface Discoverer {
        List<EObject> discover(String requestType, Discover request, XmlaRequest context);
    }

    private final ContextListSupplyer contexts;
    private final SessionScenarios scenarios;
    private final LcidService lcidService;
    private final Discoverer discoverer;

    public OlapExecute(ContextListSupplyer contexts, SessionScenarios scenarios, LcidService lcidService,
            Discoverer discoverer) {
        this.contexts = contexts;
        this.scenarios = scenarios;
        this.lcidService = lcidService;
        this.discoverer = discoverer;
    }

    /**
     * Runs one command. Returns the result, or {@code null} for a command that has
     * none.
     */
    public EObject execute(Execute request, XmlaRequest context) {
        if (request.getCommand() instanceof Statement statement) {
            return statement(statement, request, context);
        }
        if (request.getCommand() instanceof Cancel) {
            return cancel(context);
        }
        if (request.getCommand() instanceof Batch batch) {
            return batch(batch, request, context);
        }
        if (request.getCommand() instanceof org.eclipse.daanse.xmla.model.xmla.Alter
                || request.getCommand() instanceof ClearCache) {
            // The bridge answered both with an empty result: Alter because the schema comes
            // from a provider it cannot change, ClearCache because old mondrian never had
            // it.
            return null;
        }
        throw new UnsupportedOperationException(
                "the command " + ExtendedMetaData.INSTANCE.getName(request.getCommand().eClass())
                        + " is not run by this connector yet");
    }

    /**
     * Cancels every running statement the caller's roles can reach, as the bridge
     * did.
     */
    private EObject cancel(XmlaRequest context) {
        List<String> roles = rolesOf(context);
        for (Context<?> olapContext : contexts.getContexts()) {
            try {
                Connection connection = olapContext.getConnection(new ConnectionProps(roles));
                @SuppressWarnings("unchecked")
                Context<Connection> typed = (Context<Connection>) connection.getContext();
                for (org.eclipse.daanse.olap.api.execution.Statement statement : typed.getStatements(connection)) {
                    statement.cancel();
                }
            } catch (java.sql.SQLException e) {
                throw new RuntimeException("Cancel failed against " + olapContext.getName(), e);
            }
        }
        return null;
    }

    private EObject statement(Statement statement, Execute request, XmlaRequest context) {
        String mdx = statement.getStatement();
        if (mdx == null || mdx.isBlank()) {
            LOGGER.warn("Empty statement received");
            return null;
        }

        PropertyList properties = request.getProperties() == null ? null : request.getProperties().getPropertyList();

        Optional<String> catalogName = Optional.empty();
        if (properties != null && properties.getCatalog() != null && !properties.getCatalog().isEmpty()) {
            catalogName = Optional.of(properties.getCatalog());
        }
        // Some clients (Power BI) send an empty catalog when there is only one.
        if (catalogName.isEmpty() && contexts.getContexts() != null && contexts.getContexts().size() == 1) {
            catalogName = Optional.of(contexts.getContexts().getFirst().getName());
        }
        if (catalogName.isEmpty()) {
            LOGGER.warn("no catalog named and more than one available; nothing is run");
            return null;
        }

        Optional<Context<?>> olapContext = contexts.getContext(catalogName.get());
        if (olapContext.isEmpty()) {
            LOGGER.warn("no context found for catalog {}", catalogName.get());
            return null;
        }

        Connection connection = olapContext.get()
                .getConnection(new ConnectionProps(rolesOf(context), locale(properties)));
        QueryComponent queryComponent = connection.parseStatement(mdx);

        String sessionId = context.sessionId();
        if (queryComponent instanceof Query query) {
            return runQuery(query, properties, sessionId);
        }
        if (queryComponent instanceof DmvQuery dmvQuery) {
            return dmv(dmvQuery, request, context);
        }
        if (queryComponent instanceof DrillThrough drillThrough) {
            return drillThrough(drillThrough, properties, sessionId);
        }
        if (queryComponent instanceof SqlQuery sqlQuery) {
            return sql(sqlQuery, properties);
        }
        if (queryComponent instanceof TransactionCommand transaction) {
            return transaction(connection, transaction, sessionId, context.userName());
        }
        if (queryComponent instanceof Update update) {
            return update(connection, update, sessionId);
        }
        if (queryComponent instanceof Refresh refresh) {
            return refresh(connection, refresh);
        }
        if (queryComponent instanceof CalculatedFormula calculatedFormula) {
            return calculatedFormula(connection, calculatedFormula);
        }
        throw new UnsupportedOperationException("the statement kind " + queryComponent.getClass().getSimpleName()
                + " is not run by this connector yet");
    }

    private EObject runQuery(Query query, PropertyList properties, String sessionId) {
        Cube cube = query.getCube();
        try {
            // A writeback session's pending values take part in every query of that
            // session. Without one, the query gets a scenario of its own that nothing
            // outlives - which is what the previous code did too, except that it also
            // registered an entry it never put the scenario into.
            Scenario scenario = scenarios.of(sessionId);
            if (scenario == null) {
                scenario = query.getConnection().createScenario();
            }
            query.getConnection().setScenario(scenario);
            // This cube's own pending values take part in the query; another cube's
            // would describe columns it does not have.
            cube.modifyFact(scenario.pendingRows(cube));

            org.eclipse.daanse.olap.api.execution.Statement statement = query.getConnection().createStatement();
            CellSet cellSet = statement.executeQuery(query);

            // Content=Data (the default) omits the default slicer info, as SSAS does;
            // DATA_INCLUDE_DEFAULT_SLICER asks for it back.
            boolean omitDefaultSlicerInfo = true;
            if (properties != null && properties.getContent() != null
                    && "DataIncludeDefaultSlicer".equalsIgnoreCase(properties.getContent())) {
                omitDefaultSlicerInfo = false;
            }
            // Format=Native/Multidimensional answers a dataset; Tabular flattens to a
            // rowset.
            if (properties != null && properties.getFormat() != null
                    && "Tabular".equalsIgnoreCase(properties.getFormat())) {
                return TabularResults.fromCellSet(cellSet, schemaIncluded(properties));
            }
            return CellSetToMdDataset.toMdDataset(cellSet, omitDefaultSlicerInfo);
        } finally {
            cube.restoreFact();
        }
    }

    // --- the writeback family, ported from the bridge clause by clause ---

    private EObject transaction(Connection connection, TransactionCommand transaction, String sessionId,
            String userId) {
        LOGGER.info("Writeback[xmla] TransactionCommand {} sessionId='{}' userId='{}'", transaction.getCommand(),
                sessionId, userId);
        if (transaction.getCommand() == Command.BEGIN) {
            scenarios.begin(sessionId, connection.createScenario());
        } else if (transaction.getCommand() == Command.ROLLBACK) {
            scenarios.clear(sessionId);
        } else if (transaction.getCommand() == Command.COMMIT) {
            Scenario scenario = scenarios.require(sessionId);
            // Only the cubes this scenario produced rows for, and each only its own.
            // Handing every cube the whole list wrote the same values into every
            // writeback table in the catalog.
            for (Cube cube : scenario.pendingCubes()) {
                cube.commit(scenario.pendingRows(cube), userId);
            }
            scenario.clear();
            scenarios.clear(sessionId);
        }
        return null;
    }

    private EObject update(Connection connection, Update update, String sessionId) {
        int clauseCount = update.getUpdateClauses() == null ? 0 : update.getUpdateClauses().size();
        LOGGER.info("Writeback[xmla] UPDATE cube='{}' clauses={} sessionId='{}'", update.getCubeName(), clauseCount,
                sessionId);

        Scenario scenario = scenarios.require(sessionId);
        connection.setScenario(scenario);
        String cubeName = update.getCubeName();
        Cube cube = connection.getCatalog().lookupCube(cubeName)
                .orElseThrow(() -> new RuntimeException("cube " + cubeName + " not found"));
        // modifyFact because the data can already be in the writeback table. Only this
        // cube's own pending rows: another cube's rows describe columns this one does
        // not have, and would answer the caller with values never meant for it.
        cube.modifyFact(scenario.pendingRows(cube));
        try {
            for (UpdateClause clause : update.getUpdateClauses()) {
                applyUpdateClause(connection, scenario, cube, update.getCubeName(), clause);
            }
        } finally {
            cube.restoreFact();
        }
        return null;
    }

    private void applyUpdateClause(Connection connection, Scenario scenario, Cube cube, String cubeName,
            UpdateClause clause) {
        String tuple = unparse(writer -> clause.getTupleExp().unparse(writer));
        CellSet tupleSet = connection.createStatement().executeQuery("SELECT " + tuple + " ON 0 FROM " + cubeName);
        CellSetAxis axis = tupleSet.getAxes().getFirst();

        String valueExpression = unparse(writer -> clause.getValueExp().unparse(writer));
        CellSet valueSet = connection.createStatement().executeQuery("WITH MEMBER [Measures].[m1] AS " + valueExpression
                + " SELECT [Measures].[m1] ON 0 FROM " + cubeName + " CELL PROPERTIES VALUE");
        Cell cell = valueSet.getCell(Arrays.asList(0));
        Object resolvedValue = cell.getValue();
        AllocationPolicy allocationPolicy = allocationPolicy(clause.getAllocation());

        // The measure goes first for setCellValue; a text-typed measure takes the text
        // short-path there, because the numeric allocation path drops text writebacks.
        List<Member> resolvedMembers = axis.getPositions().isEmpty() ? List.of()
                : axis.getPositions().get(0).getMembers();
        Member measureMember = null;
        List<Member> reordered = new ArrayList<>(resolvedMembers.size());
        for (Member member : resolvedMembers) {
            if (measureMember == null && member instanceof Measure) {
                measureMember = member;
            } else {
                reordered.add(member);
            }
        }
        if (measureMember != null) {
            reordered.add(0, measureMember);
        }
        boolean isText = measureMember != null
                && "String".equals(measureMember.getPropertyValue(StandardProperty.DATATYPE.getName()));

        LOGGER.info(
                "Writeback[xmla] UPDATE tuple='{}' valueExp='{}' resolvedValue={}"
                        + " allocation={} targetMeasure='{}' isTextMeasure={}",
                tuple, valueExpression, resolvedValue, allocationPolicy,
                measureMember == null ? "<unresolved>" : measureMember.getUniqueName(), isText);

        if (isText) {
            scenario.setCellValue(connection, reordered, resolvedValue, null, allocationPolicy, new Object[0]);
        } else {
            scenario.addPendingRows(cube,
                    cube.getAllocationValues(tuple, resolvedValue, allocationPolicy, connection.getRole()));
        }
        connection.getCacheControl(null).flushSchemaCache();
    }

    private interface Unparsed {
        void unparse(PrintWriter writer);
    }

    private static String unparse(Unparsed expression) {
        StringWriter buffer = new StringWriter();
        expression.unparse(new QueryPrintWriter(buffer));
        return buffer.toString();
    }

    private static AllocationPolicy allocationPolicy(Allocation allocation) {
        if (allocation == Allocation.USE_EQUAL_INCREMENT) {
            return AllocationPolicy.EQUAL_INCREMENT;
        }
        if (allocation == Allocation.USE_WEIGHTED_ALLOCATION) {
            return AllocationPolicy.WEIGHTED_ALLOCATION;
        }
        if (allocation == Allocation.USE_WEIGHTED_INCREMENT) {
            return AllocationPolicy.WEIGHTED_INCREMENT;
        }
        return AllocationPolicy.EQUAL_ALLOCATION;
    }

    private EObject refresh(Connection connection, Refresh refresh) {
        String cubeName = refresh.getCubeName();
        Cube cube = connection.getCatalog().lookupCube(cubeName)
                .orElseThrow(() -> new RuntimeException("cube " + cubeName + " not found"));
        CacheControl cacheControl = connection.getCacheControl(null);
        cacheControl.flush(cacheControl.createMeasuresRegion(cube));
        return null;
    }

    private EObject calculatedFormula(Connection connection, CalculatedFormula calculatedFormula) {
        Formula formula = calculatedFormula.getFormula();
        String cubeName = calculatedFormula.getCubeName();
        Cube cube = connection.getCatalog().lookupCube(cubeName)
                .orElseThrow(() -> new RuntimeException("cube " + cubeName + " not found"));
        if (formula.isMember()) {
            cube.createCalculatedMember(formula);
        } else {
            cube.createNamedSet(formula);
        }
        return null;
    }

    // --- the rowset-answering kinds
    // -----------------------------------------------------

    /**
     * The Content property decides whether the inline schema travels with the rows.
     */
    private static boolean schemaIncluded(PropertyList properties) {
        if (properties == null || properties.getContent() == null) {
            return true;
        }
        String content = properties.getContent();
        return !"Data".equalsIgnoreCase(content) && !"None".equalsIgnoreCase(content);
    }

    /**
     * A DMV query: SELECT against a {@code $SYSTEM} table. The table name is the
     * request type, the rows come from the same discover implementations a Discover
     * would reach, and the SELECT's column projection and WHERE are applied on top
     * - the bridge held a twenty-case copy of its discover wiring here, which one
     * dispatch replaces.
     */
    private EObject dmv(DmvQuery dmvQuery, Execute request, XmlaRequest context) {
        org.eclipse.daanse.dmv.model.api.DmvStatement statement = dmvQuery.statement();
        String tableName = statement.table().toUpperCase(Locale.ROOT);
        RequestTypeEnum requestType = RequestTypeEnum.getByName(tableName);
        java.util.Optional<org.eclipse.emf.ecore.EClass> rowClass = RowsetCatalog.forRequestType(tableName);
        if (requestType == null || rowClass.isEmpty()) {
            throw new UnsupportedOperationException("the DMV table " + statement.table() + " is not served");
        }

        Discover discover = XmlaFactory.eINSTANCE.createDiscover();
        discover.setRequestType(requestType);
        if (request.getProperties() != null) {
            discover.setProperties(org.eclipse.emf.ecore.util.EcoreUtil.copy(request.getProperties()));
        }
        // SYSTEMRESTRICTSCHEMA restrictions become the Discover's restriction list -
        // the
        // same wire shape a real Discover carries, so the transport's restriction rules
        // apply to a DMV exactly as they do to a Discover.
        if (!statement.restrictions().isEmpty()) {
            discover.setRestrictions(XmlaFactory.eINSTANCE.createRestrictions());
            for (org.eclipse.daanse.dmv.model.api.Restriction restriction : statement.restrictions()) {
                org.eclipse.daanse.xmla.model.xmla.RestrictionEntry entry = XmlaFactory.eINSTANCE
                        .createRestrictionEntry();
                entry.setName(restriction.name());
                entry.setValue(literalText(restriction.value()));
                discover.getRestrictions().getRestrictionList().add(entry);
            }
        }
        List<EObject> rows = discoverer.discover(tableName, discover, context);

        List<Parameter> parameters = request.getParameters() == null ? List.of()
                : request.getParameters().getParameter();
        PropertyList properties = request.getProperties() == null ? null : request.getProperties().getPropertyList();
        return RowsetResults.fromRows(rowClass.get(), rows, statement, parameters, schemaIncluded(properties));
    }

    private static String literalText(org.eclipse.daanse.dmv.model.api.DmvLiteral literal) {
        if (literal instanceof org.eclipse.daanse.dmv.model.api.StringLiteral text) {
            return text.value();
        }
        return ((org.eclipse.daanse.dmv.model.api.NumericLiteral) literal).value().toPlainString();
    }

    private EObject drillThrough(DrillThrough drillThrough, PropertyList properties, String sessionId) {
        Connection connection = drillThrough.getQuery().getConnection();
        boolean enableRowCount = connection.getContext().getConfig().enableTotalCount();
        int[] rowCountSlot = enableRowCount ? new int[] { 0 } : null;
        Cube cube = drillThrough.getQuery().getCube();
        java.sql.ResultSet resultSet = null;
        try {
            Scenario scenario = scenarios.of(sessionId);
            if (scenario == null) {
                scenario = connection.createScenario();
            }
            connection.setScenario(scenario);
            if (cube != null && connection.getScenario() != null) {
                cube.modifyFact(scenario.pendingRows(cube));
            }
            // The model carries no TableFields property (the api had one); nothing is
            // passed.
            resultSet = connection.createStatement().executeQuery(drillThrough, java.util.Optional.empty(),
                    rowCountSlot);
            int rowCount = enableRowCount ? rowCountSlot[0] : -1;
            return RowsetResults.fromResultSet(resultSet, rowCount, schemaIncluded(properties));
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Drill through SQL failed", e);
        } finally {
            if (cube != null) {
                cube.restoreFact();
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (java.sql.SQLException ignored) {
                    // closing is best effort, as it was in the bridge
                }
            }
            connection.close();
        }
    }

    private EObject sql(SqlQuery sqlQuery, PropertyList properties) {
        try {
            return RowsetResults.fromResultSet(sqlQuery.execute(), -1, schemaIncluded(properties));
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A Batch: every command in order, one entry per command in the
     * {@code xmla-m:results} answer. A command with nothing to say contributes an
     * {@code Emptyresult}, because a list cannot carry null.
     */
    private EObject batch(Batch batch, Execute request, XmlaRequest context) {
        org.eclipse.daanse.xmla.model.multipleresults.Results results = org.eclipse.daanse.xmla.model.multipleresults.MultipleResultsFactory.eINSTANCE
                .createResults();
        List<org.eclipse.daanse.xmla.model.xmla.Command> commands = batch.getCommand();
        for (org.eclipse.daanse.xmla.model.xmla.Command command : commands) {
            Execute single = XmlaFactory.eINSTANCE.createExecute();
            single.setCommand(org.eclipse.emf.ecore.util.EcoreUtil.copy(command));
            if (request.getProperties() != null) {
                single.setProperties(org.eclipse.emf.ecore.util.EcoreUtil.copy(request.getProperties()));
            }
            EObject result = execute(single, context);
            results.getResults().add(result != null ? result
                    : org.eclipse.daanse.xmla.model.empty.EmptyFactory.eINSTANCE.createEmptyresult());
        }
        return results;
    }

    /** The caller's roles, filtered against what the contexts actually define. */
    private List<String> rolesOf(XmlaRequest request) {
        List<String> roles = new ArrayList<>();
        for (Context<?> context : contexts.getContexts()) {
            for (String role : context.getAccessRoles()) {
                if (request.hasRole(role)) {
                    roles.add(role);
                }
            }
        }
        return roles;
    }

    private Locale locale(PropertyList properties) {
        if (lcidService != null) {
            Optional<Integer> lcid = properties == null ? Optional.empty()
                    : Optional.ofNullable(properties.getLocaleIdentifier());
            Optional<Locale> locale = lcidService.lcidToLocale(lcid);
            if (locale.isPresent()) {
                return locale.get();
            }
        }
        return Locale.getDefault();
    }
}
