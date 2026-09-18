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
 * Marks the function that yields the current member of a hierarchy,
 * {@code &lt;Hierarchy&gt;.CurrentMember} in MDX.
 *
 * <p>It is the one call that makes an expression depend on the evaluation context of its
 * hierarchy, which the core must recognise without knowing the concrete definition.
 * Implementing this interface is the whole contract; it declares no methods.
 */
public interface CurrentMemberFunction extends FunctionDefinition {
    // marker
}
