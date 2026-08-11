/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.exceptions;

import java.text.MessageFormat;

import org.eclipse.daanse.olap.api.exception.OlapRuntimeException;

@SuppressWarnings("serial")
public class FailedToParseQueryException extends OlapRuntimeException {

	private final static String failedToParseQuery = "Failed to parse query ''{0}'': {1}";

	public FailedToParseQueryException(String query, Throwable e) {
		// The cause's message is part of this message: the reader of a fault sees only the
		// outermost text, and "failed to parse" without the why hides the actual error.
		super(MessageFormat.format(failedToParseQuery, query, reasonOf(e)), e);
	}

	private static String reasonOf(Throwable e) {
		if (e == null || e.getMessage() == null || e.getMessage().isBlank()) {
			return "no reason given";
		}
		return e.getMessage();
	}
}
