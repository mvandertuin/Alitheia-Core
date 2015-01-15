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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.*;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import org.apache.velocity.VelocityContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import com.google.inject.assistedinject.Assisted;

import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.Plugin;
import eu.sqooss.service.db.PluginConfiguration;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.pa.PluginInfo;
import eu.sqooss.service.util.StringUtils;

public class PluginsView extends Controller {

	private DBService db;
	private MetricActivator ma;
	private PluginAdmin pa;

	public PluginsView(BundleContext bundlecontext) {
		super(bundlecontext);
		db = AlitheiaCore.getInstance().getDBService();
		ma = AlitheiaCore.getInstance().getMetricActivator();
		pa = AlitheiaCore.getInstance().getPluginAdmin();
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

	@Action(uri = "/index", template = "index.html", method = "GET")
	public void indexGet(HttpServletRequest req, VelocityContext vc) {
		render(req, vc);
	}

	@Action(uri = "/index", template = "index.html", method = "POST")
	public void indexPost(HttpServletRequest req, VelocityContext vc) {
		render(req, vc);
	}

	@Action(uri = "/", template = "index.html", method = "GET")
	public void homeGet(HttpServletRequest req, VelocityContext vc) {
		render(req, vc);
	}

	@Action(uri = "/", template = "index.html", method = "POST")
	public void render(HttpServletRequest req, VelocityContext vc) {
		// Stores the accumulated error messages
		StringBuilder e = new StringBuilder();

		// Request parameters
		String reqParAction = "action";
		String reqParHashcode = "pluginHashcode";
		String reqParPropName = "propertyName";
		String reqParPropDescr = "propertyDescription";
		String reqParPropType = "propertyType";
		String reqParPropValue = "propertyValue";
		String reqParShowProp = "showProperties";
		String reqParShowActv = "showActivators";
		vc.put("reqParAction", reqParAction);
		vc.put("reqParHashcode", reqParHashcode);
		vc.put("reqParPropName", reqParPropName);
		vc.put("reqParPropDescr", reqParPropDescr);
		vc.put("reqParPropType", reqParPropType);
		vc.put("reqParPropValue", reqParPropValue);
		vc.put("reqParShowProp", reqParShowProp);
		vc.put("reqParShowActv", reqParShowActv);

		// ===============================================================
		// INPUT FIELDS
		// ===============================================================
		// "Action type" input field
		// Recognized "action" parameter's values
		String actValInstall = "installPlugin";
		String actValUninstall = "uninstallPlugin";
		String actValSync = "syncPlugin";
		String actValReqAddProp = "createProperty";
		String actValReqUpdProp = "updateProperty";
		String actValConAddProp = "confirmProperty";
		String actValConRemProp = "removeProperty";
		vc.put("actValInstall", actValInstall);
		vc.put("actValUninstall", actValUninstall);
		vc.put("actValSync", actValSync);
		vc.put("actValReqAddProp", actValReqAddProp);
		vc.put("actValReqUpdProp", actValReqUpdProp);
		vc.put("actValConAddProp", actValConAddProp);
		vc.put("actValConRemProp", actValConRemProp);

		// Request values
		String reqValAction = "";
		String reqValHashcode = null;
		String reqValPropName = null;
		String reqValPropDescr = null;
		String reqValPropType = null;
		String reqValPropValue = null;
		boolean reqValShowProp = false; // Show plug-in properties
		boolean reqValShowActv = false; // Show plug-in activators
		// Info object of the selected plug-in
		PluginInfo selPI = null;

		if (pa.listPlugins().isEmpty()) {
			vc.put("noPlugins", "plugins/noPlugins.html");
		}

		else {

			// ===============================================================
			// Parse the servlet's request object
			// ===============================================================
			if (req != null) {
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
					
					// Retrieve the selected plug-in's info object
					selPI = pa.getPluginInfo(reqValHashcode);
				}
			}
			// Plug-in info based actions
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
					boolean update = selPI.hasConfProp(reqValPropName,
							reqValPropType);
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

		// ===============================================================
		// Create the form
		// ===============================================================

		vc.put("reqValAction", reqValAction);
		vc.put("reqValHashcode", reqValHashcode);
		vc.put("reqValPropName", reqValPropName);
		vc.put("reqValPropDesc", reqValPropDescr);
		vc.put("reqValPropType", reqValPropType);
		vc.put("reqValPropValue", reqValPropValue);
		vc.put("reqValShowProp", reqValShowProp);
		vc.put("reqValShowActv", reqValShowActv);

		// ===============================================================
		// Display the accumulated error messages (if any)
		// ===============================================================
		vc.put("errors", e);
		vc.put("errorList", "plugins/errors.html");

		// Set the selected plugin to VC
		if (selPI != null) {
			vc.put("plugin", selPI);
		}

		if ((selPI != null) && (selPI.installed)) {
			if (((reqValAction.equals(actValReqAddProp)) || (reqValAction
					.equals(actValReqUpdProp)))) {
				// ===============================================================
				// "Create/update configuration property" editor
				// ===============================================================
				vc.put("createProperty", "plugins/createProperty.html");

				// Check for a property update request
				boolean update = selPI.hasConfProp(reqValPropName,
						reqValPropType);
				vc.put("update", update);
			} else {
				// ===============================================================
				// Plug-in editor
				// ===============================================================
				vc.put("pluginPage", "plugins/plugin.html");

				// Get the list of supported metrics
				List<Metric> metrics = pa.getPlugin(selPI)
						.getAllSupportedMetrics();
				vc.put("metrics", metrics);

				// Get the plug-in's configuration set
				Set<PluginConfiguration> config = Plugin.getPluginByHashcode(
						selPI.getHashcode()).getConfigurations();
				vc.put("pluginConfigs", config);
			}

		}
		// ===============================================================
		// Plug-ins list
		// ===============================================================
		else {
			Collection<PluginInfo> l = pa.listPlugins();
			vc.put("list", "plugins/list.html");
			vc.put("pluginList", l);
		}
		}
	}

}