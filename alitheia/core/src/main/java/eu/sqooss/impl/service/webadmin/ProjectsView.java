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

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import eu.sqooss.service.cluster.ClusterNodeService;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import org.apache.velocity.VelocityContext;
import org.osgi.framework.BundleContext;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.abstractmetric.AlitheiaPlugin;
import eu.sqooss.service.admin.AdminAction;
import eu.sqooss.service.admin.AdminService;
import eu.sqooss.service.admin.actions.AddProject;
import eu.sqooss.service.admin.actions.UpdateProject;
import eu.sqooss.service.db.Bug;
import eu.sqooss.service.db.ClusterNode;
import eu.sqooss.service.db.MailMessage;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.pa.PluginInfo;
import eu.sqooss.service.scheduler.SchedulerException;
import eu.sqooss.service.updater.UpdaterService;
import eu.sqooss.service.updater.UpdaterService.UpdaterStage;

public class ProjectsView extends AbstractView {
    // Script for submitting this page
    static String SUBMIT = "document.projects.submit();";

    // Action parameter's values
    static String ACT_REQ_ADD_PROJECT   = "reqAddProject";
    static String ACT_CON_ADD_PROJECT   = "conAddProject";
    static String ACT_REQ_REM_PROJECT   = "reqRemProject";
    static String ACT_CON_REM_PROJECT   = "conRemProject";
    static String ACT_REQ_SHOW_PROJECT  = "conShowProject";
    static String ACT_CON_UPD_ALL       = "conUpdateAll";
    static String ACT_CON_UPD           = "conUpdate";
    static String ACT_CON_UPD_ALL_NODE  = "conUpdateAllOnNode";

    // Servlet parameters
    static String REQ_PAR_ACTION        = "reqAction";
    static String REQ_PAR_PROJECT_ID    = "projectId";
    static String REQ_PAR_PRJ_NAME      = "projectName";
    static String REQ_PAR_PRJ_WEB       = "projectHomepage";
    static String REQ_PAR_PRJ_CONT      = "projectContact";
    static String REQ_PAR_PRJ_BUG       = "projectBL";
    static String REQ_PAR_PRJ_MAIL      = "projectML";
    static String REQ_PAR_PRJ_CODE      = "projectSCM";
    static String REQ_PAR_SYNC_PLUGIN   = "reqParSyncPlugin";
    static String REQ_PAR_UPD           = "reqUpd";

    static PluginAdmin pa;
    static UpdaterService updater;
    static ClusterNodeService clusterNode;
    static AdminService admin;
    private MetricActivator ma;

    /**
     * Instantiates a new projects view.
     *
     * @param bundlecontext the <code>BundleContext</code> object
     */
    public ProjectsView(BundleContext bundlecontext) {
        super(bundlecontext);
        pa = AlitheiaCore.getInstance().getPluginAdmin();
        updater = AlitheiaCore.getInstance().getUpdater();
        clusterNode = AlitheiaCore.getInstance().getClusterNodeService();
        admin = AlitheiaCore.getInstance().getAdminService();
        ma = AlitheiaCore.getInstance().getMetricActivator();
    }

    private StoredProject selectedProject(HttpServletRequest req){
        if(req.getParameter(REQ_PAR_PROJECT_ID) != null)
            return StoredProject.loadDAObyId(new Long(req.getParameter(REQ_PAR_PROJECT_ID)), StoredProject.class);
        return null;
    }

    protected void general(HttpServletRequest req, VelocityContext vc, StoredProject selProject){
        // Initialize the resource bundles with the request's locale
        initResources(req.getLocale());

        // Get this clusters projects
        Set<StoredProject> projects = ClusterNode.thisNode().getProjects();
        // Get the metrics
        Collection<PluginInfo> metrics = pa.listPlugins();

        // Prepare list of projects
        List<ProjectViewModel> projs = new ArrayList<>();
        for (StoredProject nextPrj : projects) {
            if(nextPrj == null) break;

            ProjectVersion v = null;
            MailMessage mm = null;
            Bug bug = null;
            String error = null;
            try {
                v = ProjectVersion.getLastProjectVersion(nextPrj);
                mm = MailMessage.getLatestMailMessage(nextPrj);
                bug = Bug.getLastUpdate(nextPrj);
            } catch (Exception e){
                error = e.getMessage();
            }

            error = (error == null ? getLbl("l0051") : error);

            projs.add(new ProjectViewModel(
                    selProject != null && selProject.getId() == nextPrj.getId(),
                    nextPrj.getId(),
                    nextPrj.getName(),
                    v == null ? null : String.valueOf(v.getSequence()),
                    v == null ? null : v.getRevisionId(),
                    mm == null ? error : mm.getSendDate() + "",
                    bug == null ? error : bug.getBugID(),
                    nextPrj.isEvaluated() ? getLbl("project_is_evaluated") : getLbl("project_not_evaluated"),
                    nextPrj.getClusternode() != null ? nextPrj.getClusternode().getName() : "(local)",
                    metrics
            ));
        }

        // Prepare list of updaters
        if(selProject != null) {
            try {
                vc.put("updatersImport", updater.getUpdaters(selProject, UpdaterStage.IMPORT));
                vc.put("updatersParse", updater.getUpdaters(selProject, UpdaterStage.PARSE));
                vc.put("updatersInference", updater.getUpdaters(selProject, UpdaterStage.INFERENCE));
                vc.put("updatersDefault", updater.getUpdaters(selProject, UpdaterStage.DEFAULT));
            } catch (NullPointerException e){
                error(vc, e.getMessage() + "<br>" + e.getStackTrace()[0]);
            }
        }

        vc.put("hostname", clusterNode.getClusterNodeName());

        vc.put("projectModels", projs);
    }

