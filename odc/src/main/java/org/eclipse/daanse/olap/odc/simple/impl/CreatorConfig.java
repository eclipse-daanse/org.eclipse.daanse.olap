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
package org.eclipse.daanse.olap.odc.simple.impl;

import org.eclipse.daanse.olap.odc.simple.api.Constants;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Configuration interface for OLAP ODC Creator service. Defines the OSGi
 * Metatype ObjectClassDefinition for configuration of the Simple ODC Creator
 * implementation.
 * 
 * This configuration allows administrators to customize: - Datasource targeting
 * and filtering - OLEDB connection string parameters - Security settings for
 * OLAP connections - Fallback values for connection failures
 */
@ObjectClassDefinition(name = CreatorConfig.L10N_OCD_CONFIG_NAME, description = CreatorConfig.L10N_OCD_CONFIG_DESCRIPTION, localization = CreatorConfig.OCD_LOCALIZATION)
@interface CreatorConfig {

    // Localization constants
    String OCD_LOCALIZATION = "OSGI-INF/l10n/org.eclipse.daanse.olap.odc.simple.ocd";
    String L10N_PREFIX = "%";
    String L10N_POSTFIX_DESCRIPTION = ".description";
    String L10N_POSTFIX_NAME = ".name";
    String L10N_POSTFIX_OPTION = ".option";
    String L10N_POSTFIX_LABEL = ".label";

    // ObjectClassDefinition localization constants
    String L10N_OCD_CONFIG_NAME = L10N_PREFIX + "ocd.config" + L10N_POSTFIX_NAME;
    String L10N_OCD_CONFIG_DESCRIPTION = L10N_PREFIX + "ocd.config" + L10N_POSTFIX_DESCRIPTION;
    // Datasource attribute localization
    String L10N_DATASOURCE_NAME = L10N_PREFIX + Constants.CREATOR_PROPERTY_DATASOURCE + L10N_POSTFIX_NAME;
    String L10N_DATASOURCE_DESCRIPTION = L10N_PREFIX + Constants.CREATOR_PROPERTY_DATASOURCE + L10N_POSTFIX_DESCRIPTION;

    // MSOLAP Provider attribute localization
    String L10N_MSOLAP_PROVIDER_NAME = L10N_PREFIX + Constants.CREATOR_PROPERTY_MSOLAP_PROVIDER + L10N_POSTFIX_NAME;
    String L10N_MSOLAP_PROVIDER_DESCRIPTION = L10N_PREFIX + Constants.CREATOR_PROPERTY_MSOLAP_PROVIDER
            + L10N_POSTFIX_DESCRIPTION;

    // Integrated Security attribute localization
    String L10N_INTEGRATED_SECURITY_NAME = L10N_PREFIX + Constants.CREATOR_PROPERTY_INTEGRATED_SECURITY + L10N_POSTFIX_NAME;
    String L10N_INTEGRATED_SECURITY_DESCRIPTION = L10N_PREFIX + Constants.CREATOR_PROPERTY_INTEGRATED_SECURITY
            + L10N_POSTFIX_DESCRIPTION;

    // Persist Security Info attribute localization
    String L10N_PERSIST_SECURITY_INFO_NAME = L10N_PREFIX + Constants.CREATOR_PROPERTY_PERSIST_SECURITY_INFO
            + L10N_POSTFIX_NAME;
    String L10N_PERSIST_SECURITY_INFO_DESCRIPTION = L10N_PREFIX + Constants.CREATOR_PROPERTY_PERSIST_SECURITY_INFO
            + L10N_POSTFIX_DESCRIPTION;

    /**
     * Datasource filter or target for OLAP connections. Used to specify which
     * datasource this ODC creator should work with.
     * 
     * @return the datasource filter/target string
     */
    @AttributeDefinition(name = L10N_DATASOURCE_NAME, description = L10N_DATASOURCE_DESCRIPTION, required = true)
    String datasource() default Constants.CREATOR_DEFAULT_DATA_SOURCE;

    /**
     * MSOLAP provider name for OLEDB connections. Specifies which Microsoft
     * Analysis Services OLEDB provider to use.
     * 
     * @return the MSOLAP provider name
     */
    @AttributeDefinition(name = L10N_MSOLAP_PROVIDER_NAME, description = L10N_MSOLAP_PROVIDER_DESCRIPTION, defaultValue = Constants.CREATOR_DEFAULT_MSOLAP_PROVIDER)
    String msOlapProvider() default Constants.CREATOR_DEFAULT_MSOLAP_PROVIDER;


    /**
     * Enable integrated security for OLEDB connections. When true, uses Windows
     * integrated security instead of username/password.
     * 
     * @return true if integrated security should be enabled
     */
    @AttributeDefinition(name = L10N_INTEGRATED_SECURITY_NAME, description = L10N_INTEGRATED_SECURITY_DESCRIPTION, defaultValue = Constants.CREATOR_DEFAULT_INTEGRATED_SECURITY
            + "")
    boolean integratedSecurity() default Constants.CREATOR_DEFAULT_INTEGRATED_SECURITY;

    /**
     * Enable persist security info for OLEDB connections. When true, includes
     * security information in the connection string.
     * 
     * @return true if security info should be persisted
     */
    @AttributeDefinition(name = L10N_PERSIST_SECURITY_INFO_NAME, description = L10N_PERSIST_SECURITY_INFO_DESCRIPTION, defaultValue = Constants.CREATOR_DEFAULT_PERSIST_SECURITY_INFO
            + "")
    boolean persistSecurityInfo() default Constants.CREATOR_DEFAULT_PERSIST_SECURITY_INFO;
}