/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 *
 * ---- All changes after Fork in 2023 ------------------------
 *
 * Project: Eclipse daanse
 *
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 */
package org.eclipse.daanse.olap.calc.base.compiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.mdx.model.api.expression.operation.CastOperationAtom;
import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.Parameter;
import org.eclipse.daanse.olap.api.Validator;
import org.eclipse.daanse.olap.api.calc.BooleanCalc;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.ConstantCalc;
import org.eclipse.daanse.olap.api.calc.DateTimeCalc;
import org.eclipse.daanse.olap.api.calc.DimensionCalc;
import org.eclipse.daanse.olap.api.calc.DoubleCalc;
import org.eclipse.daanse.olap.api.calc.HierarchyCalc;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.LevelCalc;
import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.calc.ResultStyle;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.calc.TupleCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.calc.compiler.ParameterSlot;
import org.eclipse.daanse.olap.api.calc.todo.TupleIteratorCalc;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.calc.todo.TupleListCalc;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.exception.OlapRuntimeException;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.Literal;
import org.eclipse.daanse.olap.api.query.component.MemberExpression;
import org.eclipse.daanse.olap.api.type.BooleanType;
import org.eclipse.daanse.olap.api.type.DecimalType;
import org.eclipse.daanse.olap.api.type.DimensionType;
import org.eclipse.daanse.olap.api.type.HierarchyType;
import org.eclipse.daanse.olap.api.type.LevelType;
import org.eclipse.daanse.olap.api.type.MemberType;
import org.eclipse.daanse.olap.api.type.NullType;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.eclipse.daanse.olap.api.type.ScalarType;
import org.eclipse.daanse.olap.api.type.SetType;
import org.eclipse.daanse.olap.api.type.StringType;
import org.eclipse.daanse.olap.api.type.TupleType;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.constant.ConstantBooleanCalc;
import org.eclipse.daanse.olap.calc.base.constant.ConstantDoubleCalc;
import org.eclipse.daanse.olap.calc.base.constant.ConstantHierarchyCalc;
import org.eclipse.daanse.olap.calc.base.constant.ConstantIntegerCalc;
import org.eclipse.daanse.olap.calc.base.constant.ConstantStringCalc;
import org.eclipse.daanse.olap.calc.base.type.booleanx.DoubleToBooleanCalc;
import org.eclipse.daanse.olap.calc.base.type.booleanx.IntgegerToBooleanCalc;
import org.eclipse.daanse.olap.calc.base.type.booleanx.UnknownToBooleanCalc;
import org.eclipse.daanse.olap.calc.base.type.datetime.UnknownToDateTimeCalc;
import org.eclipse.daanse.olap.calc.base.type.dimension.DimensionOfHierarchyCalc;
import org.eclipse.daanse.olap.calc.base.type.dimension.UnknownToDimensionCalc;
import org.eclipse.daanse.olap.calc.base.type.doublex.IntegerToDoubleCalc;
import org.eclipse.daanse.olap.calc.base.type.doublex.UnknownToDoubleCalc;
import org.eclipse.daanse.olap.calc.base.type.hierarchy.DimensionDefaultHierarchyCalc;
import org.eclipse.daanse.olap.calc.base.type.integer.DoubleToIntegerCalc;
import org.eclipse.daanse.olap.calc.base.type.integer.UnknownToIntegerCalc;
import org.eclipse.daanse.olap.calc.base.type.level.UnknownToLevelCalc;
import org.eclipse.daanse.olap.calc.base.type.member.UnknownToMemberCalc;
import org.eclipse.daanse.olap.calc.base.type.string.UnknownToStringCalc;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.IterableListCalc;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.MemberValueCalc;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.TupleValueCalc;
import org.eclipse.daanse.olap.calc.base.util.DimensionUtil;
import org.eclipse.daanse.olap.common.Util;
import org.eclipse.daanse.olap.fun.FunUtil;
import org.eclipse.daanse.olap.function.def.hierarchy.level.LevelHirarchyCalc;
import org.eclipse.daanse.olap.function.def.hierarchy.member.HierarchyCurrentMemberCalc;
import org.eclipse.daanse.olap.function.def.hierarchy.member.HierarchyCurrentMemberFixedCalc;
import org.eclipse.daanse.olap.function.def.hierarchy.member.MemberHierarchyCalc;
import org.eclipse.daanse.olap.function.def.level.member.MemberLevelCalc;
import org.eclipse.daanse.olap.query.component.SymbolLiteralImpl;
import org.eclipse.daanse.olap.query.component.UnresolvedFunCallImpl;
import org.eclipse.daanse.olap.util.type.TypeUtil;

