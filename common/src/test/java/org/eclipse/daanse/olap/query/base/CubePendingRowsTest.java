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
package org.eclipse.daanse.olap.query.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.api.element.Cube;
import org.junit.jupiter.api.Test;

/**
 * The bracket every query of a writeback-enabled cube runs in.
 * <p>
 * What it has to guarantee is not the rewriting - that is
 * {@code modifyFact}'s job - but the pairing: the fact goes back however the
 * work ends. Without that, one failed statement leaves the cube describing a
 * session that is over, and every later query on it reads those values.
 */
class CubePendingRowsTest {

    private final List<String> order = new ArrayList<>();

    private Cube bracketing() {
        Cube cube = mock(Cube.class);
        when(cube.withPendingRows(any(), any())).thenCallRealMethod();
        doAnswer(invocation -> order.add("modify")).when(cube).modifyFact(any());
        doAnswer(invocation -> order.add("restore")).when(cube).restoreFact();
        return cube;
    }

    @Test
    void theFactIsRewrittenBeforeTheWorkAndPutBackAfter() {
        String answer = bracketing().withPendingRows(List.of(), () -> {
            order.add("work");
            return "done";
        });

        assertThat(answer).isEqualTo("done");
        assertThat(order).containsExactly("modify", "work", "restore");
    }

    @Test
    void theFactIsPutBackEvenWhenTheWorkThrows() {
        Cube cube = bracketing();

        assertThatThrownBy(() -> cube.withPendingRows(List.of(), () -> {
            order.add("work");
            throw new IllegalStateException("the query failed");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(order).containsExactly("modify", "work", "restore");
    }
}
