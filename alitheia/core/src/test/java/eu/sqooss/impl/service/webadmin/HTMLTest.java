package eu.sqooss.impl.service.webadmin;

import eu.sqooss.service.logging.Logger;
import junit.framework.Assert;
import junit.framework.ComparisonFailure;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

public class HTMLTest {
    public VelocityEngine Engine(){
        VelocityEngine ve = null;
        try {
            ve = new VelocityEngine();
            ve.setProperty("runtime.log.logsystem.class",
                    "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            ve.setProperty("runtime.log.logsystem.log4j.category",
                    Logger.NAME_SQOOSS_WEBADMIN);
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
                template = vc.get("subtemplate").toString(); Template t = Engine().getTemplate( template );
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
            throw new ComparisonFailure("", expected, actual);
    }

    private static String reduceWhitespace(String in){
        String s = in.replaceAll("\\s+", " ").replaceAll(">\\s", ">").replaceAll(">\\s<", "><").replaceAll("(\\s$|^\\s)", "");
//        String o = "";
//        for(int i = 0; i < s.length(); i+=40)
//            o += s.substring(i, Math.min(i+40, s.length()-1))+"\n";
        return s;//o;
    }
}
