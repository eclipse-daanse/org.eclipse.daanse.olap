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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.daanse.olap.api.catalog.CatalogReader;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.MetaData;
import org.eclipse.daanse.olap.api.element.OlapElement;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.AssociationSetType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EntityContainerType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EmitContext {
    protected static final Logger LOGGER = LoggerFactory.getLogger(EmitContext.class);
    private final EdmFactory edm = EdmFactory.eINSTANCE;
    private final BiFactory bi = BiFactory.eINSTANCE;
    private final CsdlNames names;
    private final TypeMapper types;
    private final CsdlRequest request;
    private final CatalogReader reader;
    private final TSchema schema;

    public EmitContext(CsdlNames names, TypeMapper types, CsdlRequest request, CatalogReader reader, TSchema schema) {
        super();
        this.names = names;
        this.types = types;
        this.request = request;
        this.reader = reader;
        this.schema = schema;
    }

    private final Map<String, Set<String>> declaredProperties = new HashMap<>();
    private final List<PendingRef> pendingRefs = new ArrayList<>();

    public void registerProperty(String entityTypeName, String propertyName) {
        declaredProperties.computeIfAbsent(entityTypeName, k -> new HashSet<>())
                          .add(propertyName);
    }

    public void referenceProperty(String entityTypeName, String propertyName, String source) {
        pendingRefs.add(new PendingRef(entityTypeName, propertyName, source));
    }

    public void assertReferentialIntegrity() {
        List<PendingRef> dangling = pendingRefs.stream()
                .filter(r -> !declaredProperties
                        .getOrDefault(r.entityType(), Set.of()).contains(r.property()))
                .toList();
        if (!dangling.isEmpty()) {
            throw new CsdlEmitException("dangling bi:PropertyRef(s): " + dangling);
        }
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
        // TODO 
        
    }

    public void checkKnownGraphic(String statusGraphic) {
        // TODO 
        
    }

    public String uniquePropertyName(String string) {
        return string;
    }

    public String qualified(String dimEntity) {
        return names.namespaceOf(reader.getCatalog()) + "." + dimEntity;
    }

    public boolean factEntityHasProperty(String factEntity, String fk) {
        // TODO Auto-generated method stub
        return false;
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

    public MetaData metaDataOf(OlapElement element) {
        // TODO 
        return null;
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

    public String sanitize (String s) {
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
        return schema.getEntityType().stream().filter(et -> et.getName().equals(factEntity)).findFirst().orElseThrow(() -> 
        new CsdlEmitException("TEntityType with name " + factEntity + " not find"));
    }
   
 
}
