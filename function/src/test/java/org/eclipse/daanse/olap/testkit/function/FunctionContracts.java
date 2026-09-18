/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.testkit.function;

import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import org.eclipse.daanse.olap.function.def.vba.abs.AbsContract;
import org.eclipse.daanse.olap.function.def.set.addcalculatedmembers.AddCalculatedMembersContract;
import org.eclipse.daanse.olap.function.def.set.hierarchy.AllMembersContract;
import org.eclipse.daanse.olap.function.def.ancestor.AncestorContract;
import org.eclipse.daanse.olap.function.def.member.AncestorsContract;
import org.eclipse.daanse.olap.function.def.as.AsContract;
import org.eclipse.daanse.olap.function.def.set.ascendants.AscendantsContract;
import org.eclipse.daanse.olap.function.def.topbottomcount.BottomCountContract;
import org.eclipse.daanse.olap.function.def.topbottompercentsum.BottomPercentContract;
import org.eclipse.daanse.olap.function.def.topbottompercentsum.BottomSumContract;
import org.eclipse.daanse.olap.function.def.caption.CaptionContract;
import org.eclipse.daanse.olap.function.def.set.children.ChildrenContract;
import org.eclipse.daanse.olap.function.def.openingclosingperiod.ClosingPeriodContract;
import org.eclipse.daanse.olap.function.def.member.cousin.CousinContract;
import org.eclipse.daanse.olap.function.def.crossjoin.CrossjoinContract;
import org.eclipse.daanse.olap.function.def.hierarchy.member.CurrentContract;
import org.eclipse.daanse.olap.function.def.hierarchy.member.CurrentMemberContract;
import org.eclipse.daanse.olap.function.def.member.namedsetcurrentordinal.CurrentOrdinalContract;
import org.eclipse.daanse.olap.function.def.member.datamember.DataMemberContract;
import org.eclipse.daanse.olap.function.def.member.defaultmember.DefaultMemberContract;
import org.eclipse.daanse.olap.function.def.descendants.DescendantsContract;
import org.eclipse.daanse.olap.function.def.dimension.dimension.DimensionContract;
import org.eclipse.daanse.olap.function.def.dimensions.numeric.DimensionsContract;
import org.eclipse.daanse.olap.function.def.set.distinct.DistinctContract;
import org.eclipse.daanse.olap.function.def.drilldownleveltopbottom.DrilldownLevelBottomContract;
import org.eclipse.daanse.olap.function.def.drilldownlevel.DrilldownLevelContract;
import org.eclipse.daanse.olap.function.def.drilldownleveltopbottom.DrilldownLevelTopContract;
import org.eclipse.daanse.olap.function.def.drilldownmember.DrilldownMemberContract;
import org.eclipse.daanse.olap.function.def.except.ExceptContract;
import org.eclipse.daanse.olap.function.def.set.existing.ExistingContract;
import org.eclipse.daanse.olap.function.def.exists.ExistsContract;
import org.eclipse.daanse.olap.function.def.set.extract.ExtractContract;
import org.eclipse.daanse.olap.function.def.set.filter.FilterContract;
import org.eclipse.daanse.olap.function.def.member.firstchild.FirstChildContract;
import org.eclipse.daanse.olap.function.def.member.firstsibling.FirstSiblingContract;
import org.eclipse.daanse.olap.function.def.format.FormatContract;
import org.eclipse.daanse.olap.function.def.generate.GenerateContract;
import org.eclipse.daanse.olap.function.def.headtail.HeadContract;
import org.eclipse.daanse.olap.function.def.hierarchize.HierarchizeContract;
import org.eclipse.daanse.olap.function.def.hierarchy.level.HierarchyContract;
import org.eclipse.daanse.olap.function.def.set.setitem.ItemContract;
import org.eclipse.daanse.olap.function.def.kpi.KPICurrentTimeMemberContract;
import org.eclipse.daanse.olap.function.def.kpi.KPIGoalContract;
import org.eclipse.daanse.olap.function.def.kpi.KPIStatusContract;
import org.eclipse.daanse.olap.function.def.kpi.KPITrendContract;
import org.eclipse.daanse.olap.function.def.kpi.KPIValueContract;
import org.eclipse.daanse.olap.function.def.kpi.KPIWeightContract;
import org.eclipse.daanse.olap.function.def.leadlag.LagContract;
import org.eclipse.daanse.olap.function.def.member.lastchild.LastChildContract;
import org.eclipse.daanse.olap.function.def.lastperiods.LastPeriodsContract;
import org.eclipse.daanse.olap.function.def.member.lastsibling.LastSiblingContract;
import org.eclipse.daanse.olap.function.def.leadlag.LeadContract;
import org.eclipse.daanse.olap.function.def.string.LenContract;
import org.eclipse.daanse.olap.function.def.level.member.LevelContract;
import org.eclipse.daanse.olap.function.def.level.numeric.LevelNumberContract;
import org.eclipse.daanse.olap.function.def.operators.minus.MinusContract;
import org.eclipse.daanse.olap.function.def.periodstodate.xtd.MtdContract;
import org.eclipse.daanse.olap.function.def.nativizeset.NativizeSetContract;
import org.eclipse.daanse.olap.function.def.nonempty.NonEmptyContract;
import org.eclipse.daanse.olap.function.def.nonemptycrossjoin.NonEmptyCrossJoinContract;
import org.eclipse.daanse.olap.function.def.order.OrderContract;
import org.eclipse.daanse.olap.function.def.numeric.ordinal.OrdinalContract;
import org.eclipse.daanse.olap.function.def.parameter.ParamRefContract;
import org.eclipse.daanse.olap.function.def.parameter.ParameterContract;
import org.eclipse.daanse.olap.function.def.periodstodate.PeriodsToDateContract;
import org.eclipse.daanse.olap.function.def.periodstodate.xtd.QtdContract;
import org.eclipse.daanse.olap.function.def.set.range.RangeContract;
import org.eclipse.daanse.olap.function.def.settostr.SetToStrContract;
import org.eclipse.daanse.olap.function.def.set.siblings.SiblingsContract;
import org.eclipse.daanse.olap.function.def.crossjoin.StarContract;
import org.eclipse.daanse.olap.function.def.vba.str.StrContract;
import org.eclipse.daanse.olap.function.def.set.stripcalculatedmembers.StripCalculatedMembersContract;
import org.eclipse.daanse.olap.function.def.subset.SubsetContract;
import org.eclipse.daanse.olap.function.def.sum.SumContract;
import org.eclipse.daanse.olap.function.def.headtail.TailContract;
import org.eclipse.daanse.olap.function.def.toggledrillstate.ToggleDrillStateContract;
import org.eclipse.daanse.olap.function.def.topbottomcount.TopCountContract;
import org.eclipse.daanse.olap.function.def.topbottompercentsum.TopPercentContract;
import org.eclipse.daanse.olap.function.def.topbottompercentsum.TopSumContract;
import org.eclipse.daanse.olap.function.def.string.UCaseContract;
import org.eclipse.daanse.olap.function.def.union.UnionContract;
import org.eclipse.daanse.olap.function.def.unorder.UnorderContract;
import org.eclipse.daanse.olap.function.def.periodstodate.xtd.WtdContract;
import org.eclipse.daanse.olap.function.def.periodstodate.xtd.YtdContract;
import java.util.List;


