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
package org.eclipse.daanse.olap.common;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.ContextConfig;
import org.eclipse.daanse.olap.api.execution.ExecutionContext;

/**
 * The configuration of the execution the current thread is running.
 *
 * <p>
 * A handful of places deep in evaluation - name comparison in {@code Util},
 * sibling ordering in {@code Sorter}, the tuple list size guard - need a
 * configuration value but are reached through static methods that have no
 * context parameter and cannot get one short of changing hundreds of signatures.
 * They ask the {@link ExecutionContext} bound to this thread which context it is
 * executing for, and read the configuration from there.
 * </p>
 *
 * <p>
 * Prefer an explicit context wherever one is at hand: this lookup is only
 * correct while a query is executing, and unit tests that call such a method
 * directly get {@link #DEFAULTS}, not whatever the test context was configured
 * with. It is a bridge for code that has no other way, not a shortcut.
 * </p>
 */
public final class ExecutionConfig {

    /**
     * A configuration answering every getter with its default from
     * {@link ConfigConstants}.
     *
     * <p>
     * Used outside any execution: during construction, in unit tests, in tooling.
     * </p>
     */
    public static final ContextConfig DEFAULTS = new MapContextConfig(() -> null);

    private ExecutionConfig() {
        // utility class
    }

    /**
     * @return the configuration of the context this thread is executing a query
     *         for, or {@link #DEFAULTS} when the thread is not inside an execution
     */
    public static ContextConfig current() {
        ExecutionContext executionContext = ExecutionContext.currentOrNull();
        Context<?> context = executionContext == null ? null : executionContext.getContext();
        return context == null ? DEFAULTS : context.getConfig();
    }
}
