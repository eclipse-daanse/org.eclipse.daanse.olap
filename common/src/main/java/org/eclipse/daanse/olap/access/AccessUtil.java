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
package org.eclipse.daanse.olap.access;

import org.eclipse.daanse.olap.api.access.AccessCube;
import org.eclipse.daanse.olap.api.access.AccessDimension;
import org.eclipse.daanse.olap.api.access.AccessHierarchy;
import org.eclipse.daanse.olap.api.access.AccessMember;

public class AccessUtil {

    public static AccessDimension getAccessDimension(AccessCube accessCube) {
        return switch (accessCube) {
            case ALL -> AccessDimension.ALL;
            case NONE -> AccessDimension.NONE;
            case CUSTOM -> AccessDimension.CUSTOM;
            default -> AccessDimension.NONE;
        };
    }

    public static AccessMember getAccessMember(AccessDimension access) {
        return switch (access) {
            case NONE -> AccessMember.NONE;
            case CUSTOM -> AccessMember.CUSTOM;
            case ALL -> AccessMember.ALL;
            default -> AccessMember.NONE;
        };
    }

    public static AccessMember getAccessMember(AccessHierarchy access) {
        return switch (access) {
            case NONE -> AccessMember.NONE;
            case CUSTOM -> AccessMember.CUSTOM;
            case ALL -> AccessMember.ALL;
            default -> AccessMember.NONE;
        };
    }

}
