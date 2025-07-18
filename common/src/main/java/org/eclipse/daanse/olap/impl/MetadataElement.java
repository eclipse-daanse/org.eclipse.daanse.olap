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

/**
 * An element which describes the structure of an OLAP schema.
 *
 * @author jhyde
 * @since Oct 13, 2006
 */
public interface MetadataElement {
    /**
     * Returns the name of this element.
     *
     * Name is never null. Unlike {@link #getCaption() caption} and
     * {@link #getDescription() description}, an element's name is the same in
     * every {@link java.util.Locale}.
     *
     * @return name of this element
     */
    String getName();

    /**
     * Returns the unique name of this element within its schema.
     *
     * The unique name is never null, and is unique among all elements in
     * this Catalog.
     *
     * Unlike {@link #getCaption() caption} and
     * {@link #getDescription() description}, an element's unique name is the
     * same in every {@link java.util.Locale}.
     *
     * The structure of the unique name is provider-specific and subject to
     * change between provider versions. Applications should not attempt to
     * reverse-engineer the structure of the name.
     *
     * @return unique name of this element
     */
    String getUniqueName();

    /**
     * Returns the caption of this element in the current connection's
     * {@link java.util.Locale}.
     *
     * This method may return the empty string, but never returns null.
     * The rules for deriving an element's caption are provider-specific,
     * but generally if no caption is defined for the element in a given locale,
     * returns the name of the element.
     *
     * @return caption of this element in the current locale; never null.
     *
     * org.olap4j.OlapConnection#getLocale()
     */
    String getCaption();

    /**
     * Returns the description of this element in the current connection's
     * {@link java.util.Locale}.
     *
     * This method may return the empty string, but never returns null.
     * The rules for deriving an element's description are provider-specific,
     * but generally if no description is defined
     * for the element in a given locale, returns the description in base
     * locale.
     *
     * @return description of this element in the current locale; never null.
     *
     * org.olap4j.OlapConnection#getLocale()
     */
    String getDescription();

    /**
     * Returns whether this element is visible to end-users.
     *
     * Visibility is a hint for client applications. An element's visibility
     * does not affect how it is treated when MDX queries are evaluated.
     *
     * If you wish to hide an MDX element at a deeper level, consider two
     * OLAP concepts that sound similar to visibility but have different
     * semantics:
     *
     *
     * Member#isHidden Hidden members in ragged hierarchies;
     * org.olap4j.OlapConnection#getRoleName Access control
     *
     *
     * @return Whether this element is visible
     */
    boolean isVisible();
}

// End MetadataElement.java
