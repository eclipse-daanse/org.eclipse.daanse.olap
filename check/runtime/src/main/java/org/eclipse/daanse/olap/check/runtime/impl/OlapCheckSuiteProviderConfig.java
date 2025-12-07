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
package org.eclipse.daanse.olap.check.runtime.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Configuration for OlapCheckSuiteProvider.
 */
@ObjectClassDefinition(name = "OLAP Check Suite Provider Configuration")
public @interface OlapCheckSuiteProviderConfig {

    @AttributeDefinition(name = "Resource URL", description = "Path to the XMI file containing the OlapCheckSuite")
    String resource_url();
}
