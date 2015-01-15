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
import eu.sqooss.service.scheduler.Scheduler;
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

public class ProjectsController extends Controller {

    public static final String TEMPLATE = "projects.html";
    public static final String SUBTEMPLATE = "subtemplate";

    // Servlet parameters
    static String REQ_PAR_PROJECT_ID    = "projectId";
    static String REQ_PAR_PRJ_NAME      = "projectName";
    static String REQ_PAR_PRJ_WEB       = "projectHomepage";
    static String REQ_PAR_PRJ_CONT      = "projectContact";
    static String REQ_PAR_PRJ_BUG       = "projectBL";
    static String REQ_PAR_PRJ_MAIL      = "projectML";
    static String REQ_PAR_PRJ_CODE      = "projectSCM";
    
	static Scheduler sched;
    static PluginAdmin pa;
    static UpdaterService updater;
    static ClusterNodeService clusterNode;
    private MetricActivator ma;

    /**
     * Instantiates a new projects view.
     *
     * @param bundlecontext the <code>BundleContext</code> object
     */
    public ProjectsController(BundleContext bundlecontext) {
        super(bundlecontext);
		sched = AlitheiaCore.getInstance().getScheduler();
        pa = AlitheiaCore.getInstance().getPluginAdmin();
        updater = AlitheiaCore.getInstance().getUpdater();
        clusterNode = AlitheiaCore.getInstance().getClusterNodeService();
        ma = AlitheiaCore.getInstance().getMetricActivator();
    }

    private StoredProject selectedProject(HttpServletRequest req){
        if(req.getParameter(REQ_PAR_PROJECT_ID) != null)
            return StoredProject.loadDAObyId(new Long(req.getParameter(REQ_PAR_PROJECT_ID)), StoredProject.class);
        return null;
    }

