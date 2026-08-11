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
 * A reference written before its target exists.
 * <p>
 * CSDL is one document and the emitters do not run in dependency order, so a
 * reference is recorded here and resolved once everything has been emitted.
 */
record PendingRef(String entityType, String property, String source) {
}