/**
 * Abstract implementation of the {@link org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler} interface.
 *
 * @author jhyde
 * @since Sep 29, 2005
 */
public class AbstractExpCompiler implements ExpressionCompiler {
    private final Evaluator evaluator;
    private final Validator validator;
    private final Map<Parameter, ParameterSlotImpl> parameterSlots =
            new HashMap<>();
    private List<ResultStyle> resultStyles;
    private final static String nullNotSupported = "Function does not support NULL member parameter";

    /**
     * Creates an AbstractExpCompiler
     *
     * @param evaluator Evaluator
     * @param validator Validator
     */
    public AbstractExpCompiler(Evaluator evaluator, Validator validator) {
        this(evaluator, validator, ResultStyle.ANY_LIST);
    }

    /**
     * Creates an AbstractExpCompiler which is constrained to produce one of
     * a set of result styles.
     *
     * @param evaluator Evaluator
     * @param validator Validator
     * @param resultStyles List of result styles, preferred first, must not be
     */
    public AbstractExpCompiler(
            Evaluator evaluator,
            Validator validator,
            List<ResultStyle> resultStyles)
    {
        this.evaluator = evaluator;
        this.validator = validator;
        this.resultStyles = resultStyles == null
                ? ResultStyle.ANY_LIST : resultStyles;
    }

    @Override
    public Evaluator getEvaluator() {
        return evaluator;
    }

    @Override
    public Validator getValidator() {
        return validator;
    }

    /**
     * {@inheritDoc}
     *
     * Uses the current ResultStyle to compile the expression.
     */
    @Override
    public Calc<?> compile(Expression exp) {
        return exp.accept(this);
    }

    /**
     * {@inheritDoc}
     *
     * Uses a new ResultStyle to compile the expression.
     */
    @Override
    public Calc<?> compileAs(
            Expression exp,
            Type resultType,
            List<ResultStyle> preferredResultTypes)
    {
        if (preferredResultTypes == null) {
            throw new IllegalArgumentException("preferredResultTypes should not be null");
        }
        int substitutions = 0;

        final List<ResultStyle> save = resultStyles;
        try {
            resultStyles = preferredResultTypes;
            if (resultType != null && resultType != exp.getType()) {
                if (resultType instanceof MemberType) {
                    return compileMember(exp);
                }
                if (resultType instanceof LevelType) {
                    return compileLevel(exp);
                } else if (resultType instanceof HierarchyType) {
                    return compileHierarchy(exp);
                } else if (resultType instanceof DimensionType) {
                    return compileDimension(exp);
                } else if (resultType instanceof ScalarType) {
                    return compileScalar(exp, false);
                }
            }
            final Calc<?> calc = compile(exp);
            

            if (substitutions > 0) {
                final TupleIteratorCalc<?> tupleIteratorCalc = (TupleIteratorCalc<?>) calc;
                if (tupleIteratorCalc == null) {
                    resultStyles =
                            Collections.singletonList(ResultStyle.ITERABLE);
                    return compile(exp);
                }
                return tupleIteratorCalc;
            }
            return calc;
        } finally {
            resultStyles = save;
        }
    }

    @Override
    public MemberCalc compileMember(Expression exp) {
        final Type type = exp.getType();
        if (type instanceof HierarchyType) {
            final HierarchyCalc hierarchyCalc = compileHierarchy(exp);
            return hierarchyToMember(hierarchyCalc);
        }
        if (type instanceof NullType) {
            throw new OlapRuntimeException(nullNotSupported);
        } else if (type instanceof DimensionType) {
            final HierarchyCalc hierarchyCalc = compileHierarchy(exp);
            return hierarchyToMember(hierarchyCalc);
        }
        assert type instanceof MemberType : type;

        Calc<?>calc= compile(exp);

        if(calc instanceof MemberCalc membCalc) {
        	return membCalc;
        }

		MemberCalc mCalc = new UnknownToMemberCalc(type, calc);
		return mCalc;
    }

    private MemberCalc hierarchyToMember(
            HierarchyCalc hierarchyCalc)
    {
        final Hierarchy hierarchy = hierarchyCalc.getType().getHierarchy();
        if (hierarchy != null) {
            return new HierarchyCurrentMemberFixedCalc(
                    TypeUtil.toMemberType(hierarchyCalc.getType()),
                    hierarchy);
        }
        return new HierarchyCurrentMemberCalc(
                TypeUtil.toMemberType(hierarchyCalc.getType()),
                hierarchyCalc);
    }

