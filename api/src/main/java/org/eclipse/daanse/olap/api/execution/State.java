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
 * Represents the state of an execution context.
 *
 * <p>
 * The state transitions are:
 * 
 * <pre>
 * RUNNING → CANCELED (via cancel())
 * RUNNING → TIMEOUT  (via checkCancelOrTimeout() when timeout exceeded)
 * RUNNING → ERROR    (when an error occurs)
 * RUNNING → DONE     (when execution completes successfully)
 * </pre>
 *
 * @see ExecutionContext#state()
 */
public enum State {
    /**
     * The execution is currently running.
     */
    RUNNING,

    /**
     * The execution has been canceled by a user or system request.
     */
    CANCELED,

    /**
     * The execution has exceeded its timeout duration.
     */
    TIMEOUT,

    /**
     * The execution has encountered an error.
     */
    ERROR,

    /**
     * The execution has completed successfully.
     */
    DONE;

}