    @Action(uri = "/projects", template = "projects.html")
    public void list(HttpServletRequest req, VelocityContext vc)
    {
        StoredProject selProject = selectedProject(req);
        vc.put("selectedProject", selProject);

        if(selProject == null)
            vc.put("subtemplate", "projects/list.html");
        else
            vc.put("subtemplate", "projects/edit.html");

        general(req, vc, selProject);
    }

    @Action(uri = "/projects_add", template = "projects.html")
    public void add(HttpServletRequest req, VelocityContext vc)
    {
        vc.put("subtemplate", "projects/add.html");
    }

    @Action(uri = "/projects_add", template = "projects.html", method = "POST")
    public void addPost(HttpServletRequest req, VelocityContext vc)
    {
        StringBuilder e = new StringBuilder();
        StoredProject proj = addProject(vc, req);

        vc.put("subtemplate", "projects/add.html");
        general(req, vc, proj);
    }

    @Action(uri = "/projects_diradd", template = "projects.html", method = "POST")
    public void addDirectoryPost(HttpServletRequest req, VelocityContext vc)
    {
        AdminAction aa = admin.create(AddProject.MNEMONIC);
        aa.addArg("dir", req.getParameter("properties"));
        admin.execute(aa);

        vc.put("subtemplate", "projects/edit.html");

        if(aa.hasErrors()){
            error(vc, aa.errors());
        } else {
            result(vc, "Added project " + req.getParameter(REQ_PAR_PRJ_NAME));
        }

        general(req, vc, StoredProject.getProjectByName(req.getParameter(REQ_PAR_PRJ_NAME)));
    }

    @Action(uri = "/projects_delete", template = "projects.html", method = "GET")
    public void deleteConfirm(HttpServletRequest req, VelocityContext vc)
    {
        StoredProject selProject = selectedProject(req);
        vc.put("selectedProject", selProject);
        vc.put("subtemplate", "projects/delete_confirm.html");
    }

    @Action(uri = "/projects_delete", template = "projects.html", method = "POST")
    public void deleteConfirmed(HttpServletRequest req, VelocityContext vc){
        vc.put("subtemplate", "projects/list.html");

        StoredProject selProject = selectedProject(req);
        StringBuilder e = new StringBuilder();
        selProject = removeProject(vc, selProject, 0);

        general(req, vc, selProject);
    }

    @Action(uri = "/projects_update", template = "projects.html", method = "POST")
    public void update(HttpServletRequest req, VelocityContext vc){
        vc.put("subtemplate", "projects/list.html");
        String scope = req.getParameter("scope");
        StoredProject selProject = selectedProject(req);

        if(selProject == null){
            error(vc, "First select a project before selecting an updater.");
            return;
        } else if(scope.equals("single")){
            triggerUpdate(vc, selProject, req.getParameter("updater"));
        } else if(scope.equals("all")) {
            triggerAllUpdate(vc, selProject);
        } else if(scope.equals("host")){
            triggerAllUpdateNode(vc, selProject);
        }

        general(req, vc, selProject);
    }

    @Action(uri = "/projects_sync", template = "projects.html", method = "POST")
    public void sync(HttpServletRequest req, VelocityContext vc){
        vc.put("subtemplate", "projects/list.html");
        StoredProject selProject = selectedProject(req);
        String reqValSyncPlugin = req.getParameter("plugin");
        syncPlugin(vc, selProject, reqValSyncPlugin);

        general(req, vc, selProject);
    }

    /**
     * Renders the various project's views.
     *
     * @param req the servlet's request object
     *
     * @return The HTML presentation of the generated view.
     */
    public String render(HttpServletRequest req) {
        return "";
    }
  
