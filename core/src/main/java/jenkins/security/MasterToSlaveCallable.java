package jenkins.security;

import hudson.remoting.Callable;
import org.jenkinsci.remoting.RoleChecker;


/**
 * Convenient {@link Callable} meant to be run on agent.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.587 / 1.580.1
 */
public abstract class MasterToSlaveCallable<V, T extends Throwable> implements Callable<V,T> {
    @Override
    public void checkRoles(RoleChecker checker) throws SecurityException {
        checker.check(this,Roles.SLAVE);
    }

    private static final long serialVersionUID = 1L;
}