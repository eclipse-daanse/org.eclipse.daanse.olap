/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.api.function;

/**
 * Marks the function that builds a set from an explicit element list, written
 * {@code &#123;a, b, c&#125;} in MDX.
 *
 * <p>The core treats such a call as transparent when it decides which hierarchies an
 * expression binds: the braces themselves bind nothing. Implementing this interface is
 * the whole contract; it declares no methods.
 */
public interface SetConstructorFunction extends FunctionDefinition {
    // marker
}
