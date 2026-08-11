/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.api.query.component;

import org.eclipse.daanse.dmv.model.api.DmvStatement;

/**
 * A DMV query as a query component: the parsed statement, carried whole. Everything a
 * consumer needs - columns or star, rowset name, restrictions, WHERE, ORDER BY, DISTINCT,
 * TOP - is on the statement; this interface only ties it into the sealed component family.
 */
public non-sealed interface DmvQuery extends QueryComponent {

    DmvStatement statement();
}
