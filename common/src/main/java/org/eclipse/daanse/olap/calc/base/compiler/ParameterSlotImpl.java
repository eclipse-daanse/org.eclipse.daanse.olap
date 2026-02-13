/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * 2002-2017 Hitachi Vantara.
 * 2006      jhyde
 * 
 * Contributors after Fork in 2023:
 *   Sergei Semenkov (2001)
 *   SmartCity Jena - initial
 */
package org.eclipse.daanse.olap.calc.base.compiler;

import java.util.List;

import org.eclipse.daanse.olap.api.Parameter;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.compiler.ParameterSlot;
import org.eclipse.daanse.olap.api.calc.tuple.TupleList;
import org.eclipse.daanse.olap.api.query.component.Literal;
import org.eclipse.daanse.olap.api.query.component.MemberExpression;

/**
 * Implementation of {@link ParameterSlot}.
 *
 * <p>Stores a parameter's value during query execution, including the default value
 * calculation, current value, and assignment state.</p>
 */
public class ParameterSlotImpl implements ParameterSlot {
    private final Parameter parameter;
    private final int index;
    private Calc<?> defaultValueCalc;
    Object value;
    private boolean assigned;
    private Object cachedDefaultValue;

    /**
     * Creates a ParameterSlotImpl.
     *
     * @param parameter Parameter
     * @param index     Unique index of the slot
     */
    public ParameterSlotImpl(Parameter parameter, int index) {
        this.parameter = parameter;
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public Calc<?> getDefaultValueCalc() {
        return defaultValueCalc;
    }

    @Override
    public Parameter getParameter() {
        return parameter;
    }

    /**
     * Sets a compiled expression to compute the default value of the parameter.
     *
     * @param calc Compiled expression to compute default value of parameter
     *
     * @see #getDefaultValueCalc()
     */
    void setDefaultValueCalc(Calc<?> calc) {
        defaultValueCalc = calc;
    }

    @Override
    public void setParameterValue(Object value, boolean assigned) {
        this.value = value;
        this.assigned = assigned;

        // Validate that List values are TupleList instances
        if (value instanceof List && !(value instanceof TupleList)) {
            throw new IllegalArgumentException("List values must be TupleList instances");
        }
        if (value instanceof Literal || value instanceof MemberExpression) {
            throw new IllegalArgumentException("value should not be Literal or MemberExpr");
        }
    }

    @Override
    public Object getParameterValue() {
        return value;
    }

    @Override
    public boolean isParameterSet() {
        return assigned;
    }

    @Override
    public void unsetParameterValue() {
        value = null;
        assigned = false;
    }

    @Override
    public void setCachedDefaultValue(Object value) {
        cachedDefaultValue = value;
    }

    @Override
    public Object getCachedDefaultValue() {
        return cachedDefaultValue;
    }
}
