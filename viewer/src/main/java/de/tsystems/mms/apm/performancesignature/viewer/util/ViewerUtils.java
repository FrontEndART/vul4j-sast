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

package de.tsystems.mms.apm.performancesignature.viewer.util;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import de.tsystems.mms.apm.performancesignature.util.PerfSigUIUtils;
import de.tsystems.mms.apm.performancesignature.viewer.ViewerRecorder;
import de.tsystems.mms.apm.performancesignature.viewer.model.CredJobPair;
import de.tsystems.mms.apm.performancesignature.viewer.model.JenkinsServerConfiguration;
import hudson.FilePath;
import hudson.model.Run;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class ViewerUtils {
    private ViewerUtils() {
    }

    public static ListBoxModel listToListBoxModel(final List<?> arrayList) {
        final ListBoxModel listBoxModel = new ListBoxModel();
        for (Object item : arrayList) {
            if (item instanceof String)
                listBoxModel.add((String) item);
            else if (item instanceof JenkinsServerConfiguration) {
                JenkinsServerConfiguration conf = (JenkinsServerConfiguration) item;
                if (CollectionUtils.isNotEmpty(conf.getCredJobPairs()))
                    for (CredJobPair credJobPair : conf.getCredJobPairs()) {
                        String listItem = credJobPair.getJenkinsJob() + " (" + credJobPair.getCredentials().getUsername() + ") @ " +
                                conf.getName();
                        listBoxModel.add(listItem);
                    }
            }
        }
        return listBoxModel;
    }

    public static FilePath getReportDirectory(final Run<?, ?> run) throws IOException {
        File reportDirectory = new File(run.getRootDir(), Messages.PerfSigUtils_ReportDirectory());
        if (!reportDirectory.exists()) {
            if (!reportDirectory.mkdirs()) throw new IOException("failed to create report directory");
        }
        return new FilePath(reportDirectory);
    }

    public static List<JenkinsServerConfiguration> getJenkinsConfigurations() {
        return PerfSigUIUtils.getInstance().getDescriptorByType(ViewerRecorder.DescriptorImpl.class).getConfigurations();
    }

    public static JenkinsServerConfiguration getServerConfiguration(final String jenkinsServer) {
        for (JenkinsServerConfiguration serverConfiguration : getJenkinsConfigurations()) {
            String strippedName = jenkinsServer.replaceAll(".*@", "").trim();
            if (strippedName.equals(serverConfiguration.getName())) {
                return serverConfiguration;
            }
        }
        return null;
    }

    public static UsernamePasswordCredentials getCredentials(final String credsId) {
        return (credsId == null) ? null : CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(UsernamePasswordCredentials.class, PerfSigUIUtils.getInstance(), ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()), CredentialsMatchers.withId(credsId));
    }

    public static boolean checkNotNullOrEmpty(final String string) {
        return StringUtils.isNotBlank(string);
    }

    public static boolean checkNotEmptyAndIsNumber(final String number) {
        return StringUtils.isNotBlank(number) && NumberUtils.isNumber(number);
    }
}
