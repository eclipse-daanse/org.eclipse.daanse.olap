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
package org.eclipse.daanse.olap.util.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class FormatCacheTest {

    @Test
    void testGetReturnsSameInstance() {
        Format f1 = Format.get("#,##0.00", Locale.US);
        Format f2 = Format.get("#,##0.00", Locale.US);
        assertThat(f1).isSameAs(f2);
    }

    @Test
    void testGetDifferentFormatsReturnDifferentInstances() {
        Format f1 = Format.get("#,##0.00", Locale.US);
        Format f2 = Format.get("0.00", Locale.US);
        assertThat(f1).isNotSameAs(f2);
    }

    @Test
    void testGetDifferentLocalesReturnDifferentInstances() {
        Format f1 = Format.get("#,##0.00", Locale.US);
        Format f2 = Format.get("#,##0.00", Locale.FRANCE);
        assertThat(f1).isNotSameAs(f2);
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    // Each thread formats with a unique format string
                    String formatStr = "0." + "0".repeat(idx + 1);
                    Format f = Format.get(formatStr, Locale.US);
                    String result = f.format(123.456);
                    assertThat(result).isNotNull();
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        assertThat(errors.get()).isZero();
    }

    @Test
    void testConcurrentAccessSameFormat() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);
        Format[] results = new Format[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    results[idx] = Format.get("##0.00", Locale.US);
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        assertThat(errors.get()).isZero();

        // All results should be the same instance (from cache)
        for (int i = 1; i < threadCount; i++) {
            if (results[i] != null && results[0] != null) {
                assertThat(results[i]).isSameAs(results[0]);
            }
        }
    }
}
