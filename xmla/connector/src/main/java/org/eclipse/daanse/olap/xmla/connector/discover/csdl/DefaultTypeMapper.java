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

import java.util.Optional;

import org.eclipse.daanse.olap.api.DataTypeJdbc;
import org.eclipse.daanse.olap.api.element.Property;
import org.eclipse.daanse.sql.model.type.Datatype;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.EDMSimpleType;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TEntityProperty;

/**
 * The default mapping from this engine's types to the EDM simple types CSDL
 * uses.
 * <p>
 * It is deliberately coarse: every exact numeric becomes DECIMAL and every
 * integral type becomes INT64, because a CSDL consumer reads these to lay out
 * storage and a narrower guess costs more than it saves.
 */
public final class DefaultTypeMapper implements TypeMapper {

    @Override
    public EdmType measureType(String aggregateFunction, Optional<EdmType> source) {
        return switch (aggregateFunction) {
        case "count", "distinct-count" -> plain(EDMSimpleType.INT64);
        // Widening a sum has no cases: every source type sums into the same decimal.
        case "sum" -> decimal();
        case "avg" -> source.filter(s -> s.type() == EDMSimpleType.DECIMAL).orElse(plain(EDMSimpleType.DOUBLE));
        case "min", "max" -> source.orElse(decimal());
        case "listagg" -> stringType();
        default -> decimal();
        };
    }

    private static EdmType stringType() {
        return new EdmType(EDMSimpleType.STRING,
                new EdmType.Facets(true, null, null, null, Boolean.TRUE, Boolean.FALSE));
    }

    /**
     * DECIMAL without precision or scale.
     * <p>
     * This took (19, 4) at every call site and discarded both. The facets are left
     * off rather than guessed: nothing here knows the real precision of a measure,
     * and CSDL treats an absent facet as "unspecified", which is true, while a
     * stated 19,4 would be a claim.
     */
    private static EdmType decimal() {
        return new EdmType(EDMSimpleType.DECIMAL, EdmType.Facets.NONE);
    }

    private static EdmType plain(EDMSimpleType t) {
        return new EdmType(t, EdmType.Facets.NONE);
    }

    @Override
    public void apply(TEntityProperty p, Datatype datatype) {
        p.setType(getType(datatype).type());
    }

    @Override
    public void apply(TEntityProperty p, org.eclipse.daanse.olap.api.element.Property.Datatype type) {
        p.setType(getType(type).type());
    }

    private EdmType getType(Datatype type) {
        if (type != null) {
            switch (type) {
            case VARCHAR:
                return stringType();
            case NUMERIC, DECIMAL:
                return decimal();
            case INTEGER, BIGINT, SMALLINT:
                return plain(EDMSimpleType.INT64);
            case BOOLEAN:
                return plain(EDMSimpleType.BOOLEAN);
            case DATE, TIMESTAMP:
                return plain(EDMSimpleType.DATE_TIME);
            case TIME:
                return plain(EDMSimpleType.TIME);
            case DOUBLE, REAL, FLOAT:
                return plain(EDMSimpleType.DOUBLE);
            default:
                return stringType();
            }
        }
        return stringType();
    }

    private EdmType getType(org.eclipse.daanse.olap.api.element.Property.Datatype type) {
        if (type != null) {
            switch (type) {
            case TYPE_STRING:
                return stringType();
            case TYPE_NUMERIC:
                return decimal();
            case TYPE_INTEGER, TYPE_LONG:
                return plain(EDMSimpleType.INT64);
            case TYPE_BOOLEAN:
                return plain(EDMSimpleType.BOOLEAN);
            case TYPE_DATE, TYPE_TIME, TYPE_TIMESTAMP:
                return plain(EDMSimpleType.DATE_TIME);
            case TYPE_OTHER:
                return stringType();
            default:
                return stringType();
            }
        }
        return stringType();
    }

    @Override
    public boolean applyFromDatatypeProperty(TEntityProperty p, Object datatype) {
        if (datatype instanceof String value) {
            switch (value) {
            case "String":
            case "NUMERIC":
            case "Integer":
            case "Boolean":
            case "Date":
            case "Time":
            case "Timestamp":
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    @Override
    public Optional<EdmType> stringEdmType(String datatype) {
        if (datatype != null) {
            switch (datatype) {
            case "UNDEFINED":
            case "String":
                return Optional.of(stringType());
            case "NUMERIC":
                return Optional.of(decimal());
            case "Integer":
                return Optional.of(plain(EDMSimpleType.INT64));
            case "Boolean":
                return Optional.of(plain(EDMSimpleType.BOOLEAN));
            case "Date":
                return Optional.of(plain(EDMSimpleType.DATE_TIME));
            case "Time":
                return Optional.of(plain(EDMSimpleType.TIME));
            case "Timestamp":
                return Optional.of(plain(EDMSimpleType.DATE_TIME));
            default:
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<EdmType> forMeasure(Optional<DataTypeJdbc> dataType) {
        if (dataType.isPresent()) {
            switch (dataType.get()) {
            case VARCHAR:
                return Optional.of(stringType());
            case NUMERIC, FLOAT, REAL, DOUBLE:
                return Optional.of(decimal());
            case INTEGER, BIGINT, SMALLINT:
                return Optional.of(plain(EDMSimpleType.INT64));
            case BOOLEAN:
                return Optional.of(plain(EDMSimpleType.BOOLEAN));
            case DATE:
                return Optional.of(plain(EDMSimpleType.DATE_TIME));
            case TIME:
                return Optional.of(plain(EDMSimpleType.TIME));
            case TIMESTAMP:
                return Optional.of(plain(EDMSimpleType.DATE_TIME));
            default:
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
