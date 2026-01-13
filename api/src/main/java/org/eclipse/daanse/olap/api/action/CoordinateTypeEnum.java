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
package org.eclipse.daanse.olap.api.action;

import java.util.stream.Stream;

public enum CoordinateTypeEnum {
    /**
     * Action coordinate refers to the cube.
     */
    CUBE,

    /**
     * Action coordinate refers to a dimension.
     */
    DIMENSION,

    /**
     * Action coordinate refers to a level.
     */
    LEVEL,

    /**
     * Action coordinate refers to a member.
     */
    MEMBER,

    /**
     * Action coordinate refers to a set.
     */
    SET,

    /**
     * Action coordinate refers to a cell.
     */
    CELL;

    public static CoordinateTypeEnum fromValue(String n) {
        return Stream.of(CoordinateTypeEnum.values()).filter(e -> (e.name() == n)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        new StringBuilder("CoordinateTypeEnum Illegal argument ").append(n).toString()));
    }
}