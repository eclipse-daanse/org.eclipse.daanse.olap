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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.daanse.olap.api.catalog.CatalogReader;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.EntityContainerType;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TEntityType;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The state one emit run shares: the factories, the name mangling, the type
 * mapping, the request, the catalog reader, and the schema being filled.
 * <p>
 * It is passed to every emitter rather than held statically, so two documents
 * can be built at once and neither sees the other's names.
 */
public final class EmitContext {
    protected static final Logger LOGGER = LoggerFactory.getLogger(EmitContext.class);
    private final EdmFactory edm = EdmFactory.eINSTANCE;
    private final BiFactory bi = BiFactory.eINSTANCE;
    private final CsdlNames names;
    private final TypeMapper types;
    private final CsdlRequest request;
    private final CatalogReader reader;
    private final TSchema schema;
    private final Set<String> properties = new HashSet<String>();

    public EmitContext(CsdlNames names, TypeMapper types, CsdlRequest request, CatalogReader reader, TSchema schema) {
        super();
        this.names = names;
        this.types = types;
        this.request = request;
        this.reader = reader;
        this.schema = schema;
    }

    public EdmFactory edm() {
        return edm;
    }

    public BiFactory bi() {
        return bi;
    }

    public CsdlNames names() {
        return names;
    }

    public TypeMapper types() {
        return types;
    }

    public CsdlRequest request() {
        return request;
    }

    public CatalogReader reader() {
        return reader;
    }

    public Catalog catalog() {
        return reader.getCatalog();
    }

    public void warn(String string, Object... uniqueNames) {
        LOGGER.warn(string, uniqueNames);
    }

    public void info(String string, Object... uniqueNames) {
        LOGGER.info(string, uniqueNames);
    }

    public void debug(String string, Object... uniqueNames) {
        LOGGER.debug(string, uniqueNames);
    }

    public String entityName(Dimension measuresDimension) {
        return names.tableNameOf(measuresDimension);
    }

    public String mangle(String uniqueName) {
        return names.encode(uniqueName);
    }

    public String requireProperty(String name) {
        return name;
    }

    public void registerProperty(String name) {
        properties.add(name);
    }

    public String uniquePropertyName(String string) {
        return string;
    }

    public String qualified(String dimEntity) {
        return names.namespaceOf(reader.getCatalog()) + "." + dimEntity;
    }

    public boolean factEntityHasProperty(String factEntity, String fk) {
        return properties.contains(fk);
    }

    public String unique(String s) {
        return s;
    }

    public boolean biVersionAtLeast(int i, int j) {
        return request.version().name().equals("V" + i + "_" + j);
    }

    public boolean emitAllTranslations() {
        return false;
    }

    public Locale discoveredLocales() {
        return Locale.getDefault();
    }

    public List<Locale> union(List<Locale> configuredLocales, Locale discoveredLocales) {
        List<Locale> unions = new ArrayList<Locale>(configuredLocales);
        if (discoveredLocales != null) {
            unions.add(discoveredLocales);
        }
        return unions;
    }

    public String sanitize(String s) {
        return names.encode(s);
    }

    public TSchema schema() {
        return schema;
    }

    public boolean isHiddenByPerspective(Dimension dimension) {
        return false;
    }

    public EntityContainerType container() {
        return schema.getEntityContainer().getFirst();
    }

    public TEntityType entityType(String factEntity) {
        return schema.getEntityType().stream().filter(et -> et.getName().equals(factEntity)).findFirst()
                .orElseThrow(() -> new CsdlEmitException("TEntityType with name " + factEntity + " not find"));
    }

}
