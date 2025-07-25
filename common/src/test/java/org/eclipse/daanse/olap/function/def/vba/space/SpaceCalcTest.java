package org.eclipse.daanse.olap.function.def.vba.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.Validator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.api.function.FunctionResolver.Conversion;
import org.eclipse.daanse.olap.api.function.FunctionService;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

class SpaceCalcTest {

    @InjectService
    static FunctionService functionService;

    static Validator validator = mock(Validator.class);
    static Expression expression = mock(Expression.class);
    static ResolvedFunCall resolvedFunCall = mock(ResolvedFunCall.class);
    static ExpressionCompiler expressionCompiler = mock(ExpressionCompiler.class);
    static IntegerCalc integerCalc = mock(IntegerCalc.class);
    static Evaluator evaluator = mock(Evaluator.class);

    static Calc<?> calc;

    @BeforeAll
    static void beforeAll() {

        List<Conversion> conversions = List.of();
        when(validator.canConvert(anyInt(), eq(expression), any(), eq(conversions))).thenReturn(true);

        
        FunctionResolver resolver = functionService.getResolvers(SpaceFunDef.atom).getFirst();
        FunctionDefinition fd = resolver.resolve(new Expression[] { expression }, validator, conversions);

        when(resolvedFunCall.getArg(eq(0))).thenReturn(expression);
        when(expressionCompiler.compileInteger(expression)).thenReturn(integerCalc);

        calc = fd.compileCall(resolvedFunCall, expressionCompiler);

    }

    @Test
    void test() {

        when(integerCalc.evaluate(any())).thenReturn(0);
        assertThat(calc.evaluate(evaluator)).isEqualTo("");

        when(integerCalc.evaluate(any())).thenReturn(1);
        assertThat(calc.evaluate(evaluator)).isEqualTo(" ");

        when(integerCalc.evaluate(any())).thenReturn(5);
        assertThat(calc.evaluate(evaluator)).isEqualTo("     ");

        when(integerCalc.evaluate(any())).thenReturn(-1);
        assertThrows(Exception.class, () -> calc.evaluate(evaluator));

    }
}
