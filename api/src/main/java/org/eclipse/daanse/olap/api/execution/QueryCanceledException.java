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
 * Exception thrown when a query execution has been canceled.
 *
 * <p>This exception is thrown by {@link ExecutionContext#checkCancelOrTimeout()}
 * when the execution state is {@link State#CANCELED}.</p>
 *
 * @see ExecutionContext#cancel()
 * @see ExecutionContext#checkCancelOrTimeout()
 */
@SuppressWarnings("serial")
public class QueryCanceledException extends RuntimeException {

    /**
     * Constructs a new query canceled exception with the specified detail message.
     *
     * @param message the detail message
     */
    public QueryCanceledException(String message) {
        super(message);
    }

    /**
     * Constructs a new query canceled exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public QueryCanceledException(String message, Throwable cause) {
        super(message, cause);
    }
}
