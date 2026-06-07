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
 *   SmartCity Jena, Stefan Bischof - initial
 */
package org.eclipse.daanse.olap.testkit.core;

import java.util.List;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.check.model.check.CheckExecutionResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckSuite;
import org.eclipse.daanse.olap.check.runtime.impl.CheckExecutorImpl;

/**
 * Thin wrapper around {@link CheckExecutorImpl}. Exists so the rolap
 * harness doesn't have to depend on the runtime impl package directly,
 * and so callers from outside the rolap testkit (e.g., bespoke OLAP-only
 * tests) can drive an {@link OlapCheckSuite} against a populated
 * {@link Context} without pulling in DataSource provisioning or CWM
 * concerns.
 */
public final class OlapCheckSuiteRunner {

    private OlapCheckSuiteRunner() {
    }

    /**
     * Execute the suite against the context and return the flat list of
     * {@link CheckExecutionResult}s — one per top-level check. Each result
     * contains a tree of child {@code CheckResult}s; callers flatten as
     * needed.
     */
    public static List<CheckExecutionResult> run(OlapCheckSuite suite, Context<?> context) {
        return new CheckExecutorImpl().execute(suite, context);
    }
}
