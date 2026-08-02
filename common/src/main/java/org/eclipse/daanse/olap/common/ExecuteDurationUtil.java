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
 *   SmartCity Jena, Stefan Bischof - initial
 *
 */
package org.eclipse.daanse.olap.common;

import java.time.Duration;
import java.util.Optional;

import org.eclipse.daanse.olap.api.Context;

public class ExecuteDurationUtil {

    /**
     * The configured limit on how long a single statement may run, or empty when
     * none is set.
     *
     * <p>
     * Every {@link java.util.concurrent.TimeUnit} is honoured.
     * </p>
     */
    public static Optional<Duration> executeDurationValue(Context<?> context) {
        long duration = context.getConfig().executeDuration();
        if (duration <= 0) {
            return Optional.empty();
        }
        return Optional.of(Duration.of(duration, context.getConfig().executeDurationUnit().toChronoUnit()));
    }

}
