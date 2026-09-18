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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.element.MemberBase;
import org.eclipse.daanse.olap.evaluator.CalculableMember;
import org.eclipse.daanse.olap.evaluator.EvaluatorImpl;
import org.eclipse.daanse.olap.evaluator.EvaluatorRoot;

/**
 * A member the provider holds in memory.
 *
 * <p>It knows its name, its place in the hierarchy and its children. Everything else comes
 * from {@link MemberBase}, the same base the relational provider builds on.
 */
public class MemoryMember extends MemberBase implements CalculableMember {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryMember.class);

    private final String name;
    private final MemoryMember parent;
    private final List<MemoryMember> children = new ArrayList<>();
    private final Map<String, Object> properties = new ConcurrentHashMap<>();
    private int ordinal;
    private org.eclipse.daanse.olap.api.query.component.Formula formula;

    MemoryMember(String name, MemoryLevel level, MemoryMember parent, Member.MemberType memberType) {
        super(parent, level, memberType);
        this.name = name;
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
        this.uniqueName = parent == null
                ? level.getHierarchy().getUniqueName() + ".[" + name + "]"
                : parent.getUniqueName() + ".[" + name + "]";
        // The member belongs to its level from the moment it exists. Leaving that to a
        // separate call is what made getMembers(), getCardinality() and every ordinal
        // wrong before.
        if (memberType != Member.MemberType.NULL) {
            level.add(this);
        }
    }

    /** Adds a child on the next level down. */
    public MemoryMember child(String childName) {
        MemoryLevel childLevel = ((MemoryLevel) getLevel()).childLevel();
        if (childLevel == null) {
            throw new IllegalStateException("level " + getLevel().getUniqueName()
                    + " has no level below it, so " + getUniqueName()
                    + " cannot have a child named " + childName);
        }
        return new MemoryMember(childName, childLevel, this, Member.MemberType.REGULAR);
    }

    /** Adds several children in one go. */
    public MemoryMember children(String... names) {
        for (String n : names) {
            child(n);
        }
        return this;
    }

    /** The children, in insertion order. */
    public List<MemoryMember> childMembers() {
        return Collections.unmodifiableList(children);
    }

    /** All members below this one, depth first, this one excluded. */
    public List<MemoryMember> descendants() {
        List<MemoryMember> all = new ArrayList<>();
        for (MemoryMember c : children) {
            all.add(c);
            all.addAll(c.descendants());
        }
        return all;
    }

    /** The leaves below this one, or this one when it has no children. */
    public List<MemoryMember> leaves() {
        if (children.isEmpty()) {
            return List.of(this);
        }
        List<MemoryMember> all = new ArrayList<>();
        for (MemoryMember c : children) {
            all.addAll(c.leaves());
        }
        return all;
    }

    void ordinal(int value) {
        this.ordinal = value;
    }

    /** Sets a member property, such as a caption. */
    public MemoryMember property(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getCaptionValue() {
        Object caption = properties.get("CAPTION");
        return caption == null ? name : caption;
    }

    @Override
    public int getOrdinal() {
        return ordinal;
    }

    @Override
    public Object getPropertyValue(String propertyName) {
        return getPropertyValue(propertyName, true);
    }

    @Override
    public Object getPropertyValue(String propertyName, boolean matchCase) {
        Object own = properties.get(propertyName);
        if (own != null) {
            return own;
        }
        return switch (propertyName) {
            case "NAME" -> getName();
            case "MEMBER_UNIQUE_NAME" -> getUniqueName();
            case "MEMBER_CAPTION", "CAPTION" -> getCaption();
            case "MEMBER_ORDINAL" -> getOrdinal();
            case "LEVEL_NUMBER" -> getDepth();
            case "PARENT_UNIQUE_NAME" -> getParentUniqueName();
            default -> null;
        };
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }


    @Override
    public int getDepth() {
        return getLevel().getDepth();
    }


    @Override
    public org.eclipse.daanse.olap.api.element.Property[] getProperties() {
        return getLevel().getInheritedProperties();
    }

    @Override
    public org.eclipse.daanse.olap.api.element.MetaData getMetaData() {
        return org.eclipse.daanse.olap.element.OlapMetaDataBase.empty();
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        properties.put(propertyName, value);
    }

    @Override
    public boolean isCalculatedInQuery() {
        return false;
    }

    @Override
    public void setName(String newName) {
        throw new UnsupportedOperationException(
                "MemoryMember.setName is not implemented by the provider: members are immutable");
    }

    @Override
    public int compareTo(Object other) {
        return other instanceof org.eclipse.daanse.olap.api.element.Member m
                ? getUniqueName().compareTo(m.getUniqueName())
                : -1;
    }


    // ---- taking part in the calculation order --------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>A calculated member is not itself a context: the context becomes the default
     * member of its hierarchy, and the member is marked as the one being expanded. That
     * is what stops a formula from seeing itself.
     */
    @Override
    public void setContextIn(EvaluatorImpl evaluator) {
        evaluator.setContext(evaluator.root.defaultMembers[getHierarchyOrdinal()]);
        evaluator.setExpanding(this);
    }

    @Override
    public int getHierarchyOrdinal() {
        return getHierarchy().getOrdinalInCube();
    }

    @Override
    public Calc getCompiledExpression(EvaluatorRoot root) {
        if (formula == null) {
            throw new UnsupportedOperationException(
                    getUniqueName() + " carries no formula, so it has no compiled expression");
        }
        return root.getCompiled(formula.getExpression(), true, null);
    }

    @Override
    public org.eclipse.daanse.olap.api.query.component.Expression getExpression() {
        return formula == null ? null : formula.getExpression();
    }

    @Override
    public int getSolveOrder() {
        if (formula == null) {
            return 0;
        }
        Number order = formula.getSolveOrder();
        return order == null ? 0 : order.intValue();
    }

    /** Attaches the formula that makes this a calculated member. */
    void formula(org.eclipse.daanse.olap.api.query.component.Formula value) {
        this.formula = value;
    }

    @Override
    public boolean containsAggregateFunction() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The name identifies a member here, because a catalog written down by hand has no
     * separate key column.
     */
    @Override
    public Object getKey() {
        return getName();
    }

    /**
     * {@inheritDoc}
     *
     * <p>A parent-child hierarchy shows a parent twice: once as itself and once as a leaf
     * holding its own value. This provider builds hierarchies level by level, so there is
     * no such doubling and nothing to create.
     */
    @Override
    public Member createPseudoLeafMember(Member parent, String name) {
        throw new UnsupportedOperationException(
                "MemoryMember.createPseudoLeafMember: this provider has no parent-child"
                        + " hierarchies");
    }

}
