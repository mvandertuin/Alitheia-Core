/****************************************************************
 * WARNING: THESE TEST REQUIRE JAVA 7u72 FOR POWERMOCK TO WORK! *
 * see https://code.google.com/p/powermock/issues/detail?id=504 *
 ****************************************************************/
package eu.sqooss.impl.service.webadmin;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.admin.AdminService;
import eu.sqooss.service.cluster.ClusterNodeService;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.updater.UpdaterService;
import junit.framework.Assert;
import junit.framework.ComparisonFailure;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AlitheiaCore.class)
abstract public class HTMLTest {

    protected AlitheiaCore core;
    protected @Mock BundleContext bundle;
    protected @Mock LogManager lm;
    protected @Mock DBService dbService;
    protected @Mock PluginAdmin pa;
    protected @Mock AdminService as;
    protected @Mock MetricActivator ma;
    protected @Mock ClusterNodeService cns;
    protected @Mock Scheduler sched;
    protected @Mock UpdaterService updsrv;

    /**
     * This is unneeded after adding in DI,
     * then just add @InjectMocks to whatever you need.
     */
    public void prepareWithoutDI(){
        mockStatic(AlitheiaCore.class);
        core = Mockito.mock(AlitheiaCore.class);
        when(AlitheiaCore.getInstance()).thenReturn(core);
        when(core.getAdminService()).thenReturn(as);
        when(core.getClusterNodeService()).thenReturn(cns);
        when(core.getDBService()).thenReturn(dbService);
        when(core.getLogManager()).thenReturn(lm);
        when(core.getMetricActivator()).thenReturn(ma);
        when(core.getScheduler()).thenReturn(sched);
        when(core.getUpdater()).thenReturn(updsrv);
        when(core.getPluginAdmin()).thenReturn(pa);
    }

    public VelocityEngine Engine(){
        VelocityEngine ve = null;
        try {
            ve = new VelocityEngine();
            ve.setProperty("velocimacro.library.autoreload", true);
            ve.setProperty("file.resource.loader.cache", false);
            String resourceLoader = "classpath";
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, resourceLoader);
            ve.setProperty(resourceLoader + "."
                            + RuntimeConstants.RESOURCE_LOADER + ".class",
                    "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        } catch (Exception e) {
            System.err.println("Error: Velocity initialization");
            e.printStackTrace();
        }
        return ve;
    }

    public String render(String template, HttpServletRequest req, VelocityContext vc) {
        try {
            if(template == null && vc.containsKey("subtemplate"))
                template = vc.get("subtemplate").toString(); 
            Template t = Engine().getTemplate( template );
            StringWriter writer = new StringWriter();
            t.merge(vc, writer);
            return writer.toString();
        } catch (Exception e){
            Assert.fail("An exception occurred while rendering Velocity template: "+e.toString());
            return null;
        }
    }

    protected static void assertWhiteSpaceEqual(String expected, String actual){
        if(!reduceWhitespace(expected).equalsIgnoreCase(reduceWhitespace(actual)))
            throw new ComparisonFailure("", reduceWhitespace(expected), reduceWhitespace(actual));
    }

    private static String reduceWhitespace(String in){
        String s = in.replaceAll("\\s+", "").replaceAll(">\\s", ">").replaceAll(">\\s<", "><").replaceAll("(\\s$|^\\s)", "");
//        String o = "";
//        for(int i = 0; i < s.length(); i+=40)
//            o += s.substring(i, Math.min(i+40, s.length()-1))+"\n";
        return s;//o;
    }

}
