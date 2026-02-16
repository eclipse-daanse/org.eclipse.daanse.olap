/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2020 Hitachi Vantara..  All rights reserved.
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

package org.eclipse.daanse.olap.function.def.crossjoin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.calc.tuple.TupleCursor;
import org.eclipse.daanse.olap.api.calc.tuple.TupleIterable;
import org.eclipse.daanse.olap.api.calc.tuple.TupleList;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.execution.ExecutionContext;
import org.eclipse.daanse.olap.api.execution.ExecutionMetadata;
import org.eclipse.daanse.olap.api.execution.Statement;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionParameter;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.api.result.Position;
import org.eclipse.daanse.olap.api.result.Result;
import org.eclipse.daanse.olap.api.type.MemberType;
import org.eclipse.daanse.olap.api.type.SetType;
import org.eclipse.daanse.olap.api.type.TupleType;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.ArrayTupleList;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.UnaryTupleList;
import org.eclipse.daanse.olap.common.ConfigConstants;
import org.eclipse.daanse.olap.common.SystemWideProperties;
import org.eclipse.daanse.olap.execution.ExecutionImpl;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.query.component.ResolvedFunCallImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * <code>CrossJoint</code> tests the collation order of positive and negative
 * infinity, and {@link Double#NaN}.
 *
 * @author <a>Richard M. Emberson</a>
 * @since Jan 14, 2007
 */

public class CrossJoinTest {

  private static final String SELECT_GENDER_MEMBERS =
    "select Gender.members on 0 from sales";

  private static final String SALES_CUBE = "Sales";

  private ExecutionImpl excMock = mock( ExecutionImpl.class );

  static List<List<Member>> m3 = Arrays.asList(
    Arrays.<Member>asList( new TestMember( "k" ), new TestMember( "l" ) ),
    Arrays.<Member>asList( new TestMember( "m" ), new TestMember( "n" ) ) );

  static List<List<Member>> m4 = Arrays.asList(
    Arrays.<Member>asList( new TestMember( "U" ), new TestMember( "V" ) ),
    Arrays.<Member>asList( new TestMember( "W" ), new TestMember( "X" ) ),
    Arrays.<Member>asList( new TestMember( "Y" ), new TestMember( "Z" ) ) );

  static final Comparator<List<Member>> memberComparator =
    new Comparator<>() {
      @Override
	public int compare( List<Member> ma1, List<Member> ma2 ) {
        for ( int i = 0; i < ma1.size(); i++ ) {
          int c = ma1.get( i ).compareTo( ma2.get( i ) );
          if ( c < 0 ) {
            return c;
          } else if ( c > 0 ) {
            return c;
          }
        }
        return 0;
      }
    };

  private CrossJoinFunDef crossJoinFunDef;

  @BeforeEach
  protected void beforeEach() throws Exception {
    crossJoinFunDef = new CrossJoinFunDef( new NullFunDef().getFunctionMetaData() );
  }

  @AfterEach
  protected void afterEach() throws Exception {
	  SystemWideProperties.instance().populateInitial();
  }

  ////////////////////////////////////////////////////////////////////////
  // Iterable
  ////////////////////////////////////////////////////////////////////////

  @Test
  void testListTupleListTupleIterCalc() {
        Statement statement = mock(Statement.class);
        Connection rolapConnection = mock(Connection.class);
        Context context = mock(Context.class);
        when(context.getConfigValue(ConfigConstants.CHECK_CANCEL_OR_TIMEOUT_INTERVAL, ConfigConstants.CHECK_CANCEL_OR_TIMEOUT_INTERVAL_DEFAULT_VALUE, Integer.class)).thenReturn(0);
        when(rolapConnection.getContext()).thenReturn(context);
        when(statement.getDaanseConnection()).thenReturn(rolapConnection);
        when(excMock.getDaanseStatement()).thenReturn(statement);
        ExecutionMetadata executionMetadata = mock(ExecutionMetadata.class);
        ExecutionContext executionContext = ExecutionContext.root(Optional.empty(), executionMetadata);
        when(excMock.asContext()).thenReturn(executionContext);
      CrossJoinIterCalc calc =
        new CrossJoinIterCalc( getResolvedFunCall(), null, crossJoinFunDef.getCtag() );
      doTupleTupleIterTest( calc, excMock );
  }

    private void doTupleTupleIterTest(
    CrossJoinIterCalc calc, ExecutionImpl execution ) {
    TupleList l4 = makeListTuple( m4 );
    String s4 = toString( l4 );
    String e4 = "{[U, V], [W, X], [Y, Z]}";
    assertEquals( e4, s4 );

    TupleList l3 = makeListTuple( m3 );
    String s3 = toString( l3 );
    String e3 = "{[k, l], [m, n]}";
    assertEquals( e3, s3 );

    String s = ExecutionContext.where(execution.asContext(), () -> {
      TupleIterable iterable = calc.makeIterable( l4, l3 );
      return CrossJoinTest.this.toString( iterable );
    });
    String e =
      "{[U, V, k, l], [U, V, m, n], [W, X, k, l], "
        + "[W, X, m, n], [Y, Z, k, l], [Y, Z, m, n]}";
    assertEquals( e, s );
  }


  private static TupleList getGenderMembers( Result genders ) {
    TupleList genderMembers = new UnaryTupleList();
    for ( Position pos : genders.getAxes()[ 0 ].getPositions() ) {
      genderMembers.add( pos );
    }
    return genderMembers;
  }

  private Integer crossJoinIterCalcIterate(
    final TupleList list1, final TupleList list2,
    final ExecutionImpl execution ) {
    return ExecutionContext.where(execution.asContext(), () -> {
      TupleIterable iterable =
        new CrossJoinIterCalc(
          getResolvedFunCall(), null, crossJoinFunDef.getCtag() ).makeIterable( list1, list2 );
      TupleCursor tupleCursor = iterable.tupleCursor();
      // total count of all iterations
      int counter = 0;
      while ( tupleCursor.forward() ) {
        counter++;
      }
      return Integer.valueOf( counter );
    });
  }

  ////////////////////////////////////////////////////////////////////////
  // Immutable List
  ////////////////////////////////////////////////////////////////////////

  @Test
  void testImmutableListTupleListTupleListCalc() {
    ImmutableListCalc calc =
      new ImmutableListCalc(
        getResolvedFunCall(), null, crossJoinFunDef.getCtag() );

    doTupleTupleListTest( calc );
  }

  protected void doTupleTupleListTest(
    BaseListCalc calc ) {
    TupleList l4 = makeListTuple( m4 );
    String s4 = toString( l4 );
    String e4 = "{[U, V], [W, X], [Y, Z]}";
    assertEquals( e4, s4 );

    TupleList l3 = makeListTuple( m3 );
    String s3 = toString( l3 );
    String e3 = "{[k, l], [m, n]}";
    assertEquals( e3, s3 );

    TupleList list = calc.makeList( l4, l3 );
    String s = toString( list );
    String e =
      "{[U, V, k, l], [U, V, m, n], [W, X, k, l], "
        + "[W, X, m, n], [Y, Z, k, l], [Y, Z, m, n]}";
    assertEquals( e, s );

    TupleList subList = list.subList( 0, 6 );
    s = toString( subList );
    assertEquals( 6, subList.size() );
    assertEquals( e, s );

    subList = subList.subList( 0, 6 );
    s = toString( subList );
    assertEquals( 6, subList.size() );
    assertEquals( e, s );

    subList = subList.subList( 1, 5 );
    s = toString( subList );
    e = "{[U, V, m, n], [W, X, k, l], [W, X, m, n], [Y, Z, k, l]}";
    assertEquals( 4, subList.size() );
    assertEquals( e, s );

    subList = subList.subList( 2, 4 );
    s = toString( subList );
    e = "{[W, X, m, n], [Y, Z, k, l]}";
    assertEquals( 2, subList.size() );
    assertEquals( e, s );

    subList = subList.subList( 1, 2 );
    s = toString( subList );
    e = "{[Y, Z, k, l]}";
    assertEquals( 1, subList.size() );
    assertEquals( e, s );

    subList = list.subList( 1, 4 );
    s = toString( subList );
    e = "{[U, V, m, n], [W, X, k, l], [W, X, m, n]}";
    assertEquals( 3, subList.size() );
    assertEquals( e, s );

    subList = list.subList( 2, 4 );
    s = toString( subList );
    e = "{[W, X, k, l], [W, X, m, n]}";
    assertEquals( 2, subList.size() );
    assertEquals( e, s );

    subList = list.subList( 2, 3 );
    s = toString( subList );
    e = "{[W, X, k, l]}";
    assertEquals( 1, subList.size() );
    assertEquals( e, s );

    subList = list.subList( 4, 4 );
    s = toString( subList );
    e = "{}";
    assertEquals( 0, subList.size() );
    assertEquals( e, s );
  }


  ////////////////////////////////////////////////////////////////////////
  // Mutable List
  ////////////////////////////////////////////////////////////////////////
  @Test
  void testMutableListTupleListTupleListCalc() {
    MutableListCalc calc =
      new MutableListCalc(
        getResolvedFunCall(), null, crossJoinFunDef.getCtag() );

    doMTupleTupleListTest( calc );
  }

  protected void doMTupleTupleListTest(
    BaseListCalc calc ) {
    TupleList l1 = makeListTuple( m3 );
    String s1 = toString( l1 );
    String e1 = "{[k, l], [m, n]}";
    assertEquals( e1, s1 );

    TupleList l2 = makeListTuple( m4 );
    String s2 = toString( l2 );
    String e2 = "{[U, V], [W, X], [Y, Z]}";
    assertEquals( e2, s2 );

    TupleList list = calc.makeList( l1, l2 );
    String s = toString( list );
    String e = "{[k, l, U, V], [k, l, W, X], [k, l, Y, Z], "
      + "[m, n, U, V], [m, n, W, X], [m, n, Y, Z]}";
    assertEquals( e, s );

    if ( false ) {
      // Cannot apply Collections.reverse to TupleList
      // because TupleList.set always returns null.
      // (This is a violation of the List contract, but it is inefficient
      // to construct a list to return.)
      Collections.reverse( list );
      s = toString( list );
      e = "{[m, n, Y, Z], [m, n, W, X], [m, n, U, V], "
        + "[k, l, Y, Z], [k, l, W, X], [k, l, U, V]}";
      assertEquals( e, s );
    }

    // sort
    Collections.sort( list, memberComparator );
    s = toString( list );
    e = "{[k, l, U, V], [k, l, W, X], [k, l, Y, Z], "
      + "[m, n, U, V], [m, n, W, X], [m, n, Y, Z]}";
    assertEquals( e, s );

    List<Member> members = list.remove( 1 );
    s = toString( list );
    e = "{[k, l, U, V], [k, l, Y, Z], [m, n, U, V], "
      + "[m, n, W, X], [m, n, Y, Z]}";
    assertEquals( e, s );
  }

  ////////////////////////////////////////////////////////////////////////
  // Helper methods
  ////////////////////////////////////////////////////////////////////////
  protected String toString( TupleIterable l ) {
    StringBuilder buf = new StringBuilder( 100 );
    buf.append( '{' );
    int j = 0;
    for ( List<Member> o : l ) {
      if ( j++ > 0 ) {
        buf.append( ", " );
      }
      buf.append( o );
    }
    buf.append( '}' );
    return buf.toString();
  }

  protected TupleList makeListTuple( List<List<Member>> ms ) {
    final TupleList list = new ArrayTupleList( ms.get( 0 ).size() );
    for ( List<Member> m : ms ) {
      list.add( m );
    }
    return list;
  }

  protected ResolvedFunCallImpl getResolvedFunCall() {
    FunctionDefinition funDef = new TestFunDef();
    Expression[] args = new Expression[ 0 ];
    Type returnType =
      new SetType(
        new TupleType(
          new Type[] {
            new MemberType( null, null, null, null ),
            new MemberType( null, null, null, null ) } ) );
    return new ResolvedFunCallImpl( funDef, args, returnType );
  }

  ////////////////////////////////////////////////////////////////////////
  // Helper classes
  ////////////////////////////////////////////////////////////////////////
  public static class TestFunDef implements FunctionDefinition {
    TestFunDef() {
    }


    @Override
	public Expression createCall( Validator validator, Expression[] args ) {
      throw new UnsupportedOperationException();
    }

    @Override
	public String getSignature() {
      throw new UnsupportedOperationException();
    }

    @Override
	public void unparse( Expression[] args, PrintWriter pw ) {
      throw new UnsupportedOperationException();
    }

    @Override
	public Calc compileCall( ResolvedFunCall call, ExpressionCompiler compiler ) {
      throw new UnsupportedOperationException();
    }

	@Override
	public FunctionMetaData getFunctionMetaData() {
		return new FunctionMetaData() {

			@Override
			public OperationAtom operationAtom() {

				return new FunctionOperationAtom("SomeName");
			}

			    @Override
				public String description() {
			      throw new UnsupportedOperationException();
			    }

			    @Override
				public DataType returnCategory() {
			      throw new UnsupportedOperationException();
			    }

			    @Override
				public DataType[] parameterDataTypes() {
			      throw new UnsupportedOperationException();
			    }


				@Override
				public FunctionParameterR[] parameters() {
			      throw new UnsupportedOperationException();
				}

		};
	}
  }

  public static class NullFunDef implements FunctionDefinition {
    public NullFunDef() {
    }



    @Override
	public Expression createCall( Validator validator, Expression[] args ) {
      return null;
    }

    @Override
	public String getSignature() {
      return "";
    }

    @Override
	public void unparse( Expression[] args, PrintWriter pw ) {
      //
    }

    @Override
	public Calc<?> compileCall( ResolvedFunCall call, ExpressionCompiler compiler ) {
      return null;
    }

	@Override
	public FunctionMetaData getFunctionMetaData() {
        return new FunctionMetaData() {

            @Override
            public OperationAtom operationAtom() {
                return new FunctionOperationAtom("");
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public DataType returnCategory() {
                  return DataType.UNKNOWN;
            }

                @Override
                public DataType[] parameterDataTypes() {
                    return new DataType[ 0 ];
                }

                @Override
                public FunctionParameter[] parameters() {
                    return new FunctionParameter[0];
                }
       };
	}
  }
}
