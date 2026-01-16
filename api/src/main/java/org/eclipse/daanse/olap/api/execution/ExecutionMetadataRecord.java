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

/**
 * Record implementation of ExecutionMetadata.
 *
 * @param component        the component name (may be null)
 * @param message          the descriptive message (may be null)
 * @param purpose          the SQL statement purpose (may be null)
 * @param cellRequestCount the cell request count
 */
record ExecutionMetadataRecord(String component, String message, Execution.Purpose purpose,
        int cellRequestCount) implements ExecutionMetadata {
}
