/*
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.xmla.bridge;

import org.eclipse.daanse.xmla.api.XmlaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;

@ExtendWith(ConfigurationExtension.class)
@ExtendWith(MockitoExtension.class)
class ContextGroupXmlaService_OSGiServiceTest {

    @Test
    void serviceExists_configured(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = ContextGroupXmlaService.PID, name = "name1")) Configuration c,
            @InjectService(cardinality = 0) ServiceAware<XmlaService> saXmlaService) throws Exception {

        // Must exist from start
        /*
         * assertThat(saXmlaService).isNotNull().extracting(ServiceAware::size).isEqualTo(1);
         * assertThat(saXmlaService.getServiceReference()).isNotNull(); XmlaService xmlaService =
         * saXmlaService.waitForService(100);
         * 
         * assertThat(xmlaService).isNotNull().satisfies(x -> {
         * assertThat(x).isInstanceOf(ContextGroupXmlaService.class); assertThat((ContextGroupXmlaService)
         * x).satisfies(bx -> { assertThat(bx.discover()).isNotNull(); assertThat(bx.execute()).isNotNull();
         * }); });
         */
    }
}
