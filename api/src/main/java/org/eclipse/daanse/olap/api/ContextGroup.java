/*
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.api;

import java.util.List;

/**
 * The  ContextGroup gives access to a set of  Contexts.
 * It guarantees that names of the  Contexts are unique in the group.
 *
 * @author stbischof
 *
 */
public interface ContextGroup {

    /**
     * Gives access to the valid  Contexts.
     * 
     * A Context is only valid, if it in unique in the this Group.
     *
     * @return Context
     */
	List<Context<?>> getValidContexts();
	


}
