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
package org.eclipse.daanse.olap.api.calc;

/**
 * A tuple-valued calculation that is composed of one {@link MemberCalc} per hierarchy and
 * can hand those parts out again.
 *
 * <p>It lets the calc layer rewrite {@code (a, b).Value} into a direct member-value
 * calculation without knowing which function definition built the tuple. Without this
 * interface the core would have to name a concrete class from the function library.
 */
public interface TupleMemberCalcs {

    /** The member calculations this tuple is composed of, in hierarchy order. */
    MemberCalc[] getMemberCalcs();
}
