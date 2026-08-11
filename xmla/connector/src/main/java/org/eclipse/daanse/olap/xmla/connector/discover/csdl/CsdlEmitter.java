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
package org.eclipse.daanse.olap.xmla.connector.discover.csdl;

import org.eclipse.daanse.olap.api.catalog.CatalogReader;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TSchema;

/**
 * Turns a catalog's cubes into the CSDL-BI schema DISCOVER_CSDL_METADATA
 * answers with.
 * <p>
 * One call, one document: reporting clients read this instead of the schema
 * rowsets, so what it leaves out is not available to them at all.
 */
public interface CsdlEmitter {

    TSchema emit(CatalogReader reader, CsdlRequest req);

}