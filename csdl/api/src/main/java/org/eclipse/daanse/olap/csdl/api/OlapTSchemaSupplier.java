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
package org.eclipse.daanse.olap.csdl.api;

import java.util.function.Supplier;

import org.eclipse.daanse.xmla.csdl.model.v2.edm.TSchema;

/**
 * Supplier interface for providing an OlapCheckSuite. Implementations can load
 * suites from various sources (XMI files, JSON, etc.) and are aggregated by the
 * OlapCheckSuiteRegistry.
 */
public interface OlapTSchemaSupplier extends Supplier<TSchema> {

}
