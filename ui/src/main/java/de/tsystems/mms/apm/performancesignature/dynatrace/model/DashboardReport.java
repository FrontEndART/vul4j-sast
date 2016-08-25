/*
 * Copyright (c) 2014 T-Systems Multimedia Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tsystems.mms.apm.performancesignature.dynatrace.model;

import de.tsystems.mms.apm.performancesignature.model.ClientLinkGenerator;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.List;

@ExportedBean
public class DashboardReport {
    private final String name;
    private final List<ChartDashlet> chartDashlets;
    private final List<IncidentChart> incidents;
    private boolean unitTest;
    private ClientLinkGenerator clientLink;

    public DashboardReport(final String testCaseName) {
        this.name = testCaseName;
        this.chartDashlets = new ArrayList<ChartDashlet>();
        this.incidents = new ArrayList<IncidentChart>();
    }

    @Exported
    public List<IncidentChart> getIncidents() {
        return incidents;
    }

    public void addIncident(final IncidentChart incident) {
        this.incidents.add(incident);
    }

    @Exported
    public List<ChartDashlet> getChartDashlets() {
        return chartDashlets;
    }

    public void addChartDashlet(final ChartDashlet chartDashlet) {
        this.chartDashlets.add(chartDashlet);
    }

    @Exported
    public String getName() {
        return name;
    }

    @Exported
    public boolean isUnitTest() {
        return unitTest;
    }

    public void setUnitTest(final boolean unitTest) {
        this.unitTest = unitTest;
    }

    public ClientLinkGenerator getClientLink() {
        return clientLink;
    }

    public void setClientLink(final ClientLinkGenerator clientLink) {
        this.clientLink = clientLink;
    }

    public Measure getMeasure(final String chartDashlet, final String measure) {
        for (ChartDashlet cd : this.chartDashlets) {
            if (cd.getName().equalsIgnoreCase(chartDashlet) && cd.getMeasures() != null) {
                for (Measure m : cd.getMeasures()) {
                    if (m.getName().equalsIgnoreCase(measure))
                        return m;
                }
            }
        }
        return null;
    }
}
