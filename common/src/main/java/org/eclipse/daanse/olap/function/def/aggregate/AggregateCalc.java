/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.function.def.aggregate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.daanse.olap.api.CatalogReader;
import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.access.RollupPolicy;
import org.eclipse.daanse.olap.api.aggregator.Aggregator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.todo.TupleCursor;
import org.eclipse.daanse.olap.api.calc.todo.TupleIterator;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.calc.todo.TupleListCalc;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedUnknownCalc;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.UnaryTupleList;
import org.eclipse.daanse.olap.calc.base.util.HirarchyDependsChecker;
import org.eclipse.daanse.olap.common.StandardProperty;
import org.eclipse.daanse.olap.common.SystemWideProperties;
import org.eclipse.daanse.olap.common.Util;
import org.eclipse.daanse.olap.fun.FunUtil;
import org.eclipse.daanse.olap.function.def.crossjoin.CrossJoinFunDef;

public class AggregateCalc  extends AbstractProfilingNestedUnknownCalc {
    private static final String TIMING_NAME =
            AggregateFunDef.class.getSimpleName();
    private final TupleListCalc tupleListCalc;
    private final Calc<?> calc;
    private final Member member;

    public AggregateCalc(
        Type type, TupleListCalc tupleListCalc, Calc<?> calc, Member member)
    {
        super(type, new Calc[]{tupleListCalc, calc});
        this.tupleListCalc = tupleListCalc;
        this.calc = calc;
        this.member = member;
    }

    public AggregateCalc(Type type, TupleListCalc tupleListCalc, Calc<?> calc) {
        this(type, tupleListCalc, calc, null);
    }

    @Override
    public Object evaluate(Evaluator evaluator) {
        evaluator.getTiming().markStart(TIMING_NAME);
        final int savepoint = evaluator.savepoint();
        try {
            TupleList list = AbstractAggregateFunDef.evaluateCurrentList(tupleListCalc, evaluator);
            if (member != null) {
                evaluator.setContext(member);
            }
            return aggregate(calc, evaluator, list);
        } finally {
            evaluator.restore(savepoint);
            evaluator.getTiming().markEnd(TIMING_NAME);
        }
    }

    /**
     * Computes an expression for each element of a list, and aggregates
     * the result according to the evaluation context's current aggregation
     * strategy.
     *
     * @param calc Compiled expression to evaluate a scalar
     * @param evaluator Evaluation context
     * @param tupleList List of members or tuples
     * @return Aggregated result
     */
    public static Object aggregate(
        Calc<?> calc,
        Evaluator evaluator,
        TupleList tupleList)
    {
        Aggregator aggregator =
            (Aggregator) evaluator.getProperty(
            		StandardProperty.AGGREGATION_TYPE.getName(), null);
        if (aggregator == null) {
            throw FunUtil.newEvalException(
                null,
                "Could not find an aggregator in the current evaluation context");
        }
        Aggregator rollup = aggregator.getRollup();
        if (rollup == null) {
            throw FunUtil.newEvalException(
                null,
                new StringBuilder("Don't know how to rollup aggregator '")
                    .append(aggregator)
                    .append("'")
                    .toString());
        }
        if (!"distinct-count".equals(aggregator.getName())
            && !"avg".equals(aggregator.getName()))
        {
            final int savepoint = evaluator.savepoint();
            try {
                evaluator.setNonEmpty(false);
                return rollup.aggregate(
                    evaluator, tupleList, calc);
            } finally {
                evaluator.restore(savepoint);
            }
        }

        // All that follows is logic for distinct count. It's not like the
        // other aggregators.
        if (tupleList.isEmpty()) {
            return FunUtil.DOUBLE_NULL;
        }

        // Optimize the list
        // E.g.
        // List consists of:
        //  (Gender.[All Gender], [Product].[All Products]),
        //  (Gender.[F], [Product].[Drink]),
        //  (Gender.[M], [Product].[Food])
        // Can be optimized to:
        //  (Gender.[All Gender], [Product].[All Products])
        //
        // Similar optimization can also be done for list of members.

        boolean unlimitedIn  = false;

        //TODO: reactivate
        //TODO: Functions should know their context , (dialext and configProps)
//        if (evaluator instanceof RolapEvaluator) {
//            unlimitedIn =
//                ((RolapEvaluator) evaluator).getDialect()
//                    .supportsUnlimitedValueList();
//        }
        boolean tupleSizeWithinInListSize =
            tupleList.size()
                <= SystemWideProperties.instance().MaxConstraints;
        if (unlimitedIn || tupleSizeWithinInListSize) {
            // If the DBMS does not have an upper limit on IN list
            // predicate size, then don't attempt any list
            // optimization, since the current algorithm is
            // very slow.  May want to revisit this if someone
            // improves the algorithm.
        } else {
            tupleList = optimizeTupleList(evaluator, tupleList, true);
        }

        // Can't aggregate distinct-count values in the same way
        // which is used for other types of aggregations. To evaluate a
        // distinct-count across multiple members, we need to gather
        // the members together, then evaluate the collection of
        // members all at once. To do this, we postpone evaluation,
        // and create a lambda function containing the members.
        Evaluator evaluator2 =
            evaluator.pushAggregation(tupleList);
        // cancel nonEmpty context
        evaluator2.setNonEmpty(false);
        return evaluator2.evaluateCurrent();
    }

