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
package org.eclipse.daanse.olap.xmla.bridge;

import java.util.List;
import java.util.function.Function;

public class RoleUtils {
    
    public static List<String> getRoles(ContextListSupplyer contextListSupplyer, Function<String, Boolean> isUserInRoleFunction) {
        return contextListSupplyer.getContexts().stream().flatMap(c -> c.getAccessRoles().stream().filter(r -> isUserInRoleFunction.apply(r)).toList().stream()).toList();
    }
}
