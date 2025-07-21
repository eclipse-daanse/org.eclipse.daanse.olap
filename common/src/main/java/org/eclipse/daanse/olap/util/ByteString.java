 /*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
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
 *   Stefan Bischof (bipolis.org) - initial
 */

package org.eclipse.daanse.olap.util;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Collection of bytes.
 *
 * ByteString is to bytes what {@link String} is to chars: It is immutable,
 * implements equality (hashCode and equals), comparison (compareTo) and
 * serialization correctly.
 *
 * @author jhyde
 */
public class ByteString implements Comparable<ByteString>, Serializable {
    private final byte[] bytes;

    private static final char[] digits = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /**
     * Creates a ByteString.
     *
     * @param bytes Bytes
     */
    public ByteString(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ByteString
            && Arrays.equals(bytes, ((ByteString) obj).bytes);
    }

    @Override
	public int compareTo(ByteString that) {
        final byte[] v1 = bytes;
        final byte[] v2 = that.bytes;
        final int n = Math.min(v1.length, v2.length);
        for (int i = 0; i < n; i++) {
            byte c1 = v1[i];
            byte c2 = v2[i];
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return v1.length - v2.length;
    }

    /**
     * Returns this byte string in hexadecimal format.
     *
     * @return Hexadecimal string
     */
    @Override
    public String toString() {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0, j = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            chars[j++] = digits[(b & 0xF0) >> 4];
            chars[j++] = digits[b & 0x0F];
        }
        return new String(chars);
    }

    @SuppressWarnings({
        "CloneDoesntCallSuperClone",
        "CloneDoesntDeclareCloneNotSupportedException"
    })
    @Override
    public Object clone() {
        return this;
    }

    /**
     * Returns the number of bytes in this byte string.
     *
     * @return Length of this byte string
     */
    public int length() {
        return bytes.length;
    }

    /**
     * Returns the byte at a given position in the byte string.
     *
     * @param i Index
     * @throws  IndexOutOfBoundsException
     *          if the index argument is negative or not less than
     *          length(
     * @return Byte at given position
     */
    public byte byteAt(int i) {
        return bytes[i];
    }
}
