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

package eu.sqooss.impl.service.webadmin;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.util.StringUtils;
import org.apache.velocity.VelocityContext;
import org.joda.time.Period;
import org.osgi.framework.BundleContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * The WebAdminRender class provides functions for rendering content
 * to be displayed within the WebAdmin interface.
 *
 * @author, Paul J. Adams <paul.adams@siriusit.co.uk>
 * @author, Boryan Yotov <b.yotov@prosyst.com>
 */
public class WebAdminRenderer  extends Controller {
    public static final String SUBTEMPLATE = "subtemplate";
    /**
     * Represents the system time at which the WebAdminRender (and
     * thus the system) was started. This is required for the system
     * uptime display.
     */
    private static long startTime = new Date().getTime();

    private static final String TEMPLATE = "template.html";

    private Scheduler sched;
    private LogManager logManager;

    public WebAdminRenderer(BundleContext bundlecontext) {
        super(bundlecontext);
        sched = AlitheiaCore.getInstance().getScheduler();
        logManager = AlitheiaCore.getInstance().getLogManager();
    }

    @Action(uri = "/jobs", template = TEMPLATE)
    public void action_index(HttpServletRequest req, VelocityContext vc)
    {
        vc.put("section", 4);
        vc.put("stats", sched.getSchedulerStats());
        vc.put(SUBTEMPLATE, "jobs/index.html");
    }

    @Action(uri = "/alljobs", template = TEMPLATE)
    public void action_failed(HttpServletRequest req, VelocityContext vc)
    {
        vc.put("section", 4);
        vc.put("failed", sched.getFailedQueue());
        vc.put(SUBTEMPLATE, "jobs/failed.html");
    }

    @Action(uri = "/logs", template = TEMPLATE)
    public void action_logs(HttpServletRequest req, VelocityContext vc)
    {
        vc.put("section", 2);
        String[] names = logManager.getRecentEntries();
        if(names != null){
        	String[] safeNames = new String[names.length];
        	for(int i = 0; i<names.length; i++){
        		safeNames[i] = StringUtils.makeXHTMLSafe(names[i]);
        	}
        	vc.put("safeNames", safeNames);
        }
        vc.put(SUBTEMPLATE, "logs.html");
    }

    /**
     * Returns a string representing the uptime of the Alitheia core
     * in dd:hh:mm:ss format
     */
    public static String getUptime() {
        Period p = new Period(startTime, new Date().getTime());
        return String.format("%d:%02d:%02d:%02d", p.getDays(), p.getHours(), p.getMinutes(), p.getSeconds());
    }

}

//vi: ai nosi sw=4 ts=4 expandtab
