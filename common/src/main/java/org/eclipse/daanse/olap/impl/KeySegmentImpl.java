 /*
 * Licensed to Julian Hyde under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * Julian Hyde licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
package org.eclipse.daanse.olap.impl;
import java.util.List;

import org.eclipse.daanse.olap.api.IdentifierSegment;
import org.eclipse.daanse.olap.api.KeyIdentifierSegment;
import org.eclipse.daanse.olap.api.NameIdentifierSegment;
import org.eclipse.daanse.olap.api.ParseRegion;
import org.eclipse.daanse.olap.api.Quoting;

/**
 * Segment that represents a key or compound key.
 *
 * Such a segment appears in an identifier with each component prefixed
 * with "&amp;". For example, in the identifier
 * "[Customer].[State].&amp;[WA]&amp;[USA]", the third segment is
 * a compound key whose parts are "{@code WA}" and "{@code USA}".
 *
 * org.NameSegmentImpl.mdx.NameSegment
 *
 * @author jhyde
 */
public class KeySegmentImpl implements KeyIdentifierSegment {
    private final List<NameIdentifierSegment> subSegmentList;

    /**
     * Creates a KeySegment with one or more sub-segments.
     *
     * @param subSegments Array of sub-segments
     */
    public KeySegmentImpl(NameSegmentImpl... subSegments) {
        if (subSegments.length < 1) {
            throw new IllegalArgumentException();
        }
        this.subSegmentList = UnmodifiableArrayList.asCopyOf(subSegments);
    }

    /**
     * Creates a KeySegment a list of sub-segments.
     *
     * @param subSegmentList List of sub-segments
     */
    public KeySegmentImpl(List<NameSegmentImpl> subSegmentList) {
        if (subSegmentList.size() < 1) {
            throw new IllegalArgumentException();
        }
        this.subSegmentList = List.copyOf(subSegmentList);
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder();
        toString(buf);
        return buf.toString();
    }

    public void toString(StringBuilder buf) {
        for (IdentifierSegment segment : subSegmentList) {
            buf.append('&');
            segment.toString(buf);
        }
    }

    public ParseRegion getRegion() {
        return IdentifierNode.sumSegmentRegions(subSegmentList);
    }

    public Quoting getQuoting() {
        return Quoting.KEY;
    }

    public String getName() {
        return null;
    }

    public List<NameIdentifierSegment> getKeyParts() {
        return subSegmentList;
    }
}

// End KeySegment.java
