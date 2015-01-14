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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.VelocityContext;
import org.osgi.framework.BundleContext;

import com.google.inject.assistedinject.Assisted;

import eu.sqooss.service.abstractmetric.AlitheiaPlugin;
import eu.sqooss.service.admin.AdminAction;
import eu.sqooss.service.admin.AdminService;
import eu.sqooss.service.admin.actions.AddProject;
import eu.sqooss.service.admin.actions.UpdateProject;
import eu.sqooss.service.cluster.ClusterNodeService;
import eu.sqooss.service.db.Bug;
import eu.sqooss.service.db.ClusterNode;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.MailMessage;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.pa.PluginInfo;
import eu.sqooss.service.scheduler.Scheduler;
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
    
    private DBService db;
    private Scheduler sched;
    private MetricActivator ma;
    private PluginAdmin pa;
    private UpdaterService updater;
    private ClusterNodeService clusterNode;
    private AdminService admin;
    
    private ProjectDeleteJobFactory projDelJobFactory;
    
    /**
     * Instantiates a new projects view.
     *
     * @param vc the <code>VelocityContext</code> object
     * @param bundlecontext the <code>BundleContext</code> object
     */
    @Inject
    public ProjectsView(@Assisted BundleContext bundlecontext,
                        DBService db, Scheduler sched, MetricActivator ma, PluginAdmin pa,
                        UpdaterService updater, ClusterNodeService clusterNode, AdminService admin,
                        ProjectDeleteJobFactory projDelJobFactory) {
        super(bundlecontext);
        this.db = db;
        this.sched = sched;
        this.ma = ma;
        this.pa = pa;
        this.updater = updater;
        this.clusterNode = clusterNode;
        this.admin = admin;
        this.projDelJobFactory = projDelJobFactory;
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

            ProjectVersion v = ProjectVersion.getLastProjectVersion(nextPrj);
            MailMessage mm = MailMessage.getLatestMailMessage(nextPrj);
            Bug bug = Bug.getLastUpdate(nextPrj);

            projs.add(new ProjectViewModel(
                    selProject != null && selProject.getId() == nextPrj.getId(),
                    nextPrj.getId(),
                    nextPrj.getName(),
                    v == null ? getLbl("l0051") : String.valueOf(v.getSequence()) + "(" + v.getRevisionId() + ")",
                    mm == null ? getLbl("l0051") : mm.getSendDate() + "",
                    bug == null ? getLbl("l0051") : bug.getBugID(),
                    nextPrj.isEvaluated() ? getLbl("project_is_evaluated") : getLbl("project_not_evaluated"),
                    nextPrj.getClusternode() != null ? nextPrj.getClusternode().getName() : "(local)",
                    metrics
            ));
        }

        // Prepare list of updaters
        if(selProject != null) {
            vc.put("updatersImport", updater.getUpdaters(selProject, UpdaterStage.IMPORT));
            vc.put("updatersParse", updater.getUpdaters(selProject, UpdaterStage.PARSE));
            vc.put("updatersInference", updater.getUpdaters(selProject, UpdaterStage.INFERENCE));
            vc.put("updatersDefault", updater.getUpdaters(selProject, UpdaterStage.DEFAULT));
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
        StoredProject proj = addProject(e, req, 0);

        if(proj == null){
            vc.put("subtemplate", "errormessage.html");
            vc.put("RESULTS", e);
        } else {
            vc.put("subtemplate", "projects/edit.html");
            general(req, vc, proj);
        }
    }

    @Action(uri = "/projects_diradd", template = "projects.html", method = "POST")
    public void addDirectoryPost(HttpServletRequest req, VelocityContext vc)
    {
        AdminAction aa = admin.create(AddProject.MNEMONIC);
        aa.addArg("dir", req.getParameter("properties"));
        admin.execute(aa);

        if(aa.hasErrors()){
            vc.put("subtemplate", "errormessage.html");
            vc.put("RESULTS", aa.hasErrors());
        } else {
            vc.put("subtemplate", "projects/edit.html");
            general(req, vc, StoredProject.getProjectByName(req.getParameter(REQ_PAR_PRJ_NAME)));
        }
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
//        selProject = removeProject(e, selProject, 0);
//        if(selProject == null){
//            // OK
//        }

        System.out.println("[POST] DELETE");
    }

    @Action(uri = "projects_update", template = "projects.html", method = "POST")
    public void update(HttpServletRequest req, VelocityContext vc){
        vc.put("subtemplate", "projects/list.html");
        String scope = req.getParameter("scope");
        System.out.println("[POST] UPDATE "+scope);
    }

    @Action(uri = "projects_sync", template = "projects.html", method = "POST")
    public void sync(HttpServletRequest req, VelocityContext vc){
        vc.put("subtemplate", "projects/list.html");
        System.out.println("[POST] SYNC");
    }

    /**
     * Renders the various project's views.
     *
     * @param req the servlet's request object
     *
     * @return The HTML presentation of the generated view.
     */
    public String render(HttpServletRequest req) {
        // Stores the assembled HTML content
        StringBuilder b = new StringBuilder("\n");
        // Stores the accumulated error messages
        StringBuilder e = new StringBuilder();
        // Indentation spacer
        int in = 6;

        // Initialize the resource bundles with the request's locale
        initResources(req.getLocale());

        // Request values
        String reqValAction        = "";
        Long   reqValProjectId     = null;

        // Selected project
        StoredProject selProject = null;

        // ===============================================================
        // Parse the servlet's request object
        // ===============================================================
        if (req != null) {
            // DEBUG: Dump the servlet's request parameter
            if (DEBUG) {
                b.append(debugRequest(req));
            }

            // Retrieve the selected editor's action (if any)
            reqValAction = req.getParameter(REQ_PAR_ACTION);
            
            // Retrieve the selected project's DAO (if any)
            reqValProjectId = fromString(req.getParameter(REQ_PAR_PROJECT_ID));
            if (reqValProjectId != null) {
                selProject = db.findObjectById(
                        StoredProject.class, reqValProjectId);
            }
            
            if (reqValAction == null) {
                reqValAction = "";
            } else if (reqValAction.equals(ACT_CON_ADD_PROJECT)) {
            	selProject = addProject(e, req, in);
            } else if (reqValAction.equals(ACT_CON_REM_PROJECT)) {
            	selProject = removeProject(e, selProject, in);
            } else if (reqValAction.equals(ACT_CON_UPD)) {
            	triggerUpdate(e, selProject, in, req.getParameter(REQ_PAR_UPD));
            } else if (reqValAction.equals(ACT_CON_UPD_ALL)) {
            	triggerAllUpdate(e, selProject, in);
            } else if (reqValAction.equals(ACT_CON_UPD_ALL_NODE)) {
            	triggerAllUpdateNode(e, selProject, in);
            } else {
            	// Retrieve the selected plug-in's hash-code
        		String reqValSyncPlugin = req.getParameter(REQ_PAR_SYNC_PLUGIN);
        		syncPlugin(e, selProject, reqValSyncPlugin);
            }
        }
        createFrom(b, e, selProject, reqValAction , in);
        return b.toString();
    }
  
    private StoredProject addProject(StringBuilder e, HttpServletRequest r, int indent) {
    	AdminAction aa = admin.create(AddProject.MNEMONIC);
    	aa.addArg("scm", r.getParameter(REQ_PAR_PRJ_CODE));
    	aa.addArg("name", r.getParameter(REQ_PAR_PRJ_NAME));
    	aa.addArg("bts", r.getParameter(REQ_PAR_PRJ_BUG));
    	aa.addArg("mail", r.getParameter(REQ_PAR_PRJ_MAIL));
    	aa.addArg("web", r.getParameter(REQ_PAR_PRJ_WEB));
    	admin.execute(aa);
    	
//    	if (aa.hasErrors()) {
//            vc.put("RESULTS", aa.errors());
//            return null;
//    	} else {
//            vc.put("RESULTS", aa.results());
            return StoredProject.getProjectByName(r.getParameter(REQ_PAR_PRJ_NAME));
//    	}
    }
    
    // ---------------------------------------------------------------
    // Remove project
    // ---------------------------------------------------------------
    private StoredProject removeProject(StringBuilder e, 
    		StoredProject selProject, int indent) {
    	if (selProject != null) {
			// Deleting large projects in the foreground is
			// very slow
    	    ProjectDeleteJob pdj = projDelJobFactory.create(selProject);
			try {
				sched.enqueue(pdj);
			} catch (SchedulerException e1) {
				e.append(sp(indent)).append(getErr("e0034")).append("<br/>\n");
			}
			selProject = null;
		} else {
			e.append(sp(indent) + getErr("e0034") + "<br/>\n");
		}
    	return selProject;
    }

	// ---------------------------------------------------------------
	// Trigger an update
	// ---------------------------------------------------------------
	private void triggerUpdate(StringBuilder e,
			StoredProject selProject, int indent, String mnem) {
		AdminAction aa = admin.create(UpdateProject.MNEMONIC);
		aa.addArg("project", selProject.getId());
		aa.addArg("updater", mnem);
		admin.execute(aa);

//		if (aa.hasErrors()) {
//            vc.put("RESULTS", aa.errors());
//        } else {
//            vc.put("RESULTS", aa.results());
//        }
	}

	// ---------------------------------------------------------------
	// Trigger update on all resources for that project
	// ---------------------------------------------------------------
	private void triggerAllUpdate(StringBuilder e,
			StoredProject selProject, int indent) {
        AdminAction aa = admin.create(UpdateProject.MNEMONIC);
        aa.addArg("project", selProject.getId());
        admin.execute(aa);

//        if (aa.hasErrors()) {
//            vc.put("RESULTS", aa.errors());
//        } else {
//            vc.put("RESULTS", aa.results());
//        }
	}
	
	// ---------------------------------------------------------------
	// Trigger update on all resources on all projects of a node
	// ---------------------------------------------------------------
    private void triggerAllUpdateNode(StringBuilder e,
			StoredProject selProject, int in) {
		Set<StoredProject> projectList = ClusterNode.thisNode().getProjects();
		
		for (StoredProject project : projectList) {
			triggerAllUpdate(e, project, in);
		}
	}
	
	// ---------------------------------------------------------------
	// Trigger synchronize on the selected plug-in for that project
	// ---------------------------------------------------------------
    private void syncPlugin(StringBuilder e, StoredProject selProject, String reqValSyncPlugin) {
		if ((reqValSyncPlugin != null) && (selProject != null)) {
			PluginInfo pInfo = pa.getPluginInfo(reqValSyncPlugin);
			if (pInfo != null) {
				AlitheiaPlugin pObj = pa.getPlugin(pInfo);
				if (pObj != null) {
					ma.syncMetric(pObj, selProject);
					sobjLogger.debug("Syncronise plugin (" + pObj.getName()
							+ ") on project (" + selProject.getName() + ").");
				}
			}
		}
    }
    
    private void createFrom(StringBuilder b, StringBuilder e, 
    		StoredProject selProject, String reqValAction, int in) {

        // ===============================================================
        // Create the form
        // ===============================================================
        b.append(sp(in) + "<form id=\"projects\""
                + " name=\"projects\""
                + " method=\"post\""
                + " action=\"/projects\">\n");

        // ===============================================================
        // Display the accumulated error messages (if any)
        // ===============================================================
        b.append(errorFieldset(e, ++in));

        // Get the complete list of projects stored in the SQO-OSS framework
        Set<StoredProject> projects = ClusterNode.thisNode().getProjects();
        Collection<PluginInfo> metrics = pa.listPlugins();

        // ===============================================================
        // INPUT FIELDS
        // ===============================================================
        addHiddenFields(selProject,b,in);

        // ===============================================================
        // Close the form
        // ===============================================================
        b.append(sp(--in) + "</form>\n");
    }


    private static void addHiddenFields(StoredProject selProject,
            StringBuilder b,
            long in) {
        // "Action type" input field
        b.append(sp(in) + "<input type='hidden' id='" + REQ_PAR_ACTION + 
                "' name='" + REQ_PAR_ACTION + "' value=''>\n");
        // "Project Id" input field
        b.append(sp(in) + "<input type='hidden' id='" + REQ_PAR_PROJECT_ID +
                "' name='" + REQ_PAR_PROJECT_ID +
                "' value='" + ((selProject != null) ? selProject.getId() : "") +
                "'>\n");
        // "Plug-in hashcode" input field
        b.append(sp(in) + "<input type='hidden' id='" + REQ_PAR_SYNC_PLUGIN +
                "' name='" + REQ_PAR_SYNC_PLUGIN + 
                "' value=''>\n");
    }
    
    private static void showLastAppliedVersion(
            StoredProject project,
            Collection<PluginInfo> metrics,
            StringBuilder b) {
        for(PluginInfo m : metrics) {
            if (m.installed) {
                b.append("<tr>\n");
                b.append(sp(1) + "<td colspan=\"7\""
                        + " class=\"noattr\">\n"
                        + "<input type=\"button\""
                        + " class=\"install\""
                        + " style=\"width: 100px;\""
                        + " value=\"Synchronise\""
                        + " onclick=\"javascript:"
                        + "document.getElementById('"
                        + REQ_PAR_SYNC_PLUGIN + "').value='"
                        + m.getHashcode() + "';"
                        + SUBMIT + "\""
                        + ">"
                        + "&nbsp;"
                        + m.getPluginName()
                        + "</td>\n");
                b.append("</tr>\n");
            }
        }
    }

    private static void addHeaderRow(StringBuilder b, long in) {
        //----------------------------------------------------------------
        // Create the header row
        //----------------------------------------------------------------
        b.append(sp(in++) + "<table>\n");
        b.append(sp(in++) + "<thead>\n");
        b.append(sp(in++) + "<tr class=\"head\">\n");
        b.append(sp(in) + "<td class='head'  style='width: 10%;'>"
                + getLbl("l0066")
                + "</td>\n");
        b.append(sp(in) + "<td class='head' style='width: 35%;'>"
                + getLbl("l0067")
                + "</td>\n");
        b.append(sp(in) + "<td class='head' style='width: 15%;'>"
                + getLbl("l0068")
                + "</td>\n");
        b.append(sp(in) + "<td class='head' style='width: 15%;'>"
                + getLbl("l0069")
                + "</td>\n");
        b.append(sp(in) + "<td class='head' style='width: 15%;'>"
                + getLbl("l0070")
                + "</td>\n");
        b.append(sp(in) + "<td class='head' style='width: 10%;'>"
                + getLbl("l0071")
                + "</td>\n");
        b.append(sp(in) + "<td class='head' style='width: 10%;'>"
                + getLbl("l0073")
                + "</td>\n");
        b.append(sp(--in) + "</tr>\n");
        b.append(sp(--in) + "</thead>\n");
    }
}

// vi: ai nosi sw=4 ts=4 expandtab

