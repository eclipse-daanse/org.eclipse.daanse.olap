/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.function.core.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.ArgumentBinding;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionParameter;
import org.eclipse.daanse.olap.api.function.FunctionResolver.Conversion;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.Expression;

/**
 * Matches one declared {@link FunctionMetaData} against actual call arguments,
 * honoring the parameter flags optional, skippable and repeatGroup.
 *
 * <p>The walk is positional and greedy: a parameter that can bind the current
 * argument binds it; on a conversion failure an optional or skippable parameter
 * is skipped without consuming the argument. A repeat group is matched
 * unit-by-unit while enough arguments remain for the parameters after the
 * group. Without any flags this reduces to the classic exact-arity,
 * per-position canConvert check.
 */
public final class FunctionMetaDataMatcher {

    private FunctionMetaDataMatcher() {
    }

    public record Match(List<ArgumentBinding> bindings, List<Conversion> conversions) {
    }

    public static Optional<Match> match(FunctionMetaData functionMetaData, Expression[] args, Validator validator) {
        FunctionParameter[] params = functionMetaData.parameters();
        int argCount = args.length;
        if (argCount < functionMetaData.minArity() || argCount > functionMetaData.maxArity()) {
            return Optional.empty();
        }

        List<Conversion> conversions = new ArrayList<>();
        List<ArgumentBinding> bindings = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (j < params.length) {
            FunctionParameter parameter = params[j];
            int group = parameter.repeatGroup();
            if (group > 0) {
                int groupEnd = j;
                while (groupEnd < params.length && params[groupEnd].repeatGroup() == group) {
                    groupEnd++;
                }
                int minAfter = minArityOfRange(params, groupEnd);
                boolean requiredGroup = !params[j].optional();
                int units = 0;
                while (i < argCount - minAfter) {
                    int saveArg = i;
                    int saveBindings = bindings.size();
                    int saveConversions = conversions.size();
                    boolean unitOk = true;
                    boolean consumed = false;
                    for (int g = j; g < groupEnd && unitOk; g++) {
                        FunctionParameter groupParameter = params[g];
                        if (i < argCount - minAfter
                                && tryBind(i, args, g, groupParameter, validator, conversions, bindings)) {
                            i++;
                            consumed = true;
                        } else if (!groupParameter.skippable() && !groupParameter.optional()) {
                            unitOk = false;
                        }
                    }
                    if (!unitOk || !consumed) {
                        i = saveArg;
                        truncate(bindings, saveBindings);
                        truncate(conversions, saveConversions);
                        break;
                    }
                    units++;
                }
                if (units == 0 && requiredGroup) {
                    return Optional.empty();
                }
                j = groupEnd;
            } else {
                if (i < argCount && tryBind(i, args, j, parameter, validator, conversions, bindings)) {
                    i++;
                    j++;
                } else if (parameter.optional() || parameter.skippable()) {
                    j++;
                } else {
                    return Optional.empty();
                }
            }
        }
        if (i != argCount) {
            return Optional.empty();
        }
        return Optional.of(new Match(bindings, conversions));
    }

    /** True when a SET argument is possible at position k for this overload. */
    public static boolean setPossibleAt(FunctionMetaData functionMetaData, int k) {
        FunctionParameter[] params = functionMetaData.parameters();
        boolean flexible = false;
        for (int idx = 0; idx < params.length; idx++) {
            FunctionParameter parameter = params[idx];
            boolean flagged = parameter.optional() || parameter.skippable() || parameter.repeatGroup() > 0;
            if (parameter.dataType() == DataType.SET) {
                if (idx == k || (flexible && idx <= k) || (parameter.repeatGroup() > 0 && idx <= k)) {
                    return true;
                }
            }
            flexible |= flagged;
        }
        return false;
    }

    private static int minArityOfRange(FunctionParameter[] params, int from) {
        int min = 0;
        int countedGroup = -1;
        for (int idx = from; idx < params.length; idx++) {
            FunctionParameter parameter = params[idx];
            if (parameter.optional() || parameter.skippable()) {
                continue;
            }
            int group = parameter.repeatGroup();
            if (group > 0) {
                if (group == countedGroup) {
                    continue;
                }
                countedGroup = group;
            }
            min++;
        }
        return min;
    }

    private static boolean tryBind(int argIndex, Expression[] args, int paramIndex, FunctionParameter parameter,
            Validator validator, List<Conversion> conversions, List<ArgumentBinding> bindings) {
        int before = conversions.size();
        if (!validator.canConvert(argIndex, args[argIndex], parameter.dataType(), conversions)) {
            truncate(conversions, before);
            return false;
        }
        int cost = 0;
        for (int c = before; c < conversions.size(); c++) {
            cost += conversions.get(c).getCost();
        }
        bindings.add(new ArgumentBinding(argIndex, paramIndex, args[argIndex].getCategory(), parameter.dataType(),
                cost));
        return true;
    }

    private static void truncate(List<?> list, int size) {
        while (list.size() > size) {
            list.remove(list.size() - 1);
        }
    }
}
