/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.testkit.function;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.ResultStyle;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.testkit.function.FunctionRegistryIndex;

/**
 * What one MDX function promises. A contract is data; {@code AbstractFunctionContractTest}
 * runs it, and {@code FunctionContracts.all()} enumerates it for the coverage meta test.
 *
 * <p>Every promise must either have cases or a waiver with a reason — a promise with
 * neither would pass silently and give false confidence. {@link Builder#build()} enforces
 * that at construction time, not as a test failure.
 */
public record FunctionContract(
        String name,
        Class<? extends OperationAtom> atomClass,
        List<String> declaredSignatures,
        Optional<DataType> returnCategory,
        OptionalInt minArity,
        OptionalInt maxArity,
        List<String> reservedWords,
        List<CallCase> calls,
        List<EdgeCase> edgeCases,
        List<ValueCase> values,
        List<DependencyCase> dependencies,
        List<ResultStyleCase> resultStyles,
        Map<Promise, String> waivers) {

    /** The eight promises of §3. */
    public enum Promise {
        REGISTRATION, SIGNATURE, RESOLUTION, TYPE, RESULT, EDGE, DEPENDENCIES, RESULT_SHAPE
    }

    /** A call with stub arguments — stage A. */
    public record CallCase(
            List<DataType> argumentCategories,
            boolean scalarContext,
            Optional<Class<? extends FunctionDefinition>> expectedDefinition,
            Optional<String> expectedOverload,
            Optional<DataType> expectedReturnCategory,
            OptionalInt expectedConversionCost,
            boolean expectRejection,
            Optional<String> expectedRejectionMessage) {
    }

    /** An edge case — stage A when only categories are given, stage B when MDX is given. */
    /**
     * One edge case.
     *
     * @param knownCrash when present, this case is known to crash undiagnosed today, and
     *                   the text says why it is open. The case still runs: the crash is
     *                   expected, and the test fails if it stops happening, so an entry
     *                   here cannot outlive the defect it records.
     */
    public record EdgeCase(String label, List<DataType> argumentCategories, Optional<String> mdx,
            Optional<String> knownCrash) {

        public EdgeCase(String label, List<DataType> argumentCategories, Optional<String> mdx) {
            this(label, argumentCategories, mdx, Optional.empty());
        }

    }

    /**
     * An MDX expression and its expected formatted value — stage B. {@code formatString},
     * when present, is set as {@code FORMAT_STRING} on the probing calculated member — needed
     * to see fractional digits, since the harness's default cell format has none.
     */
    public record ValueCase(String mdx, String expectedFormattedValue, Optional<String> formatString,
            Optional<String> knownDefect) {

        public ValueCase(String mdx, String expectedFormattedValue, Optional<String> formatString) {
            this(mdx, expectedFormattedValue, formatString, Optional.empty());
        }
    }

    /**
     * An MDX expression and exactly the hierarchies it may depend on — stage B.
     *
     * @param knownDefect when present, this case is known to fail today and the text says
     *                    why. It still runs, and the test fails if it starts passing.
     */
    public record DependencyCase(String mdx, boolean scalar, List<String> hierarchyUniqueNames,
            List<String> forbiddenHierarchyUniqueNames, Optional<String> knownDefect) {

        public DependencyCase(String mdx, boolean scalar, List<String> hierarchyUniqueNames,
                List<String> forbiddenHierarchyUniqueNames) {
            this(mdx, scalar, hierarchyUniqueNames, forbiddenHierarchyUniqueNames,
                    Optional.empty());
        }
    }

    /**
     * An MDX set expression and its result shape — stage B.
     *
     * @param knownDefect see {@link DependencyCase#knownDefect()}
     */
    public record ResultStyleCase(String mdx, ResultStyle requested, ResultStyle expected,
            boolean requireIndependentMutableList, Optional<String> knownDefect, boolean scalar) {

        public ResultStyleCase(String mdx, ResultStyle requested, ResultStyle expected,
                boolean requireIndependentMutableList) {
            this(mdx, requested, expected, requireIndependentMutableList, Optional.empty(), false);
        }

        public ResultStyleCase(String mdx, ResultStyle requested, ResultStyle expected,
                boolean requireIndependentMutableList, Optional<String> knownDefect) {
            this(mdx, requested, expected, requireIndependentMutableList, knownDefect, false);
        }
    }

    public static Builder of(String name) {
        return new Builder(name);
    }

    public boolean isWaived(Promise promise) {
        return waivers.containsKey(promise);
    }

    public String waiverReason(Promise promise) {
        return waivers.get(promise);
    }

    /** {@code "Head (FunctionOperationAtom)"} — the key of the coverage meta test. */
    public String key() {
        return name + " (" + atomClass.getSimpleName() + ")";
    }

    public OperationAtom atom() {
        return atomClass == FunctionOperationAtom.class
                ? new FunctionOperationAtom(name)
                : FunctionRegistryIndex.syntheticAtom(name, atomClass);
    }
}