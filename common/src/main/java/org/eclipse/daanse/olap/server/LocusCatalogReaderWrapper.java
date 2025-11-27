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
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.olap.server;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.eclipse.daanse.olap.api.CatalogReader;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.Execution;
import org.eclipse.daanse.olap.api.MatchType;
import org.eclipse.daanse.olap.api.NameResolver;
import org.eclipse.daanse.olap.api.NameSegment;
import org.eclipse.daanse.olap.api.NativeEvaluator;
import org.eclipse.daanse.olap.api.Parameter;
import org.eclipse.daanse.olap.api.Segment;
import org.eclipse.daanse.olap.api.access.AccessMember;
import org.eclipse.daanse.olap.api.access.Role;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.DatabaseSchema;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.NamedSet;
import org.eclipse.daanse.olap.api.element.OlapElement;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.query.component.Expression;

/**
 * Decorator for {@link CatalogReader} that wraps each method call with Locus context.
 *
 * <p>This class replaces the InvocationHandler-based Proxy approach to provide
 * better type safety, debuggability, and performance. Each method call is
 * automatically wrapped with {@link LocusImpl#execute} to ensure proper
 * Locus tracking for profiling and monitoring.
 *
 * <p>Usage:
 * <pre>
 * CatalogReader reader = new LocusCatalogReaderWrapper(
 *     execution, "Schema reader", delegateReader);
 * </pre>
 *
 * @see org.eclipse.daanse.olap.common.Util#locusCatalogReader
 */
public class LocusCatalogReaderWrapper implements CatalogReader {

    private final Execution execution;
    private final String component;
    private final CatalogReader delegate;

