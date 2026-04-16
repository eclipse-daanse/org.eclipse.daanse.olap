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
package org.eclipse.daanse.olap.check.reporter.api;

import java.util.List;

import org.eclipse.daanse.olap.check.model.check.CheckExecutionResult;

/**
 * Service interface for check result reporters. Multiple implementations can be
 * active simultaneously, each writing to a different target (log, file,
 * telemetry, etc.).
 */
public interface CheckResultReporter {

    /**
     * Report the results of a check execution.
     *
     * @param results   the execution results from CheckExecutor
     * @param suiteName logical name of the check suite (e.g. catalog name)
     */
    void report(List<CheckExecutionResult> results, String suiteName);
}
