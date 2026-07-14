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
package org.eclipse.daanse.olap.xmla.bridge.discover.csdl.dimensional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;

import org.eclipse.daanse.olap.api.element.MetaData;
import org.eclipse.daanse.olap.api.element.OlapElement;
import org.eclipse.daanse.olap.api.element.OlapElement.LocalizedProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TCulture;
import org.eclipse.daanse.xmla.csdl.model.v2.bi.TCultures;

public final class CultureEmitter {

    private static final BiFactory BI = BiFactory.eINSTANCE;

    private final EmitContext ctx;
    private final List<Locale> configuredLocales;
    private final Locale containerLocale;

    public CultureEmitter(EmitContext ctx, List<Locale> configuredLocales, Locale containerLocale) {
        this.ctx = ctx;
        this.configuredLocales = configuredLocales;
        this.containerLocale = containerLocale;
    }

    public Optional<TCultures> culturesFor(OlapElement element) {
        if (!ctx.emitAllTranslations() || !ctx.biVersionAtLeast(2, 0)) {
            return Optional.empty();
        }
        TCultures cultures = BI.createTCultures();
        candidateLocales().stream()
                .filter(l -> !sameLanguageTag(l, containerLocale))
                .sorted((a, b) -> a.toLanguageTag().compareTo(b.toLanguageTag()))
                .forEach(locale -> {
                    Optional<String> caption = translated(element, LocalizedProperty.CAPTION, locale,
                            element.getCaption());
                    Optional<String> description = translated(element, LocalizedProperty.DESCRIPTION, locale,
                            element.getDescription());
                    if (caption.isEmpty() && description.isEmpty()) {
                        return;
                    }
                    TCulture culture = BI.createTCulture();
                    culture.setName(locale.toLanguageTag());
                    caption.ifPresent(culture::setCaption);
                    description.ifPresent(culture::setDescription);
                    cultures.getCulture().add(culture);
                });
        return cultures.getCulture().isEmpty() ? Optional.empty() : Optional.of(cultures);
    }

    private Optional<String> translated(OlapElement element, LocalizedProperty prop, Locale locale,
            String defaultValue) {
        MetaData metaData = ctx.metaDataOf(element);
        if (metaData == null) {
            return Optional.empty();
        }
        return metaData.getLocalized(prop, locale)
                .filter(s -> !s.isBlank())
                .filter(s -> !s.equals(defaultValue));
    }

    private List<Locale> candidateLocales() {
        return ctx.union(configuredLocales, ctx.discoveredLocales());
    }

    private static boolean sameLanguageTag(Locale a, Locale b) {
        return a.toLanguageTag().equalsIgnoreCase(b.toLanguageTag());
    }

    public static Optional<Locale> fromLcid(Integer lcid) {
        if (lcid == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(switch (lcid) {
            case 1031 -> Locale.forLanguageTag("de-DE");
            case 1033 -> Locale.forLanguageTag("en-US");
            case 1036 -> Locale.forLanguageTag("fr-FR");
            case 2057 -> Locale.forLanguageTag("en-GB");
            case 3084 -> Locale.forLanguageTag("fr-CA");
            default -> null;
        });
    }
}