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

import org.eclipse.daanse.olap.api.element.ParentChildMember;

/**
 * A member that can also take part in the calculation order.
 *
 * <p>The evaluator needs both halves of a member at once: where it sits in the hierarchy,
 * and, when it carries a formula, how it ranks against the other calculations in scope.
 * This interface is the pair, and it is what a provider's member implements so that the
 * evaluator can work with it without knowing the provider.
 */
public interface CalculableMember extends ParentChildMember, Calculation {
}
