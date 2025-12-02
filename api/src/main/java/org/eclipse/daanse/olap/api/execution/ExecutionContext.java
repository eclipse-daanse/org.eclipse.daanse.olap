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

import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Execution context using JDK 25 ScopedValues for thread-safe, immutable
 * context propagation.
 *
 * <p>
 * Replaces the ThreadLocal-based Locus/LocusImpl system with modern ScopedValue
 * approach. This provides better performance, memory characteristics, and
 * clearer lifecycle management compared to ThreadLocal.
 * </p>
 *
 * <p>
 * ExecutionContext tracks the state of a query execution including:
 * <ul>
 * <li>Unique execution ID</li>
 * <li>Start time and timeout</li>
 * <li>Execution state (RUNNING, CANCELED, TIMEOUT, ERROR, DONE)</li>
 * <li>Parent execution for nested queries</li>
 * <li>SQL statements registered for cancellation</li>
 * <li>Metadata for tracing and monitoring</li>
 * </ul>
 *
 * <p>
 * Usage example:
 *
 * <pre>
 * // Create root context with metadata
 * ExecutionMetadata rootMetadata = ExecutionMetadata.of("QueryExecution", "MDX Query", null, 0);
 * ExecutionContext ctx = ExecutionContext.root(Duration.ofMinutes(5), rootMetadata);
 *
 * ExecutionContext.where(ctx, () -> {
 *     // Your code here has access to ctx via ExecutionContext.current()
 *     ExecutionContext current = ExecutionContext.current();
 *     current.checkCancelOrTimeout();
 *
 *     // Create child context inheriting parent timeout
 *     ExecutionMetadata childMetadata = ExecutionMetadata.of("Component", "message", null, 0);
 *     ExecutionContext child1 = current.createChild(childMetadata, Optional.empty());
 *
 *     // Create child context with custom timeout (shorter than parent)
 *     ExecutionMetadata fastMetadata = ExecutionMetadata.of("FastOp", "quick operation", null, 0);
 *     ExecutionContext child2 = current.createChild(fastMetadata, Optional.of(Duration.ofSeconds(30)));
 *
 *     return performQuery();
 * });
 * </pre>
 *
 * @see ScopedValue
 */
public final class ExecutionContext {
    private static final ScopedValue<ExecutionContext> CURRENT = ScopedValue.newInstance();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    // Core state
    private final long id;
    private final Instant startTime;
    private final Duration timeout;
    private final AtomicReference<State> state;

    // Hierarchy
    private final ExecutionContext parent;

    // SQL Statements for cancellation
    private final List<Statement> sqlStatements;

    // Execution reference (for compatibility with legacy code during migration)
    private Execution execution;

    // Metadata for tracing, monitoring, and debugging (OpenTelemetry support)
    private final ExecutionMetadata metadata;

    /**
     * Unified constructor for both root and child contexts.
     *
     * @param parent   the parent context (null for root contexts)
     * @param metadata the metadata (defaults to empty if null)
     * @param timeout  the timeout duration (inherits from parent if empty and parent exists)
     */
    private ExecutionContext(ExecutionContext parent, ExecutionMetadata metadata, Optional<Duration> timeout) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.parent = parent;
        this.startTime = Instant.now();
        this.state = new AtomicReference<>(State.RUNNING);
        this.sqlStatements = Collections.synchronizedList(new ArrayList<>());

        // Timeout: explicit > parent > default
        if (timeout.isPresent()) {
            this.timeout = timeout.get();
        } else if (parent != null) {
            this.timeout = parent.timeout;
        } else {
            this.timeout = Duration.ofMinutes(5);
        }

