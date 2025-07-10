/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2002-2005 Julian Hyde
 * Copyright (C) 2005-2017 Hitachi Vantara and others
 * All Rights Reserved.
 *
 * jhyde, Oct 5, 2002
 *
 * Contributors:
 *   SmartCity Jena - refactor, clean API
 *
 * ---- All changes after Fork in 2023 ------------------------
 *
 * Project: Eclipse daanse
 *
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */

package org.eclipse.daanse.olap.api.access;

import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.DatabaseColumn;
import org.eclipse.daanse.olap.api.element.DatabaseSchema;
import org.eclipse.daanse.olap.api.element.DatabaseTable;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.NamedSet;
import org.eclipse.daanse.olap.api.element.OlapElement;
import org.eclipse.daanse.olap.api.element.Catalog;

/**
 * A Role is a collection of access rights to cubes, permissions,
 * and so forth.
 *
 * At present, the only way to create a role is programmatically. You then
 * add appropriate permissions, and associate the role with a connection.
 * Queries executed for the duration of the connection will be using the role
 * for security control.
 *
 * Mondrian does not have any notion of a 'user'. It is the client
 * application's responsibility to create a role appropriate for the user who
 * is establishing the connection.
 *
 * @author jhyde
 * @since Oct 5, 2002
 */
public interface Role {

    /**
     * Returns the access this role has to a given schema.
     *
     *  schema != null
     *  return == Access.ALL
     * || return == Access.NONE
     * || return == Access.ALL_DIMENSIONS
     */
    AccessCatalog getAccess(Catalog schema);

    /**
     * Returns the access this role has to a given cube.
     *
     *  cube != null
     *  return == Access.ALL || return == Access.NONE
     */
    AccessCube getAccess(Cube cube);


    /**
     * Returns the access this role has to a given dimension.
     *
     *  dimension != null
     *  Access.instance().isValid(return)
     */
    AccessDimension getAccess(Dimension dimension);

    /**
     * Returns the access this role has to a given hierarchy.
     *
     *  hierarchy != null
     *  return == Access.NONE
     *   || return == Access.ALL
     *   || return == Access.CUSTOM
     */
    AccessHierarchy getAccess(Hierarchy hierarchy);

    /**
     * Returns the details of this hierarchy's access, or null if the hierarchy
     * has not been given explicit access.
     *
     *  hierarchy != null
     */
    HierarchyAccess getAccessDetails(Hierarchy hierarchy);

    /**
     * Returns the access this role has to a given level.
     *
     *  level != null
     *  Access.instance().isValid(return)
     */
    AccessMember getAccess(Level level);

    /**
     * Returns the access this role has to a given member.
     *
     *  member != null
     *  isMutable()
     *  return == Access.NONE
     *    || return == Access.ALL
     *    || return == Access.CUSTOM
     */
    AccessMember getAccess(Member member);

    /**
     * Returns the access this role has to a given named set.
     *
     *  set != null
     *  isMutable()
     *  return == Access.NONE || return == Access.ALL
     */
    AccessMember getAccess(NamedSet set);

    /**
     * Returns whether this role is allowed to see a given element.
     *  olapElement != null
     */
    boolean canAccess(OlapElement olapElement);

    boolean canAccess(DatabaseSchema databaseSchema, Catalog catalog);

    AccessDatabaseSchema getAccess(DatabaseSchema databaseSchema, Catalog catalog);

    AccessDatabaseTable getAccess(DatabaseTable databaseTable, AccessDatabaseSchema accessDatabaseSchemaParent);

    AccessDatabaseColumn getAccess(DatabaseColumn column, AccessDatabaseTable accessDatabaseTable);
}