    @Override
    public LevelCalc compileLevel(Expression exp) {
        final Type type = exp.getType();
        if (type instanceof MemberType) {
            // <Member> --> <Member>.Level
            final MemberCalc memberCalc = compileMember(exp);
            return new MemberLevelCalc(
                    LevelType.forType(type),
                    memberCalc);
        }
        assert type instanceof LevelType;
		Calc<?> calc = compile(exp);

		if (calc instanceof LevelCalc lCalc) {
			return lCalc;
		}
		LevelCalc levelCalc = new UnknownToLevelCalc(type,  calc);
		return levelCalc;
    }

    @Override
    public DimensionCalc compileDimension(Expression exp) {
        final Type type = exp.getType();
		if (type instanceof HierarchyType) {
			final HierarchyCalc hierarchyCalc = compileHierarchy(exp);
			return new DimensionOfHierarchyCalc(new DimensionType(type.getDimension()), hierarchyCalc);
		}
        assert type instanceof DimensionType : type;
        Calc<?> calc=	compile(exp);

        if(calc instanceof DimensionCalc dimCalc) {
        	return dimCalc;
        }
		return new UnknownToDimensionCalc(type, calc);
    }

    @Override
    public HierarchyCalc compileHierarchy(Expression exp) {
        final Type type = exp.getType();
        if (type instanceof DimensionType) {
            // <Dimension> --> unique Hierarchy else error
            // Resolve at compile time if constant
            final Dimension dimension = type.getDimension();
            if (dimension != null) {
                final Hierarchy hierarchy =
                        DimensionUtil.getDimensionDefaultHierarchyOrThrow(dimension);
                if (hierarchy != null) {
                    return ConstantHierarchyCalc.of(hierarchy);
                }
            }
            final DimensionCalc dimensionCalc = compileDimension(exp);
            return new DimensionDefaultHierarchyCalc(
            		HierarchyType.forType(type),
                    dimensionCalc);
        }
        if (type instanceof MemberType) {
            // <Member> --> <Member>.Hierarchy
            final MemberCalc memberCalc = compileMember(exp);
            return new MemberHierarchyCalc(
            		HierarchyType.forType(type),
                    memberCalc);
        }
        if (type instanceof LevelType) {
            // <Level> --> <Level>.Hierarchy
            final LevelCalc levelCalc = compileLevel(exp);
            return new LevelHirarchyCalc(
            		HierarchyType.forType(type),
                    levelCalc);
        }
        assert type instanceof HierarchyType;
        return (HierarchyCalc) compile(exp);
    }

    @Override
    public IntegerCalc compileInteger(Expression exp) {
        final Calc<?> calc = compileScalar(exp, false);
        final Type type = calc.getType();
        if (type instanceof DecimalType decimalType
                && decimalType.getScale() == 0)
        {
            return (IntegerCalc) calc;
        }
		if (type instanceof NullType) {
			if (calc instanceof org.eclipse.daanse.olap.api.calc.ConstantCalc<?> constantCalc) {
				//no evaluate on constantCalc  result is null and constant - nothing expected while evaluate
				return new ConstantIntegerCalc(new DecimalType(Integer.MAX_VALUE, 0), null);
			}

		}
		if (type instanceof NumericType) {
			if (calc instanceof org.eclipse.daanse.olap.api.calc.ConstantCalc<?> constantCalc) {

				Object o = constantCalc.evaluate(evaluator);
				Integer i = null;
				if (o != null) {
					Number n = (Number) o;
					i = n.intValue();
				}
				return new ConstantIntegerCalc(new DecimalType(Integer.MAX_VALUE, 0), i);
			} else if (calc instanceof DoubleCalc doubleCalc) {
				return new DoubleToIntegerCalc(exp.getType(),  doubleCalc);
			}

		} else {
			return new UnknownToIntegerCalc(new DecimalType(Integer.MAX_VALUE, 0),calc);
		}

		if (calc instanceof IntegerCalc) {
            return (IntegerCalc) calc;
        }
        return new UnknownToIntegerCalc(new DecimalType(Integer.MAX_VALUE, 0),calc);
    }

	@Override
	public StringCalc compileString(Expression exp) {
		Calc<?> calc = compileScalar(exp, false);

		if (calc instanceof StringCalc stringCalc) {
			return stringCalc;
		}else if (calc instanceof org.eclipse.daanse.olap.api.calc.ConstantCalc cc) {
			Object o = cc.evaluate(null);
			String s = null;
			if (o != null) {
				s = o.toString();
			}
			return new ConstantStringCalc(StringType.INSTANCE, s);
		}else {
			return new UnknownToStringCalc(StringType.INSTANCE,  calc);
		}
	}

