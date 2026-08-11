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
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.xmla.connector.discover;

/**
 * The OLE DB types this server speaks, with their DBTYPE ordinals.
 * <p>
 * It sits with the connector rather than with the codec, because which types a
 * provider supports is a property of that provider and not of the format.
 * <p>
 * The ordinals are VARENUM values from OLEDB.H and they are the wire:
 * DBSCHEMA_PROVIDER_TYPES announces them, and DBSCHEMA_COLUMNS and
 * MDSCHEMA_MEASURES type their columns with them. Only the six this server
 * actually emits are here — VARENUM is far larger, and a constant nothing emits
 * would be a claim about a type this server cannot serve.
 */
public enum OleDbType {

    I4("INTEGER", 3), R8("DOUBLE", 5), CY("CURRENCY", 6), BOOL("BOOLEAN", 11), I8("LARGE_INTEGER", 20),
    WSTR("STRING", 130);

    private final String userName;
    private final int dbTypeOrdinal;

    OleDbType(String userName, int dbTypeOrdinal) {
        this.userName = userName;
        this.dbTypeOrdinal = dbTypeOrdinal;
    }

    /** The TYPE_NAME token DBSCHEMA_PROVIDER_TYPES answers. */
    public String userName() {
        return userName;
    }

    /** The DBTYPE ordinal, as DATA_TYPE columns carry it. */
    public int dbTypeOrdinal() {
        return dbTypeOrdinal;
    }
}
