/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.daanse.olap.check.runtime.api;

import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.check.model.check.CheckExecutionResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckModel;
import org.eclipse.emf.common.util.URI;

/**
 * Executor for OLAP check models.
 * <p>
 * Loads check definitions from XMI files or EMF model objects,
 * executes them against OLAP connections, and returns EMF result objects.
 * </p>
 * <p>
 * Use cases:
 * <ul>
 *   <li>System health/readiness checks</li>
 *   <li>Cache warmup</li>
 *   <li>Verification and testing</li>
 *   <li>Role-based access validation</li>
 * </ul>
 * </p>
 */
public interface CheckExecutor {

    /**
     * Execute checks defined in the model against the given connection.
     *
     * @param model the check model containing check definitions
     * @param connection the OLAP connection to check against
     * @return the execution result containing all check results
     */
    CheckExecutionResult execute(OlapCheckModel model, Connection connection);

    /**
     * Load a check model from URI and execute against the given connection.
     *
     * @param modelUri URI to the XMI file containing check definitions
     * @param connection the OLAP connection to check against
     * @return the execution result containing all check results
     */
    CheckExecutionResult execute(URI modelUri, Connection connection);
}
