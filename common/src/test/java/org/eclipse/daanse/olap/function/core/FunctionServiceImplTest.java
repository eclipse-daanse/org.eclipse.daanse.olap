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
package org.eclipse.daanse.olap.function.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FunctionServiceImplTest {

    private FunctionServiceImpl functionService;
    private FunctionResolver resolver;

    @BeforeEach
    void setUp() {
        functionService = new FunctionServiceImpl();

        FunctionOperationAtom atom = new FunctionOperationAtom("TestFun");
        FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, "test function", DataType.NUMERIC,
                new FunctionParameterR[] { new FunctionParameterR(DataType.SET) });

        resolver = mock(FunctionResolver.class);
        when(resolver.getFunctionAtom()).thenReturn(atom);
        when(resolver.getRepresentativeFunctionMetaDatas()).thenReturn(List.of(functionMetaData));
        when(resolver.getReservedWords()).thenReturn(List.of("TESTWORD"));
    }

    @Test
    void addResolverRegistersResolverAndMetaData() {
        functionService.addResolver(resolver);

        assertThat(functionService.getResolvers()).containsExactly(resolver);
        assertThat(functionService.getResolvers(resolver.getFunctionAtom())).containsExactly(resolver);
        assertThat(functionService.getFunctionMetaDatas()).hasSize(1);
        assertThat(functionService.isReservedWord("testword")).isTrue();
    }

    @Test
    void removeResolverDeregistersResolver() {
        functionService.addResolver(resolver);

        functionService.removeResolver(resolver);

        assertThat(functionService.getResolvers()).isEmpty();
        assertThat(functionService.getResolvers(resolver.getFunctionAtom())).isEmpty();
        assertThat(functionService.getFunctionMetaDatas()).isEmpty();
        assertThat(functionService.isReservedWord("testword")).isFalse();
    }

    @Test
    void removeResolverIsIdempotentAndKeepsOthers() {
        functionService.addResolver(resolver);
        functionService.removeResolver(resolver);
        functionService.removeResolver(resolver);

        assertThat(functionService.getResolvers()).isEmpty();
    }
}