        // Metadata: explicit > empty
        this.metadata = metadata != null ? metadata : ExecutionMetadata.empty();
    }

    /**
     * Creates a root execution context with the specified timeout and metadata.
     *
     * @param timeout  the timeout duration
     * @param metadata the metadata for the root context
     * @return a new root ExecutionContext
     */
    public static ExecutionContext root(Optional<Duration> timeout, ExecutionMetadata metadata) {
        return new ExecutionContext(null, Objects.requireNonNull(metadata, "metadata"), timeout);
    }

    /**
     * Returns the current execution context, or null if not in execution scope.
     *
     * @return the current ExecutionContext, or null if outside execution scope
     */
    public static ExecutionContext current() {
        return CURRENT.isBound() ? CURRENT.get() : null;
    }

    /**
     * Executes the given task within the scope of this execution context. The
     * context will be available via {@link #current()} during task execution.
     *
     * @param ctx  the execution context to bind
     * @param task the task to execute
     * @param <R>  the result type
     * @param <X>  the exception type
     * @return the result of the task
     * @throws X if the task throws an exception
     */
    public static <R, X extends Throwable> R where(ExecutionContext ctx, CallableTask<R, X> task) throws X {
        return ScopedValue.where(CURRENT, ctx).call(() -> task.call());
    }

    /**
     * Executes the given task within the scope of this execution context. The
     * context will be available via {@link #current()} during task execution.
     *
     * @param ctx  the execution context to bind
     * @param task the task to execute
     */
    public static void where(ExecutionContext ctx, Runnable task) {
        ScopedValue.where(CURRENT, ctx).run(task);
    }

    /**
     * Functional interface for tasks that can throw checked exceptions.
     *
     * @param <R> the result type
     * @param <X> the exception type
     */
    @FunctionalInterface
    public interface CallableTask<R, X extends Throwable> {
        /**
         * Executes the task.
         *
         * @return the result
         * @throws X if an error occurs
         */
        R call() throws X;
    }

    /**
     * Checks if this execution has been canceled or timed out. Throws an exception
     * if the execution has been canceled or exceeded its timeout.
     *
     * @throws QueryCanceledException if the execution was canceled
     * @throws QueryTimeoutException  if the execution has timed out
     */
    public void checkCancelOrTimeout() {
        State currentState = state.get();

        if (currentState == State.CANCELED) {
            throw new QueryCanceledException("Query canceled");
        }

        if (currentState == State.TIMEOUT) {
            throw new QueryTimeoutException("Query timeout");
        }

        // Check timeout
        if (Duration.between(startTime, Instant.now()).compareTo(timeout) > 0) {
            if (state.compareAndSet(State.RUNNING, State.TIMEOUT)) {
                cancel(); // Cancel all statements
            }
            throw new QueryTimeoutException("Query timeout");
        }
    }

    /**
     * Cancels this execution and all registered SQL statements. This method is
     * idempotent - calling it multiple times has the same effect as calling it
     * once.
     */
    public void cancel() {
        state.set(State.CANCELED);

        // Cancel all SQL statements
        synchronized (sqlStatements) {
            for (Statement stmt : sqlStatements) {
                try {
                    stmt.cancel();
                } catch (Exception e) {
                    // Log but continue canceling others
                    // Intentionally swallow exception to ensure all statements are canceled
                }
            }
        }
    }

    /**
     * Registers a SQL statement for automatic cancellation when this execution is
     * canceled.
     *
     * @param stmt the SQL statement to register
     * @throws NullPointerException if stmt is null
     */
    public void registerStatement(Statement stmt) {
        Objects.requireNonNull(stmt, "statement");
        sqlStatements.add(stmt);
    }

    /**
     * Returns the unique ID of this execution.
     *
     * @return the execution ID
     */
    public long id() {
        return id;
    }

    /**
     * Returns the start time of this execution.
     *
     * @return the start time
     */
    public Instant startTime() {
        return startTime;
    }

    /**
     * Returns the timeout duration for this execution.
     *
     * @return the timeout duration
     */
    public Duration timeout() {
        return timeout;
    }

    /**
     * Returns the current state of this execution.
     *
     * @return the current state
     */
    public State state() {
        return state.get();
    }

    /**
     * Returns the parent execution context, or null if this is a top-level
     * execution.
     *
     * @return the parent execution context, or null
     */
    public ExecutionContext parent() {
        return parent;
    }

    /**
     * Returns the Execution object associated with this context. This is provided
     * for compatibility with legacy code during migration. If this context doesn't
     * have an execution, walks up the parent chain.
     *
     * @return the Execution object, or null if not set in this context or any
     *         parent
     */
    public Execution getExecution() {
        if (execution != null) {
            return execution;
        }
        // Walk up the parent chain to find an execution
        if (parent != null) {
            return parent.getExecution();
        }
        return null;
    }

    /**
     * Sets the Execution object for this context. This is called by ExecutionImpl
     * during initialization.
     *
     * @param execution the Execution object
     */
    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    /**
     * Returns the metadata associated with this execution context. The metadata
     * contains component name, message, purpose, and cell request count used for
     * tracing, monitoring, and debugging (OpenTelemetry support).
     *
     * @return the ExecutionMetadata, never null
     */
    public ExecutionMetadata metadata() {
        return metadata;
    }

    /**
     * Creates a child execution context with new metadata and custom timeout.
     *
     * @param metadata the metadata for the child context
     * @param timeout  the timeout for the child context (empty to inherit from parent)
     * @return a new child ExecutionContext with the specified metadata and timeout
     */
    public ExecutionContext createChild(ExecutionMetadata metadata, Optional<Duration> timeout) {
        return new ExecutionContext(this, metadata, timeout);
    }

}
