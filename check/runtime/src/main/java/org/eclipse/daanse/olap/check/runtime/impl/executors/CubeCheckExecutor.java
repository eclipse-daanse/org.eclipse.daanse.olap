/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.check.runtime.impl.executors;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.CatalogReader;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.check.model.check.AttributeCheckResult;
import org.eclipse.daanse.olap.check.model.check.CheckStatus;
import org.eclipse.daanse.olap.check.model.check.CubeAttribute;
import org.eclipse.daanse.olap.check.model.check.CubeAttributeCheck;
import org.eclipse.daanse.olap.check.model.check.CubeCheck;
import org.eclipse.daanse.olap.check.model.check.CubeCheckResult;
import org.eclipse.daanse.olap.check.model.check.DimensionCheck;
import org.eclipse.daanse.olap.check.model.check.DimensionCheckResult;
import org.eclipse.daanse.olap.check.model.check.DrillThroughActionCheck;
import org.eclipse.daanse.olap.check.model.check.DrillThroughActionCheckResult;
import org.eclipse.daanse.olap.check.model.check.KPICheck;
import org.eclipse.daanse.olap.check.model.check.KPICheckResult;
import org.eclipse.daanse.olap.check.model.check.MeasureCheck;
import org.eclipse.daanse.olap.check.model.check.MeasureCheckResult;
import org.eclipse.daanse.olap.check.model.check.NamedSetCheck;
import org.eclipse.daanse.olap.check.model.check.NamedSetCheckResult;
import org.eclipse.daanse.olap.check.model.check.OlapCheckFactory;

/**
 * Executor for CubeCheck that verifies cube existence and structure.
 */
public class CubeCheckExecutor {

    private final CubeCheck check;
    private final List<Cube> cubes;
    private final CatalogReader catalogReader;
    private final Connection connection;
    private final OlapCheckFactory factory;

    public CubeCheckExecutor(CubeCheck check, List<Cube> cubes, CatalogReader catalogReader, Connection connection,
            OlapCheckFactory factory) {
        this.check = check;
        this.cubes = cubes;
        this.catalogReader = catalogReader;
        this.connection = connection;
        this.factory = factory;
    }

