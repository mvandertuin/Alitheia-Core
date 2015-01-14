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

import eu.sqooss.service.admin.AdminService;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.util.Pair;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import com.google.inject.assistedinject.Assisted;

public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static BundleContext bc = null;

    /// Logger given by our owner to write log messages to.
    private Logger logger;
    
    private DBService db;
    private AdminService adminService;
    private Scheduler scheduler;

    // Content tables
    private List<Controller> controllerList = null;
    private Hashtable<String, String> templateContent = null;
    private Hashtable<String, Pair<String, String>> staticContentMap = null;

    VelocityEngine ve = null;

    // Renderer of content
    WebAdminRenderer adminView;

    // Plug-ins view
    PluginsView pluginsView;

    // Projects view
    ProjectsController projectsController;

    TranslationProxy tr = new TranslationProxy();
    
    @Inject
    public AdminServlet(@Assisted BundleContext bc,
            @Assisted Logger logger,
            VelocityEngine ve,
            VelocityContext vc,
            DBService db,
            AdminService adminService,
            Scheduler scheduler,
            WebAdminRendererFactory webAdminRendererFactory,
            PluginsViewFactory pluginsViewFactory,
            ProjectsViewFactory projectsViewFactory) {
        this.bc = bc;
        this.logger = logger;
        this.ve = ve;
        this.db = db;
        this.adminService = adminService;
        this.scheduler = scheduler;
        
        // Create the static content map
        staticContentMap = new Hashtable<String, Pair<String, String>>();
        addStaticContent("/screen.css", "text/css");
        addStaticContent("/webadmin.css", "text/css");
        addStaticContent("/sqo-oss.png", "image/x-png");
        addStaticContent("/queue.png", "image/x-png");
        addStaticContent("/uptime.png", "image/x-png");
        addStaticContent("/greyBack.jpg", "image/x-jpg");
        addStaticContent("/projects.png", "image/x-png");
        addStaticContent("/logs.png", "image/x-png");
        addStaticContent("/metrics.png", "image/x-png");
        addStaticContent("/gear.png", "image/x-png");
        addStaticContent("/header-repeat.png", "image/x-png");
        addStaticContent("/add_user.png", "image/x-png");
        addStaticContent("/edit.png", "image/x-png");
        addStaticContent("/jobs.png", "image/x-png");
        addStaticContent("/rules.png", "image/x-png");

        // Create the template content map
        templateContent = new Hashtable<>();
        templateContent.put("/users", "users.html");
        templateContent.put("/rules", "rules.html");
        templateContent.put("/jobstat", "jobstat.html");

        // Create the controller content map
        controllerList = new ArrayList<>();
        adminView = webAdminRendererFactory.create(bc);
        pluginsView = pluginsViewFactory.create(bc);
        projectsController = projectsViewFactory.create(bc);
        controllerList.add(adminView);
        controllerList.add(projectsController);
        controllerList.add(pluginsView);
    }

    /**
     * Add content to the static map
     */
    private void addStaticContent(String path, String type) {
        Pair<String, String> p = new Pair<String, String> (path,type);
        staticContentMap.put(path, p);
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException,
                                                              IOException {
        if (!db.isDBSessionActive()) {
            db.startDBSession();
        } 
        
        try {
            String query = request.getPathInfo();

            // Add the request to the log
            logger.debug("GET:" + query);

            // This is static content
            if (query.startsWith("/stop")) {
                VelocityContext vc = new VelocityContext();
                vc.put("RESULTS", "<p>Alitheia Core is now shutdown.</p>");
                sendPage(response, request, "/results.html", vc);

                // Now stop the system
                logger.info("System stopped by user request to webadmin.");
                try {
                    bc.getBundle(0).stop();
                } catch (BundleException be) {
                    logger.warn("Could not stop bundle 0.");
                    // And ignore
                }
                return;
            }
            if (query.startsWith("/restart")) {
                VelocityContext vc = new VelocityContext();
                vc.put("RESULTS", "<p>Alitheia Core is now restarting.</p>");
                sendPage(response, request, "/results.html", vc);

                //FIXME: How do we do a restart?
                return;
            }
            else if (staticContentMap.containsKey(query)) {
                sendResource(response, staticContentMap.get(query));
            }
            else if (templateContent.containsKey(query)) {
                sendPage(response, request, templateContent.get(query), new VelocityContext());
            }
            else if(!handleWithController(request, response)) {
                logger.warn("Request ("+ request.getMethod() + " " + request.getPathInfo() + ") was unhandled.");
            }
        } catch (NullPointerException e) {
            logger.warn("Got a NPE while rendering a page.",e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            if (db.isDBSessionActive()) {
                db.commitDBSession();
            }
        }
    }

    /**
     * A very basic Web MVC framework's routing
     * @param req
     * @param resp
     * @return A controller handles this request or not
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws ServletException
     * @throws IOException
     */
    protected boolean handleWithController(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, ServletException, IOException {
        VelocityContext vc = new VelocityContext();

        String query = req.getPathInfo();
        for(Controller view : controllerList){
            for(Method m : view.getClass().getMethods()){
                Action a = m.getAnnotation(Action.class);
                if(a != null && query.equals(a.uri()) && a.method().equals(req.getMethod())){
                    m.invoke(view, req, vc);
                    sendPage(resp, req, a.template(), vc);
                    return true;
                }
            }
        }
        return false;
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException,
                                                               IOException {
        if (!db.isDBSessionActive()) {
            db.startDBSession();
        } 
        
        try {
            logger.debug("POST:" + request.getPathInfo());

            if(handleWithController(request, response)){
                // already handled
            } else {
                doGet(request,response);
            }
        } catch (NullPointerException e) {
            logger.warn("Got a NPE while handling POST data.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            if (db.isDBSessionActive()) {
                db.commitDBSession();
            }
        }
    }
    
    /**
     * Sends a resource (stored in the jar file) as a response. The mime-type
     * is set to @p mimeType . The @p path to the resource should start
     * with a / .
     *
     * Test cases:
     *   - null mimetype, null path, bad path, relative path, path not found,
     *   - null response
     *
     * TODO: How to simulate conditions that will cause IOException
     */
    protected void sendResource(HttpServletResponse response, Pair<String,String> source)
        throws ServletException, IOException
    {
        InputStream istream = getClass().getResourceAsStream(source.first);
        if ( istream == null ) {
            throw new IOException("Path not found: " + source.first);
        }

        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        int totalBytes = 0;

        response.setContentType(source.second);
        ServletOutputStream ostream = response.getOutputStream();
        while ((bytesRead = istream.read(buffer)) > 0) {
            ostream.write(buffer,0,bytesRead);
            totalBytes += bytesRead;
        }
    }

    protected void sendPage(HttpServletResponse response, HttpServletRequest request, String path, VelocityContext vc)
            throws ServletException, IOException
    {
        Template t = null;
        try {
            t = ve.getTemplate( path );
        } catch (Exception e) {
            logger.warn("Failed to get template <" + path + ">");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        StringWriter writer = new StringWriter();
        PrintWriter print = response.getWriter();

        // Do any substitutions that may be required
        createSubstitutions(request, vc);
        response.setContentType("text/html");
        t.merge(vc, writer);

        print.print(writer.toString());
    }

    private void createSubstitutions(HttpServletRequest request, VelocityContext vc) {
        // Initialize the resource bundles with the provided locale
        Controller.initResources(Locale.ENGLISH);

        // Simple string substitutions
        vc.put("APP_NAME", Controller.getLbl("app_name"));
        vc.put("COPYRIGHT",
                "Copyright 2007-2008"
                + "<a href=\"http://www.sqo-oss.eu/about/\">"
                + "&nbsp;SQO-OSS Consortium Members"
                + "</a>");
        vc.put("LOGO", "<img src='/logo' id='logo' alt='Logo' />");
        vc.put("UPTIME", WebAdminRenderer.getUptime());

        // Object-based substitutions
        vc.put("scheduler", scheduler.getSchedulerStats());
        vc.put("tr",tr); // translations proxy
        vc.put("admin",adminView);
        vc.put("projects", projectsController);
        vc.put("metrics",pluginsView);
        vc.put("request", request); // The request can be used by the render() methods
    }

}

// vi: ai nosi sw=4 ts=4 expandtab
