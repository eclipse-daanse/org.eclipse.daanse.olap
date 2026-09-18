/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.provider.memory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.daanse.olap.api.Parameter;
import org.eclipse.daanse.olap.api.access.Role;
import org.eclipse.daanse.olap.api.catalog.CatalogReader;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.NamedSet;
import org.eclipse.daanse.olap.api.element.db.DatabaseSchema;
import org.eclipse.daanse.olap.access.RoleImpl;
import org.eclipse.daanse.olap.api.query.IdentifierSegment;

/**
 * A catalog the provider holds in memory.
 *
 * <p>Build one with {@link #builder(String)}; the builder is the only public way in, so
 * that a catalog is complete the moment it exists.
 */
public class MemoryCatalog implements Catalog {

    private final String name;
    private final String id = UUID.randomUUID().toString();
    private final Instant loadDate = Instant.now();
    private final List<Cube> cubes = new ArrayList<>();
    private final Role defaultRole;
    private Connection internalConnection;

    MemoryCatalog(String name, List<MemoryCube> cubes) {
        this.name = name;
        RoleImpl role = new RoleImpl();
        role.grant(this, org.eclipse.daanse.olap.api.access.AccessCatalog.ALL);
        for (MemoryCube cube : cubes) {
            cube.catalog(this);
            this.cubes.add(cube);
            role.grant(cube, org.eclipse.daanse.olap.api.access.AccessCube.ALL);
        }
        role.makeImmutable();
        this.defaultRole = role;
    }

    /** Starts a new catalog. */
    public static MemoryCatalogBuilder builder(String name) {
        return new MemoryCatalogBuilder(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<Cube> getCubes() {
        return List.copyOf(cubes);
    }

    @Override
    public Optional<? extends Cube> lookupCube(String cubeName) {
        return cubes.stream().filter(c -> c.getName().equals(cubeName)).findFirst();
    }

    @Override
    public Role getDefaultRole() {
        return defaultRole;
    }

    @Override
    public Role lookupRole(String role) {
        return null;
    }

    @Override
    public CatalogReader getCatalogReaderWithDefaultRole() {
        return new MemoryCatalogReader(this, defaultRole, context);
    }

    private org.eclipse.daanse.olap.api.Context<?> context;

    /**
     * Attaches this catalog to the context serving it.
     *
     * <p>A reader has to be able to answer which context it belongs to: the calc layer
     * reads configuration through that path, and a reader that answers null makes a
     * perfectly ordinary expression fail with a null pointer.
     */
    void context(org.eclipse.daanse.olap.api.Context<?> value) {
        this.context = value;
    }

    /** The context serving this catalog, or {@code null} while none has claimed it. */
    org.eclipse.daanse.olap.api.Context<?> context() {
        return context;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    @Override
    public Instant getCatalogLoadDate() {
        return loadDate;
    }

    @Override
    public List<Exception> getWarnings() {
        return List.of();
    }

    @Override
    public NamedSet getNamedSet(String setName) {
        return null;
    }

    @Override
    public NamedSet getNamedSet(IdentifierSegment segment) {
        return null;
    }

    @Override
    public List<? extends DatabaseSchema> getDatabaseSchemas() {
        // There is no database under this provider, so there is no schema to describe.
        return List.of();
    }

    /**
     * The connection the engine uses for its own lookups while it resolves a query.
     *
     * <p>Set by the context that serves this catalog, because the catalog itself does not
     * know which context it belongs to until one is built over it.
     */
    @Override
    public Connection getInternalConnection() {
        if (internalConnection == null) {
            throw new IllegalStateException("catalog " + name
                    + " has no context yet; build one with MemoryContext.over(...) first");
        }
        return internalConnection;
    }

    void internalConnection(Connection connection) {
        this.internalConnection = connection;
    }

    @Override
    public org.eclipse.daanse.olap.api.element.MetaData getMetaData() {
        return org.eclipse.daanse.olap.element.OlapMetaDataBase.empty();
    }

    @Override
    public String toString() {
        return "MemoryCatalog[" + name + "]";
    }
}