    public CubeCheckResult execute() {
        long startTime = System.currentTimeMillis();
        Date start = new Date();

        CubeCheckResult result = factory.createCubeCheckResult();
        result.setCheckName(check.getName());
        result.setCheckDescription(check.getDescription());
        result.setCubeName(check.getCubeName());
        result.setStartTime(start);
        result.setSourceCheck(check);

        try {
            // Find the cube
            Optional<Cube> foundCube = findCube();

            if (foundCube.isEmpty()) {
                result.setStatus(CheckStatus.FAILURE);
                result.setEndTime(new Date());
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }

            Cube cube = foundCube.get();
            result.setCubeUniqueName(cube.getUniqueName());
            result.setStatus(CheckStatus.SUCCESS);

            // Execute attribute checks
            for (CubeAttributeCheck attrCheck : check.getCubeAttributeChecks()) {
                AttributeCheckResult attrResult = executeAttributeCheck(attrCheck, cube);
                result.getAttributeResults().add(attrResult);
                if (attrResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute dimension checks
            List<Dimension> dimensions = catalogReader.getCubeDimensions(cube);
            for (DimensionCheck dimensionCheck : check.getDimensionChecks()) {
                if (!dimensionCheck.isEnabled()) {
                    continue;
                }

                DimensionCheckExecutor dimExecutor = new DimensionCheckExecutor(dimensionCheck, dimensions, cube,
                        catalogReader, connection, factory);
                DimensionCheckResult dimResult = dimExecutor.execute();
                result.getDimensionResults().add(dimResult);

                if (dimResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute measure checks
            List<Member> measures = cube.getMeasures();
            for (MeasureCheck measureCheck : check.getMeasureChecks()) {
                if (!measureCheck.isEnabled()) {
                    continue;
                }

                MeasureCheckExecutor measureExecutor = new MeasureCheckExecutor(measureCheck, measures, cube,
                        catalogReader, connection, factory);
                MeasureCheckResult measureResult = measureExecutor.execute();
                result.getMeasureResults().add(measureResult);

                if (measureResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute KPI checks
            for (KPICheck kpiCheck : check.getKpiChecks()) {
                if (!kpiCheck.isEnabled()) {
                    continue;
                }

                KPICheckExecutor kpiExecutor = new KPICheckExecutor(kpiCheck, cube.getKPIs(), factory);
                KPICheckResult kpiResult = kpiExecutor.execute();
                result.getKpiResults().add(kpiResult);

                if (kpiResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute NamedSet checks
            for (NamedSetCheck namedSetCheck : check.getNamedSetChecks()) {
                if (!namedSetCheck.isEnabled()) {
                    continue;
                }

                NamedSetCheckExecutor namedSetExecutor = new NamedSetCheckExecutor(namedSetCheck,
                        Arrays.asList(cube.getNamedSets()), factory);
                NamedSetCheckResult namedSetResult = namedSetExecutor.execute();
                result.getNamedSetResults().add(namedSetResult);

                if (namedSetResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

            // Execute DrillThroughAction checks
            for (DrillThroughActionCheck dtaCheck : check.getDrillThroughActionChecks()) {
                if (!dtaCheck.isEnabled()) {
                    continue;
                }

                DrillThroughActionCheckExecutor dtaExecutor = new DrillThroughActionCheckExecutor(dtaCheck,
                        cube.getDrillThroughActions(), factory);
                DrillThroughActionCheckResult dtaResult = dtaExecutor.execute();
                result.getDrillThroughActionResults().add(dtaResult);

                if (dtaResult.getStatus() == CheckStatus.FAILURE) {
                    result.setStatus(CheckStatus.FAILURE);
                }
            }

        } catch (Exception e) {
            result.setStatus(CheckStatus.FAILURE);
        }

        result.setEndTime(new Date());
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        return result;
    }

    private Optional<Cube> findCube() {
        String cubeName = check.getCubeName();
        String cubeUniqueName = check.getCubeUniqueName();

        return cubes.stream().filter(c -> {
            if (cubeUniqueName != null && !cubeUniqueName.isEmpty()) {
                return cubeUniqueName.equals(c.getUniqueName());
            }
            return cubeName != null && cubeName.equals(c.getName());
        }).findFirst();
    }

    private AttributeCheckResult executeAttributeCheck(CubeAttributeCheck attrCheck, Cube cube) {
        AttributeCheckResult result = factory.createAttributeCheckResult();
        result.setCheckName(attrCheck.getName());
        result.setAttributeName(attrCheck.getAttributeType().getName());
        result.setExpectedValue(attrCheck.getExpectedValue());

        String actualValue = getCubeAttributeValue(cube, attrCheck.getAttributeType());
        result.setActualValue(actualValue);

        boolean matches;
        if (attrCheck.getAttributeType() == CubeAttribute.VISIBLE) {
            // Boolean comparison
            Boolean expectedBool = attrCheck.getExpectedBoolean();
            Boolean actualBool = cube.isVisible();
            matches = AttributeCheckHelper.compareBooleans(expectedBool, actualBool);
        } else {
            // String comparison
            matches = AttributeCheckHelper.compareValues(attrCheck.getExpectedValue(), actualValue,
                    attrCheck.getMatchMode(), attrCheck.isCaseSensitive());
        }

        result.setStatus(matches ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        return result;
    }

    private String getCubeAttributeValue(Cube cube, CubeAttribute attributeType) {
        return switch (attributeType) {
        case NAME -> cube.getName();
        case UNIQUE_NAME -> cube.getUniqueName();
        case CAPTION -> cube.getCaption();
        case DESCRIPTION -> cube.getDescription();
        case VISIBLE -> String.valueOf(cube.isVisible());
        case CUBE_TYPE -> cube.getClass().getSimpleName();
        };
    }
}
