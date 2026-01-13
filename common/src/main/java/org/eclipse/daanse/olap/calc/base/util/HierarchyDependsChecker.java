/*
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.eclipse.daanse.olap.calc.base.util;

import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.element.Hierarchy;

public class HierarchyDependsChecker {

	private HierarchyDependsChecker() {
		// Utility class
	}

	/**
	 * Returns true if any child calc depends on the hierarchy.
	 */
	public static boolean checkAnyDependsOnChildren(Calc<?>[] calcs, Hierarchy hierarchy) {
		return Stream.of(calcs)
				.filter(Objects::nonNull)
				.anyMatch(calc -> calc.dependsOn(hierarchy));
	}

	/**
	 * Returns true if first calc depends on the hierarchy, else false if calc[0]
	 * returns dimension, else true if any of the other calcs depend on dimension.
	 *
	 * Typical application: Aggregate({Set}, {Value Expression})
	 * depends upon everything {Value Expression} depends upon, except the
	 * dimensions of {Set}.
	 */
	public static boolean checkAnyDependsButFirst(Calc<?>[] calcs, Hierarchy hierarchy) {
		if (calcs.length == 0) {
			return false;
		}
		if (calcs[0].dependsOn(hierarchy)) {
			return true;
		}
		if (calcs[0].getType().usesHierarchy(hierarchy, true)) {
			return false;
		}
		for (int i = 1; i < calcs.length; i++) {
			final Calc<?> calc = calcs[i];
			if (calc != null && calc.dependsOn(hierarchy)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if any of the calcs depend on hierarchy, else false if any of
	 * the calcs return hierarchy, else true.
	 */
	public static boolean butDepends(Calc<?>[] calcs, Hierarchy hierarchy) {
		boolean result = true;
		for (final Calc<?> calc : calcs) {
			if (calc != null) {
				if (calc.dependsOn(hierarchy)) {
					return true;
				}
				if (calc.getType().usesHierarchy(hierarchy, true)) {
					result = false;
				}
			}
		}
		return result;
	}
}
