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
package org.eclipse.daanse.olap.evaluator;

/**
 * Marks the synthetic member that stands for a compound slicer.
 *
 * <p>A {@code WHERE} clause over a set rather than a single tuple is represented by one
 * member that carries the whole set. The evaluator has to recognise it, because a cell
 * below such a member is aggregated rather than looked up, and because it must not be
 * expanded again while it is already being expanded.
 *
 * <p>Implementing this interface is the whole contract; it declares no methods.
 */
public interface CompoundSlicerMember {
}
