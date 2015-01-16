package eu.sqooss.impl.service.webadmin;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.abstractmetric.AlitheiaPlugin;
import eu.sqooss.service.db.*;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.pa.PluginInfo;

import org.apache.velocity.VelocityContext;
//import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.ServiceReference;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.*;

/**
 * A test verifying that the HTML output is unchanged before and after the refactor.
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginsViewTest extends HTMLTest {
    static long failid;
    static long successid;

    @InjectMocks AlitheiaCore core;
    @InjectMocks PluginsView pluginsView;

    
    // Init mock instances here, otherwise null in inserted into the ProjectsView and AlitheiaCore
    @Mock DBService dbService;
    @Mock PluginAdmin pa;
    VelocityContext vc;
    
    MockHttpServletRequest request;

    @Before
    public void setup(){
        vc = new VelocityContext();
        vc.put("tr",new TranslationProxy());
        vc.put("metrics",pluginsView);
    	
    	//Setup HttpServletRequest
        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setPathInfo("/plugins");
        request.addHeader("Accept-Language", "9=nl,en-US;q=0.8,en;q=0.6");

    }

    @Test
    public void testNoPlugins() {
    	when(pa.listPlugins()).thenReturn(new ArrayList<PluginInfo>());
        request.setParameter("pluginHashcode", "hash");
    	//String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/noPlugins.html", request, vc);
        assertWhiteSpaceEqual(noPlugins, result);
    }
    
    @Test
    public void testNullRequest() {
    	
        PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = true;
        when(plugin.getInstalled()).thenReturn(true);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
    	//String result = pluginsView.render(null);
        
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);
    	
        assertWhiteSpaceEqual(NoNullnoReq,result);
    }
    
    @Test
    public void testRequest(){
        PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = true;
        when(plugin.getInstalled()).thenReturn(true);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        request.setParameter("pluginHashcode", "hash");
    	
        //String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);
    	assertWhiteSpaceEqual(NoNullReq,result);
    }
    
    @Test
    public void testCreateProperty(){
    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = true;
        when(plugin.getInstalled()).thenReturn(true);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        when(pa.getPluginInfo("hash")).thenReturn(plugin);
        when(plugin.hasConfProp(anyString(), anyString())).thenReturn(true);
        
        request.setParameter("pluginHashcode", "hash");
        request.setParameter("action", "createProperty");
        
    	//String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);
    	assertWhiteSpaceEqual(createProp,result);
    }
    
    @Test
    public void testSelectedPluginNoMetrics(){
        request.setParameter("pluginHashcode", "hash");

    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = true;
        when(plugin.getInstalled()).thenReturn(true);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        plugin.setHashcode("hash");
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        when(pa.getPluginInfo("hash")).thenReturn(plugin);
        
        AlitheiaPlugin ap = mock(AlitheiaPlugin.class);
        Plugin p = mock(Plugin.class);
        
        when(pa.getPlugin(plugin)).thenReturn(ap);
        when(ap.getAllSupportedMetrics()).thenReturn(null);
        
        ArrayList<Plugin> list = new ArrayList<Plugin>();
        list.add(p);
        when(dbService.findObjectsByProperties(eq(Plugin.class), anyMap())).thenReturn(list);
        
        when(p.getConfigurations()).thenReturn(null);
        
    	//String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);
    	assertWhiteSpaceEqual(NoNullselectedPluginNoMetrics,result);
    }
    
    @Test
    public void testPluginNotInstalled(){
    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = false;
        when(plugin.getInstalled()).thenReturn(false);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        
        plugin.setHashcode("hash");
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        when(pa.getPluginInfo("hash")).thenReturn(plugin);
        
        request.setParameter("showProperties", "true");
        request.setParameter("showActivators", "true");

     
    	//String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);

    	assertWhiteSpaceEqual(NoNullpluginNotInstalled,result);
    }
    
    @Test
    public void testPluginConfiguration(){
        request.setParameter("pluginHashcode", "hash");
        request.setParameter("showProperties", "true");

    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = true;
        when(plugin.getInstalled()).thenReturn(true);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        plugin.setHashcode("hash");
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        when(pa.getPluginInfo("hash")).thenReturn(plugin);
        
        AlitheiaPlugin ap = mock(AlitheiaPlugin.class);
        Plugin p = mock(Plugin.class);
        
        MetricType mt = mock(MetricType.class);
        
        Metric m = mock(Metric.class);
        when(m.getId()).thenReturn((long)1);
        when(m.getMnemonic()).thenReturn("Metric Mnemonic");
        when(m.getMetricType()).thenReturn(mt);
        when(mt.getType()).thenReturn("Metric type");
        when(m.getDescription()).thenReturn("Metric description");
        List<Metric> metriclist= new ArrayList<Metric>();
        metriclist.add(m);
        
        when(pa.getPlugin(plugin)).thenReturn(ap);
        when(ap.getAllSupportedMetrics()).thenReturn(metriclist);
        
        ArrayList<Plugin> list = new ArrayList<Plugin>();
        list.add(p);
        when(dbService.findObjectsByProperties(eq(Plugin.class), anyMap())).thenReturn(list);
        
        PluginConfiguration pc = mock(PluginConfiguration.class);
        when(pc.getName()).thenReturn("Some config name");
        when(pc.getType()).thenReturn("Some config type");
        when(pc.getMsg()).thenReturn("Some config msg");
        when(pc.getValue()).thenReturn("Some config value");
        
        Set<PluginConfiguration> pcs = new HashSet<PluginConfiguration>();
        pcs.add(pc);
        when(p.getConfigurations()).thenReturn(pcs);
        when(plugin.getConfiguration()).thenReturn(pcs);
        
    	//String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);
    	assertWhiteSpaceEqual(NoNullpluginConfig,result);
    }
    
    @Test
    public void testSelectedPluginMetrics(){
        request.setParameter("pluginHashcode", "hash");

    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = true;
        when(plugin.getInstalled()).thenReturn(true);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        plugin.setHashcode("hash");
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        when(pa.getPluginInfo("hash")).thenReturn(plugin);
        
        AlitheiaPlugin ap = mock(AlitheiaPlugin.class);
        Plugin p = mock(Plugin.class);
        MetricType mt = mock(MetricType.class);
        
        Metric m = mock(Metric.class);
        when(m.getId()).thenReturn((long)1);
        when(m.getMnemonic()).thenReturn("Metric Mnemonic");
        when(m.getMetricType()).thenReturn(mt);
        when(mt.getType()).thenReturn("Metric type");
        when(m.getDescription()).thenReturn("Metric description");
        List<Metric> metriclist= new ArrayList<Metric>();
        metriclist.add(m);
        
        when(pa.getPlugin(plugin)).thenReturn(ap);
        when(ap.getAllSupportedMetrics()).thenReturn(metriclist);
        
        ArrayList<Plugin> list = new ArrayList<Plugin>();
        list.add(p);
        when(dbService.findObjectsByProperties(eq(Plugin.class), anyMap())).thenReturn(list);
        
        when(p.getConfigurations()).thenReturn(null);
        
    	//String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);
    	assertWhiteSpaceEqual(NoNullselectedPluginMetrics,result);
    }
    
    @Test
    public void testInstallPlugin(){
        request.setParameter("action","installPlugin");
    	request.setParameter("pluginHashcode", "hash");

    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = false;
        when(plugin.getInstalled()).thenReturn(false);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        plugin.setHashcode("hash");
        
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        when(pa.getPluginInfo("hash")).thenReturn(plugin);
               
    	//String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);
    	assertWhiteSpaceEqual(NoNullinstallPlugin,result);
    }
    
    @Test
    public void testUnInstallPlugin(){
        request.setParameter("action","uninstallPlugin");
    	request.setParameter("pluginHashcode", "hash");

    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = false;
        when(plugin.getInstalled()).thenReturn(false);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        plugin.setHashcode("hash");
        
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));      
        when(pa.getPluginInfo("hash")).thenReturn(plugin);
        
    	//String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);
    	assertWhiteSpaceEqual(NoNulluninstallPlugin,result);
    }
    
    @Test
    public void testRemoveProperty(){
    	request.setParameter("action","removeProperty");
    	request.setParameter("pluginHashcode", "hash");

    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = true;
        when(plugin.getInstalled()).thenReturn(true);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        plugin.setHashcode("hash");
        when(plugin.hasConfProp(anyString(), anyString())).thenReturn(true);       
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        when(pa.getPluginInfo("hash")).thenReturn(plugin);
        
        //String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);

    	assertWhiteSpaceEqual(removeProperty,result);
    }
    
    @Test
    public void testConfirmProperty() throws Exception{
    	request.setParameter("action","confirmProperty");
    	request.setParameter("pluginHashcode", "hash");

    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = false;
        when(plugin.getInstalled()).thenReturn(false);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        plugin.setHashcode("hash");
        
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        when(pa.getPluginInfo("hash")).thenReturn(plugin);

        //String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);

    	assertWhiteSpaceEqual(NoNullconfirmProperty,result);
    }
    @Test
    public void testConfirmPropertyUpdate() throws Exception{
    	request.setParameter("action","confirmProperty");
    	request.setParameter("pluginHashcode", "hash");

    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = false;
        when(plugin.getInstalled()).thenReturn(false);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        plugin.setHashcode("hash");
        when(plugin.hasConfProp(anyString(), anyString())).thenReturn(true);
        when(plugin.updateConfigEntry(eq(dbService), anyString(), anyString())).thenReturn(true);
        
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        when(pa.getPluginInfo("hash")).thenReturn(plugin);

        //String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);

    	assertWhiteSpaceEqual(NoNullconfirmPropertyUpdate,result);
    }
    
    @Test
    public void testUpdateProperty(){
    	request.setParameter("action","updateProperty");
    	request.setParameter("pluginHashcode", "hash");

    	PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = true;
        when(plugin.getInstalled()).thenReturn(true);
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
        plugin.setHashcode("hash");
        
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
        when(pa.getPluginInfo("hash")).thenReturn(plugin);

        //String result = pluginsView.render(request);
        pluginsView.render(request,vc);
        String result = render("plugins/content.html", request, vc);

    	assertWhiteSpaceEqual(updateProperty,result);
    }

    @Test
    public void testRenderPluginAttributes(){
        	request.setParameter("showProperties", "true");
            PluginInfo plugin = mock(PluginInfo.class);
            plugin.installed = true;
            when(plugin.getInstalled()).thenReturn(true);
            when(plugin.getPluginName()).thenReturn("Some Plugin");
            when(plugin.getServiceRef()).thenReturn(mock(ServiceReference.class));
            when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
                                        
            PluginConfiguration pc = mock(PluginConfiguration.class);
            when(pc.getName()).thenReturn("Some config name");
            when(pc.getType()).thenReturn("Some config type");
            when(pc.getMsg()).thenReturn("Some config msg");
            when(pc.getValue()).thenReturn("Some config value");
            
            Set<PluginConfiguration> pcs = new HashSet<PluginConfiguration>();
            pcs.add(pc);
            when(plugin.getConfiguration()).thenReturn(pcs);
            
        	//String result = pluginsView.render(request);
            pluginsView.render(request,vc);
            String result = render("plugins/content.html", request, vc);

        	assertWhiteSpaceEqual(NoNullrenderPluginAttribute,result);
    }

    private static final String noPlugins = "<fieldset>\n"
                    + "<legend>All plug-ins</legend>\n"
                    + "<span>No plug-ins found!&nbsp;"
                    + "<input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:"
                    + "window.location.reload(true);\"></span></fieldset>\n";  

    private static final String noReq = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Allplug-ins</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\">null</td><tdclass=\"trans\">null</td></tr><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\">null</td><tdclass=\"trans\">null</td></tr></tbody></table><span><inputtype=\"checkbox\"onclick=\"javascript:document.getElementById('showProperties').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayproperties<inputtype=\"checkbox\"onclick=\"javascript:document.getElementById('showActivators').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayactivators</span></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String NoNullnoReq = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Allplug-ins</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\"></td><tdclass=\"trans\"></td></tr><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\"></td><tdclass=\"trans\"></td></tr></tbody></table><span><inputtype=\"checkbox\"onclick=\"javascript:document.getElementById('showProperties').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayproperties<inputtype=\"checkbox\"onclick=\"javascript:document.getElementById('showActivators').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayactivators</span></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";

    private static final String Req = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Allplug-ins</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\">null</td><tdclass=\"trans\">null</td></tr><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\">null</td><tdclass=\"trans\">null</td></tr></tbody></table><span><inputtype=\"checkbox\"onclick=\"javascript:document.getElementById('showProperties').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayproperties<inputtype=\"checkbox\"onclick=\"javascript:document.getElementById('showActivators').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayactivators</span></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String NoNullReq = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Allplug-ins</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\"></td><tdclass=\"trans\"></td></tr><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\"></td><tdclass=\"trans\"></td></tr></tbody></table><span><inputtype=\"checkbox\"onclick=\"javascript:document.getElementById('showProperties').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayproperties<inputtype=\"checkbox\"onclick=\"javascript:document.getElementById('showActivators').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayactivators</span></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    
    private static final String pluginNotInstalled = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Allplug-ins</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Registered</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\">null</td><tdclass=\"trans\">null</td></tr><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Registered</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\">null</td><tdclass=\"trans\">null</td></tr></tbody></table><span><inputtype=\"checkbox\"checkedonclick=\"javascript:document.getElementById('showProperties').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayproperties<inputtype=\"checkbox\"checkedonclick=\"javascript:document.getElementById('showActivators').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayactivators</span></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"true\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"true\"></form>";
    private static final String NoNullpluginNotInstalled = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Allplug-ins</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Registered</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\"></td><tdclass=\"trans\"></td></tr><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Registered</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\"></td><tdclass=\"trans\"></td></tr></tbody></table><span><inputtype=\"checkbox\"checkedonclick=\"javascript:document.getElementById('showProperties').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayproperties<inputtype=\"checkbox\"checkedonclick=\"javascript:document.getElementById('showActivators').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayactivators</span></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"true\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"true\"></form>";

    private static final String pluginConfig = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Installed</td><td>SomePlugin</td><td>null</td><td>null</td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Uninstall\"onclick=\"javascript:document.getElementById('action').value='uninstallPlugin';document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Synchronise\"onclick=\"javascript:document.getElementById('action').value='syncPlugin';document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"></td></tr></tbody></table><fieldset><legend>Supportedmetrics</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:10%;\">Id</td><tdclass=\"head\"style=\"width:25%;\">Name</td><tdclass=\"head\"style=\"width:25%;\">Type</td><tdclass=\"head\"style=\"width:40%;\">Description</td></tr></thead><tbody><tr><td>1</td><td>MetricMnemonic</td><td>Metrictype</td><td>Metricdescription</td></tr></tbody></table></fieldset><fieldset><legend>Configurationproperties</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:20%;\">Type</td><tdclass=\"head\"style=\"width:50%;\">Value</td></tr></thead><tbody><trclass=\"edit\"onclick=\"javascript:document.getElementById('action').value='updateProperty';document.getElementById('propertyName').value='Someconfigname';document.getElementById('propertyType').value='Someconfigtype';document.getElementById('propertyDescription').value='Someconfigmsg';document.getElementById('propertyValue').value='Someconfigvalue';document.metrics.submit();\"><tdclass=\"trans\"title=\"Someconfigmsg\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Someconfigname</td><tdclass=\"trans\">Someconfigtype</td><tdclass=\"trans\">Someconfigvalue</td></tr><tr><tdcolspan=\"3\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Addproperty\"onclick=\"javascript:document.getElementById('action').value='createProperty';document.metrics.submit();\"></td></tr></tbody></table></fieldset></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"true\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String NoNullpluginConfig = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Installed</td><td>SomePlugin</td><td></td><td></td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Uninstall\"onclick=\"javascript:document.getElementById('action').value='uninstallPlugin';document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Synchronise\"onclick=\"javascript:document.getElementById('action').value='syncPlugin';document.getElementById('pluginHashcode').value='';document.metrics.submit();\"></td></tr></tbody></table><fieldset><legend>Supportedmetrics</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:10%;\">Id</td><tdclass=\"head\"style=\"width:25%;\">Name</td><tdclass=\"head\"style=\"width:25%;\">Type</td><tdclass=\"head\"style=\"width:40%;\">Description</td></tr></thead><tbody><tr><td>1</td><td>MetricMnemonic</td><td>Metrictype</td><td>Metricdescription</td></tr></tbody></table></fieldset><fieldset><legend>Configurationproperties</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:20%;\">Type</td><tdclass=\"head\"style=\"width:50%;\">Value</td></tr></thead><tbody><trclass=\"edit\"onclick=\"javascript:document.getElementById('action').value='updateProperty';document.getElementById('propertyName').value='Someconfigname';document.getElementById('propertyType').value='Someconfigtype';document.getElementById('propertyDescription').value='Someconfigmsg';document.getElementById('propertyValue').value='Someconfigvalue';document.metrics.submit();\"><tdclass=\"trans\"title=\"Someconfigmsg\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Someconfigname</td><tdclass=\"trans\">Someconfigtype</td><tdclass=\"trans\">Someconfigvalue</td></tr><tr><tdcolspan=\"3\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Addproperty\"onclick=\"javascript:document.getElementById('action').value='createProperty';document.metrics.submit();\"></td></tr></tbody></table></fieldset></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"true\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";

    private static final String selectedPluginMetrics = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Installed</td><td>SomePlugin</td><td>null</td><td>null</td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Uninstall\"onclick=\"javascript:document.getElementById('action').value='uninstallPlugin';document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Synchronise\"onclick=\"javascript:document.getElementById('action').value='syncPlugin';document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"></td></tr></tbody></table><fieldset><legend>Supportedmetrics</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:10%;\">Id</td><tdclass=\"head\"style=\"width:25%;\">Name</td><tdclass=\"head\"style=\"width:25%;\">Type</td><tdclass=\"head\"style=\"width:40%;\">Description</td></tr></thead><tbody><tr><td>1</td><td>MetricMnemonic</td><td>Metrictype</td><td>Metricdescription</td></tr></tbody></table></fieldset><fieldset><legend>Configurationproperties</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:20%;\">Type</td><tdclass=\"head\"style=\"width:50%;\">Value</td></tr></thead><tbody><tr><tdcolspan=\"3\"class=\"noattr\">Thisplug-inhasnoconfigurationproperties.</td></tr><tr><tdcolspan=\"3\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Addproperty\"onclick=\"javascript:document.getElementById('action').value='createProperty';document.metrics.submit();\"></td></tr></tbody></table></fieldset></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String NoNullselectedPluginMetrics = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Installed</td><td>SomePlugin</td><td></td><td></td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Uninstall\"onclick=\"javascript:document.getElementById('action').value='uninstallPlugin';document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Synchronise\"onclick=\"javascript:document.getElementById('action').value='syncPlugin';document.getElementById('pluginHashcode').value='';document.metrics.submit();\"></td></tr></tbody></table><fieldset><legend>Supportedmetrics</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:10%;\">Id</td><tdclass=\"head\"style=\"width:25%;\">Name</td><tdclass=\"head\"style=\"width:25%;\">Type</td><tdclass=\"head\"style=\"width:40%;\">Description</td></tr></thead><tbody><tr><td>1</td><td>MetricMnemonic</td><td>Metrictype</td><td>Metricdescription</td></tr></tbody></table></fieldset><fieldset><legend>Configurationproperties</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:20%;\">Type</td><tdclass=\"head\"style=\"width:50%;\">Value</td></tr></thead><tbody><tr><tdcolspan=\"3\"class=\"noattr\">Thisplug-inhasnoconfigurationproperties.</td></tr><tr><tdcolspan=\"3\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Addproperty\"onclick=\"javascript:document.getElementById('action').value='createProperty';document.metrics.submit();\"></td></tr></tbody></table></fieldset></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";

    private static final String selectedPluginNoMetrics = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Installed</td><td>SomePlugin</td><td>null</td><td>null</td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Uninstall\"onclick=\"javascript:document.getElementById('action').value='uninstallPlugin';document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Synchronise\"onclick=\"javascript:document.getElementById('action').value='syncPlugin';document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"></td></tr></tbody></table><fieldset><legend>Supportedmetrics</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:10%;\">Id</td><tdclass=\"head\"style=\"width:25%;\">Name</td><tdclass=\"head\"style=\"width:25%;\">Type</td><tdclass=\"head\"style=\"width:40%;\">Description</td></tr></thead><tbody><tr><tdcolspan=\"4\"class=\"noattr\">Thisplug-indoesnotsupportmetrics.</td></tr></tbody></table></fieldset><fieldset><legend>Configurationproperties</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:20%;\">Type</td><tdclass=\"head\"style=\"width:50%;\">Value</td></tr></thead><tbody><tr><tdcolspan=\"3\"class=\"noattr\">Thisplug-inhasnoconfigurationproperties.</td></tr><tr><tdcolspan=\"3\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Addproperty\"onclick=\"javascript:document.getElementById('action').value='createProperty';document.metrics.submit();\"></td></tr></tbody></table></fieldset></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String NoNullselectedPluginNoMetrics = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Installed</td><td>SomePlugin</td><td></td><td></td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Uninstall\"onclick=\"javascript:document.getElementById('action').value='uninstallPlugin';document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Synchronise\"onclick=\"javascript:document.getElementById('action').value='syncPlugin';document.getElementById('pluginHashcode').value='';document.metrics.submit();\"></td></tr></tbody></table><fieldset><legend>Supportedmetrics</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:10%;\">Id</td><tdclass=\"head\"style=\"width:25%;\">Name</td><tdclass=\"head\"style=\"width:25%;\">Type</td><tdclass=\"head\"style=\"width:40%;\">Description</td></tr></thead><tbody><tr><tdcolspan=\"4\"class=\"noattr\">Thisplug-indoesnotsupportmetrics.</td></tr></tbody></table></fieldset><fieldset><legend>Configurationproperties</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:20%;\">Type</td><tdclass=\"head\"style=\"width:50%;\">Value</td></tr></thead><tbody><tr><tdcolspan=\"3\"class=\"noattr\">Thisplug-inhasnoconfigurationproperties.</td></tr><tr><tdcolspan=\"3\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Addproperty\"onclick=\"javascript:document.getElementById('action').value='createProperty';document.metrics.submit();\"></td></tr></tbody></table></fieldset></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";

    
    private static final String installPlugin = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Errors</legend>Plug-incannotbeinstalled!Checklogfordetails.</fieldset><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Registered</td><td>SomePlugin</td><td>null</td><td>null</td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Install\"onclick=\"javascript:document.getElementById('action').value='installPlugin';document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"></td></tr></tbody></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String NoNullinstallPlugin = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Errors</legend>Plug-incannotbeinstalled!Checklogfordetails.</fieldset><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Registered</td><td>SomePlugin</td><td></td><td></td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Install\"onclick=\"javascript:document.getElementById('action').value='installPlugin';document.getElementById('pluginHashcode').value='';document.metrics.submit();\"></td></tr></tbody></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";

    private static final String uninstallPlugin = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Errors</legend>Plug-incannotbeuninstalled.Checklogfordetails.</fieldset><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Registered</td><td>SomePlugin</td><td>null</td><td>null</td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Install\"onclick=\"javascript:document.getElementById('action').value='installPlugin';document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"></td></tr></tbody></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String NoNulluninstallPlugin = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Errors</legend>Plug-incannotbeuninstalled.Checklogfordetails.</fieldset><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Registered</td><td>SomePlugin</td><td></td><td></td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Install\"onclick=\"javascript:document.getElementById('action').value='installPlugin';document.getElementById('pluginHashcode').value='';document.metrics.submit();\"></td></tr></tbody></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";

    private static final String confirmPropertyUpdate = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Registered</td><td>SomePlugin</td><td>null</td><td>null</td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Install\"onclick=\"javascript:document.getElementById('action').value='installPlugin';document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"></td></tr></tbody></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String NoNullconfirmPropertyUpdate = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Registered</td><td>SomePlugin</td><td></td><td></td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Install\"onclick=\"javascript:document.getElementById('action').value='installPlugin';document.getElementById('pluginHashcode').value='';document.metrics.submit();\"></td></tr></tbody></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";

    private static final String renderPluginAttribute = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Allplug-ins</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\">null</td><tdclass=\"trans\">null</td></tr><tr><td>&nbsp;</td><tdcolspan=\"3\"class=\"attr\"><b>Property:</b>Someconfigname&nbsp;<b>Type:</b>Someconfigtype&nbsp;<b>Value:</b>Someconfigvalue</td></tr><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\">null</td><tdclass=\"trans\">null</td></tr><tr><td>&nbsp;</td><tdcolspan=\"3\"class=\"attr\"><b>Property:</b>Someconfigname&nbsp;<b>Type:</b>Someconfigtype&nbsp;<b>Value:</b>Someconfigvalue</td></tr></tbody></table><span><inputtype=\"checkbox\"checkedonclick=\"javascript:document.getElementById('showProperties').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayproperties<inputtype=\"checkbox\"onclick=\"javascript:document.getElementById('showActivators').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayactivators</span></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"true\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String NoNullrenderPluginAttribute = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Allplug-ins</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\"></td><tdclass=\"trans\"></td></tr><tr><td>&nbsp;</td><tdcolspan=\"3\"class=\"attr\"><b>Property:</b>Someconfigname&nbsp;<b>Type:</b>Someconfigtype&nbsp;<b>Value:</b>Someconfigvalue</td></tr><trclass=\"edit\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><tdclass=\"trans\"><imgsrc=\"/edit.png\"alt=\"[Edit]\"/>&nbsp;Installed</td><tdclass=\"trans\">SomePlugin</td><tdclass=\"trans\"></td><tdclass=\"trans\"></td></tr><tr><td>&nbsp;</td><tdcolspan=\"3\"class=\"attr\"><b>Property:</b>Someconfigname&nbsp;<b>Type:</b>Someconfigtype&nbsp;<b>Value:</b>Someconfigvalue</td></tr></tbody></table><span><inputtype=\"checkbox\"checkedonclick=\"javascript:document.getElementById('showProperties').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayproperties<inputtype=\"checkbox\"onclick=\"javascript:document.getElementById('showActivators').value=this.checked;document.getElementById('pluginHashcode').value='';document.metrics.submit();\">Displayactivators</span></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"true\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    
    private static final String confirmProperty = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Registered</td><td>SomePlugin</td><td>null</td><td>null</td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Install\"onclick=\"javascript:document.getElementById('action').value='installPlugin';document.getElementById('pluginHashcode').value='null';document.metrics.submit();\"></td></tr></tbody></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String NoNullconfirmProperty = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>SomePlugin</legend><table><thead><trclass=\"head\"><tdclass=\"head\"style=\"width:80px;\">Status</td><tdclass=\"head\"style=\"width:30%;\">Name</td><tdclass=\"head\"style=\"width:40%;\">Class</td><tdclass=\"head\">Version</td></tr></thead><tbody><tr><td>Registered</td><td>SomePlugin</td><td></td><td></td></tr><tr><tdcolspan=\"4\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Plug-inslist\"onclick=\"javascript:document.getElementById('pluginHashcode').value='';document.metrics.submit();\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Install\"onclick=\"javascript:document.getElementById('action').value='installPlugin';document.getElementById('pluginHashcode').value='';document.metrics.submit();\"></td></tr></tbody></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    
    private static final String createProp = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>UpdatepropertyofSomePlugin</legend><tableclass=\"borderless\"><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Name</b></td><tdclass=\"borderless\"></td></tr><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Description</b></td><tdclass=\"borderless\"></td></tr><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Type</b></td><tdclass=\"borderless\"></td></tr><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Value</b></td><tdclass=\"borderless\"><inputtype=\"text\"class=\"form\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"></td></tr><tr><tdcolspan=\"2\"class=\"borderless\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Update\"onclick=\"javascript:document.getElementById('action').value='confirmProperty';document.metrics.submit();\">&nbsp;<inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Remove\"onclick=\"javascript:document.getElementById('action').value='removeProperty';document.metrics.submit();\">&nbsp;<inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Cancel\"onclick=\"javascript:document.metrics.submit();\"></td></tr></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>"; 
    private static final String removeProperty = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>Errors</legend>Propertyremovalhasfailed!Checklogfordetails.</fieldset><fieldset><legend>UpdatepropertyofSomePlugin</legend><tableclass=\"borderless\"><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Name</b></td><tdclass=\"borderless\"></td></tr><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Description</b></td><tdclass=\"borderless\"></td></tr><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Type</b></td><tdclass=\"borderless\"></td></tr><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Value</b></td><tdclass=\"borderless\"><inputtype=\"text\"class=\"form\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"></td></tr><tr><tdcolspan=\"2\"class=\"borderless\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Update\"onclick=\"javascript:document.getElementById('action').value='confirmProperty';document.metrics.submit();\">&nbsp;<inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Remove\"onclick=\"javascript:document.getElementById('action').value='removeProperty';document.metrics.submit();\">&nbsp;<inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Cancel\"onclick=\"javascript:document.metrics.submit();\"></td></tr></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";
    private static final String updateProperty = "<formid=\"metrics\"name=\"metrics\"method=\"post\"action=\"/index\"><fieldset><legend>CreatepropertyforSomePlugin</legend><tableclass=\"borderless\"><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Name</b></td><tdclass=\"borderless\"><inputtype=\"text\"class=\"form\"id=\"propertyName\"name=\"propertyName\"value=\"\"></td></tr><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Description</b></td><tdclass=\"borderless\"><inputtype=\"text\"class=\"form\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"></td></tr><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Type</b></td><tdclass=\"borderless\"><selectclass=\"form\"id=\"propertyType\"name=\"propertyType\"><optionvalue=\"INTEGER\">INTEGER</option><optionvalue=\"STRING\">STRING</option><optionvalue=\"BOOLEAN\">BOOLEAN</option><optionvalue=\"DOUBLE\">DOUBLE</option></select></td></tr><tr><tdclass=\"borderless\"style=\"width:100px;\"><b>Value</b></td><tdclass=\"borderless\"><inputtype=\"text\"class=\"form\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"></td></tr><tr><tdcolspan=\"2\"class=\"borderless\"><inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Create\"onclick=\"javascript:document.getElementById('action').value='confirmProperty';document.metrics.submit();\">&nbsp;<inputtype=\"button\"class=\"install\"style=\"width:100px;\"value=\"Cancel\"onclick=\"javascript:document.metrics.submit();\"></td></tr></table></fieldset><inputtype=\"hidden\"id=\"action\"name=\"action\"value=\"\"><inputtype=\"hidden\"id=\"pluginHashcode\"name=\"pluginHashcode\"value=\"hash\"><inputtype=\"hidden\"id=\"propertyName\"name=\"propertyName\"value=\"\"><inputtype=\"hidden\"id=\"propertyDescription\"name=\"propertyDescription\"value=\"\"><inputtype=\"hidden\"id=\"propertyType\"name=\"propertyType\"value=\"\"><inputtype=\"hidden\"id=\"propertyValue\"name=\"propertyValue\"value=\"\"><inputtype=\"hidden\"id=\"showProperties\"name=\"showProperties\"value=\"false\"><inputtype=\"hidden\"id=\"showActivators\"name=\"showActivators\"value=\"false\"></form>";

    



} 