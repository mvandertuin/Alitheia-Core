/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 - 2010 - Organization for Free and Open Source Software,  
 *                Athens, Greece.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package eu.sqooss.impl.service.webadmin;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.Plugin;
import eu.sqooss.service.db.PluginConfiguration;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.pa.PluginInfo;
import eu.sqooss.service.pa.PluginInfo.ConfigurationType;
import eu.sqooss.service.util.StringUtils;
import org.apache.velocity.VelocityContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class PluginsView extends Controller {

	private DBService db;
	private MetricActivator ma;
	private PluginAdmin pa;

	// Request parameters
	String reqParAction = "action";
	String reqParHashcode = "pluginHashcode";
	String reqParPropName = "propertyName";
	String reqParPropDescr = "propertyDescription";
	String reqParPropType = "propertyType";
	String reqParPropValue = "propertyValue";
	String reqParShowProp = "showProperties";
	String reqParShowActv = "showActivators";
	// Recognized "action" parameter's values
	String actValInstall = "installPlugin";
	String actValUninstall = "uninstallPlugin";
	String actValSync = "syncPlugin";
	String actValReqAddProp = "createProperty";
	String actValReqUpdProp = "updateProperty";
	String actValConAddProp = "confirmProperty";
	String actValConRemProp = "removeProperty";

	/**
	 * Constructor for PluginsView
	 * @param bundlecontext
	 */
	public PluginsView(BundleContext bundlecontext) {
		super(bundlecontext);
		db = AlitheiaCore.getInstance().getDBService();
		ma = AlitheiaCore.getInstance().getMetricActivator();
		pa = AlitheiaCore.getInstance().getPluginAdmin();
	}

	/**
	 * Render method for a GET request from the index.html page
	 * @param req The servletrequest
	 * @param vc The velocitycontext object
	 */
	
	@Action(uri = "/index", template = "index.html", method = "GET")
	public void indexGet(HttpServletRequest req, VelocityContext vc) {
		render(req, vc);
	}
	
	/**
	 * Render method for a POST request from the index.html page
	 * @param req The servletrequest
	 * @param vc The velocitycontext object
	 */
	@Action(uri = "/index", template = "index.html", method = "POST")
	public void indexPost(HttpServletRequest req, VelocityContext vc) {
		render(req, vc);
	}

	/**
	 * Render method for a GET request from the homepage
	 * @param req The servletrequest
	 * @param vc The velocitycontext object
	 */
	@Action(uri = "/", template = "index.html", method = "GET")
	public void homeGet(HttpServletRequest req, VelocityContext vc) {
		render(req, vc);
	}

	/**
	 * Render method for a POST request from the homepage
	 * @param req The servletrequest
	 * @param vc The velocitycontext object
	 */
	@Action(uri = "/", template = "index.html", method = "POST")
	public void render(HttpServletRequest req, VelocityContext vc) {
		//put request parameters to Velocity context
		vc.put("reqParAction", reqParAction);
		vc.put("reqParHashcode", reqParHashcode);
		vc.put("reqParPropName", reqParPropName);
		vc.put("reqParPropDescr", reqParPropDescr);
		vc.put("reqParPropType", reqParPropType);
		vc.put("reqParPropValue", reqParPropValue);
		vc.put("reqParShowProp", reqParShowProp);
		vc.put("reqParShowActv", reqParShowActv);
		//put action parameters to Velocity context
		vc.put("actValInstall", actValInstall);
		vc.put("actValUninstall", actValUninstall);
		vc.put("actValSync", actValSync);
		vc.put("actValReqAddProp", actValReqAddProp);
		vc.put("actValReqUpdProp", actValReqUpdProp);
		vc.put("actValConAddProp", actValConAddProp);
		vc.put("actValConRemProp", actValConRemProp);
		
		//If no plugin present, show noPlugin page
		if (pa.listPlugins().isEmpty()) {
			vc.put("noPlugins", "plugins/noPlugins.html");
		}

		else {
			//Set request parameters
			PluginInfo selPI = null;
			if (req != null) {
				  selPI = initializeRequest(req,vc);
			}
			
			generateHTML(vc,selPI);
		}
	}

	/**
	 * Helper method to get the pluginclasses of a certain plugin i
	 * @param i plugin
	 * @return A String with the different classes of plugin i concatenated 
	 */
	public String getPluginClass(PluginInfo i) {
		return StringUtils.join((String[]) (i.getServiceRef()
				.getProperty(Constants.OBJECTCLASS)), ",");
	}
	
	/**
	 * Method reads the several request parameters from req, initialize the selected Plugin and stores the parameters to the vc
	 * @param req The servletrequest
	 * @param vc The velocitycontext object
	 * @return The Selected Plugin which is used to render the correct HTML page
	 */
	private PluginInfo initializeRequest(HttpServletRequest req, VelocityContext vc){	
		// Stores the accumulated error messages
		StringBuilder e = new StringBuilder();
		// Request values
		String reqValAction = "";
		String reqValHashcode = null;
		String reqValPropName = null;
		String reqValPropDescr = null;
		String reqValPropType = null;
		String reqValPropValue = null;
		boolean reqValShowProp = false; // Show plug-in properties
		boolean reqValShowActv = false; // Show plug-in activators

		// ===============================================================
		// Parse the servlet's request object
		// ===============================================================
		if(req!=null){
			// Retrieve the selected editor's action (if any)
			reqValAction = req.getParameter(reqParAction);

			if (reqValAction == null) {
				reqValAction = "";
			}
			// Retrieve the various display flags
			if ((req.getParameter(reqParShowProp) != null)
					&& (req.getParameter(reqParShowProp).equals("true"))) {
				reqValShowProp = true;
			}
			if ((req.getParameter(reqParShowActv) != null)
					&& (req.getParameter(reqParShowActv).equals("true"))) {
				reqValShowActv = true;
			}
			// Retrieve the selected configuration property's values
			reqValPropName = req.getParameter(reqParPropName);
			reqValPropDescr = req.getParameter(reqParPropDescr);
			reqValPropType = req.getParameter(reqParPropType);
			reqValPropValue = req.getParameter(reqParPropValue);
			reqValHashcode = req.getParameter(reqParHashcode);
			
			// Plug-in based actions
			if (reqValHashcode != null) {
				// =======================================================
				// Plug-in install request
				// =======================================================
				if (reqValAction.equals(actValInstall)) {
					if (pa.installPlugin(reqValHashcode) == false) {
						e.append("Plug-in can not be installed!"
								+ " Check log for details.");
					}
					// Persist the DB changes
					else {
						PluginInfo pInfo = pa.getPluginInfo(reqValHashcode);
						pa.pluginUpdated(pa.getPlugin(pInfo));
					}
				}
				// =======================================================
				// Plug-in un-install request
				// =======================================================
				else if (reqValAction.equals(actValUninstall)) {
					if (pa.uninstallPlugin(reqValHashcode) == false) {
						e.append("Plug-in can not be uninstalled."
								+ " Check log for details.");
					} else {
						e.append("A job was scheduled to remove the plug-in");
					}
				}
			}
		}
		
		
        // Retrieve the selected plug-in's info object
		PluginInfo selPI = null;
        if (reqValHashcode != null) {
            selPI = pa.getPluginInfo(reqValHashcode);
        }

        if ((selPI != null) && (selPI.installed)) {
			// =======================================================
			// Plug-in synchronize (on all projects) request
			// =======================================================
			if (reqValAction.equals(actValSync)) {
				ma.syncMetrics(pa.getPlugin(selPI));
			}
			// =======================================================
			// Plug-in's configuration property removal
			// =======================================================
			else if (reqValAction.equals(actValConRemProp)) {
				if (selPI.hasConfProp(reqValPropName, reqValPropType)) {
					try {
						if (selPI.removeConfigEntry(db, reqValPropName,
								reqValPropType)) {
							// Update the Plug-in Admin's information
							pa.pluginUpdated(pa.getPlugin(selPI));
							// Reload the PluginInfo object
							selPI = pa.getPluginInfo(reqValHashcode);
						} else {
							e.append("Property removal" + " has failed!"
									+ " Check log for details.");
						}
					} catch (Exception ex) {
						e.append(ex.getMessage());
					}
				} else {
					e.append("Unknown configuration property!");
				}
				// Return to the update view upon error
				if (e.toString().length() > 0) {
					reqValAction = actValReqUpdProp;
				}
			}
			// =======================================================
			// Plug-in's configuration property creation/update
			// =======================================================
			else if (reqValAction.equals(actValConAddProp)) {
				// Check for a property update
				boolean update = false;
				try{
					update = selPI.hasConfProp(reqValPropName,reqValPropType);
				}
				catch(Exception E){}
						
				// Update configuration property
				if (update) {
					try {
						if (selPI.updateConfigEntry(db, reqValPropName,
								reqValPropValue)) {
							// Update the Plug-in Admin's information
							pa.pluginUpdated(pa.getPlugin(selPI));
							// Reload the PluginInfo object
							selPI = pa.getPluginInfo(reqValHashcode);
						} else {
							e.append("Property update" + " has failed!"
									+ " Check log for details.");
						}
					} catch (Exception ex) {
						e.append(ex.getMessage());
					}
				}
				// Create configuration property
				else {
					try {
						if (selPI.addConfigEntry(db, reqValPropName,
								reqValPropDescr, reqValPropType,
								reqValPropValue)) {
							// Update the Plug-in Admin's information
							pa.pluginUpdated(pa.getPlugin(selPI));
							// Reload the PluginInfo object
							selPI = pa.getPluginInfo(reqValHashcode);
						} else {
							e.append("Property creation" + " has failed!"
									+ " Check log for details.");
						}
					} catch (Exception ex) {
						e.append(ex.getMessage());
					}
				}
				// Return to the create/update view upon error
				if (e.toString().length() > 0) {
					if (update)
						reqValAction = actValReqUpdProp;
					else
						reqValAction = actValReqAddProp;
				}
			}
		}
        
        if(e.length()>0){
			vc.put("errors", e);
		}
		if (selPI != null && selPI.installed &&
				((reqValAction.equals(actValReqAddProp)) || reqValAction.equals(actValReqUpdProp))){
					boolean update=false;
					try{
						update = (selPI.hasConfProp(reqValPropName, reqValPropType));
					}
					catch(Exception E){}
			vc.put("update",update);
		}
        
        
		vc.put("reqValAction", reqValAction);
		vc.put("reqValHashcode", reqValHashcode);
		vc.put("reqValPropName", reqValPropName);
		vc.put("reqValPropDesc", reqValPropDescr);
		vc.put("reqValPropType", reqValPropType);
		vc.put("reqValPropValue", reqValPropValue);
		vc.put("reqValShowProp", reqValShowProp);
		vc.put("reqValShowActv", reqValShowActv);	
		return selPI;
	}
	
	/**
	 * Puts the correct HTML pages to the vc according to reqValAction servlet request (action)parameter which should
	 * be set in the vc beforehand
	 * @param vc The VelocityContext object
	 * @param selPI The selected plugin (if any)
	 */
	private void generateHTML(VelocityContext vc,PluginInfo selPI){ 
		if (selPI != null) {
			vc.put("plugin", selPI);
		}
		
		// ===============================================================
		// Create the form
		// ===============================================================
		vc.put("content","plugins/content.html");
		// ===============================================================
		// Display the accumulated error messages (if any)
		// ===============================================================
		if(vc.containsKey("errors")){
			vc.put("errorList", "plugins/errors.html");
		}

		if ((selPI != null)) {
			if ((selPI.installed)&&((vc.get("reqValAction").equals(actValReqAddProp)) || (vc.get("reqValAction")
					.equals(actValReqUpdProp)))) {
				// ===============================================================
				// "Create/update configuration property" editor
				// ===============================================================
			
				vc.put("createProperty", "plugins/createProperty.html");

				// Check for a property update request
				vc.put("ConfigurationTypes", ConfigurationType.values());
			} else {
				// ===============================================================
				// Plug-in editor
				// ===============================================================
				vc.put("pluginPage", "plugins/plugin.html");
					if (selPI.installed) {
						// Get the list of supported metrics
						List<Metric> metrics = pa.getPlugin(selPI)
								.getAllSupportedMetrics();
						vc.put("PluginMetrics", metrics);

						// Get the plug-in's configuration set
						Set<PluginConfiguration> config = Plugin
								.getPluginByHashcode(selPI.getHashcode())
								.getConfigurations();
						vc.put("pluginConfigs", config);
					}
			}

		}
		// ===============================================================
		// Plug-ins list
		// ===============================================================
		else {
			Collection<PluginInfo> l = pa.listPlugins();

			// Order by installed or not installed, otherwise don't touch ordering
			Collection<PluginInfo> ordered = new ArrayList<>();
			for(PluginInfo i : l) if(i.installed) ordered.add(i);
			for(PluginInfo i : l) if(!i.installed) ordered.add(i);

			vc.put("list", "plugins/list.html");
			vc.put("pluginList", ordered);
		}
	}
}