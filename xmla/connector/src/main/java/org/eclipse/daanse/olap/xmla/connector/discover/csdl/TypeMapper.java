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
 */
package org.eclipse.daanse.olap.xmla.connector.discover.csdl;

import java.math.BigInteger;
import java.util.Optional;

import org.eclipse.daanse.olap.api.DataTypeJdbc;
import org.eclipse.daanse.sql.model.type.Datatype;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.EDMSimpleType;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TEntityProperty;

/**
 * How this engine's types become the EDM simple types CSDL uses.
 * <p>
 * An interface because the mapping is a policy: a deployment that knows more
 * about its measures than the default does can be narrower without touching the
 * emitters.
 */
public interface TypeMapper {
    EdmType measureType(String aggregateFunction, Optional<EdmType> sourceType);

    Optional<EdmType> stringEdmType(String datatype);

    record EdmType(EDMSimpleType type, Facets facets) {
        public record Facets(Boolean nullable, BigInteger precision, BigInteger scale, BigInteger maxLength,
                Boolean unicode, Boolean fixedLength) {

            public static final Facets NONE = null;
        }
    }

    void apply(TEntityProperty p, Datatype datatype);

    void apply(TEntityProperty p, org.eclipse.daanse.olap.api.element.Property.Datatype type);

    boolean applyFromDatatypeProperty(TEntityProperty p, Object datatype);

    Optional<EdmType> forMeasure(Optional<DataTypeJdbc> dataType);
}