    /**
     * Analyzes a list of tuples and determines if the list can
     * be safely optimized. If a member of the tuple list is on
     * a hierarchy for which a rollup policy of PARTIAL is set,
     * it is not safe to optimize that list.
     */
    private static boolean canOptimize(
        Evaluator evaluator,
        TupleList tupleList)
    {
        // If members of this hierarchy are controlled by a role which
        // enforces a rollup policy of partial, we cannot safely
        // optimize the tuples list as it might end up rolling up to
        // the parent while not all children are actually accessible.
        if (!tupleList.isEmpty()) {
            for (Member member : tupleList.get(0)) {
                final RollupPolicy policy =
                    evaluator.getCatalogReader().getRole()
                        .getAccessDetails(member.getHierarchy())
                        .getRollupPolicy();
                if (policy == RollupPolicy.PARTIAL) {
                    return false;
                }
            }
        }
        return true;
    }

    public static TupleList optimizeTupleList(
        Evaluator evaluator, TupleList tupleList, boolean checkSize)
    {
        if (!canOptimize(evaluator, tupleList)) {
            return tupleList;
        }

        // FIXME: We remove overlapping tuple entries only to pass
        // AggregationOnDistinctCountMeasuresTest
        // .testOptimizeListWithTuplesOfLength3 on Access. Without
        // the optimization, we generate a statement 7000
        // characters long and Access gives "Query is too complex".
        // The optimization is expensive, so we only want to do it
        // if the DBMS can't execute the query otherwise.
        if (false) {
            tupleList = removeOverlappingTupleEntries(tupleList);
        }
        tupleList =
            optimizeChildren(
                tupleList,
                evaluator.getCatalogReader(),
                evaluator.getMeasureCube());
        if (checkSize) {
            checkIfAggregationSizeIsTooLarge(tupleList, SystemWideProperties.instance().MaxConstraints);
        }
        return tupleList;
    }

    /**
     * In case of distinct count aggregation if a tuple which is a super
     * set of other tuples in the set exists then the child tuples can be
     * ignored.
     *
     *   For example. A list consisting of:
     *  (Gender.[All Gender], [Product].[All Products]),
     *  (Gender.[F], [Product].[Drink]),
     *  (Gender.[M], [Product].[Food])
     * Can be optimized to:
     *  (Gender.[All Gender], [Product].[All Products])
     *
     * @param list List of tuples
     */
    private static TupleList removeOverlappingTupleEntries(
        TupleList list)
    {
        TupleList trimmedList = list.copyList(list.size());
        Member[] tuple1 = new Member[list.getArity()];
        Member[] tuple2 = new Member[list.getArity()];
        final TupleCursor cursor1 = list.tupleCursor();
        while (cursor1.forward()) {
            cursor1.currentToArray(tuple1, 0);
            if (trimmedList.isEmpty()) {
                trimmedList.addTuple(tuple1);
            } else {
                boolean ignore = false;
                final TupleIterator iterator = trimmedList.tupleIterator();
                while (iterator.forward()) {
                    iterator.currentToArray(tuple2, 0);
                    if (isSuperSet(tuple1, tuple2)) {
                        iterator.remove();
                    } else if (isSuperSet(tuple2, tuple1)
                        || isEqual(tuple1, tuple2))
                    {
                        ignore = true;
                        break;
                    }
                }
                if (!ignore) {
                    trimmedList.addTuple(tuple1);
                }
            }
        }
        return trimmedList;
    }