/**
 * The enumeration of all contracts. Hand-maintained, exactly like
 * {@code StandardFunctions.standard()} — and for the same reason: subclasses cannot be
 * enumerated without classpath scanning, and there is no scanner in the build.
 */
public final class FunctionContracts {

    private FunctionContracts() {
    }

    public static List<FunctionContract> all() {
        return List.of(
                AbsContract.CONTRACT,
                AddCalculatedMembersContract.CONTRACT,
                AllMembersContract.CONTRACT,
                AncestorContract.CONTRACT,
                AncestorsContract.CONTRACT,
                AsContract.CONTRACT,
                AscendantsContract.CONTRACT,
                BottomCountContract.CONTRACT,
                BottomPercentContract.CONTRACT,
                BottomSumContract.CONTRACT,
                CaptionContract.CONTRACT,
                ChildrenContract.CONTRACT,
                ClosingPeriodContract.CONTRACT,
                CousinContract.CONTRACT,
                CrossjoinContract.CONTRACT,
                CurrentContract.CONTRACT,
                CurrentMemberContract.CONTRACT,
                CurrentOrdinalContract.CONTRACT,
                DataMemberContract.CONTRACT,
                DefaultMemberContract.CONTRACT,
                DescendantsContract.CONTRACT,
                DimensionContract.CONTRACT,
                DimensionsContract.CONTRACT,
                DistinctContract.CONTRACT,
                DrilldownLevelBottomContract.CONTRACT,
                DrilldownLevelContract.CONTRACT,
                DrilldownLevelTopContract.CONTRACT,
                DrilldownMemberContract.CONTRACT,
                ExceptContract.CONTRACT,
                ExistingContract.CONTRACT,
                ExistsContract.CONTRACT,
                ExtractContract.CONTRACT,
                FilterContract.CONTRACT,
                FirstChildContract.CONTRACT,
                FirstSiblingContract.CONTRACT,
                FormatContract.CONTRACT,
                GenerateContract.CONTRACT,
                HeadContract.CONTRACT,
                HierarchizeContract.CONTRACT,
                HierarchyContract.CONTRACT,
                ItemContract.CONTRACT,
                KPICurrentTimeMemberContract.CONTRACT,
                KPIGoalContract.CONTRACT,
                KPIStatusContract.CONTRACT,
                KPITrendContract.CONTRACT,
                KPIValueContract.CONTRACT,
                KPIWeightContract.CONTRACT,
                LagContract.CONTRACT,
                LastChildContract.CONTRACT,
                LastPeriodsContract.CONTRACT,
                LastSiblingContract.CONTRACT,
                LeadContract.CONTRACT,
                LenContract.CONTRACT,
                LevelContract.CONTRACT,
                LevelNumberContract.CONTRACT,
                MinusContract.CONTRACT,
                MtdContract.CONTRACT,
                NativizeSetContract.CONTRACT,
                NonEmptyContract.CONTRACT,
                NonEmptyCrossJoinContract.CONTRACT,
                OrderContract.CONTRACT,
                OrdinalContract.CONTRACT,
                ParamRefContract.CONTRACT,
                ParameterContract.CONTRACT,
                PeriodsToDateContract.CONTRACT,
                QtdContract.CONTRACT,
                RangeContract.CONTRACT,
                SetToStrContract.CONTRACT,
                SiblingsContract.CONTRACT,
                StarContract.CONTRACT,
                StrContract.CONTRACT,
                StripCalculatedMembersContract.CONTRACT,
                SubsetContract.CONTRACT,
                SumContract.CONTRACT,
                TailContract.CONTRACT,
                ToggleDrillStateContract.CONTRACT,
                TopCountContract.CONTRACT,
                TopPercentContract.CONTRACT,
                TopSumContract.CONTRACT,
                UCaseContract.CONTRACT,
                UnionContract.CONTRACT,
                UnorderContract.CONTRACT,
                WtdContract.CONTRACT,
                YtdContract.CONTRACT
                // ... one line per function
        );
    }
}