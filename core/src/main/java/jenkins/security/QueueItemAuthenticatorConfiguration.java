package jenkins.security;

import hudson.Extension;
import hudson.model.queue.Tasks;
import hudson.util.DescribableList;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Show the {@link QueueItemAuthenticator} configurations on the system config page.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.520
 */
@Extension @Symbol("queueItemAuthenticator")
public class QueueItemAuthenticatorConfiguration extends GlobalConfiguration {
    private final DescribableList<QueueItemAuthenticator,QueueItemAuthenticatorDescriptor> authenticators
        = new DescribableList<QueueItemAuthenticator, QueueItemAuthenticatorDescriptor>(this);

    public QueueItemAuthenticatorConfiguration() {
        load();
    }

    private Object readResolve() {
        authenticators.setOwner(this);
        return this;
    }

    @Override
    public @Nonnull GlobalConfigurationCategory getCategory() {
        return GlobalConfigurationCategory.get(GlobalConfigurationCategory.Security.class);
    }

    /**
     * Provides all user-configured authenticators.
     * Note that if you are looking to determine all <em>effective</em> authenticators,
     * including any potentially supplied by plugins rather than user configuration,
     * you should rather call {@link QueueItemAuthenticatorProvider#authenticators};
     * or if you are looking for the authentication of an actual project, build, etc., use
     * {@link hudson.model.Queue.Item#authenticate} or {@link Tasks#getAuthenticationOf}.
     */
    public DescribableList<QueueItemAuthenticator, QueueItemAuthenticatorDescriptor> getAuthenticators() {
        return authenticators;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        try {
            authenticators.rebuildHetero(req,json, QueueItemAuthenticatorDescriptor.all(),"authenticators");
            return true;
        } catch (IOException e) {
            throw new FormException(e,"authenticators");
        }
    }

    public static @Nonnull QueueItemAuthenticatorConfiguration get() {
        return GlobalConfiguration.all().getInstance(QueueItemAuthenticatorConfiguration.class);
    }

    @Extension(ordinal = 100)
    public static class ProviderImpl extends QueueItemAuthenticatorProvider {

        @Nonnull
        @Override
        public List<QueueItemAuthenticator> getAuthenticators() {
            return get().getAuthenticators();
        }
    }
}