import java.util.*;
/**
 * Entry point to auto-generated tests (generated by maven-hpi-plugin).
 * If this fails to compile, you are probably using Hudson &lt; 1.327. If so, disable
 * this code generation by configuring maven-hpi-plugin to &lt;disabledTestInjection>true&lt;/disabledTestInjection>.
 */
public class InjectedTest extends junit.framework.TestCase {
  public static junit.framework.Test suite() throws Exception {
    Map parameters = new HashMap();
    parameters.put("basedir","C:\\Users\\shchea\\Documents\\Diploma\\GitHub");
    parameters.put("artifactId","zos-connector");
    parameters.put("outputDirectory","C:\\Users\\shchea\\Documents\\Diploma\\GitHub\\target\\classes");
    parameters.put("testOutputDirectory","C:\\Users\\shchea\\Documents\\Diploma\\GitHub\\target\\test-classes");
    parameters.put("requirePI","false");
    return new org.jvnet.hudson.test.PluginAutomaticTestBuilder().build(parameters);
  }
}