    protected void general(HttpServletRequest req, VelocityContext vc, StoredProject selProject){
        // Initialize the resource bundles with the requests locale
        initResources(req.getLocale());

        vc.put("selectedProject", selProject);

        // Get this clusters projects
        Set<StoredProject> projects = ClusterNode.thisNode().getProjects();
        // Get the metrics
        vc.put("projectMetrics", pa.listPlugins());

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
                    nextPrj.getClusternode() != null ? nextPrj.getClusternode().getName() : "(local)"
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

    @Action(uri = "/projects", template = TEMPLATE)
    public void list(HttpServletRequest req, VelocityContext vc)
    {
        StoredProject selProject = selectedProject(req);

        vc.put(SUBTEMPLATE, selProject == null ? "projects/list.html" : "projects/edit.html");

        general(req, vc, selProject);
    }

    @Action(uri = "/projects_add", template = "projects.html")
    public void add(HttpServletRequest req, VelocityContext vc)
    {
        vc.put(SUBTEMPLATE, "projects/add.html");
        vc.put("form", projectTable(req));
    }

    private Table projectTable(HttpServletRequest req){
        return new Table(
            new InputRow("Project name",    "projectName",      req.getParameter(REQ_PAR_PRJ_NAME)),
            new InputRow("Homepage",        "projectHomepage",  req.getParameter(REQ_PAR_PRJ_WEB )),
            new InputRow("Contact e-mail",  "projectContact",   req.getParameter(REQ_PAR_PRJ_CONT)),
            new InputRow("Bug database",    "projectBL",        req.getParameter(REQ_PAR_PRJ_BUG )),
            new InputRow("Mailing list",    "projectML",        req.getParameter(REQ_PAR_PRJ_MAIL)),
            new InputRow("Source code",     "projectSCM",       req.getParameter(REQ_PAR_PRJ_CODE))
        );
    }

    @Action(uri = "/projects_add", template = TEMPLATE, method = Action.POST)
    public void addPost(HttpServletRequest req, VelocityContext vc)
    {
        AdminService as = AlitheiaCore.getInstance().getAdminService();
        AdminAction aa = as.create(AddProject.MNEMONIC);
        // Properties below correspond to where AddProject will be looking
        aa.addArg("scm",     req.getParameter(REQ_PAR_PRJ_CODE));
        aa.addArg("name",    req.getParameter(REQ_PAR_PRJ_NAME));
        aa.addArg("bts",     req.getParameter(REQ_PAR_PRJ_BUG));
        aa.addArg("mail",    req.getParameter(REQ_PAR_PRJ_MAIL));
        aa.addArg("web",     req.getParameter(REQ_PAR_PRJ_WEB));
        aa.addArg("contact", req.getParameter(REQ_PAR_PRJ_CONT));
        as.execute(aa);

        StoredProject proj = null;

        if (aa.hasErrors()) {
            error(vc, aa.errors());
            vc.put("form", projectTable(req));
        } else {
            result(vc, aa.results());
            proj = StoredProject.getProjectByName(req.getParameter(REQ_PAR_PRJ_NAME));
        }

        vc.put(SUBTEMPLATE, proj == null ? "projects/add.html" : "projects/list.html");

        general(req, vc, proj);
    }

    @Action(uri = "/projects_diradd", template = TEMPLATE, method = Action.POST)
    public void addDirectoryPost(HttpServletRequest req, VelocityContext vc)
    {
        AdminService as = AlitheiaCore.getInstance().getAdminService();
        AdminAction aa = as.create(AddProject.MNEMONIC);
        aa.addArg("dir", req.getParameter("properties"));
        as.execute(aa);

        vc.put(SUBTEMPLATE, "projects/edit.html");

        if(aa.hasErrors()){
            error(vc, aa.errors());
        } else {
            result(vc, "Added project " + req.getParameter(REQ_PAR_PRJ_NAME));
        }

        StoredProject selProject = StoredProject.getProjectByName(req.getParameter(REQ_PAR_PRJ_NAME));
        general(req, vc, selProject);
    }

    @Action(uri = "/projects_delete", template = TEMPLATE)
    public void deleteConfirm(HttpServletRequest req, VelocityContext vc)
    {
        StoredProject selProject = selectedProject(req);
        vc.put("selectedProject", selProject);
        vc.put(SUBTEMPLATE, "projects/delete_confirm.html");
    }

    @Action(uri = "/projects_delete", template = TEMPLATE, method = Action.POST)
    public void deleteConfirmed(HttpServletRequest req, VelocityContext vc){
        vc.put(SUBTEMPLATE, "projects/list.html");

        StoredProject selProject = selectedProject(req);
        if (selProject != null)
        {
            // Deleting large projects in the foreground is very slow
            ProjectDeleteJob pdj = new ProjectDeleteJob(sobjCore, selProject);
            try {
                sched.enqueue(pdj);
            } catch (SchedulerException e1) {
                error(vc, getErr("e0034"));
            }
            selProject = null;
        } else {
            error(vc, getErr("e0034"));
        }

        general(req, vc, selProject);
    }

    @Action(uri = "/projects_update", template = TEMPLATE, method = Action.POST)
    public void update(HttpServletRequest req, VelocityContext vc){
        vc.put(SUBTEMPLATE, "projects/list.html");
        String scope = req.getParameter("scope");
        StoredProject selProject = selectedProject(req);

        Set<StoredProject> projectList = null;
        if("host".equals(scope)){
            // For all projects on this host
            projectList = ClusterNode.thisNode().getProjects();
        } else if(selProject != null){
            // For single project
            projectList = Collections.singleton(selProject);
        } else {
            error(vc, "First select a project before selecting an updater.");
        }

        if(projectList != null)
        for (StoredProject project : projectList) {
            AdminService as = AlitheiaCore.getInstance().getAdminService();
            AdminAction aa = as.create(UpdateProject.MNEMONIC);
            aa.addArg("project", project.getId());
            if("single".equals(scope))
                aa.addArg("updater", req.getParameter("updater"));
            as.execute(aa);
            if (aa.hasErrors()) {
                error(vc, aa.errors());
            } else {
                result(vc, aa.results());
            }
        }

        general(req, vc, selProject);
    }

    @Action(uri = "/projects_sync", template = TEMPLATE, method = Action.POST)
    public void sync(HttpServletRequest req, VelocityContext vc){
        vc.put(SUBTEMPLATE, "projects/list.html");
        StoredProject selProject = selectedProject(req);
        String reqValSyncPlugin = req.getParameter("plugin");

        /* Try to sync */
        boolean done = false;
        if (reqValSyncPlugin != null && selProject != null) {
            PluginInfo pInfo = pa.getPluginInfo(reqValSyncPlugin);
            if (pInfo != null) {
                AlitheiaPlugin pObj = pa.getPlugin(pInfo);
                if (pObj != null) {
                    ma.syncMetric(pObj, selProject);
                    if(sobjLogger!=null)
                    sobjLogger.debug("Synchronise plugin (" + pObj.getName() + ") on project (" + selProject.getName() + ").");
                    done = true;
                }
            }
        }

        if (!done) {
            error(vc, "Could not synchronise plugin");
        } else {
            result(vc, "Synchronised plugin");
        }

        general(req, vc, selProject);
    }

}

// vi: ai nosi sw=4 ts=4 expandtab
