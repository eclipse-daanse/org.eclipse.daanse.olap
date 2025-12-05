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

import java.util.AbstractList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.daanse.olap.api.IdentifierSegment;
import org.eclipse.daanse.olap.api.ParseRegion;

/**
 * Multi-part identifier.
 *
 * An identifier is immutable.
 *
 * An identifer consists of one or more {@link IdentifierSegment}s. A segment
 * is either:
 * An unquoted value such as '{@code CA}',
 * A value quoted in brackets, such as '{@code [San Francisco]}', or
 * A key of one or more parts, each of which is prefixed with '&amp;',
 *     such as '{@code &amp;[Key 1]&amp;Key2&amp;[5]}'.
 *
 *
 * Segment types are indicated by the Quoting enumeration.
 *
 * A key segment is of type Quoting#KEY, and has one or more
 * component parts accessed via the
 * IdentifierSegment#getKeyParts() method. The parts
 * are of type Quoting#UNQUOTED or Quoting#QUOTED.
 *
 * A simple example is the identifier Measures.[Unit Sales]. It
 * has two segments:
 * Segment #0 is
 *     Quoting#UNQUOTED UNQUOTED,
 *     name "Measures"
 * Segment #1 is
 *     Quoting#QUOTED QUOTED,
 *     name "Unit Sales"
 *
 *
 * A more complex example illustrates a compound key. The identifier {@code
 * [Customers].[City].&amp;[San Francisco]&amp;CA&amp;USA.&amp;[cust1234]}
 * contains four segments as follows:
 *
 * Segment #0 is QUOTED, name "Customers"
 * Segment #1 is QUOTED, name "City"
 * Segment #2 is a Quoting#KEY KEY.
 *     It has 3 sub-segments:
 *
 *     Sub-segment #0 is QUOTED, name "San Francisco"
 *     Sub-segment #1 is UNQUOTED, name "CA"
 *     Sub-segment #2 is UNQUOTED, name "USA"
 *
 *
 * Segment #3 is a KEY. It has 1 sub-segment:
 *
 *     Sub-segment #0 is QUOTED, name "cust1234"
 *
 *
 *
 *
 * @author jhyde
 */
public class IdentifierNode
{

    private final List<IdentifierSegment> segments;

    /**
     * Creates an identifier containing one or more segments.
     *
     * @param segments Array of Segments, each consisting of a name and quoting
     * style
     */
    public IdentifierNode(IdentifierSegment... segments) {
        if (segments.length < 1) {
            throw new IllegalArgumentException();
        }
        this.segments = List.of(segments);
    }


    /**
     * Creates an identifier containing a list of segments.
     *
     * @param segments List of segments
     */
    public IdentifierNode(List<IdentifierSegment> segments) {
        if (segments.size() < 1) {
            throw new IllegalArgumentException();
        }
        this.segments = List.copyOf(segments);
    }
    
    public String toString() {
        if (segments != null) {
            return "[" + segments.stream().map(is -> is.getName())
                    .collect(Collectors.joining("].[")) + "]"; 
        }
        return "";
    }
    /**
     * Returns string quoted in [...].
     *
     * For example, "San Francisco" becomes
     * "[San Francisco]"; "a [bracketed] string" becomes
     * "[a [bracketed]] string]".
     *
     * @param id Unquoted name
     * @return Quoted name
     */
    static String quoteMdxIdentifier(String id) {
        StringBuilder buf = new StringBuilder(id.length() + 20);
        quoteMdxIdentifier(id, buf);
        return buf.toString();
    }

    /**
     * Returns a string quoted in [...], writing the results to a
     * {@link StringBuilder}.
     *
     * @param id Unquoted name
     * @param buf Builder to write quoted string to
     */
    static void quoteMdxIdentifier(String id, StringBuilder buf) {
        buf.append('[');
        int start = buf.length();
        buf.append(id);
        Olap4jUtil.replace(buf, start, "]", "]]");
        buf.append(']');
    }


    /**
     * Returns a region encompassing the regions of the first through the last
     * of a list of segments.
     *
     * @param segments List of segments
     * @return Region encompassed by list of segments
     */
    static ParseRegion sumSegmentRegions(
        final List<? extends IdentifierSegment> segments)
    {
        return ParseRegionImpl.sum(
            new AbstractList<ParseRegion>() {
                public ParseRegion get(int index) {
                    return segments.get(index).getRegion();
                }

                public int size() {
                    return segments.size();
                }
            });
    }
}

// End IdentifierNode.java
