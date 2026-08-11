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

import java.util.Optional;

/** Anfrage-Kontext, 1:1 aus DISCOVER_CSDL_METADATA abgeleitet. */
public record CsdlRequest(CsdlVersion version, Optional<String> perspective, LocalePolicy localePolicy) {
}
