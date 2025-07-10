/*
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.eclipse.daanse.olap.calc.base;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import org.eclipse.daanse.olap.api.type.Type;

public abstract class AbstractProfilingIteratorCalc<T> extends AbstractProfilingCalc<T> {

	public AbstractProfilingIteratorCalc(Type type) {
		super(type);
	}

	private long elementCount;
	private long elementSquaredCount;

	@Override
	protected void profileEvaluation(Instant evaluationStart, Instant evaluationEnd, T evaluationResult) {
		super.profileEvaluation(evaluationStart, evaluationEnd, evaluationResult);
		if (evaluationResult instanceof Collection c) {
			long size = c.size();
			elementCount += size;
			elementSquaredCount += size * size;
		}
	}

	@Override
	protected Map<String, Object> profilingProperties(Map<String, Object> properties) {

		// TODO: may be removed writer can calculate its won because has access to the
		// objects
		properties.put("elementCount", elementCount);
		properties.put("elementSquaredCount", elementSquaredCount);
		return properties;
	}

}
