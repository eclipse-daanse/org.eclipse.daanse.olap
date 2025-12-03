/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.daanse.olap.check.runtime.api;

import java.io.IOException;

import org.eclipse.daanse.olap.check.model.check.OlapCheckModel;
import org.eclipse.emf.common.util.URI;

/**
 * Loader for OLAP check models from XMI or JSON files.
 */
public interface CheckModelLoader {

    /**
     * Load a check model from an XMI file.
     *
     * @param uri the URI to the XMI file
     * @return the loaded check model
     * @throws IOException if the file cannot be read or parsed
     */
    OlapCheckModel loadFromXMI(URI uri) throws IOException;

    /**
     * Load a check model from a JSON file.
     *
     * @param uri the URI to the JSON file
     * @return the loaded check model
     * @throws IOException if the file cannot be read or parsed
     */
    OlapCheckModel loadFromJSON(URI uri) throws IOException;
}
