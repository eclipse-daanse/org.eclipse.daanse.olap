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

/**
 * A cube that cannot be expressed as CSDL at all.
 * <p>
 * Unchecked, because there is nothing a caller can do about it per cube: the
 * answer to DISCOVER_CSDL_METADATA either forms or it does not.
 */
public class CsdlEmitException extends RuntimeException {

    public CsdlEmitException(String message) {
        super(message);
    }

}
