/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.api.execution;

import org.eclipse.daanse.olap.api.monitor.event.SqlStatementEvent;

/**
 * Metadata associated with an execution context for tracing, monitoring, and debugging.
 *
 * <p>This metadata is primarily used for:
 * <ul>
 *   <li>OpenTelemetry tracing and spans</li>
 *   <li>Logging and debugging</li>
 *   <li>Performance monitoring</li>
 *   <li>Error reporting and diagnostics</li>
 * </ul>
 *
 * @since 2.0
 */
public interface ExecutionMetadata {

    /**
     * Returns the component name that initiated this execution.
     * Examples: "SqlStatisticsProvider.getTableCardinality", "RolapCell.getDrillThroughCount"
     *
     * @return the component name, or null if not set
     */
    String component();

    /**
     * Returns a descriptive message about what this execution is doing.
     * Examples: "Reading row count from table ...", "Error while counting drill-through"
     *
     * @return the message, or null if not set
     */
    String message();

    /**
     * Returns the purpose of the SQL statement execution.
     * Used to categorize SQL operations for monitoring.
     *
     * @return the purpose, or null if not set
     */
    SqlStatementEvent.Purpose purpose();

    /**
     * Returns the number of cell requests associated with this execution.
     * Returns 0 if not applicable.
     *
     * @return the cell request count
     */
    int cellRequestCount();

    /**
     * Creates an empty metadata instance with no values set.
     *
     * @return an empty ExecutionMetadata
     */
    static ExecutionMetadata empty() {
        return new ExecutionMetadataRecord(null, null, null, 0);
    }

    /**
     * Creates a metadata instance with the given values.
     *
     * @param component        the component name (may be null)
     * @param message          the descriptive message (may be null)
     * @param purpose          the SQL statement purpose (may be null)
     * @param cellRequestCount the cell request count
     * @return an ExecutionMetadata instance
     */
    static ExecutionMetadata of(String component, String message, SqlStatementEvent.Purpose purpose, int cellRequestCount) {
        return new ExecutionMetadataRecord(component, message, purpose, cellRequestCount);
    }
}