	@Override
	public DateTimeCalc compileDateTime(Expression exp) {

		Calc<?> calc = compileScalar(exp, false);
		if (calc instanceof DateTimeCalc dtc) {
			return dtc;
		}
		return new UnknownToDateTimeCalc(calc.getType(), calc);
	}

    @Override
    public TupleListCalc compileList(Expression exp) {
        return compileList(exp, false);
    }

    @Override
    public TupleListCalc compileList(Expression exp, boolean mutable) {
        if (!(exp.getType() instanceof SetType)) {
            throw new IllegalArgumentException("must be a set: " + exp);
        }
        final List<ResultStyle> resultStyleList;
        if (mutable) {
            resultStyleList = ResultStyle.MUTABLELIST_ONLY;
        } else {
            resultStyleList = ResultStyle.LIST_ONLY;
        }
        Calc<?> calc = compileAs(exp, null, resultStyleList);
        if (calc instanceof TupleListCalc tupleListCalc) {
            return tupleListCalc;
        }
        if (calc == null) {
            calc = compileAs(exp, null, ResultStyle.ITERABLE_ANY);
            assert calc != null;
        }
        if (calc instanceof TupleListCalc tupleListCalc) {
        	return tupleListCalc;
        }
        // If expression is an iterator, convert it to a list. Don't check
        // 'calc instanceof TupleIteratorCalc' because some generic calcs implement both
        // TupleListCalc and TupleIteratorCalc.
        if (!(calc instanceof TupleListCalc)) {
            return toList((TupleIteratorCalc<?>) calc);
        }
        // A set can only be implemented as a list or an iterable.
        throw Util.newInternal("Cannot convert calc to list: " + calc);
    }

    /**
     * Converts an iterable over tuples to a list of tuples.
     *
     * @param calc Calc
     * @return List calculation.
     */
    public TupleListCalc toList(TupleIteratorCalc<?> calc) {
        return new IterableListCalc(calc);
    }

    @Override
    public TupleIteratorCalc<?> compileIter(Expression exp) {
        TupleIteratorCalc<?> calc =
                (TupleIteratorCalc<?>) compileAs(exp, null, ResultStyle.ITERABLE_ONLY);
        if (calc == null) {
            calc = (TupleIteratorCalc<?>) compileAs(exp, null, ResultStyle.ANY_ONLY);
            assert calc != null;
        }
        return calc;
    }

    @Override
    public BooleanCalc compileBoolean(Expression exp) {
        final Calc<?> calc = compileScalar(exp, false);
        if (calc instanceof BooleanCalc bc) {
            return bc;
        }
        //
        if (calc instanceof ConstantCalc constantCalc)
        {
        	Object o=constantCalc.evaluate(null);

        	Boolean b = null;
        	if( o ==null) {
        		b=FunUtil.BOOLEAN_NULL;
        	}else if (o instanceof Boolean bt) {
				b=bt;
			}else if (o instanceof Number n) {
				b=n.intValue()>0;
			}else {
				throw new RuntimeException("wring type. was: "+o);
			}
            return 	new ConstantBooleanCalc(BooleanType.INSTANCE,b);
        }
        //


        if (calc instanceof DoubleCalc doubleCalc) {
            return new DoubleToBooleanCalc(exp.getType(),  doubleCalc);
        } else if (calc instanceof IntegerCalc integerCalc) {
            return new IntgegerToBooleanCalc(exp.getType(),  integerCalc);
        } else {
            return new UnknownToBooleanCalc(exp.getType(),  calc);
        }
    }

    @Override
    public DoubleCalc compileDouble(Expression exp) {
        final Calc<?> calc = compileScalar(exp, false);
        if (calc instanceof ConstantCalc constantCalc
                && !(calc.evaluate(null) instanceof Double))
        {
        	Object o=constantCalc.evaluate(null);

        	Double d = null;
        	if( o ==null) {
        		d=FunUtil.DOUBLE_NULL;
        	}else if (o instanceof Double dt) {
				d=dt;
			}else if (o instanceof Number n) {
				d=n.doubleValue();
			}else {
				throw new RuntimeException("wring type. was: "+o);
			}

            return 	new ConstantDoubleCalc(NumericType.INSTANCE,d);

        }
        if (calc instanceof DoubleCalc doubleCalc) {
            return doubleCalc;
        }
        if (calc instanceof IntegerCalc integerCalc) {
            return new IntegerToDoubleCalc(exp.getType(),  integerCalc);
        }

		return new UnknownToDoubleCalc(NumericType.INSTANCE, calc);

     //   throw Util.newInternal("cannot cast " + exp);
    }

