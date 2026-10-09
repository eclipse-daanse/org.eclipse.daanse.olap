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
 *   dbulahov - initial
 */
package org.eclipse.daanse.olap.xmla.connector.discover.csdl;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The format string of a measure as CSDL gives it to a tabular client.
 * <p>
 * MDX names some formats, as {@code Currency} or {@code Percent}; a tabular
 * client such as Power BI takes the format string as a .NET custom format, in
 * which such a name is text shown instead of the value. A named number format
 * is given as the .NET format it stands for; the named date and time formats
 * a tabular client knows by name stay.
 * </p>
 */
final class CsdlFormatStrings {

    private static final Map<String, String> NAMED = Map.ofEntries(
            Map.entry("currency", "\\$#,0.00;(\\$#,0.00);\\$#,0.00"),
            Map.entry("fixed", "0.00"),
            Map.entry("standard", "#,0.00"),
            Map.entry("percent", "0.00%"),
            Map.entry("scientific", "0.00E+00"),
            Map.entry("yes/no", "\"Yes\";\"Yes\";\"No\""),
            Map.entry("true/false", "\"True\";\"True\";\"False\""),
            Map.entry("on/off", "\"On\";\"On\";\"Off\""),
            Map.entry("medium date", "dd-MMM-yy"),
            Map.entry("medium time", "hh:mm tt"));

    private CsdlFormatStrings() {
    }

    /**
     * @param format the format string of the measure in MDX
     * @return the format string CSDL gives; empty for none, as of a blank one
     *         or {@code General Number}, which shows numbers as they are
     */
    static Optional<String> of(String format) {
        if (format == null || format.isBlank()) {
            return Optional.empty();
        }
        String name = format.strip().toLowerCase(Locale.ROOT);
        if (name.equals("general number") || name.equals("general")) {
            return Optional.empty();
        }
        return Optional.of(NAMED.getOrDefault(name, format));
    }
}
