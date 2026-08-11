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
 */
package org.eclipse.daanse.olap.xmla.connector.discover.csdl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.daanse.xmla.model.csdl.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TDisplayFolder;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TDisplayFolders;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TEntityType;

/**
 * The display-folder paths a client groups hierarchies and properties by in its
 * field list.
 */
public class DisplayFolderBuilder {

    private static BiFactory biFactory = BiFactory.eINSTANCE;

    public void forHierarchy(TEntityType biEntity, String displayFolder, String name) {
        fillDisplayFolder(biEntity, displayFolder, name);
    }

    public void forProperty(TEntityType biEntity, String displayFolder, String name) {
        fillDisplayFolder(biEntity, displayFolder, name);

    }

    private void fillDisplayFolder(TEntityType biEntity, String displayFolder, String name) {
        for (String folder : getFolders(displayFolder)) {
            if (biEntity.getDisplayFolders() == null) {
                TDisplayFolders displayFolders = biFactory.createTDisplayFolders();
                biEntity.setDisplayFolders(displayFolders);
            }
            TDisplayFolder dFolder = biFactory.createTDisplayFolder();
            dFolder.setName(folder);
            biEntity.getDisplayFolders().getDisplayFolder().add(dFolder);
        }
    }

    private List<String> getFolders(String displayFolder) {
        if (displayFolder == null || displayFolder.isBlank()) {
            return List.of();
        }
        return Arrays.asList(displayFolder.split("[\\\\/]"));
    }
}
