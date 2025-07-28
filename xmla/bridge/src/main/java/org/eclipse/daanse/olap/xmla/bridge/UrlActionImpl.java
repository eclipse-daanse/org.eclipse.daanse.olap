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

import java.util.Map;

import org.eclipse.daanse.olap.action.api.UrlAction;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.Converters;

@Component(service = UrlAction.class)
@Designate(factory = true, ocd = UrlActionConfig.class)
public class UrlActionImpl extends AbstractAction implements UrlAction {

    private static final Converter CONVERTER = Converters.standardConverter();
    private UrlActionConfig config;

    @Activate
    void activate(Map<String, Object> props) {
        this.config = CONVERTER.convert(props).to(UrlActionConfig.class);
    }

    @Override
    public String content(String coordinate, String cubeName) {
        return config.actionUrl();
    }

    @Override
    protected AbstractActionConfig getConfig() {
        return config;
    }
}
