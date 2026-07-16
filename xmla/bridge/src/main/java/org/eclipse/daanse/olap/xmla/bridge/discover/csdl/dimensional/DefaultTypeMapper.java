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
package org.eclipse.daanse.olap.xmla.bridge.discover.csdl.dimensional;

import java.math.BigInteger;
import java.util.Optional;

import org.eclipse.daanse.olap.api.DataTypeJdbc;
import org.eclipse.daanse.olap.api.element.Property;
import org.eclipse.daanse.sql.model.type.Datatype;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.EDMSimpleType;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TEntityProperty;
import org.eclipse.daanse.xmla.csdl.model.v2.edm.TMax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultTypeMapper implements TypeMapper {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultTypeMapper.class);

    @Override
    public EdmType levelType(Datatype dt) {
        return switch (dt) {
            case VARCHAR                    -> stringType();
            case INTEGER, SMALLINT, BIGINT  -> plain(EDMSimpleType.INT64);
            case NUMERIC, DECIMAL           -> decimal(19, 4);
            case DOUBLE, REAL, FLOAT        -> plain(EDMSimpleType.DOUBLE);
            case BOOLEAN                    -> plain(EDMSimpleType.BOOLEAN);
            case DATE, TIMESTAMP            -> plain(EDMSimpleType.DATE_TIME);
            case TIME                       -> plain(EDMSimpleType.TIME);
            case BINARY                     -> plain(EDMSimpleType.BINARY);
            case UUID                       -> plain(EDMSimpleType.GUID);
            default -> {
                LOGGER.warn("No EDM mapping for {}, falling back to String", dt);
                yield stringType();
            }
        };
    }

    @Override
    public EdmType memberPropertyType(Property.Datatype dt) {
        return switch (dt) {
            case TYPE_STRING              -> stringType();
            case TYPE_INTEGER, TYPE_LONG  -> plain(EDMSimpleType.INT64);
            case TYPE_NUMERIC             -> decimal(19, 4);
            case TYPE_BOOLEAN             -> plain(EDMSimpleType.BOOLEAN);
            case TYPE_DATE, TYPE_TIMESTAMP -> plain(EDMSimpleType.DATE_TIME);
            case TYPE_TIME                -> plain(EDMSimpleType.TIME);
            case TYPE_OTHER               -> stringType();
        };
    }

    @Override
    public EdmType measureType(String aggregateFunction, Optional<EdmType> source) {
        return switch (aggregateFunction) {
            case "count", "distinct-count" -> plain(EDMSimpleType.INT64);
            case "sum"  -> source.map(this::widenForSum).orElse(decimal(19, 4));
            case "avg"  -> source.filter(s -> s.type() == EDMSimpleType.DECIMAL)
                                 .orElse(plain(EDMSimpleType.DOUBLE));
            case "min", "max" -> source.orElse(decimal(19, 4));
            case "listagg"    -> stringType();
            default -> decimal(19, 4);
        };
    }

    
    private static EdmType stringType() {
        return new EdmType(EDMSimpleType.STRING,
                new EdmType.Facets(true, null, null, null, Boolean.TRUE, Boolean.FALSE));
    }
    private static EdmType decimal(int p, int s) {
        return new EdmType(EDMSimpleType.DECIMAL, EdmType.Facets.NONE);
        //return new EdmType(EDMSimpleType.DECIMAL, new EdmType.Facets(false,
        //        BigInteger.valueOf(p), BigInteger.valueOf(s), null, false, false));
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

    private EdmType widenForSum(EdmType t) {
        return decimal(19, 4);
    }

    private EdmType getType(Datatype type) {
        if (type != null) {
            switch (type) {
            case VARCHAR:
                return stringType();
            case NUMERIC, DECIMAL:
                return decimal(19, 4);
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
                return decimal(19, 4);
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
        if (datatype != null && datatype instanceof String value) {
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
                    return  Optional.of(stringType());
                case "NUMERIC":
                    return Optional.of(decimal(19, 4));
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
    public Optional<EdmType>forMeasure(Optional<DataTypeJdbc> dataType) {
        if (dataType.isPresent()) {
            switch (dataType.get()) {
            case VARCHAR:
                return  Optional.of(stringType());
            case NUMERIC, FLOAT, REAL, DOUBLE:
                return Optional.of(decimal(19, 4));
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

