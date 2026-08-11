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
 * Which CSDL-BI version to emit. Clients ask through the FORMAT property, and
 * the two differ in enough places that the emitters branch on it.
 */
public enum CsdlVersion {
    V1_1, V2_0
}
