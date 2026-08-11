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
package org.eclipse.daanse.olap.xmla.connector.discover.csdl;

import java.util.Locale;

/**
 * Which locale the emitted captions are in, and where that locale came from.
 * <p>
 * The three cases are kept apart rather than collapsed to a Locale because a
 * caption chosen by the connection and one chosen by the server default are
 * worth telling apart when a client complains about the language.
 */
public sealed interface LocalePolicy {
    record Fixed(Locale locale) implements LocalePolicy {
    }

    record FromConnection(Locale locale) implements LocalePolicy {
    }

    record ServerDefault(Locale locale) implements LocalePolicy {
    }

    public Locale locale();
}