    @Override
    public TupleCalc compileTuple(Expression exp) {
        return (TupleCalc) compile(exp);
    }

    @Override
    public Calc<?> compileScalar(Expression exp, boolean specific) {
        final Type type = exp.getType();
        if (type instanceof MemberType) {
            final MemberCalc calc = compileMember(exp);
            return memberToScalar(calc);
        }
        if ((type instanceof DimensionType) || (type instanceof HierarchyType)) {
            final HierarchyCalc hierarchyCalc = compileHierarchy(exp);
            return hierarchyToScalar(hierarchyCalc);
        } else if (type instanceof TupleType tupleType) {
            final TupleCalc tupleCalc = compileTuple(exp);
            final TupleValueCalc scalarCalc =
                    new TupleValueCalc(
                            tupleType.getValueType(),
                            tupleCalc,
                            getEvaluator().mightReturnNullForUnrelatedDimension());
            return scalarCalc.optimize();
        } else if (type instanceof ScalarType) {
            if (specific) {
                if (type instanceof BooleanType) {
                    return compileBoolean(exp);
                } else if (type instanceof NumericType) {
                    return compileDouble(exp);
                } else if (type instanceof StringType) {
                    return compileString(exp);
                } else {
                    return compile(exp);
                }
            } else {
                return compile(exp);
            }
        } else {
            return compile(exp);
        }
    }

    private Calc<?> hierarchyToScalar(HierarchyCalc hierarchyCalc) {
        final MemberCalc memberCalc = hierarchyToMember(hierarchyCalc);
        return memberToScalar(memberCalc);
    }

    private Calc<?> memberToScalar(MemberCalc memberCalc) {
        final MemberType memberType = (MemberType) memberCalc.getType();
        return MemberValueCalc.create(
                memberType.getValueType(),
                new MemberCalc[] {memberCalc},
                getEvaluator().mightReturnNullForUnrelatedDimension());
    }

    @Override
    public ParameterSlot registerParameter(Parameter parameter) {
        final ParameterSlot slot = parameterSlots.get(parameter);
        if (slot != null) {
            return slot;
        }
        final int index = parameterSlots.size();
        final ParameterSlotImpl slot2 = new ParameterSlotImpl(parameter, index);
        parameterSlots.put(parameter, slot2);
        slot2.value = parameter.getValue();

        // Compile the expression only AFTER the parameter has been
        // registered with a slot. Otherwise a cycle is possible.
        final Type type = parameter.getType();
        Expression defaultExp = parameter.getDefaultExp();
        Calc<?> calc;
        if (type instanceof ScalarType) {
            if (!defaultExp.getType().equals(type)) {
                defaultExp =
                        new UnresolvedFunCallImpl(new CastOperationAtom(),
                                new Expression[] {
                                        defaultExp,
                                    SymbolLiteralImpl.create(
                                                        TypeUtil.typeToCategory(type).getName())});
                defaultExp = getValidator().validate(defaultExp, true);
            }
            calc = compileScalar(defaultExp, true);
        } else {
            calc = compileAs(defaultExp, type, resultStyles);
        }
        slot2.setDefaultValueCalc(calc);
        return slot2;
    }

    @Override
    public List<ResultStyle> getAcceptableResultStyles() {
        return resultStyles;
    }

	/**
     * Implementation of {@link ParameterSlot}.
     */
    private static class ParameterSlotImpl implements ParameterSlot {
        private final Parameter parameter;
        private final int index;
        private Calc<?> defaultValueCalc;
        private Object value;
        private boolean assigned;
        private Object cachedDefaultValue;

        /**
         * Creates a ParameterSlotImpl.
         *
         * @param parameter Parameter
         * @param index Unique index of the slot
         */
        public ParameterSlotImpl(
                Parameter parameter, int index)
        {
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
         * Sets a compiled expression to compute the default value of the
         * parameter.
         *
         * @param calc Compiled expression to compute default value of
         * parameter
         *
         * @see #getDefaultValueCalc()
         */
        private void setDefaultValueCalc(Calc<?> calc) {
            defaultValueCalc = calc;
        }

        @Override
        public void setParameterValue(Object value, boolean assigned) {
            this.value = value;
            this.assigned = assigned;

            // make sure caller called convert first
            assert (!(value instanceof List) || (value instanceof TupleList));
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


}
