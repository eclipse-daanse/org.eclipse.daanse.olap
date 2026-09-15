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
package org.eclipse.daanse.olap.element.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class DatabaseTableImplTest {

    private static DatabaseColumnImpl column(String name) {
        DatabaseColumnImpl c = new DatabaseColumnImpl();
        c.setName(name);
        return c;
    }

    @Test
    void aTableWithoutKeysAnswersEmpty() {
        DatabaseTableImpl table = new DatabaseTableImpl();
        table.setName("sales_fact");
        assertThat(table.getPrimaryKey()).isEmpty();
        assertThat(table.getForeignKeys()).isEmpty();
    }

    @Test
    void aDeclaredPrimaryKeyWinsOverADerivedOne() {
        DatabaseTableImpl table = new DatabaseTableImpl();
        DatabaseKeyImpl declared = new DatabaseKeyImpl("pk_customer", List.of(column("customer_id")));
        DatabaseKeyImpl derived = new DatabaseKeyImpl("customer_customer_id", List.of(column("customer_id")));
        table.setPrimaryKey(declared);
        table.setPrimaryKeyIfAbsent(derived);
        assertThat(table.getPrimaryKey()).contains(declared);
    }

    @Test
    void aForeignKeyForTheSameJoinIsRecordedOnce() {
        DatabaseTableImpl customer = new DatabaseTableImpl();
        customer.setName("customer");
        DatabaseTableImpl fact = new DatabaseTableImpl();
        fact.setName("sales_fact");
        DatabaseForeignKeyImpl declared = new DatabaseForeignKeyImpl("fk_customer", List.of(column("customer_id")),
                customer, List.of(column("customer_id")));
        DatabaseForeignKeyImpl derived = new DatabaseForeignKeyImpl("sales_fact_customer", List.of(column("customer_id")),
                customer, List.of(column("customer_id")));
        DatabaseForeignKeyImpl other = new DatabaseForeignKeyImpl("fk_product", List.of(column("product_id")),
                customer, List.of(column("product_id")));
        fact.addForeignKey(declared);
        fact.addForeignKey(derived);
        fact.addForeignKey(other);
        assertThat(fact.getForeignKeys()).containsExactly(declared, other);
    }

    @Test
    void foreignKeyRulesAndReferencedKeyNameFallBackToTheInterfaceDefaults() {
        DatabaseTableImpl customer = new DatabaseTableImpl();
        customer.setPrimaryKey(new DatabaseKeyImpl("pk_customer", List.of(column("customer_id"))));
        DatabaseForeignKeyImpl fk = new DatabaseForeignKeyImpl("fk", List.of(column("customer_id")), customer,
                List.of(column("customer_id")));
        assertThat(fk.getReferencedKeyName()).isEqualTo("pk_customer");
        assertThat(fk.getUpdateRule()).isEqualTo("NO ACTION");
        assertThat(fk.getDeleteRule()).isEqualTo("NO ACTION");

        DatabaseForeignKeyImpl explicit = new DatabaseForeignKeyImpl("fk", List.of(column("customer_id")), customer,
                List.of(column("customer_id")), "uq_customer", "CASCADE", "SET NULL");
        assertThat(explicit.getReferencedKeyName()).isEqualTo("uq_customer");
        assertThat(explicit.getUpdateRule()).isEqualTo("CASCADE");
        assertThat(explicit.getDeleteRule()).isEqualTo("SET NULL");
    }
}
