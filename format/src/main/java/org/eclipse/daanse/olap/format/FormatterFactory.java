/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2016-2017 Hitachi Vantara and others
 * All Rights Reserved.
 *
 * ---- All changes after Fork in 2023 ------------------------
 *
 * Project: Eclipse daanse
 *
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 */

package org.eclipse.daanse.olap.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.daanse.olap.api.exception.OlapRuntimeException;
import org.eclipse.daanse.olap.api.formatter.CellFormatter;
import org.eclipse.daanse.olap.api.formatter.MemberFormatter;
import org.eclipse.daanse.olap.api.formatter.MemberPropertyFormatter;


/**
 * Formatter factory to provide a single point
 * to create different formatters for element values.
 *
 *
 * Uses provided context data to instantiate a formatter for the element
 * either by specified class name or script.
 *
 * <p><b>TODO (planned):</b> migrate formatter resolution to OSGi Declarative Services. Each formatter
 * implementation would be published as a {@code @Component} (e.g. with a {@code name} service property),
 * all of them {@code @Reference}-injected here into a {@code Map<String, CellFormatter/MemberFormatter/…>}
 * keyed by that name, and a catalog's {@code formatter='…'} would be resolved by a map lookup instead of
 * reflective {@link Class#forName} loading. That removes the cross-bundle class-visibility problem entirely
 * (no class loader guessing); the {@code Class.forName} + thread-context-class-loader fallback in
 * {@link #createFormatter} is the interim bridge until then.
 */
public class FormatterFactory {

    /**
     * The default formatter which is used
     * when no custom formatter is specified.
     */
    private static final DefaultFormatter DEFAULT_FORMATTER =
        new DefaultFormatter();


    private static final MemberPropertyFormatter DEFAULT_PROPERTY_FORMATTER =
        new PropertyFormatterAdapter(DEFAULT_FORMATTER);

    private static final MemberFormatter DEFAULT_MEMBER_FORMATTER =
        new DefaultRolapMemberFormatter(DEFAULT_FORMATTER);


    private static final FormatterFactory INSTANCE = new FormatterFactory();
    private final static String memberFormatterLoadFailed = "Failed to load formatter class ''{0}'' for level ''{1}''.";
    private final static String cellFormatterLoadFailed = "Failed to load formatter class ''{0}'' for member ''{1}''.";
    private final static String propertyFormatterLoadFailed =
        "Failed to load formatter class ''{0}'' for property ''{1}''.";

    private FormatterFactory() {
    }

    public static FormatterFactory instance() {
        return INSTANCE;
    }

    /**
     * Given the name of a cell formatter class and/or a cell formatter script,
     * returns a cell formatter.
     * 
     *     Returns null if empty context is passed.
     * 
     */
    public CellFormatter createCellFormatter(FormatterCreateContext context) {
        try {
            if (context.getFormatterClassName() != null) {
                return createFormatter(context.getFormatterClassName());
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new OlapRuntimeException(MessageFormat.format(cellFormatterLoadFailed,
                context.getFormatterClassName(),
                context.getElementName(),
                e));
        }
        return null;
    }

    /**
     * Given the name of a member formatter class
     * and/or a member formatter script, returns a member formatter.
     *
     *     Returns default formatter implementation
     *     if empty context is passed.
     *
     */
    public MemberFormatter createRolapMemberFormatter(
        FormatterCreateContext context)
    {
        try {
            if (context.getFormatterClassName() != null) {
                return createFormatter(context.getFormatterClassName());
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new OlapRuntimeException(MessageFormat.format(memberFormatterLoadFailed,
                context.getFormatterClassName(),
                context.getElementName(),
                e));
        }
        return DEFAULT_MEMBER_FORMATTER;
    }

    /**
     * Given the name of a property formatter class
     * and/or a property formatter script,
     * returns a property formatter.
     *
     *     Returns default formatter implementation
     *     if empty context is passed.
     *
     */
    public MemberPropertyFormatter createPropertyFormatter(
        FormatterCreateContext context)
    {
        try {
            if (context.getFormatterClassName() != null) {
                return createFormatter(context.getFormatterClassName());
            }
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new OlapRuntimeException(MessageFormat.format(propertyFormatterLoadFailed,
                context.getFormatterClassName(),
                context.getElementName(),
                e));
        }
        return DEFAULT_PROPERTY_FORMATTER;
    }

    private static <T> T createFormatter(String className) throws
        ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
        InstantiationException, IllegalAccessException {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) loadFormatterClass(className);
        Constructor<T> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    // Interim, to be replaced by DS-injected formatters looked up by name (see the class javadoc TODO):
    // reflective class loading cannot reliably see a formatter defined in another bundle under OSGi.
    private static Class<?> loadFormatterClass(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            // A formatter class named in a catalog can live in a bundle (e.g. a test fragment, or the
            // application bundle) that this format bundle's class loader cannot see. Fall back to the
            // thread context class loader before giving up.
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            if (tccl != null) {
                return Class.forName(className, true, tccl);
            }
            throw e;
        }
    }
}
