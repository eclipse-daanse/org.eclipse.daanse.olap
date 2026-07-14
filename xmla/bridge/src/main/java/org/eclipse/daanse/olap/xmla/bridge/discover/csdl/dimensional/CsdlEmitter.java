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
package org.eclipse.daanse.olap.xmla.bridge.discover.csdl.dimensional;

import org.eclipse.daanse.olap.api.catalog.CatalogReader;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TSchema;

public interface CsdlEmitter {

    TSchema emit(CatalogReader reader, CsdlRequest req);


}