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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.odc.api.OdcCreator;
import org.eclipse.daanse.olap.odc.simple.api.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.Designate;

@Designate(ocd = AutOdcConfig.class, factory = true)
@Component(configurationPid = Constants.AUTO_ODC_PID, immediate = true)
public class AutoOdc {

    private ExecutorService newVirtualThreadPerTaskExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private OdcCreator odcCreator;
    private Path outputBasePath;

    @Activate
    public AutoOdc(AutOdcConfig autOdcConfig, @Reference OdcCreator odcCreator) {
        String outputBase = autOdcConfig.outputBasePath();
        outputBasePath = Path.of(outputBase);
        this.odcCreator = odcCreator;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void bindContext(Context<?> context) throws Exception {

        Path path = outputBasePath.resolve(context.getName());
        Files.createDirectories(path);

        newVirtualThreadPerTaskExecutor.execute(() -> {

            try {

                Connection connection = context.getConnectionWithDefaultRole();
                Catalog catalog = connection.getCatalog();
                String out = odcCreator.createCatalogOdc(catalog);

                Files.write(path.resolve("catalog.odc"), out.getBytes());

                for (Cube cube : catalog.getCubes()) {

                    out = odcCreator.createCubeOdc(cube);
                    Files.write(path.resolve("cube_plain_" + cube.getName() + ".odc"), out.getBytes());

                    out = odcCreator.createMdxOdcWithAllMeasures(cube);
                    Files.write(path.resolve("cube_measures_" + cube.getName() + ".odc"), out.getBytes());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    public void unbindContext(Context<?> context, Map<String, Object> props) throws Exception {
        // here that we do not recreate class when a context leave
    }

}