    /**
     * Creates a wrapper that adds Locus context to all CatalogReader operations.
     *
     * @param execution Execution context for Locus
     * @param component Component name for profiling (e.g., "Schema reader")
     * @param delegate The actual CatalogReader to delegate to
     * @throws NullPointerException if any parameter is null
     */
    public LocusCatalogReaderWrapper(
            Execution execution,
            String component,
            CatalogReader delegate) {
        this.execution = Objects.requireNonNull(execution, "execution");
        this.component = Objects.requireNonNull(component, "component");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public Catalog getCatalog() {
        return LocusImpl.execute(execution, component, () -> delegate.getCatalog());
    }

    @Override
    public Role getRole() {
        return LocusImpl.execute(execution, component, () -> delegate.getRole());
    }

    @Override
    public List<Dimension> getCubeDimensions(Cube cube) {
        return LocusImpl.execute(execution, component, () -> delegate.getCubeDimensions(cube));
    }

    @Override
    public List<Hierarchy> getDimensionHierarchies(Dimension dimension) {
        return LocusImpl.execute(execution, component, () -> delegate.getDimensionHierarchies(dimension));
    }

    @Override
    public List<Member> getHierarchyRootMembers(Hierarchy hierarchy) {
        return LocusImpl.execute(execution, component, () -> delegate.getHierarchyRootMembers(hierarchy));
    }

    @Override
    public int getChildrenCountFromCache(Member member) {
        return LocusImpl.execute(execution, component, () -> delegate.getChildrenCountFromCache(member));
    }

    @Override
    public int getLevelCardinality(Level level, boolean approximate, boolean materialize) {
        return LocusImpl.execute(execution, component, () -> delegate.getLevelCardinality(level, approximate, materialize));
    }

    @Override
    public Member substitute(Member member) {
        return LocusImpl.execute(execution, component, () -> delegate.substitute(member));
    }

    @Override
    public List<Member> getMemberChildren(Member member) {
        return LocusImpl.execute(execution, component, () -> delegate.getMemberChildren(member));
    }

    @Override
    public List<Member> getMemberChildren(Member member, Evaluator context) {
        return LocusImpl.execute(execution, component, () -> delegate.getMemberChildren(member, context));
    }

    @Override
    public List<Member> getMemberChildren(List<Member> members) {
        return LocusImpl.execute(execution, component, () -> delegate.getMemberChildren(members));
    }

    @Override
    public List<Member> getMemberChildren(List<Member> members, Evaluator context) {
        return LocusImpl.execute(execution, component, () -> delegate.getMemberChildren(members, context));
    }

    @Override
    public void getParentChildContributingChildren(Member dataMember, Hierarchy hierarchy, List<Member> list) {
        LocusImpl.execute(execution, component, () -> {
            delegate.getParentChildContributingChildren(dataMember, hierarchy, list);
            return null;
        });
    }

    @Override
    public Member getMemberParent(Member member) {
        return LocusImpl.execute(execution, component, () -> delegate.getMemberParent(member));
    }

    @Override
    public void getMemberAncestors(Member member, List<Member> ancestorList) {
        LocusImpl.execute(execution, component, () -> {
            delegate.getMemberAncestors(member, ancestorList);
            return null;
        });
    }

    @Override
    public int getMemberDepth(Member member) {
        return LocusImpl.execute(execution, component, () -> delegate.getMemberDepth(member));
    }

    @Override
    public Member getMemberByUniqueName(List<Segment> uniqueNameParts, boolean failIfNotFound, MatchType matchType) {
        return LocusImpl.execute(execution, component, () -> delegate.getMemberByUniqueName(uniqueNameParts, failIfNotFound, matchType));
    }

    @Override
    public Member getMemberByUniqueName(List<Segment> uniqueNameParts, boolean failIfNotFound) {
        return LocusImpl.execute(execution, component, () -> delegate.getMemberByUniqueName(uniqueNameParts, failIfNotFound));
    }

    @Override
    public OlapElement lookupCompound(OlapElement parent, List<Segment> names, boolean failIfNotFound, DataType category, MatchType matchType) {
        return LocusImpl.execute(execution, component, () -> delegate.lookupCompound(parent, names, failIfNotFound, category, matchType));
    }

    @Override
    public OlapElement lookupCompound(OlapElement parent, List<Segment> names, boolean failIfNotFound, DataType category) {
        return LocusImpl.execute(execution, component, () -> delegate.lookupCompound(parent, names, failIfNotFound, category));
    }

    @Override
    public Member getCalculatedMember(List<Segment> nameParts) {
        return LocusImpl.execute(execution, component, () -> delegate.getCalculatedMember(nameParts));
    }

    @Override
    public NamedSet getNamedSet(List<Segment> nameParts) {
        return LocusImpl.execute(execution, component, () -> delegate.getNamedSet(nameParts));
    }

    @Override
    public void getMemberRange(Level level, Member startMember, Member endMember, List<Member> list) {
        LocusImpl.execute(execution, component, () -> {
            delegate.getMemberRange(level, startMember, endMember, list);
            return null;
        });
    }

    @Override
    public Member getLeadMember(Member member, int n) {
        return LocusImpl.execute(execution, component, () -> delegate.getLeadMember(member, n));
    }

    @Override
    public int compareMembersHierarchically(Member m1, Member m2) {
        return LocusImpl.execute(execution, component, () -> delegate.compareMembersHierarchically(m1, m2));
    }

    @Override
    public OlapElement getElementChild(OlapElement parent, Segment name, MatchType matchType) {
        return LocusImpl.execute(execution, component, () -> delegate.getElementChild(parent, name, matchType));
    }

    @Override
    public OlapElement getElementChild(OlapElement parent, Segment name) {
        return LocusImpl.execute(execution, component, () -> delegate.getElementChild(parent, name));
    }

    @Override
    public List<Member> getLevelMembers(Level level, boolean includeCalculated) {
        return LocusImpl.execute(execution, component, () -> delegate.getLevelMembers(level, includeCalculated));
    }

    @Override
    public List<Member> getLevelMembers(Level level, boolean includeCalculated, Evaluator context) {
        return LocusImpl.execute(execution, component, () -> delegate.getLevelMembers(level, includeCalculated, context));
    }

    @Override
    public List<Member> getLevelMembers(Level level, Evaluator context) {
        return LocusImpl.execute(execution, component, () -> delegate.getLevelMembers(level, context));
    }

    @Override
    public List<Level> getHierarchyLevels(Hierarchy hierarchy) {
        return LocusImpl.execute(execution, component, () -> delegate.getHierarchyLevels(hierarchy));
    }

    @Override
    public Member getHierarchyDefaultMember(Hierarchy hierarchy) {
        return LocusImpl.execute(execution, component, () -> delegate.getHierarchyDefaultMember(hierarchy));
    }

    @Override
    public boolean isDrillable(Member member) {
        return LocusImpl.execute(execution, component, () -> delegate.isDrillable(member));
    }

    @Override
    public boolean isVisible(Member member) {
        return LocusImpl.execute(execution, component, () -> delegate.isVisible(member));
    }

    @Override
    public List<Cube> getCubes() {
        return LocusImpl.execute(execution, component, () -> delegate.getCubes());
    }

    @Override
    public List<Member> getCalculatedMembers(Hierarchy hierarchy) {
        return LocusImpl.execute(execution, component, () -> delegate.getCalculatedMembers(hierarchy));
    }

    @Override
    public List<Member> getCalculatedMembers(Level level) {
        return LocusImpl.execute(execution, component, () -> delegate.getCalculatedMembers(level));
    }

    @Override
    public List<Member> getCalculatedMembers() {
        return LocusImpl.execute(execution, component, () -> delegate.getCalculatedMembers());
    }

    @Override
    public Member lookupMemberChildByName(Member parent, Segment childName, MatchType matchType) {
        return LocusImpl.execute(execution, component, () -> delegate.lookupMemberChildByName(parent, childName, matchType));
    }

    @Override
    public List<Member> lookupMemberChildrenByNames(Member parent, List<NameSegment> childNames, MatchType matchType) {
        return LocusImpl.execute(execution, component, () -> delegate.lookupMemberChildrenByNames(parent, childNames, matchType));
    }

    @Override
    public NativeEvaluator getNativeSetEvaluator(FunctionDefinition fun, Expression[] args, Evaluator evaluator, Calc calc) {
        return LocusImpl.execute(execution, component, () -> delegate.getNativeSetEvaluator(fun, args, evaluator, calc));
    }

    @Override
    public Parameter getParameter(String name) {
        return LocusImpl.execute(execution, component, () -> delegate.getParameter(name));
    }

    @Override
    @Deprecated
    public DataSource getDataSource() {
        return LocusImpl.execute(execution, component, () -> delegate.getDataSource());
    }

    @Override
    public CatalogReader withoutAccessControl() {
        return LocusImpl.execute(execution, component, () -> delegate.withoutAccessControl());
    }

    @Override
    public CatalogReader withLocus() {
        return LocusImpl.execute(execution, component, () -> delegate.withLocus());
    }

    @Override
    public List<NameResolver.Namespace> getNamespaces() {
        return LocusImpl.execute(execution, component, () -> delegate.getNamespaces());
    }

    @Override
    public Map<? extends Member, AccessMember> getMemberChildrenWithDetails(Member member, Evaluator evaluator) {
        return LocusImpl.execute(execution, component, () -> delegate.getMemberChildrenWithDetails(member, evaluator));
    }

    @Override
    public Context<?> getContext() {
        return LocusImpl.execute(execution, component, () -> delegate.getContext());
    }

    @Override
    public List<? extends DatabaseSchema> getDatabaseSchemas() {
        return LocusImpl.execute(execution, component, () -> delegate.getDatabaseSchemas());
    }
}
