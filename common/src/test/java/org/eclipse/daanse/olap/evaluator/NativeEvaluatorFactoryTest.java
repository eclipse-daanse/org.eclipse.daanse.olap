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
package org.eclipse.daanse.olap.evaluator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.evaluator.NativeEvaluator;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.junit.jupiter.api.Test;

class NativeEvaluatorFactoryTest {

    /** A factory that answers only while enabled and tells its listener. */
    static class Probe extends NativeEvaluatorFactory {
        final NativeEvaluator answer = style -> "native";

        @Override
        public NativeEvaluator createEvaluator(Evaluator evaluator, FunctionDefinition fun, Expression[] args,
                boolean enableNativeFilter) {
            if (!isEnabled()) {
                return null;
            }
            if (listener != null) {
                listener.foundEvaluator(new NativeEvent(this));
            }
            return answer;
        }
    }

    @Test
    void disabledByDefaultAndSwitchedLive() {
        Probe probe = new Probe();
        assertThat(probe.isEnabled()).isFalse();
        assertThat(probe.createEvaluator(null, null, new Expression[0], false)).isNull();

        AtomicBoolean flag = new AtomicBoolean(true);
        probe.setEnabled(flag::get);
        assertThat(probe.createEvaluator(null, null, new Expression[0], false)).isSameAs(probe.answer);
        flag.set(false);
        assertThat(probe.createEvaluator(null, null, new Expression[0], false)).isNull();

        probe.setEnabled(true);
        assertThat(probe.isEnabled()).isTrue();
    }

    @Test
    void theListenerHearsAboutAFoundEvaluator() {
        Probe probe = new Probe();
        probe.setEnabled(true);
        List<Object> sources = new ArrayList<>();
        probe.setListener(new NativeEvaluatorFactory.Listener() {
            @Override
            public void foundEvaluator(NativeEvaluatorFactory.NativeEvent e) {
                sources.add(e.getSource());
            }

            @Override
            public void foundInCache(NativeEvaluatorFactory.TupleEvent e) {
            }

            @Override
            public void executingSql(NativeEvaluatorFactory.TupleEvent e) {
            }
        });
        probe.createEvaluator(null, null, new Expression[0], false);
        assertThat(sources).containsExactly(probe);
    }
}
