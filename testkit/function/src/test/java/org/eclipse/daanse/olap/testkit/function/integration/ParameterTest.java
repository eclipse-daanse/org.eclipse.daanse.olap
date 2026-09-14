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
 */
package org.eclipse.daanse.olap.testkit.function.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionParameter;
import org.eclipse.daanse.olap.function.services.standard.StandardFunctions;
import org.eclipse.daanse.olap.testkit.function.SignatureText;
import org.junit.jupiter.api.Test;

public class ParameterTest {

	@Test
	void parameterNamesAreUniqueWithinAnOverload() {
	    List<String> offenders = new ArrayList<>();
	    for (FunctionMetaData metaData : StandardFunctions.standard().getFunctionMetaDatas()) {
	        Map<String, List<FunctionParameter>> byName = new LinkedHashMap<>();
	        for (FunctionParameter parameter : metaData.parameters()) {
	            parameter.name().ifPresent(n ->
	                    byName.computeIfAbsent(n, k -> new ArrayList<>()).add(parameter));
	        }
	        byName.forEach((name, group) -> {
	            // a repeat group is one logical parameter and may share a name
	            boolean oneRepeatGroup = group.size() > 1
	                    && group.stream().allMatch(p -> p.repeatGroup() > 0)
	                    && group.stream().map(FunctionParameter::repeatGroup).distinct().count() == 1;
	            if (group.size() > 1 && !oneRepeatGroup) {
	                offenders.add(SignatureText.ofDeclaration(metaData) + "  ->  " + name
	                        + " x" + group.size());
	            }
	        });
	    }
	    assertThat(offenders)
	            .as("""
	                Two independent parameters share a name. They then share one text key, so
	                MDSCHEMA_FUNCTIONS cannot tell them apart and they cannot be localised
	                separately. A repeat group is exempt: there it is one logical parameter.
	                """)
	            .isEmpty();
	}

}
