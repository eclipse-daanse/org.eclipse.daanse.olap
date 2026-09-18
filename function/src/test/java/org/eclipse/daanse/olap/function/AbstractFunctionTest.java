/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.function;

import java.util.Optional;

import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.function.FunctionService;
import org.eclipse.daanse.olap.function.services.standard.StandardFunctions;
import org.eclipse.daanse.olap.testkit.function.AbstractFunctionContractTest;

/**
 * The base class every function contract test in this module extends.
 *
 * <p>It supplies the two things the test kit deliberately does not know, because it must
 * not depend on this module: the registry the functions are actually in, and the
 * connection stage B evaluates against.
 *
 * <p>A concrete test is three lines:
 *
 * <pre>
 * class HeadContractTest extends AbstractFunctionTest {
 *     &#64;Override protected FunctionContract contract() { return HeadContract.CONTRACT; }
 * }
 * </pre>
 */
public abstract class AbstractFunctionTest extends AbstractFunctionContractTest {

    @Override
    protected FunctionService functionService() {
        return StandardFunctions.standard();
    }

    @Override
    protected Optional<Connection> connection() {
        return TestCatalogs.connection();
    }

    @Override
    protected String cubeName() {
        return TestCatalogs.CUBE_NAME;
    }
}
