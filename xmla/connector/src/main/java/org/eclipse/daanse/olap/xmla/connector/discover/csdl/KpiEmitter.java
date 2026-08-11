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

import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.KPI;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.BiFactory;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.KpiGoalType;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.KpiStatusType;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.KpiTrendType;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TDocumentation;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TKpi;
import org.eclipse.daanse.xmla.model.csdl.v2.bi.TMeasure;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.EdmFactory;
import org.eclipse.daanse.xmla.model.csdl.v2.edm.TEntityProperty;

/**
 * The KPIs of a cube as bi:Kpi elements on the measures they belong to.
 * <p>
 * CSDLBI requires both a goal and a status, and it allows only one bi:Kpi per
 * measure - so a KPI that has neither stays a plain measure, and a second KPI
 * on the same measure gets a hidden carrier of its own.
 */
public final class KpiEmitter {

    private static final String DEFAULT_STATUS_GRAPHIC = "Three Circles Colored";

    private final BiFactory bi = BiFactory.eINSTANCE;
    private final EdmFactory edm = EdmFactory.eINSTANCE;
    private final EmitContext ctx;
    private final MeasuresEntity measures;
    private final DisplayFolderBuilder folders;

    public KpiEmitter(EmitContext ctx, MeasuresEntity measures, DisplayFolderBuilder folders) {
        super();
        this.ctx = ctx;
        this.measures = measures;
        this.folders = folders;
    }

    public void emitKpis(Cube cube) {
        for (KPI kpi : cube.getKPIs()) {
            emitKpi(kpi);
        }
    }

    private void emitKpi(KPI kpi) {
        Optional<TEntityProperty> valueProp = measures.resolveMeasureProperty(kpi.getValue());
        TEntityProperty anchor = valueProp.orElseGet(
                () -> emitSupportMeasure(ctx.mangle("v_" + kpi.getName() + "_Value"), "Value of KPI " + kpi.getName()));
        TMeasure anchorMeasure = (TMeasure) anchor.getBiMeasure();
        if (anchorMeasure.getKpi() != null) {
            ctx.warn("measure {} already carries a bi:Kpi, so KPI {} gets a hidden carrier of " + "its own",
                    anchor.getName(), kpi.getName());
            anchor = emitSupportMeasure(ctx.mangle("v_" + kpi.getName() + "_Value"), "Value of KPI " + kpi.getName());
            anchorMeasure = (TMeasure) anchor.getBiMeasure();
        }

        if (isBlank(kpi.getGoal()) || isBlank(kpi.getStatus())) {
            ctx.warn("KPI {} has no goal or status and CSDLBI requires both, so it stays a "
                    + "plain measure here; MDSCHEMA_KPIS still reports it in full", kpi.getName());
            return;
        }

        TKpi tKpi = bi.createTKpi();

        tKpi.setKpiGoal(goalOf(supportRef(anchor, "Goal")));
        tKpi.setKpiStatus(statusOf(supportRef(anchor, "Status")));
        if (!isBlank(kpi.getTrend())) {
            tKpi.setKpiTrend(trendOf(supportRef(anchor, "Trend")));
            if (!isBlank(kpi.getTrendGraphic())) {
                tKpi.setTrendGraphic(kpi.getTrendGraphic());
            }
        }

        tKpi.setStatusGraphic(isBlank(kpi.getStatusGraphic()) ? DEFAULT_STATUS_GRAPHIC : kpi.getStatusGraphic());

        if (!isBlank(kpi.getDescription())) {
            TDocumentation doc = bi.createTDocumentation();
            doc.setSummary(kpi.getDescription());
            tKpi.setDocumentation(doc);
            measures.setEdmDocumentation(anchor, kpi.getDescription());
        }

        folders.forProperty(measures.biEntityType(), kpi.getDisplayFolder(), anchor.getName());

        anchorMeasure.setKpi(tKpi);
    }

    private org.eclipse.daanse.xmla.model.csdl.v2.bi.TPropertyRef supportRef(TEntityProperty anchor, String role) {
        String name = ctx.uniquePropertyName("v_" + anchor.getName() + "_" + role);
        emitSupportMeasure(name, anchor.getName() + " " + role);
        org.eclipse.daanse.xmla.model.csdl.v2.bi.TPropertyRef ref = bi.createTPropertyRef();
        ref.setName(ctx.requireProperty(name));
        return ref;
    }

    private TEntityProperty emitSupportMeasure(String name, String referenceName) {
        TEntityProperty p = edm.createTEntityProperty();
        p.setName(name);
        p.setType("Double");
        TMeasure m = bi.createTMeasure();
        m.setHidden(true);
        m.setReferenceName(referenceName);
        p.setBiMeasure(m);
        measures.edmEntityType().getProperty().add(p);
        return p;
    }

    private KpiGoalType goalOf(org.eclipse.daanse.xmla.model.csdl.v2.bi.TPropertyRef r) {
        KpiGoalType g = bi.createKpiGoalType();
        g.setPropertyRef(r);
        return g;
    }

    private KpiStatusType statusOf(org.eclipse.daanse.xmla.model.csdl.v2.bi.TPropertyRef r) {
        KpiStatusType s = bi.createKpiStatusType();
        s.setPropertyRef(r);
        return s;
    }

    private KpiTrendType trendOf(org.eclipse.daanse.xmla.model.csdl.v2.bi.TPropertyRef r) {
        KpiTrendType t = bi.createKpiTrendType();
        t.setPropertyRef(r);
        return t;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}