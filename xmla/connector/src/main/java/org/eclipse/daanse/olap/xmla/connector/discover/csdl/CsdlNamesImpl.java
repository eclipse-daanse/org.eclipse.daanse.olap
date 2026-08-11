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
 * The default name mangling: strip what CSDL forbids and keep a per-document
 * map so the same source name always yields the same identifier, and two
 * different ones never collide.
 */
public class CsdlNamesImpl implements CsdlNames {

    @Override
    public String tableNameOf(Dimension dimension) {
        return removeSquareBrackets(dimension.getUniqueName());
    }

    @Override
    public String columnNameOf(Level level) {
        return removeSquareBrackets(level.getUniqueName());
    }

    @Override
    public String columnNameOf(Level level, Property memberProperty) {
        return removeSquareBrackets(level.getUniqueName()) + "_" + memberProperty.getName();
    }

    @Override
    public String measureNameOf(Member measure) {
        return removeSquareBrackets(measure.getUniqueName());
    }

    @Override
    public String namespaceOf(Catalog catalog) {
        return removeSquareBrackets(catalog.getName());
    }

    @Override
    public String qualifiedTypeName(Catalog catalog, Dimension dimension) {
        return removeSquareBrackets(catalog.getName()) + "." + removeSquareBrackets(dimension.getUniqueName());
    }

    @Override
    public String encode(String rawName) {
        return removeSquareBrackets(rawName);
    }

    private static String removeSquareBrackets(String name) {
        return name.replace("[", "").replace("]", "").replace(".", "_").replace(" ", "_");
    }
}
