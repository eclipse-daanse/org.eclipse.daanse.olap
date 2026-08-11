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

import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.Property;

/**
 * How model names become CSDL identifiers.
 * <p>
 * CSDL identifiers are far stricter than cube names - no spaces, no brackets,
 * no leading digits - so every name has to be mangled, and it has to be mangled
 * the same way everywhere or the references inside the document will not
 * resolve.
 */
public interface CsdlNames {

    String tableNameOf(Dimension dimension);

    String columnNameOf(Level level);

    String columnNameOf(Level level, Property memberProperty);

    String measureNameOf(Member measure);

    String namespaceOf(Catalog catalog);

    String qualifiedTypeName(Catalog catalog, Dimension dimension);

    String encode(String rawName);
}
