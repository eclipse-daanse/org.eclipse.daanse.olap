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
 * Exception thrown when code attempts to access the current ExecutionContext
 * but none is bound to the current scope.
 *
 * <p>
 * This typically happens when:
 * <ul>
 * <li>Code runs outside of an {@link ExecutionContext#where} block</li>
 * <li>Background threads (timers, executors) try to access context</li>
 * <li>Async operations lose the scoped context</li>
 * </ul>
 *
 * <p>
 * To fix this, ensure the code runs within an ExecutionContext scope:
 * 
 * <pre>
 * ExecutionContext.where(execution.asContext(), () -> {
 *     // Your code here can safely call ExecutionContext.current()
 * });
 * </pre>
 *
 * @see ExecutionContext#current()
 * @see ExecutionContext#where(ExecutionContext, Runnable)
 */
public class NoExecutionContextException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new NoExecutionContextException with a default message.
     */
    public NoExecutionContextException() {
        super("No ExecutionContext is bound to the current scope. "
                + "Ensure code runs within ExecutionContext.where() block.");
    }

    /**
     * Creates a new NoExecutionContextException with a custom message.
     *
     * @param message the detail message
     */
    public NoExecutionContextException(String message) {
        super(message);
    }

    /**
     * Creates a new NoExecutionContextException with a message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public NoExecutionContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
