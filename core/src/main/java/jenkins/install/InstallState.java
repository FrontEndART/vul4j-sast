/*
 * The MIT License
 *
 * Copyright (c) 2015, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.install;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.commons.lang.StringUtils;
/**
 * Jenkins install state.
 *
 * In order to hook into the setup wizard lifecycle, you should
 * include something in a script that call
 * to `onSetupWizardInitialized` with a callback, for example:
 * 
 * See <em><code>upgradeWizard.js</code></em> for an example
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class InstallState implements ExtensionPoint {

    /**
     * Only here for XStream compatibility. <p>
     * 
     * Please DO NOT ADD ITEM TO THIS LIST. <p>
     * If you add an item here, the deserialization process will break 
     * because it is used for serialized state like "jenkins.install.InstallState$4" 
     * before the change from anonymous class to named class. If you need to add a new InstallState, you can just add a new inner named class but nothing to change in this list.
     * 
     * @see #readResolve
     */
    @Deprecated
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private static final InstallState[] UNUSED_INNER_CLASSES = {
        new InstallState("UNKNOWN", false) {},
        new InstallState("INITIAL_SETUP_COMPLETED", false) {},
        new InstallState("CREATE_ADMIN_USER", false) {},
        new InstallState("INITIAL_SECURITY_SETUP", false) {},
        new InstallState("RESTART", false) {},
        new InstallState("DOWNGRADE", false) {},
    };

    /**
     * Need InstallState != NEW for tests by default
     */
    @Extension
    public static final InstallState UNKNOWN = new Unknown();
    private static class Unknown extends InstallState {
        Unknown() {
            super("UNKNOWN", true);
        }
        @Override
        public void initializeState() {
            InstallUtil.proceedToNextStateFrom(this);
        }
    }
    
    /**
     * After any setup / restart / etc. hooks are done, states should be running
     */
    @Extension
    public static final InstallState RUNNING = new InstallState("RUNNING", true);
    
    /**
     * The initial set up has been completed
     */
    @Extension
    public static final InstallState INITIAL_SETUP_COMPLETED = new InitialSetupCompleted();
    private static final class InitialSetupCompleted extends InstallState {
        InitialSetupCompleted() {
            super("INITIAL_SETUP_COMPLETED", true);
        }
        public void initializeState() {
            Jenkins j = Jenkins.get();
            try {
                j.getSetupWizard().completeSetup();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            j.setInstallState(RUNNING);
        }
    }
    
    /**
     * Creating an admin user for an initial Jenkins install.
     */
    @Extension
    public static final InstallState CREATE_ADMIN_USER = new CreateAdminUser();
    private static final class CreateAdminUser extends InstallState {
        CreateAdminUser() {
            super("CREATE_ADMIN_USER", false);
        }
        public void initializeState() {
            Jenkins j = Jenkins.get();
            // Skip this state if not using the security defaults
            // e.g. in an init script set up security already
            if (!j.getSetupWizard().isUsingSecurityDefaults()) {
                InstallUtil.proceedToNextStateFrom(this);
            }
        }
    }
    
    @Extension
    public static final InstallState CONFIGURE_INSTANCE = new ConfigureInstance();
    private static final class ConfigureInstance extends InstallState {
        ConfigureInstance() {
            super("CONFIGURE_INSTANCE", false);
        }
        public void initializeState() {
            // Skip this state if a boot script already configured the root URL
            // in case we add more fields in this page, this should be adapted
            if (StringUtils.isNotBlank(JenkinsLocationConfiguration.getOrDie().getUrl())) {
                InstallUtil.proceedToNextStateFrom(this);
            }
        }
    }
    
    /**
     * New Jenkins install. The user has kicked off the process of installing an
     * initial set of plugins (via the install wizard).
     */
    @Extension
    public static final InstallState INITIAL_PLUGINS_INSTALLING = new InstallState("INITIAL_PLUGINS_INSTALLING", false);

    /**
     * Security setup for a new Jenkins install.
     */
    @Extension
    public static final InstallState INITIAL_SECURITY_SETUP = new InitialSecuritySetup();
    private static final class InitialSecuritySetup extends InstallState {
        InitialSecuritySetup() {
            super("INITIAL_SECURITY_SETUP", false);
        }
        public void initializeState() {
            try {
                Jenkins.get().getSetupWizard().init(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            InstallUtil.proceedToNextStateFrom(INITIAL_SECURITY_SETUP);
        }
    }
    
    /**
     * New Jenkins install.
     */
    @Extension
    public static final InstallState NEW = new InstallState("NEW", false);

    /**
     * Restart of an existing Jenkins install.
     */
    @Extension
    public static final InstallState RESTART = new Restart();
    private static final class Restart extends InstallState {
        Restart() {
            super("RESTART", true);
        }
        public void initializeState() {
            InstallUtil.saveLastExecVersion();
        }
    }
    
    /**
     * Upgrade of an existing Jenkins install.
     */
    @Extension
    public static final InstallState UPGRADE = new UpgradeWizard();
    
    /**
     * Downgrade of an existing Jenkins install.
     */
    @Extension
    public static final InstallState DOWNGRADE = new Downgrade();
    private static final class Downgrade extends InstallState {
        Downgrade() {
            super("DOWNGRADE", true);
        }
        public void initializeState() {
            InstallUtil.saveLastExecVersion();
        }
    }
    
    private static final Logger LOGGER = Logger.getLogger(InstallState.class.getName());
    
    /**
     * Jenkins started in test mode (JenkinsRule).
     */
    public static final InstallState TEST = new InstallState("TEST", true);
    
    /**
     * Jenkins started in development mode: Boolean.getBoolean("hudson.Main.development").
     * Can be run normally with the -Djenkins.install.runSetupWizard=true
     */
    public static final InstallState DEVELOPMENT = new InstallState("DEVELOPMENT", true);

    private final transient boolean isSetupComplete;

    /**
     * Link with the pluginSetupWizardGui.js map: "statsHandlers"
     */
    private final String name;

    public InstallState(@Nonnull String name, boolean isSetupComplete) {
        this.name = name;
        this.isSetupComplete = isSetupComplete;
    }
    
    /**
     * Process any initialization this install state requires
     */
    public void initializeState() {
    }

    /**
     * The actual class name is irrelevant; this is functionally an enum.
     * <p>Creating a {@code writeReplace} does not help much since XStream then just saves:
     * {@code <installState class="jenkins.install.InstallState$CreateAdminUser" resolves-to="jenkins.install.InstallState">}
     * @see #UNUSED_INNER_CLASSES
     * @deprecated Should no longer be used, as {@link Jenkins} now saves only {@link #name}.
     */
    @Deprecated
    protected Object readResolve() {
        // If we get invalid state from the configuration, fallback to unknown
        if (StringUtils.isBlank(name)) {
            LOGGER.log(Level.WARNING, "Read install state with blank name: ''{0}''. It will be ignored", name);
            return UNKNOWN;
        }
        
        InstallState state = InstallState.valueOf(name);
        if (state == null) {
            LOGGER.log(Level.WARNING, "Cannot locate an extension point for the state ''{0}''. It will be ignored", name);
            return UNKNOWN;
        }
        
        // Otherwise we return the actual state
        return state;
    }

    /**
     * Indicates the initial setup is complete
     */
    public boolean isSetupComplete() {
        return isSetupComplete;
    }
    
    public String name() {
        return name;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof InstallState) {
            return name.equals(((InstallState)obj).name());
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "InstallState (" + name + ")";
    }

    /**
     * Find an install state by name
     * @param name
     * @return
     */
    @CheckForNull
    public static InstallState valueOf(@Nonnull String name) {
        for (InstallState state : all()) {
            if (name.equals(state.name)) {
                return state;
            }
        }
        return null;
    }

    /**
     * Returns all install states in the system
     */
    static ExtensionList<InstallState> all() {
        return ExtensionList.lookup(InstallState.class);
    }
}