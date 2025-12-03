/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.daanse.olap.check.runtime.impl;

import java.io.IOException;

import org.eclipse.daanse.olap.check.model.check.OlapCheckModel;
import org.eclipse.daanse.olap.check.model.check.OlapCheckPackage;
import org.eclipse.daanse.olap.check.runtime.api.CheckModelLoader;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation of CheckModelLoader that loads OLAP check models from XMI files.
 */
@Component(service = CheckModelLoader.class)
public class CheckModelLoaderImpl implements CheckModelLoader {

    private final ResourceSet resourceSet;

    public CheckModelLoaderImpl() {
        this.resourceSet = new ResourceSetImpl();
        initialize();
    }

    private void initialize() {
        // Register the package
        OlapCheckPackage.eINSTANCE.eClass();

        // Register XMI resource factory for .xmi and .olapcheck extensions
        resourceSet.getResourceFactoryRegistry()
            .getExtensionToFactoryMap()
            .put("xmi", new XMIResourceFactoryImpl());

        resourceSet.getResourceFactoryRegistry()
            .getExtensionToFactoryMap()
            .put("olapcheck", new XMIResourceFactoryImpl());

        // Register the package in the resource set
        resourceSet.getPackageRegistry()
            .put(OlapCheckPackage.eNS_URI, OlapCheckPackage.eINSTANCE);
    }

    @Override
    public OlapCheckModel loadFromXMI(URI uri) throws IOException {
        Resource resource = resourceSet.getResource(uri, true);

        if (resource.getContents().isEmpty()) {
            throw new IOException("No content found in resource: " + uri);
        }

        Object root = resource.getContents().get(0);
        if (!(root instanceof OlapCheckModel)) {
            throw new IOException(
                "Root element is not an OlapCheckModel: " + root.getClass().getName()
            );
        }

        return (OlapCheckModel) root;
    }

    @Override
    public OlapCheckModel loadFromJSON(URI uri) throws IOException {
        // TODO: Implement JSON loading with Gecko EMF JSON support
        throw new UnsupportedOperationException("JSON loading not yet implemented");
    }
}
