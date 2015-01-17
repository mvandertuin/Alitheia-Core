/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2008 - 2010 - Organization for Free and Open Source Software,  
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

package eu.sqooss.core;

import eu.sqooss.service.admin.AdminService;
import eu.sqooss.service.cluster.ClusterNodeService;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.fds.FDSService;
import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.rest.RestService;
import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.tds.TDSService;
import eu.sqooss.service.updater.UpdaterService;
import eu.sqooss.service.webadmin.WebadminService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Startup class of the Alitheia framework's core. Its main goal is to
 * initialize all core components and be able to provide them upon request.
 * There is one AlitheiaCore instance which may be retrieved statically
 * through getInstance(); after that you can use the get*Service() methods
 * to get each of the other core components as needed.
 */
public class AlitheiaCore {

    /** The Core is singleton-line because it has a special instance */
    private static AlitheiaCore instance = null;

    /* Service Configuration */
    @Autowired
    LogManager logManager;
    @Autowired
    DBService dbService;
    @Autowired
    PluginAdmin pluginAdmin;
    @Autowired
    Scheduler scheduler;
    @Autowired
    TDSService tdsService;
    @Autowired
    ClusterNodeService clusterNodeService;
    @Autowired
    FDSService fdsService;
    @Autowired
    MetricActivator metricActivator;
    @Autowired
    UpdaterService updaterService;
    @Autowired
    WebadminService webadminService;
    @Autowired
    RestService restService;
    @Autowired
    AdminService adminService;

    /**
     * Simple constructor.
     */
    public AlitheiaCore() {
        instance = this;
        err("Instance Created");
    }

    /**
     * The core has a blessed instance which you can get from here;
     * that instance in turn will give you the DB service and others
     * that it holds on to. So code that needs a particular service
     * can use AlitheiaCore.getInstance().get*Service() to get
     * a reference to specific services.
     * 
     * @return Instance, or null if it's not initialized yet
     */
    public static AlitheiaCore getInstance() {
        return instance;
    }
    
    /*Create a temp instance to use for testing.*/
    public static AlitheiaCore testInstance() {
        instance = new AlitheiaCore();
        return instance;
    }

    /**
     * Returns the locally stored Logger component's instance.
     * 
     * @return The Logger component's instance.
     */
    public LogManager getLogManager() {
        return logManager;
    }

    /**
     * Returns the locally stored WebAdmin component's instance.
     * 
     * @return The WebAdmin component's instance.
     */
    public WebadminService getWebadminService() {
        return webadminService;
    }

    /**
     * Returns the locally stored Plug-in Admin component's instance.
     * 
     * @return The Plug-in Admin component's instance.
     */
    public PluginAdmin getPluginAdmin() {
        return pluginAdmin;
    }

    /**
     * Returns the locally stored DB component's instance.
     * 
     * @return The DB component's instance.
     */
    public DBService getDBService() {
        return dbService;
    }

    /**
     * Returns the locally stored FDS component's instance.
     * <br/>
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The FDS component's instance.
     */
    public FDSService getFDSService() {
        return fdsService;
    }

    /**
     * Returns the locally stored Scheduler component's instance.
     * <br/>
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The Scheduler component's instance.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Returns the locally stored Security component's instance.
     * <br/>
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The Security component's instance.
     */
    public SecurityManager getSecurityManager() {
        // FIXME: implement if needed
        return null;
    }

    /**
     * Returns the locally stored TDS component's instance.
     * <br/>
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The TDS component's instance.
     */
    public TDSService getTDSService() {
        return tdsService;
    }

    /**
     * Returns the locally stored Updater component's instance.
     * <br/>
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The Updater component's instance.
     */
    public UpdaterService getUpdater() {
        return updaterService;
    }

    /**
     * Returns the locally stored ClusterNodeService component's instance.
     * <br/>
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The ClusterNodeSerive component's instance.
     */
    public ClusterNodeService getClusterNodeService() {
        return clusterNodeService;
    }

    /**
     * Returns the locally stored Metric Activator component's instance.
     * 
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The Metric Activator component's instance.
     */
    public MetricActivator getMetricActivator() {
    	return metricActivator;
    }
    
    /**
     * Returns the locally stored Administration Service component's instance.
     * 
     * @return The Administration Service component's instance.
     */
    public AdminService getAdminService() {
    	return adminService;
    }
	
	private void err(String msg) {
		System.err.println("AlitheiaCore: " + msg);
	}
}

// vi: ai nosi sw=4 ts=4 expandtab