    /**
     * Returns whether tuple1 is a superset of tuple2.
     *
     * @param tuple1 First tuple
     * @param tuple2 Second tuple
     * @return boolean Whether tuple1 is a superset of tuple2
     */
    public static boolean isSuperSet(Member[] tuple1, Member[] tuple2) {
        int parentLevelCount = 0;
        for (int i = 0; i < tuple1.length; i++) {
            Member member1 = tuple1[i];
            Member member2 = tuple2[i];

            if (!member2.isChildOrEqualTo(member1)) {
                return false;
            }
            if (member1.getLevel().getDepth()
                < member2.getLevel().getDepth())
            {
                parentLevelCount++;
            }
        }
        return parentLevelCount > 0;
    }

    private static void checkIfAggregationSizeIsTooLarge(List list, int maxConstraints) {
        if (list.size() > maxConstraints) {
            throw FunUtil.newEvalException(
                null,
                new StringBuilder("Aggregation is not supported over a list").
                append(" with more than ").append(maxConstraints).append(" predicates").
                append(" (see property ").append("MaxConstraints").append(")").toString());
        }
    }

    @Override
    public boolean dependsOn(Hierarchy hierarchy) {
        if (hierarchy.getDimension().isMeasures()) {
            return true;
        }
        return HirarchyDependsChecker.checkAnyDependsButFirst(getChildCalcs(), hierarchy);
    }

    /**
     * In distinct Count aggregation, if tuple list is a result
     * m.children * n.children then it can be optimized to m * n
     *
     *
     * E.g.
     * List consist of:
     *  (Gender.[F], [Store].[USA]),
     *  (Gender.[F], [Store].[USA].[OR]),
     *  (Gender.[F], [Store].[USA].[CA]),
     *  (Gender.[F], [Store].[USA].[WA]),
     *  (Gender.[F], [Store].[CANADA])
     *  (Gender.[M], [Store].[USA]),
     *  (Gender.[M], [Store].[USA].[OR]),
     *  (Gender.[M], [Store].[USA].[CA]),
     *  (Gender.[M], [Store].[USA].[WA]),
     *  (Gender.[M], [Store].[CANADA])
     * Can be optimized to:
     *  (Gender.[All Gender], [Store].[USA])
     *  (Gender.[All Gender], [Store].[CANADA])
     *
     *
     * @param tuples Tuples
     * @param reader Schema reader
     * @param baseCubeForMeasure Cube
     * @return xxxx
     */
    public static TupleList optimizeChildren(
        TupleList tuples,
        CatalogReader reader,
        Cube baseCubeForMeasure)
    {
        Map<Member, Integer>[] membersOccurencesInTuple =
            membersVersusOccurencesInTuple(tuples);
        int tupleLength = tuples.getArity();

        //noinspection unchecked
        Set<Member>[] sets = new Set[tupleLength];
        boolean optimized = false;
        for (int i = 0; i < tupleLength; i++) {
            if (Util.areOccurencesEqual(membersOccurencesInTuple[i].values())) {
                Set<Member> members = membersOccurencesInTuple[i].keySet();
                int originalSize = members.size();
                sets[i] =
                    optimizeMemberSet(
                        new LinkedHashSet<>(members),
                        reader,
                        baseCubeForMeasure);
                if (sets[i].size() != originalSize) {
                    optimized = true;
                }
            } else {
                sets[i] =
                    new LinkedHashSet<>(
                        membersOccurencesInTuple[i].keySet());
            }
        }
        if (optimized) {
            return crossProd(sets);
        }
        return tuples;
    }

