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
package org.eclipse.daanse.olap.api.function;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;

/**
 * Trace of one overload resolution: every considered resolver with its outcome
 * and the minimum-cost matches. Resolution succeeds iff there is exactly one
 * best match.
 */
public record ResolutionExplanation(OperationAtom operationAtom, String signature,
        List<CandidateReport> candidates, List<FunctionResolutionResult> bestMatches) {

    public Optional<FunctionResolutionResult> winner() {
        return bestMatches.size() == 1 ? Optional.of(bestMatches.get(0)) : Optional.empty();
    }
}
