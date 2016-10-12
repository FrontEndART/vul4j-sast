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

package de.tsystems.mms.apm.performancesignature.dynatrace;

import de.tsystems.mms.apm.performancesignature.dynatrace.configuration.CredProfilePair;
import de.tsystems.mms.apm.performancesignature.dynatrace.configuration.DynatraceServerConfiguration;
import de.tsystems.mms.apm.performancesignature.dynatrace.configuration.GenericTestCase;
import de.tsystems.mms.apm.performancesignature.dynatrace.rest.CommandExecutionException;
import de.tsystems.mms.apm.performancesignature.dynatrace.rest.DTServerConnection;
import de.tsystems.mms.apm.performancesignature.dynatrace.rest.RESTErrorException;
import de.tsystems.mms.apm.performancesignature.dynatrace.rest.model.BaseConfiguration;
import de.tsystems.mms.apm.performancesignature.dynatrace.rest.model.SystemProfile;
import de.tsystems.mms.apm.performancesignature.util.PerfSigUtils;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Failure;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public class PerfSigStartRecording extends Builder implements SimpleBuildStep {
    private final String dynatraceProfile, testCase;
    private String recordingOption;
    private boolean lockSession;

    @DataBoundConstructor
    public PerfSigStartRecording(final String dynatraceProfile, final String testCase) {
        this.dynatraceProfile = dynatraceProfile;
        this.testCase = StringUtils.deleteWhitespace(testCase);
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> run, @Nonnull final FilePath workspace, @Nonnull final Launcher launcher, @Nonnull final TaskListener listener)
            throws InterruptedException, IOException {
        final PrintStream logger = listener.getLogger();

        DynatraceServerConfiguration serverConfiguration = PerfSigUtils.getServerConfiguration(dynatraceProfile);
        if (serverConfiguration == null) {
            throw new AbortException("failed to lookup Dynatrace server configuration");
        }

        CredProfilePair pair = serverConfiguration.getCredProfilePair(dynatraceProfile);
        if (pair == null) {
            throw new AbortException("failed to lookup Dynatrace server profile");
        }

        logger.println("starting session recording ...");
        final DTServerConnection connection = new DTServerConnection(serverConfiguration, pair);
        if (!connection.validateConnection()) {
            throw new RESTErrorException(Messages.PerfSigRecorder_DTConnectionError());
        }

        final String testCase = run.getEnvironment(listener).expand(this.testCase);
        String sessionName = pair.getProfile() + "_" + run.getParent().getName() + "_Build-" + run.getNumber() + "_" + testCase;
        sessionName = sessionName.replace("/", "_");

        for (BaseConfiguration profile : connection.getSystemProfiles()) {
            SystemProfile systemProfile = (SystemProfile) profile;
            if (pair.getProfile().equals(systemProfile.getId()) && systemProfile.isRecording()) {
                logger.println("another session is still recording, trying to stop recording");
                PerfSigStopRecording stopRecording = new PerfSigStopRecording(dynatraceProfile);
                stopRecording.perform(run, workspace, launcher, listener);
                break;
            }
        }

        String result = null;
        Date timeframeStart = null;

        try {
            result = connection.startRecording(sessionName, "This session is triggered by Jenkins", getRecordingOption(), lockSession, false);
        } catch (CommandExecutionException e) {
            if (e.getMessage().contains("continuous")) {
                timeframeStart = new Date();
            } else throw e;
        }
        if ((result != null && result.contains(sessionName)) || timeframeStart != null) {
            logger.println(String.format(Messages.PerfSigStartRecording_StartedSessionRecording(), pair.getProfile(), sessionName));
        } else {
            throw new RESTErrorException(String.format(Messages.PerfSigStartRecording_SessionRecordingError(), pair.getProfile()));
        }

        logger.println("registering new TestRun");
        String testRunId = connection.registerTestRun(run.getNumber());
        if (testRunId != null) {
            logger.println(String.format(Messages.PerfSigStartRecording_StartedTestRun(), pair.getProfile(), testRunId));
            logger.println("Dynatrace: registered test run " + testRunId + "" +
                    " (available as environment variables " + PerfSigEnvContributor.TESTRUN_ID_KEY +
                    " and " + PerfSigEnvContributor.SESSIONCOUNT + ")");
        } else {
            logger.println("warning: could not register TestRun");
        }

        run.addAction(new PerfSigEnvInvisAction(result, timeframeStart, testCase, testRunId));
    }

    public String getTestCase() {
        return testCase;
    }

    public String getRecordingOption() {
        return recordingOption == null ? DescriptorImpl.defaultRecordingOption : recordingOption;
    }

    @DataBoundSetter
    public void setRecordingOption(final String recordingOption) {
        this.recordingOption = recordingOption;
    }

    public boolean isLockSession() {
        return lockSession;
    }

    @DataBoundSetter
    public void setLockSession(final boolean lockSession) {
        this.lockSession = lockSession;
    }

    public String getDynatraceProfile() {
        return dynatraceProfile;
    }

    @Symbol("startSession")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public static final boolean defaultLockSession = false;
        public static final String defaultRecordingOption = "all";

        public ListBoxModel doFillRecordingOptionItems() {
            return new ListBoxModel(new ListBoxModel.Option("all"), new ListBoxModel.Option("violations"), new ListBoxModel.Option("timeseries"));
        }

        public FormValidation doCheckTestCase(@QueryParameter final String testCase) {
            try {
                Jenkins.checkGoodName(testCase);
                GenericTestCase.DescriptorImpl.addTestCases(testCase);
                return FormValidation.ok();
            } catch (Failure e) {
                return FormValidation.error(e.getMessage());
            }
        }

        public ListBoxModel doFillDynatraceProfileItems() {
            return PerfSigUtils.listToListBoxModel(PerfSigUtils.getDTConfigurations());
        }

        public boolean isApplicable(final Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return Messages.PerfSigStartRecording_DisplayName();
        }
    }
}
