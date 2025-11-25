/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena, Stefan Bischof - initial
 *
 */
package org.eclipse.daanse.olap.common;

import java.io.File;
/**
 * Configuration properties that determine the
 * behavior of a mondrian instance.
 *
 * There is a method for property valid in a
 * mondrian.properties file. Although it is possible to retrieve
 * properties using the inherited {@link java.util.Properties#getProperty(String)}
 * method, we recommend that you use methods in this class.
 */
public class SystemWideProperties extends DaansePropertiesBase {
    /**
     * Properties, drawn from {@link System#getProperties},
     * plus the contents of "mondrian.properties" if it
     * exists. A singleton.
     */
    private static final SystemWideProperties instance =
        new SystemWideProperties();

    private SystemWideProperties() {
        super(
            new FilePropertySource(
                new File(MONDRIAN_DOT_PROPERTIES)));
        populate();
        populateInitial();
    }

    public void populateInitial() {
        CaseSensitive =
            this.getBoolean("daanse.olap.case.sensitive", false);
        CaseSensitiveMdxInstr =
            this.getBoolean("daanse.olap.case.sensitive.CaseSensitiveMdxInstr", false);
        CompareSiblingsByOrderKey =
            this.getBoolean("daanse.rolap.compareSiblingsByOrderKey", false);
        EnableExpCache =
            this.getBoolean("daanse.expCache.enable", true);
        EnableNativeNonEmpty =
            this.getBoolean("daanse.native.nonempty.enable", true);
        EnableNonEmptyOnAllAxis =
            this.getBoolean("daanse.rolap.nonempty", false);
        EnableRolapCubeMemberCache =
            this.getBoolean("daanse.rolap.EnableRolapCubeMemberCache", true);
        EnableTriggers =
            this.getBoolean("daanse.olap.triggers.enable", true);
        FilterChildlessSnowflakeMembers =
            this.getBoolean("daanse.rolap.FilterChildlessSnowflakeMembers", true);
        MaxConstraints =
            this.getInteger("daanse.rolap.maxConstraints", 1000);
        NullMemberRepresentation =
            getProperty("daanse.olap.NullMemberRepresentation", "#null");
        ResultLimit =
            this.getInteger("daanse.result.limit", 0);

    }

    /**
     * Returns the singleton.
     *
     * @return Singleton instance
     */
    public static SystemWideProperties instance() {
        // NOTE: We used to instantiate on demand, but
        // synchronization overhead was significant. See
        // MONDRIAN-978.
        return instance;
    }



    /**
     * Boolean property that controls whether the MDX parser resolves uses
     * case-sensitive matching when looking up identifiers. The default is
     * false.
     */
    @PropertyAnnotation(path = "daanse.olap.case.sensitive")
    public transient Boolean CaseSensitive;


    /**
     *
     *                 If true, then MDX functions InStr and InStrRev are case sensitive.
     *                 Default value is false.
     *
     */
    @PropertyAnnotation(path = "daanse.olap.case.sensitive.CaseSensitiveMdxInstr")
    public transient Boolean CaseSensitiveMdxInstr;


    /**
     * Boolean property that controls whether sibling members are
     * compared according to order key value fetched from their ordinal
     * expression.  The default is false (only database ORDER BY is used).
     */
    @PropertyAnnotation(path = "daanse.rolap.compareSiblingsByOrderKey")
    public transient Boolean CompareSiblingsByOrderKey;


    /**
     * Boolean property that controls whether to use a cache for frequently
     * evaluated expressions. With the cache disabled, an expression like
     * Rank([Product].CurrentMember,
     * Order([Product].MEMBERS, [Measures].[Unit Sales])) would perform
     * many redundant sorts. The default is true.
     */
    @PropertyAnnotation(path = "daanse.expCache.enable")
    public transient Boolean EnableExpCache;

    /**
     * If enabled some NON EMPTY set operations like member.children,
     * level.members and member descendants will be computed in SQL.
     */
    @PropertyAnnotation(path = "daanse.native.nonempty.enable")
    public transient Boolean EnableNativeNonEmpty;
    /**
     * Boolean property that controls whether each query axis implicit has the
     * NON EMPTY option set. The default is false.
     */
    @PropertyAnnotation(path = "")
    public transient Boolean EnableNonEmptyOnAllAxis;

    /**
     * Property that determines whether to cache RolapCubeMember objects,
     * each of which associates a member of a shared hierarchy with a
     * particular cube in which it is being used.
     *
     * The default is {@code true}, that is, use a cache. If you wish to use
     * the member cache control aspects of {@link org.eclipse.daanse.olap.api.CacheControl},
     * you must set this property to {@code false}.
     *
     * RolapCubeMember has recently become more lightweight to
     * construct, and we may obsolete this cache and this
     * property.
     */
    @PropertyAnnotation(path = "daanse.rolap.EnableRolapCubeMemberCache")
    public transient Boolean EnableRolapCubeMemberCache;


    /**
     * Boolean property that controls whether to notify the Mondrian system
     * when a {@link SystemWideProperties property value} changes.
     *
     * This allows objects dependent on Mondrian properties to react (that
     * is, reload), when a given property changes via, say,
     * MondrianProperties.instance().populate(null) or
     * MondrianProperties.instance().QueryLimit.set(50).
     */
    @PropertyAnnotation(path = "daanse.olap.triggers.enable")
    public transient Boolean EnableTriggers;



    /**
     * Property that defines
     * whether to generate joins to filter out members in a snowflake
     * dimension that do not have any children.
     *
     * If true (the default), some queries to query members of high
     * levels snowflake dimensions will be more expensive. If false, and if
     * there are rows in an outer snowflake table that are not referenced by
     * a row in an inner snowflake table, then some queries will return members
     * that have no children.
     *
     * Our recommendation, for best performance, is to remove rows outer
     * snowflake tables are not referenced by any row in an inner snowflake
     * table, during your ETL process, and to set this property to
     * {@code false}.
     */
    @PropertyAnnotation(path = "daanse.rolap.FilterChildlessSnowflakeMembers")
    public transient Boolean FilterChildlessSnowflakeMembers;

    /**
     * Max number of constraints in a single 'IN' SQL clause.
     *
     * This value may be variant among database products and their runtime
     * settings. Oracle, for example, gives the error "ORA-01795: maximum
     * number of expressions in a list is 1000".
     *
     * Recommended values:
     *
     * Oracle: 1,000
     * DB2: 2,500
     * Other: 10,000
     *
     */
    @PropertyAnnotation(path = "daanse.rolap.maxConstraints")
    public transient Integer MaxConstraints;


    /**
     * Property that determines how a null member value is represented in the
     * result output.
     * AS 2000 shows this as empty value
     * AS 2005 shows this as "(null)" value
     */
    @PropertyAnnotation(path = "mondrian.olap.NullMemberRepresentation")
    public transient String NullMemberRepresentation;



    /**
     * Integer property that, if set to a value greater than zero, limits the
     * maximum size of a result set.
     */
    @PropertyAnnotation(path = "daanse.result.limit")
    public transient Integer ResultLimit;

}

// End MondrianProperties.java
