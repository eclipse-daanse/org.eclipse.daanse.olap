/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.execution;

import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.result.CellSetMetaData;

public class PreparedStatementImpl extends StatementImpl {

    public PreparedStatementImpl(Connection connection) {
        super(connection);
    }

    public CellSetMetaData getCellSetMetaData() {
        //TODO
        return null;
    }
}
