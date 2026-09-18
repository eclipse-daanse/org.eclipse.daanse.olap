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
package org.eclipse.daanse.olap.provider.memory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.result.CellValue;
import org.eclipse.daanse.olap.api.result.NullValue;
import org.eclipse.daanse.olap.api.result.ObjectValue;
import org.eclipse.daanse.olap.api.result.CellReader;

/**
 * Answers from values written down by hand, keyed by the unique names of the members that
 * address them.
 *
 * <p>A coordinate that was written down is answered directly. A coordinate above the
 * leaves is <b>summed over the leaves below it</b>, which is what makes a handful of
 * entries enough to answer a whole cube: write down the leaves and every total follows.
 * A coordinate with nothing below it that was written down is an empty cell.
 */
public class MapCellReader implements CellReader {

    private final Map<List<String>, Object> values = new LinkedHashMap<>();
    private final List<String> keyDimensions = new ArrayList<>();

    /** Records one value, addressed by the unique names of its members. */
    public MapCellReader at(Object value, String... memberUniqueNames) {
        if (keyDimensions.isEmpty()) {
            for (String name : memberUniqueNames) {
                keyDimensions.add(dimensionOf(name));
            }
        }
        values.put(List.of(memberUniqueNames), value);
        return this;
    }

    /** The leading bracketed segment, which names the dimension a member belongs to. */
    private static String dimensionOf(String memberUniqueName) {
        int end = memberUniqueName.indexOf(']');
        return end < 0 ? memberUniqueName : memberUniqueName.substring(0, end + 1);
    }

    /**
     * The part of the coordinate this reader was told about, in the order it was told.
     *
     * <p>An evaluator names the current member of every hierarchy in the cube, but a value
     * written down for {@code (South, Amount)} says nothing about time. Those hierarchies
     * are dropped rather than made part of the key, which is what "this value holds
     * whatever else is in context" means.
     */
    private List<Member> project(List<Member> coordinate) {
        if (keyDimensions.isEmpty()) {
            return coordinate;
        }
        List<Member> projected = new ArrayList<>(keyDimensions.size());
        for (String dimension : keyDimensions) {
            for (Member member : coordinate) {
                if (member.getUniqueName().startsWith(dimension)) {
                    projected.add(member);
                    break;
                }
            }
        }
        return projected.size() == keyDimensions.size() ? projected : coordinate;
    }

    @Override
    public CellValue get(Evaluator evaluator) {
        Object value = valueAt(List.of(evaluator.getMembers()));
        return value == null ? NullValue.INSTANCE : new ObjectValue(value);
    }

    @Override
    public int getMissCount() {
        return 0;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    /**
     * The value at one coordinate, or {@code null} for an empty cell. Exposed so a test
     * can check the rollup without building an evaluator.
     */
    public Object valueAt(List<Member> fullCoordinate) {
        List<Member> coordinate = project(fullCoordinate);
        Object exact = values.get(keyOf(coordinate));
        if (exact != null) {
            return exact;
        }
        return rollUp(coordinate, 0);
    }

    /**
     * Sums over the leaves below the coordinate, expanding one position at a time.
     *
     * <p>Returns {@code null} when no leaf combination was written down, which is an empty
     * cell rather than a zero.
     */
    private Object rollUp(List<Member> coordinate, int position) {
        if (position == coordinate.size()) {
            return values.get(keyOf(coordinate));
        }
        Member member = coordinate.get(position);
        List<? extends Member> leaves = leavesOf(member);
        if (leaves.size() == 1 && leaves.get(0).equals(member)) {
            return rollUp(coordinate, position + 1);
        }
        BigDecimal sum = null;
        for (Member leaf : leaves) {
            List<Member> below = new ArrayList<>(coordinate);
            below.set(position, leaf);
            Object part = rollUp(below, position + 1);
            if (part instanceof Number n) {
                sum = sum == null ? toDecimal(n) : sum.add(toDecimal(n));
            }
        }
        return sum == null ? null : normalise(sum);
    }

    private static List<? extends Member> leavesOf(Member member) {
        return member instanceof MemoryMember m ? m.leaves() : List.of(member);
    }

    private static BigDecimal toDecimal(Number n) {
        return n instanceof BigDecimal d ? d : BigDecimal.valueOf(n.doubleValue());
    }

    /**
     * Keeps whole sums whole, so a total reads as 120 and not as 120.0.
     *
     * <p>Written as an if and not as a conditional expression on purpose: {@code long} and
     * {@code double} in the two arms of a conditional are promoted to {@code double}, so
     * the whole-number branch would silently produce a {@code Double}.
     */
    private static Object normalise(BigDecimal sum) {
        BigDecimal stripped = sum.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            return Long.valueOf(stripped.longValueExact());
        }
        return Double.valueOf(stripped.doubleValue());
    }

    private static List<String> keyOf(List<Member> coordinate) {
        List<String> key = new ArrayList<>(coordinate.size());
        for (Member m : coordinate) {
            key.add(m.getUniqueName());
        }
        return key;
    }

    /** The recorded coordinates, for diagnostics. */
    public Map<List<String>, Object> values() {
        return Map.copyOf(values);
    }
}
