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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.NameResolver;
import org.eclipse.daanse.olap.api.access.Role;
import org.eclipse.daanse.olap.api.agg.Segment;
import org.eclipse.daanse.olap.api.catalog.CatalogReader;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.MatchType;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.OlapElement;
import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.provider.api.AbstractRefusingCatalogReader;

/**
 * Answers navigation questions by walking the element tree the catalog holds.
 *
 * <p>Every method here is pure navigation: children, parents, level members, name lookup.
 * Nothing consults a backend, because the backend is the tree itself. What this reader
 * does not implement it refuses by name rather than answering {@code null}.
 */
public class MemoryCatalogReader extends AbstractRefusingCatalogReader
        implements NameResolver.Namespace {

    private final MemoryCatalog catalog;
    private final Role role;
    private final Context<?> context;

    MemoryCatalogReader(MemoryCatalog catalog, Role role, Context<?> context) {
        this.catalog = catalog;
        this.role = role;
        this.context = context;
    }

    // ---- identity -------------------------------------------------------------

    @Override
    public Catalog getCatalog() {
        return catalog;
    }

    @Override
    public Context<?> getContext() {
        return context;
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public CatalogReader withLocus() {
        return this;
    }

    @Override
    public CatalogReader withoutAccessControl() {
        return this;
    }

    /**
     * This reader is its own namespace: a name is resolved by walking the same tree it
     * navigates, so there is nothing else to consult.
     */
    @Override
    public List<NameResolver.Namespace> getNamespaces() {
        return List.of(this);
    }

    @Override
    public OlapElement lookupCompound(OlapElement parent, List<Segment> names,
            boolean failIfNotFound, org.eclipse.daanse.olap.api.DataType category) {
        return lookupCompound(parent, names, failIfNotFound, category, MatchType.EXACT);
    }

    @Override
    public OlapElement lookupCompound(OlapElement parent, List<Segment> names,
            boolean failIfNotFound, org.eclipse.daanse.olap.api.DataType category,
            MatchType matchType) {
        return new org.eclipse.daanse.olap.common.NameResolverImpl().resolve(parent,
                org.eclipse.daanse.olap.common.Util.toOlap4j(names), failIfNotFound, category,
                matchType, getNamespaces());
    }

    @Override
    public OlapElement lookupChild(OlapElement parent,
            org.eclipse.daanse.olap.api.query.IdentifierSegment segment) {
        return lookupChild(parent, segment, MatchType.EXACT);
    }

    @Override
    public OlapElement lookupChild(OlapElement parent,
            org.eclipse.daanse.olap.api.query.IdentifierSegment segment, MatchType matchType) {
        return parent.lookupChild(this, org.eclipse.daanse.olap.common.Util.convert(segment), matchType);
    }

    // ---- structure ------------------------------------------------------------

    @Override
    public List<Cube> getCubes() {
        return catalog.getCubes();
    }

    @Override
    public List<Dimension> getCubeDimensions(Cube cube) {
        return List.copyOf(cube.getDimensions());
    }

    @Override
    public List<Hierarchy> getDimensionHierarchies(Dimension dimension) {
        return List.copyOf(dimension.getHierarchies());
    }

    @Override
    public List<Level> getHierarchyLevels(Hierarchy hierarchy) {
        return List.copyOf(hierarchy.getLevels());
    }

    // ---- members --------------------------------------------------------------

    @Override
    public List<Member> getHierarchyRootMembers(Hierarchy hierarchy) {
        return hierarchy.getRootMembers();
    }

    @Override
    public Member getHierarchyDefaultMember(Hierarchy hierarchy) {
        return hierarchy.getDefaultMember();
    }

    @Override
    public List<Member> getLevelMembers(Level level, boolean includeCalculated) {
        return level.getMembers();
    }

    @Override
    public List<Member> getLevelMembers(Level level, Evaluator evaluator) {
        return level.getMembers();
    }

    @Override
    public List<Member> getLevelMembers(Level level, boolean includeCalculated, Evaluator evaluator) {
        return level.getMembers();
    }

    @Override
    public int getLevelCardinality(Level level, boolean approximate, boolean materialize) {
        return level.getMembers().size();
    }

    @Override
    public List<Member> getMemberChildren(Member member) {
        return member instanceof MemoryMember m ? List.copyOf(m.childMembers()) : List.of();
    }

    @Override
    public List<Member> getMemberChildren(Member member, Evaluator evaluator) {
        return getMemberChildren(member);
    }

    @Override
    public List<Member> getMemberChildren(List<Member> members) {
        List<Member> all = new ArrayList<>();
        for (Member m : members) {
            all.addAll(getMemberChildren(m));
        }
        return all;
    }

    @Override
    public List<Member> getMemberChildren(List<Member> members, Evaluator evaluator) {
        return getMemberChildren(members);
    }

    @Override
    public Member getMemberParent(Member member) {
        return member.getParentMember();
    }

    @Override
    public int getMemberDepth(Member member) {
        return member.getDepth();
    }

    /**
     * Every member above this one, nearest first.
     *
     * <p>Walked here rather than asked of the member: a member answers that question by
     * asking its catalogue reader, so delegating back to it is an infinite recursion that
     * ends as a stack overflow rather than as an answer.
     */
    @Override
    public void getMemberAncestors(Member member, List<Member> ancestors) {
        for (Member parent = member.getParentMember(); parent != null;
                parent = parent.getParentMember()) {
            ancestors.add(parent);
        }
    }

    @Override
    public Member getLeadMember(Member member, int n) {
        if (n == 0) {
            return member;
        }
        List<Member> siblings = member.getLevel().getMembers();
        int target = siblings.indexOf(member) + n;
        return target < 0 || target >= siblings.size()
                ? member.getHierarchy().getNullMember()
                : siblings.get(target);
    }

    @Override
    public int compareMembersHierarchically(Member m1, Member m2) {
        return m1.getUniqueName().compareTo(m2.getUniqueName());
    }

    @Override
    public Member substitute(Member member) {
        return member;
    }

    @Override
    public boolean isVisible(Member member) {
        return member != null && member.isVisible();
    }

    @Override
    public boolean isDrillable(Member member) {
        return !getMemberChildren(member).isEmpty();
    }

    @Override
    public List<Member> getCalculatedMembers() {
        return List.of();
    }

    @Override
    public List<Member> getCalculatedMembers(Level level) {
        return List.of();
    }

    @Override
    public List<Member> getCalculatedMembers(Hierarchy hierarchy) {
        return List.of();
    }

    // ---- name lookup ----------------------------------------------------------

    @Override
    public Member lookupMemberChildByName(Member parent, Segment segment, MatchType matchType) {
        for (Member child : getMemberChildren(parent)) {
            if (segment.matches(child.getName())) {
                return child;
            }
        }
        return null;
    }

    @Override
    public OlapElement getElementChild(OlapElement parent, Segment segment) {
        return getElementChild(parent, segment, MatchType.EXACT);
    }

    @Override
    public OlapElement getElementChild(OlapElement parent, Segment segment, MatchType matchType) {
        return parent.lookupChild(this, segment, matchType);
    }

    /**
     * {@code null}: there is no native path here. A provider that cannot push a set
     * expression down to a backend says so by declining, and the engine then evaluates the
     * expression itself, which is the whole point of this provider.
     */
    @Override
    public org.eclipse.daanse.olap.api.evaluator.NativeEvaluator getNativeSetEvaluator(
            org.eclipse.daanse.olap.api.function.FunctionDefinition function,
            org.eclipse.daanse.olap.api.query.component.Expression[] args,
            Evaluator evaluator, org.eclipse.daanse.olap.api.calc.Calc calc) {
        return null;
    }

    /**
     * {@code null}: this provider defines no catalog parameters. A query may still declare
     * its own; those never reach here.
     */
    @Override
    public org.eclipse.daanse.olap.api.Parameter getParameter(String name) {
        return null;
    }

    /**
     * The members of a level between two bounds, inclusive, in declaration order.
     *
     * <p>An empty range when the bounds are the wrong way round, which is what a range
     * expression like {@code [a]:[b]} expects rather than an error.
     */
    @Override
    public void getMemberRange(Level level, Member startMember, Member endMember,
            List<Member> list) {
        List<Member> members = level.getMembers();
        int from = members.indexOf(startMember);
        int to = members.indexOf(endMember);
        if (from < 0 || to < 0 || from > to) {
            return;
        }
        list.addAll(members.subList(from, to + 1));
    }

}