    private StoredProject addProject(VelocityContext vc, HttpServletRequest r) {
    	AdminAction aa = admin.create(AddProject.MNEMONIC);
    	aa.addArg("scm", r.getParameter(REQ_PAR_PRJ_CODE));
    	aa.addArg("name", r.getParameter(REQ_PAR_PRJ_NAME));
    	aa.addArg("bts", r.getParameter(REQ_PAR_PRJ_BUG));
    	aa.addArg("mail", r.getParameter(REQ_PAR_PRJ_MAIL));
    	aa.addArg("web", r.getParameter(REQ_PAR_PRJ_WEB));
    	aa.addArg("contact", r.getParameter(REQ_PAR_PRJ_CONT));
		admin.execute(aa);

    	if (aa.hasErrors()) {
            error(vc, aa.errors());
            StoredProject falsy = new StoredProject(r.getParameter(REQ_PAR_PRJ_NAME));
            falsy.setScmUrl(r.getParameter(REQ_PAR_PRJ_CODE));
            falsy.setBtsUrl(r.getParameter(REQ_PAR_PRJ_BUG));
            falsy.setMailUrl(r.getParameter(REQ_PAR_PRJ_MAIL));
            falsy.setWebsiteUrl(r.getParameter(REQ_PAR_PRJ_WEB));
            falsy.setContactUrl(r.getParameter(REQ_PAR_PRJ_CONT));
            return falsy;
    	} else {
            result(vc, aa.results());
            return StoredProject.getProjectByName(r.getParameter(REQ_PAR_PRJ_NAME));
    	}
    }
    
    // ---------------------------------------------------------------
    // Remove project
    // ---------------------------------------------------------------
    private StoredProject removeProject(VelocityContext vc, StoredProject selProject, int indent) {
    	if (selProject != null) {
			// Deleting large projects in the foreground is
			// very slow
			ProjectDeleteJob pdj = new ProjectDeleteJob(sobjCore, selProject);
			try {
				sobjSched.enqueue(pdj);
			} catch (SchedulerException e1) {
                error(vc, getErr("e0034"));
			}
			selProject = null;
        } else {
            error(vc, getErr("e0034"));
		}
    	return selProject;
    }

	// ---------------------------------------------------------------
	// Trigger an update
	// ---------------------------------------------------------------
	private void triggerUpdate(VelocityContext vc, StoredProject selProject, String mnem) {
		AdminAction aa = admin.create(UpdateProject.MNEMONIC);
		aa.addArg("project", selProject.getId());
		aa.addArg("updater", mnem);
		admin.execute(aa);

		if (aa.hasErrors()) {
            error(vc, aa.errors());
        } else {
            result(vc, aa.results());
        }
	}

	// ---------------------------------------------------------------
	// Trigger update on all resources for that project
	// ---------------------------------------------------------------
	private void triggerAllUpdate(VelocityContext vc, StoredProject selProject) {
        AdminAction aa = admin.create(UpdateProject.MNEMONIC);
        aa.addArg("project", selProject.getId());
        admin.execute(aa);

        if (aa.hasErrors()) {
            error(vc, aa.errors());
        } else {
            result(vc, aa.results());
        }
	}
	
	// ---------------------------------------------------------------
	// Trigger update on all resources on all projects of a node
	// ---------------------------------------------------------------
    private void triggerAllUpdateNode(VelocityContext vc, StoredProject selProject) {
		Set<StoredProject> projectList = ClusterNode.thisNode().getProjects();
		
		for (StoredProject project : projectList) {
			triggerAllUpdate(vc, project);
		}
	}
	
	// ---------------------------------------------------------------
	// Trigger synchronize on the selected plug-in for that project
	// ---------------------------------------------------------------
    private void syncPlugin(VelocityContext vc, StoredProject selProject, String reqValSyncPlugin) {
        boolean done = false;

		if ((reqValSyncPlugin != null) && (selProject != null)) {
			PluginInfo pInfo = sobjPA.getPluginInfo(reqValSyncPlugin);
			if (pInfo != null) {
				AlitheiaPlugin pObj = sobjPA.getPlugin(pInfo);
				if (pObj != null) {
					ma.syncMetric(pObj, selProject);
					sobjLogger.debug("Synchronise plugin (" + pObj.getName()
							+ ") on project (" + selProject.getName() + ").");

                    done = true;
				}
			}
		}

        if (done) {
            error(vc, "Could not synchronise plugin");
        } else {
            result(vc, "Synchronised plugin");
        }
    }

}

// vi: ai nosi sw=4 ts=4 expandtab

