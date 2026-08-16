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
            "Password", "ProviderName", "ProviderVersion", "ResponseMimeType", "ServerName", "StateSupport", "Timeout",
            "UserName", "VisualMode", "TableFields", "AdvancedFlag", "SafetyOptions", "MdxMissingMemberMode",
            "DbpropMsmdMDXCompatibility", "MdpropMdxSubqueries", "MdpropMdxDrillFunctions", "MdpropMdxNamedSets",
            "MdpropMdxFormulas", "MdpropMdxDdlExtensions", "ClientProcessID", "SspropInitAppName",
            "DbpropMsmdSubqueries", "DbpropMsmdActivityID", "DbpropMsmdCurrentActivityID", "DbpropMsmdOptimizeResponse",
            "DBMSVersion");

    /**
     * The prefix a deployment overrides a value with, as in
     * {@code -Ddaanse.xmla.property.MdpropMdxSubqueries=63}.
     * <p>
     * The masks below are what this engine can actually do. A deployment that knows
     * better - a client that will only generate MDX this server does handle, or a
     * capability added since - can say so without a rebuild, and gets to own the
     * consequence: a mask claiming more than the engine does turns a "not
     * supported" into a parse failure.
     */
    private static final String OVERRIDE_PREFIX = "daanse.xmla.property.";

    /**
     * Values that are server truth rather than specification default.
     * <p>
     * The {@code MdpropMdx*} bitmasks are what a client reads to decide which MDX
     * it may generate, and it asks for them before it builds anything. They are
     * this engine's own claim rather than Analysis Services' copied over, which is
     * why they are lower.
     * <p>
     * {@code Catalog} is answered live and is not listed here.
     */
    public static final Map<String, String> VALUES = withOverrides(Map.ofEntries(
            Map.entry("ProviderName", "Daanse XML for Analysis Provider"),
            Map.entry("ProviderVersion", "11.0.7001.0"), Map.entry("DBMSVersion", "11.0.7001.0"),
            Map.entry("ServerName", serverName()), Map.entry("DbpropMsmdSubqueries", "1"),
            // MDPROPVAL_MSQ_BASIC: subselects without arbitrary shapes.
            Map.entry("MdpropMdxSubqueries", "1"),
            // DRILLDOWNMEMBER, DRILLDOWNLEVEL and their -TOP/-BOTTOM forms.
            Map.entry("MdpropMdxDrillFunctions", "7"),
            // Named sets in a query, in a session, and as a cube object.
            Map.entry("MdpropMdxNamedSets", "15"),
            // MDPROPVAL_MF_WITH_CALCMEMBERS (0x01) and MDPROPVAL_MF_WITH_NAMEDSETS
            // (0x02). The CREATE forms (0x04, 0x08) and the SESSION/GLOBAL scopes (0x10,
            // 0x20) are out: the MDX model knows Select, Drillthrough, Explain, Refresh
            // and Update, and no standalone CREATE statement to carry them.
            Map.entry("MdpropMdxFormulas", "3"),
            // Zero, and not for want of looking: [MS-SSAS] names the property and its
            // type but defines no bit vocabulary for it - only that Analysis Services
            // answers 31 for MOLAP and 23 in-memory. Without stated bits there is
            // nothing here this server could claim on evidence, and it takes no DDL over
            // MDX in any case.
            Map.entry("MdpropMdxDdlExtensions", "0")));

    /** Applies the system-property overrides over the built-in values. */
    private static Map<String, String> withOverrides(Map<String, String> built) {
        Map<String, String> resolved = new java.util.HashMap<>(built);
        for (String name : NAMES) {
            String override = System.getProperty(OVERRIDE_PREFIX + name);
            if (override != null) {
                resolved.put(name, override);
            }
        }
        return Map.copyOf(resolved);
    }

    /**
     * What this server calls itself, which is what a client shows the user. The host
     * name is what Analysis Services answers; a host that cannot be resolved is not
     * worth failing over, so the provider name stands in.
     * <p>
     * Package-private rather than private: the Server element of
     * DISCOVER_XML_METADATA carries the same name, and a client that compares the
     * two must find them agreeing.
     */
    static String serverName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException unresolved) {
            return "Daanse";
        }
    }

    private SupportedProperties() {
        // static access only
    }
}
