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
package org.eclipse.daanse.olap.xmla.connector.discover;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The MDX capability masks say what this engine does, no more.
 * <p>
 * A client reads them to decide which MDX it may generate. Claiming a capability
 * the engine lacks does not fail politely - the client emits the query and the
 * parser refuses it - so each bit here has to be one the engine can answer for.
 */
class CapabilityMasksTest {

    /**
     * MDPROPVAL_MF_WITH_CALCMEMBERS (0x01) and MDPROPVAL_MF_WITH_NAMEDSETS (0x02).
     * <p>
     * The CREATE forms (0x04, 0x08) and the SESSION/GLOBAL scopes (0x10, 0x20) are
     * out because there is no standalone CREATE statement to carry them: the MDX
     * model knows Select, Drillthrough, Explain, Refresh and Update, and the
     * calculated members and named sets it does support arrive in a SELECT's WITH
     * clause.
     */
    @Test
    void formulasClaimsTheTwoWithForms() {
        assertThat(SupportedProperties.VALUES).containsEntry("MdpropMdxFormulas", "3");
        assertThat(SupportedProperties.NAMES).contains("MdpropMdxFormulas");
    }

    /**
     * Zero, and not for want of looking: [MS-SSAS] names the property and its type
     * but states no bit vocabulary for it - only that Analysis Services answers 31
     * for MOLAP and 23 in-memory. There is nothing here to claim on evidence, and
     * this server takes no DDL over MDX in any case.
     */
    @Test
    void ddlExtensionsClaimsNothing() {
        assertThat(SupportedProperties.VALUES).containsEntry("MdpropMdxDdlExtensions", "0");
    }

    /** Every mask this server answers is a number a client can parse. */
    @Test
    void everyMaskIsANumber() {
        SupportedProperties.VALUES.entrySet().stream().filter(e -> e.getKey().startsWith("MdpropMdx"))
                .forEach(e -> assertThat(e.getValue()).as("%s", e.getKey()).containsOnlyDigits());
    }

    /**
     * A deployment may override a value without a rebuild, and owns the
     * consequence.
     */
    @Test
    void aDeploymentCanOverrideAValue() {
        assertThat(System.getProperty("daanse.xmla.property.MdpropMdxSubqueries"))
                .as("the override is read at class initialisation, so this test only pins the naming").isNull();
        assertThat(SupportedProperties.VALUES).containsEntry("MdpropMdxSubqueries", "1");
    }
}
