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
 */
package org.eclipse.daanse.olap.api.exception;

/**
 * Exception thrown when a type conversion fails during expression compilation.
 *
 * <p>This exception indicates that a value could not be converted to the expected
 * target type during the compilation or evaluation of an MDX expression.</p>
 */
@SuppressWarnings("serial")
public class TypeConversionException extends OlapRuntimeException {

    /**
     * Creates a TypeConversionException with a message.
     *
     * @param message the detail message
     */
    public TypeConversionException(String message) {
        super(message);
    }

    /**
     * Creates a TypeConversionException with a message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public TypeConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a TypeConversionException for a failed type conversion.
     *
     * @param value the value that could not be converted
     * @param targetType the target type name
     * @return a new TypeConversionException with a descriptive message
     */
    public static TypeConversionException cannotConvert(Object value, String targetType) {
        String valueType = value == null ? "null" : value.getClass().getName();
        return new TypeConversionException(
            "Cannot convert %s to %s".formatted(valueType, targetType));
    }
}
