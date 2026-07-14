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

import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.Property;

public interface CsdlNames {

 String tableNameOf(Dimension dimension);

 String columnNameOf(Level level);

 String columnNameOf(Level level, Property memberProperty);

 String measureNameOf(Member measure);

 String namespaceOf(Catalog catalog);

 String qualifiedTypeName(Catalog catalog, Dimension dimension);

 String encode(String rawName);
}
