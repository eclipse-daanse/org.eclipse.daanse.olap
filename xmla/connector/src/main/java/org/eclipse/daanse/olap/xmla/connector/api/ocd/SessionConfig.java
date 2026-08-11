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
package org.eclipse.daanse.olap.xmla.connector.api.ocd;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * How long a session lives, decided here rather than by the protocol.
 * <p>
 * [MS-SSAS] 3.1.3.1 makes idle expiry a {@code MAY} with no value at all, and
 * 3.1.2 Timers says the protocol has none - so a server that never expires a
 * session is conformant, and nothing above this component may impose a
 * lifetime. What makes the decision belong here is that this is what actually
 * holds something: an OLAP connection per catalog, for as long as the session
 * lasts.
 * <p>
 * Every duration is in seconds, and {@code 0} means never.
 */
@ObjectClassDefinition(name = "%ocd.name", description = "%ocd.description", localization = "OSGI-INF/l10n/org.eclipse.daanse.olap.xmla.connector")
public @interface SessionConfig {

    /**
     * How long a session may go unused before it is ended.
     * <p>
     * The default is the one number Analysis Services documents for this
     * ({@code MinIdleSessionTimeout}, 45 minutes), so clients built against it -
     * Excel, Power BI, ADOMD - meet what they expect. It is a default, not a rule.
     */
    @AttributeDefinition(name = "%sessionIdleTimeoutSeconds.name", description = "%sessionIdleTimeoutSeconds.description")
    long sessionIdleTimeoutSeconds() default 2700;

    /**
     * How long a session may live however busy it is. Off by default: nothing
     * normative asks for an absolute cap, and it exists as a safety valve.
     */
    @AttributeDefinition(name = "%sessionMaxLifetimeSeconds.name", description = "%sessionMaxLifetimeSeconds.description")
    long sessionMaxLifetimeSeconds() default 0;

    /**
     * How often expired sessions are swept up. It bounds how long a session
     * outlives its timeout, which is why it is not simply the timeout itself: a
     * session is also checked the moment it is used again.
     */
    @AttributeDefinition(name = "%sessionSweepIntervalSeconds.name", description = "%sessionSweepIntervalSeconds.description")
    long sessionSweepIntervalSeconds() default 60;

    /**
     * How many sessions may be open at once. Reaching it declines further
     * {@code BeginSession} requests, which is a better answer than running out of
     * connections.
     */
    @AttributeDefinition(name = "%maxSessions.name", description = "%maxSessions.description")
    int maxSessions() default 0;
}
