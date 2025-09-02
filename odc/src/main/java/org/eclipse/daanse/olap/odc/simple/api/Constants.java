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
*/
package org.eclipse.daanse.olap.odc.simple.api;

/**
 * Constants for the OLAP ODC Simple implementation. Contains property names,
 * service PIDs, and other configuration constants used for OSGi service
 * configuration and metatype definitions.
 */
public class Constants {

    private Constants() {
        // Utility class
    }

    /**
     * Constant for the {@link org.osgi.framework.Constants#SERVICE_PID} of the ODC
     * Creator service.
     */
    public static final String AUTO_ODC_PID = "daanse.olap.odc.simple.auto";

    /**
     * Constant for the {@link org.osgi.framework.Constants#SERVICE_PID} of the ODC
     * Creator service.
     */
    public static final String CREATOR_PID = "daanse.olap.odc.simple.creator";

    /**
     * Constant for Properties of the Service that could be configured using the
     * {@link Constants#CREATOR_PID}.
     * 
     * Specifies the datasource filter or target for OLAP connections.
     */
    public static final String CREATOR_PROPERTY_DATASOURCE = "datasource";

    /**
     * Constant for Properties of the Service that could be configured using the
     * {@link Constants#CREATOR_PID}.
     * 
     * Specifies the default MSOLAP provider for OLEDB connections.
     */
    public static final String CREATOR_PROPERTY_MSOLAP_PROVIDER = "msOlapProvider";

    /**
     * Constant for Properties of the Service that could be configured using the
     * {@link Constants#CREATOR_PID}.
     * 
     * Enables or disables integrated security for OLEDB connections.
     */
    public static final String CREATOR_PROPERTY_INTEGRATED_SECURITY = "integratedSecurity";

    /**
     * Constant for Properties of the Service that could be configured using the
     * {@link Constants#CREATOR_PID}.
     * 
     * Enables or disables persist security info for OLEDB connections.
     */
    public static final String CREATOR_PROPERTY_PERSIST_SECURITY_INFO = "persistSecurityInfo";

    /**
     * Default value for MSOLAP provider.
     */
    public static final String CREATOR_DEFAULT_MSOLAP_PROVIDER = "MSOLAP";

    /**
     * Default value for data source when extraction fails.
     */
    public static final String CREATOR_DEFAULT_DATA_SOURCE = "http://localhost";

    /**
     * Default value for integrated security.
     */
    public static final boolean CREATOR_DEFAULT_INTEGRATED_SECURITY = true;

    /**
     * Default value for persist security info.
     */
    public static final boolean CREATOR_DEFAULT_PERSIST_SECURITY_INFO = true;
}
