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
package org.eclipse.daanse.olap.check.runtime.api;

import java.util.List;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.check.model.check.CheckExecutionResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckSuite;

public interface CheckExecutor {

    /**
     * Execute all connection checks in the suite. Each connection check uses its
     * own ConnectionConfig to create the appropriate connection.
     *
     * @param suite   the check suite containing connection checks
     * @param context the OLAP context used to create connections
     * @return list of execution results, one per connection check
     */
    List<CheckExecutionResult> execute(OlapCheckSuite suite, Context<?> context);

}