    /**
     * Finds member occurrences in tuple and generates a map of Members
     * versus their occurrences in tuples.
     *
     * @param tupleList List of tuples
     * @return Map of the number of occurrences of each member in a tuple
     */
    public static Map<Member, Integer>[] membersVersusOccurencesInTuple(
        TupleList tupleList)
    {
        int tupleLength = tupleList.getArity();
        //noinspection unchecked
        Map<Member, Integer>[] counters = new Map[tupleLength];
        for (int i = 0; i < counters.length; i++) {
            counters[i] = new LinkedHashMap<>();
        }
        for (List<Member> tuple : tupleList) {
            for (int i = 0; i < tuple.size(); i++) {
                Member member = tuple.get(i);
                Map<Member, Integer> map = counters[i];
                if (map.containsKey(member)) {
                    Integer count = map.get(member);
                    map.put(member, ++count);
                } else {
                    map.put(member, 1);
                }
            }
        }
        return counters;
    }

    private static Set<Member> optimizeMemberSet(
        Set<Member> members,
        CatalogReader reader,
        Cube baseCubeForMeasure)
    {
        boolean didOptimize;
        Set<Member> membersToBeOptimized = new LinkedHashSet<>();
        Set<Member> optimizedMembers = new LinkedHashSet<>();
        while (!members.isEmpty()) {
            Iterator<Member> iterator = members.iterator();
            Member first = iterator.next();
            if (first.isAll()) {
                optimizedMembers.clear();
                optimizedMembers.add(first);
                return optimizedMembers;
            }
            membersToBeOptimized.add(first);
            iterator.remove();

            Member firstParentMember = first.getParentMember();
            while (iterator.hasNext()) {
                Member current =  iterator.next();
                if (current.isAll()) {
                    optimizedMembers.clear();
                    optimizedMembers.add(current);
                    return optimizedMembers;
                }

                Member currentParentMember = current.getParentMember();
                if (firstParentMember == null
                    && currentParentMember == null
                    || (firstParentMember != null
                    && firstParentMember.equals(currentParentMember)))
                {
                    membersToBeOptimized.add(current);
                    iterator.remove();
                }
            }

            int childCountOfParent = -1;
            if (firstParentMember != null) {
                childCountOfParent =
                    getChildCount(firstParentMember, reader);
            }
            if (childCountOfParent != -1
                && membersToBeOptimized.size() == childCountOfParent
                && firstParentMember != null && canOptimize(firstParentMember, baseCubeForMeasure))
            {
                optimizedMembers.add(firstParentMember);
                didOptimize = true;
            } else {
                optimizedMembers.addAll(membersToBeOptimized);
                didOptimize = false;
            }
            membersToBeOptimized.clear();

            if (members.isEmpty() && didOptimize) {
                Set<Member> temp = members;
                members = optimizedMembers;
                optimizedMembers = temp;
            }
        }
        return optimizedMembers;
    }

    /**
     * Returns whether tuples are equal. They must have the same length.
     *
     * @param tuple1 First tuple
     * @param tuple2 Second tuple
     * @return whether tuples are equal
     */
    private static boolean isEqual(Member[] tuple1, Member[] tuple2) {
        for (int i = 0; i < tuple1.length; i++) {
            if (!tuple1[i].getUniqueName().equals(
                    tuple2[i].getUniqueName()))
            {
                return false;
            }
        }
        return true;
    }

    private static boolean canOptimize(
        Member parentMember,
        Cube baseCube)
    {
        return dimensionJoinsToBaseCube(
            parentMember.getDimension(), baseCube)
            || !parentMember.isAll();
    }

    private static TupleList crossProd(Set<Member>[] sets) {
        final List<TupleList> tupleLists = new ArrayList<>();
        for (Set<Member> set : sets) {
            tupleLists.add(
                new UnaryTupleList(
                    new ArrayList<>(set)));
        }
        if (tupleLists.size() == 1) {
            return tupleLists.get(0);
        }
        return CrossJoinFunDef.mutableCrossJoin(tupleLists);
    }

    private static boolean dimensionJoinsToBaseCube(
        Dimension dimension,
        Cube baseCube)
    {
        HashSet<Dimension> dimensions = new HashSet<>();
        dimensions.add(dimension);
        return baseCube.nonJoiningDimensions(dimensions).isEmpty();
    }

    private static int getChildCount(
        Member parentMember,
        CatalogReader reader)
    {
        int childrenCountFromCache =
            reader.getChildrenCountFromCache(parentMember);
        if (childrenCountFromCache != -1) {
            return childrenCountFromCache;
        }
        return reader.getMemberChildren(parentMember).size();
    }
}
