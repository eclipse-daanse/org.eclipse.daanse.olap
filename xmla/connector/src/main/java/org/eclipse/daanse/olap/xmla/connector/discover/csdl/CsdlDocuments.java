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
package org.eclipse.daanse.olap.xmla.connector.discover.csdl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.daanse.xmla.model.csdl.v2.bi.BiPackage;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.EdmPackage;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TSchema;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.util.EdmResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;

/**
 * Serializes a CSDL schema to the document string DISCOVER_CSDL_METADATA
 * answers with.
 * <p>
 * The Edmx model it writes is the xmla repository's own
 * ({@code model.csdl.v2}), but this class lives here, beside the emitters that
 * fill that model from a backend's cubes. Serializing is the last step of
 * emitting and has no other caller: the one use of it is the tail of
 * {@code OtherDiscover.csdlMetaData}.
 */
public final class CsdlDocuments {

    private CsdlDocuments() {
        // static access only
    }

    public static String asString(TSchema schema) {
        try {
            return serializeToXml(schema);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String serializeToXml(EObject eObject) throws IOException {
        ResourceSetImpl resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new EdmResourceFactoryImpl());
        resourceSet.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(BiPackage.eNS_URI, BiPackage.eINSTANCE);

        Resource resource = resourceSet.createResource(URI.createURI("temp.xml"));
        resource.getContents().add(eObject);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Map<String, Object> options = new HashMap<>();
        options.put(XMLResource.OPTION_ENCODING, "UTF-8");
        options.put(XMLResource.OPTION_FORMATTED, Boolean.TRUE);
        options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);

        resource.save(baos, options);

        resource.getContents().clear();
        resourceSet.getResources().remove(resource);

        return baos.toString(StandardCharsets.UTF_8);
    }
}
