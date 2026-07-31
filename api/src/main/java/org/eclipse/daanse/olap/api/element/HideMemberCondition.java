/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 */
package org.eclipse.daanse.olap.api.element;

import java.util.stream.Stream;

public enum HideMemberCondition {

	NEVER, IF_BLANK_NAME, IF_PARENTS_NAME;

    /**
     * Accepts both the enum-constant style ("IF_BLANK_NAME") and the mapping
     * literal style ("IfBlankName"); unknown values fail fast instead of
     * silently disabling raggedness.
     */
    public static HideMemberCondition fromValue(String v) {
        if (v == null || v.isBlank()) {
            return NEVER;
        }
        String normalized = v.replace("_", "");
        return Stream.of(HideMemberCondition.values())
                .filter(e -> e.name().replace("_", "").equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown HideMemberCondition: " + v));
	}
}
