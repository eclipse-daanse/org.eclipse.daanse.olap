/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2004-2005 TONBELLER AG
 * Copyright (C) 2006-2017 Hitachi Vantara and others
 * All Rights Reserved.
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


package org.eclipse.daanse.olap.common;

import java.util.List;

import org.eclipse.daanse.olap.api.calc.ResultStyle;
import org.eclipse.daanse.olap.api.exception.OlapRuntimeException;

/**
 * Exception that indicates a compiler could not implement an expression in any
 * of the result styles requested by the client.
 *
 * @author Richard Emberson
 */
public class ResultStyleException extends OlapRuntimeException {
    public static ResultStyleException generate(
        List<ResultStyle> producer,
        List<ResultStyle> consumer)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("Producer expected ResultStyles: ");
        buf.append('{');
        for (int i = 0; i < producer.size(); i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(producer.get(i));
        }
        buf.append('}');
        buf.append(" but Consumer wanted: ");
        buf.append('{');
        for (int i = 0; i < consumer.size(); i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(consumer.get(i));
        }
        buf.append('}');
        throw new ResultStyleException(buf.toString());
    }

    public static ResultStyleException generateBadType(
        List<ResultStyle> wanted,
        ResultStyle got)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("Wanted ResultStyles: ");
        buf.append('{');
        for (int i = 0; i < wanted.size(); i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(wanted.get(i));
        }
        buf.append('}');
        buf.append(" but got: ");
        buf.append(got);
        return new ResultStyleException(buf.toString());
    }

    public ResultStyleException(String message) {
        super(message);
    }
}
