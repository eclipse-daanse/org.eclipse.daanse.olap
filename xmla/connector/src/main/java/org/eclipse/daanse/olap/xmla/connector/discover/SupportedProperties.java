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

import java.util.List;
import java.util.Map;

/**
 * What is this server's own about DISCOVER_PROPERTIES - and nothing else.
 * <p>
 * The facts per property (type, access, default, description) come from the
 * model through {@code PropertyCatalog}; the specification document was read
 * into the model once and is not repeated here. What remains genuinely ours:
 * <em>which</em> of the modelled properties this server answers, and the few
 * values that are server truth rather than specification default.
 */
public final class SupportedProperties {

    /**
     * The properties this server answers, in the order it has always answered them.
     */
    public static final List<String> NAMES = List.of("AxisFormat", "BeginRange", "Catalog", "Content", "Cube",
            "DataSourceInfo", "Deep", "EmitInvisibleMembers", "EndRange", "Format", "LocaleIdentifier", "MDXSupport",
            "Password", "ProviderName", "ProviderVersion", "ResponseMimeType", "StateSupport", "Timeout", "UserName",
            "VisualMode", "TableFields", "AdvancedFlag", "SafetyOptions", "MdxMissingMemberMode",
            "DbpropMsmdMDXCompatibility", "MdpropMdxSubqueries", "ClientProcessID", "SspropInitAppName",
            "DbpropMsmdSubqueries", "DbpropMsmdActivityID", "DBMSVersion");

    /**
     * Values that are server truth, not specification default: who this provider
     * is, and that its engine runs subselects. {@code Catalog} is answered live and
     * is not listed here.
     */
    public static final Map<String, String> VALUES = Map.of("ProviderName", "Daanse XML for Analysis Provider",
            "ProviderVersion", "11.0.7001.0", "DBMSVersion", "11.0.7001.0", "DbpropMsmdSubqueries", "1");

    private SupportedProperties() {
        // static access only
    }
}
