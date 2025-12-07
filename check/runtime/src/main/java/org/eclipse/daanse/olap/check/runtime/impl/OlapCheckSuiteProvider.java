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

import java.io.IOException;
import java.util.Map;

import org.eclipse.daanse.olap.check.model.check.OlapCheckPackage;
import org.eclipse.daanse.olap.check.model.check.OlapCheckSuite;
import org.eclipse.daanse.olap.check.runtime.api.OlapCheckSuiteSupplier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.Designate;

/**
 * Provider that loads an OlapCheckSuite from an XMI file and exposes it as an
 * OSGi service. Multiple instances can be created via factory configuration.
 */
@Component(service = OlapCheckSuiteSupplier.class, scope = ServiceScope.SINGLETON, configurationPid = OlapCheckSuiteProvider.PID)
@Designate(factory = true, ocd = OlapCheckSuiteProviderConfig.class)
public class OlapCheckSuiteProvider implements OlapCheckSuiteSupplier {

    public static final String PID = "org.eclipse.daanse.olap.check.suite.provider";

    @Reference(target = "(" + EMFNamespaces.EMF_MODEL_NAME + "=" + OlapCheckPackage.eNAME + ")")
    private ResourceSet resourceSet;

    private OlapCheckSuite checkSuite;

    @Activate
    public void activate(OlapCheckSuiteProviderConfig config) throws IOException {
        String url = config.resource_url();
        URI uri = URI.createFileURI(url);
        Resource resource = resourceSet.getResource(uri, true);
        resource.load(Map.of());
        EcoreUtil.resolveAll(resource);
        EList<EObject> contents = resource.getContents();

        for (EObject eObject : contents) {
            if (eObject instanceof OlapCheckSuite suite) {
                checkSuite = suite;
                return;
            }
        }

        throw new IOException("No OlapCheckSuite found in resource: " + uri);
    }

    @Deactivate
    public void deactivate() {
        resourceSet.getResources().forEach(Resource::unload);
    }

    @Override
    public OlapCheckSuite get() {
        return checkSuite;
    }
}
