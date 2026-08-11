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
package org.eclipse.daanse.olap.xmla.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.daanse.olap.api.ContextGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** The bridge's {@code ContextsSupplyerImplTest}, unchanged in substance. */
@ExtendWith(MockitoExtension.class)
class ContextsSupplyerImplTest {

    @Mock
    private ContextGroup contextGroup;

    @Test
    void anEmptyGroupAnswersAnEmptyList() {
        when(contextGroup.getValidContexts()).thenReturn(List.of());
        ContextsSupplyerImpl supplyer = new ContextsSupplyerImpl(contextGroup);
        assertThat(supplyer.getContexts()).isNotNull().isEmpty();
    }
}
