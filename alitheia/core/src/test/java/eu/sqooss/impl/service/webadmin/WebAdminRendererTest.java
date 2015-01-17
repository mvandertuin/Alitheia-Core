/****************************************************************
 * WARNING: THESE TEST REQUIRE JAVA 7u72 FOR POWERMOCK TO WORK! *
 * see https://code.google.com/p/powermock/issues/detail?id=504 *
 ****************************************************************/
package eu.sqooss.impl.service.webadmin;

import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.scheduler.Job;
import eu.sqooss.service.scheduler.SchedulerStats;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.*;

import static org.mockito.Mockito.*;

/**
 * A test verifying that the HTML output is unchanged before and after the refactor.
 */
@RunWith(PowerMockRunner.class)
public class WebAdminRendererTest extends HTMLTest {

    WebAdminRenderer webAdmin;
    
    // Init mock instances here, otherwise null in inserted into the ProjectsView and AlitheiaCore
    MockHttpServletRequest req = new MockHttpServletRequest();
    @Mock SchedulerStats s;
    
    VelocityContext vc;

    @Before
    public void setup() {
        prepareWithoutDI();

        webAdmin = new WebAdminRenderer(null);

        vc = new VelocityContext();
        vc.put("tr",new TranslationProxy());
        vc.put("adminView", webAdmin);
        vc.put("scheduler", s);
    }

    @Test
    public void testrenderJobFailStats(){
    	HashMap<String,Integer> fjobs = new HashMap<String,Integer>();
    	fjobs.put("Job1",1);
    	fjobs.put(null,0);
    	when(sched.getSchedulerStats()).thenReturn(s);
    	when(s.getFailedJobTypes()).thenReturn(fjobs);
    	
        webAdmin.action_index(req,vc);
        String result = render("jobs/index.html", req, vc);
    	assertWhiteSpaceEqual(renderJobFailStatsRF,result);
    }
    
    @Test
    public void testrenderFailedJobs(){
    	Job[] jobs = new Job[2];
    	Job j1 = mock(Job.class);
    	when(j1.toString()).thenReturn("Some Job");
    	jobs[0] = null;
    	jobs[1] = j1;
    	when(sched.getFailedQueue()).thenReturn(jobs);

        webAdmin.action_failed(req,vc);
        String result = render("jobs/failed.html", req, vc);
    	assertWhiteSpaceEqual(renderFailedJobsRF,result);
    	
    }
    
    @Test
    public void testrenderFailedJobsEmpty(){
    	when(sched.getFailedQueue()).thenReturn(null);

        webAdmin.action_failed(req,vc);
        String result = render("jobs/failed.html", req, vc);
    	assertWhiteSpaceEqual(renderFailedJobsEmptyRF,result);
    }
    
    @Test 
    public void testRenderLogs(){
    	String[] s = new String[2];
    	s[0] = "Some log";
    	s[1] = "Some other log";
    	when(lm.getRecentEntries()).thenReturn(s);
    	
        webAdmin.action_logs(req,vc);
        String result = render("logs.html", req, vc);
    	assertWhiteSpaceEqual(renderLogsRF,result);
    }
    
    @Test 
    public void testRenderLogsEmpty(){
    	when(lm.getRecentEntries()).thenReturn(null);
    	
        webAdmin.action_logs(req,vc);
        String result = render("logs.html", req, vc);
    	assertWhiteSpaceEqual(renderLogsEmptyRF,result);
    }
    
    @Test
    public void testRenderJobWaitStats(){
    	HashMap<String,Integer> fjobs = new HashMap<String,Integer>();
    	fjobs.put("Job1",1);
    	fjobs.put(null,0);
    	when(sched.getSchedulerStats()).thenReturn(s);
    	when(s.getWaitingJobTypes()).thenReturn(fjobs);
    	
        webAdmin.action_index(req,vc);
        String result = render("jobs/index.html", req, vc);
    	assertWhiteSpaceEqual(renderJobWaitStatsRF,result);
    }
    
    @Test
    public void testRenderJobRunStats(){
    	List<String> rjobs = new ArrayList<String>();
    	rjobs.add("Job1");
    	rjobs.add("Job2");

    	when(sched.getSchedulerStats()).thenReturn(s);
    	when(s.getRunJobs()).thenReturn(rjobs);
    	
        webAdmin.action_index(req,vc);
        String result = render("jobs/index.html", req, vc);
    	assertWhiteSpaceEqual(renderJobRunStatsRF,result);
    }
    
    @Test
    public void testRenderJobRunStatsEmpty(){
    	List<String> rjobs = new ArrayList<String>();

    	when(sched.getSchedulerStats()).thenReturn(s);
    	when(s.getRunJobs()).thenReturn(rjobs);
    	
        webAdmin.action_index(req,vc);
        String result = render("jobs/index.html", req, vc);
    	assertWhiteSpaceEqual(renderJobRunStatsEmptyRF,result);
    }

    private String renderJobFailStatsRF = "<h2>Jobstatistics</h2><ul><li>JobsExecuting:0</li><li>JobsWaiting:0</li><li>FailedJobs:0</li><li>WorkerThreads:0</li><li>TotalJobs:0</li></ul><h2>RunningJobs</h2><divid=\"bundles\">Norunningjobs</div><h2>WaitingJobsstatistics</h2><divid=\"bundles\">Nowaitingjobs</div><h2>Jobfailurestatistics</h2><ahref=\"alljobs\">Details...</a><divid=\"bundles\"><tablewidth=\"100%\"cellpadding=\"0\"cellspacing=\"0\"><thead><tr><td>JobType</td><td>NumJobsFailed</td></tr></thead><tbody><tr><td>Nofailures</td><td>&nbsp;</td></tr><tr><td>Job1</td><td>1</td></tr></tbody></table></div>";
    private String renderFailedJobsRF = "<h2>FailedJobs(Last1000)</h2><divid=\"bundles\"><tablewidth=\"100%\"cellpadding=\"0\"cellspacing=\"0\"><thead><tr><td>JobType</td><td>Exceptiontype</td><td>Exceptiontext</td><td>Exceptionbacktrace</td></tr></thead><tbody><tr><td><b>N/A</b></td><td><b>N/A</b></td><td><b>N/A</b></td><td><b>N/A</b><divid=\"end\"></div></td></tr><tr><td>SomeJob</td><td><b>N/A</b></td><td><b>N/A</b></td><td><b>N/A</b><divid=\"end\"></div></td></tr></tbody></table></div>";
    private String renderFailedJobsEmptyRF = "<h2>FailedJobs(Last1000)</h2><divid=\"bundles\"><tablewidth=\"100%\"cellpadding=\"0\"cellspacing=\"0\"><thead><tr><td>JobType</td><td>Exceptiontype</td><td>Exceptiontext</td><td>Exceptionbacktrace</td></tr></thead><tbody><tr><tdcolspan=\"4\">Nofailedjobs.</td></tr></tbody></table></div>";
    private String renderLogsRF = "<h2>AlitheiaLogs</h2><ulclass=\"logs\"><li>Somelog</li><li>Someotherlog</li></ul>";
    private String renderLogsEmptyRF = "<h2>AlitheiaLogs</h2><ulclass=\"logs\"><li>&lt;none&gt;</li></ul>";
    private String renderJobWaitStatsRF = "<h2>Jobstatistics</h2><ul><li>JobsExecuting:0</li><li>JobsWaiting:0</li><li>FailedJobs:0</li><li>WorkerThreads:0</li><li>TotalJobs:0</li></ul><h2>RunningJobs</h2><divid=\"bundles\">Norunningjobs</div><h2>WaitingJobsstatistics</h2><divid=\"bundles\"><tablewidth=\"100%\"cellpadding=\"0\"cellspacing=\"0\"><thead><tr><td>JobType</td><td>NumJobsWaiting</td></tr></thead><tbody><tr><td>Nofailures</td><td>&nbsp;</td></tr><tr><td>Job1</td><td>1</td></tr></tbody></table></div><h2>Jobfailurestatistics</h2><ahref=\"alljobs\">Details...</a><divid=\"bundles\">Nofailures</div>";
    private String renderJobRunStatsRF = "<h2>Jobstatistics</h2><ul><li>JobsExecuting:0</li><li>JobsWaiting:0</li><li>FailedJobs:0</li><li>WorkerThreads:0</li><li>TotalJobs:0</li></ul><h2>RunningJobs</h2><divid=\"bundles\"><ul><li>Job1</li><li>Job2</li></ul></div><h2>WaitingJobsstatistics</h2><divid=\"bundles\">Nowaitingjobs</div><h2>Jobfailurestatistics</h2><ahref=\"alljobs\">Details...</a><divid=\"bundles\">Nofailures</div>";
    private String renderJobRunStatsEmptyRF = "<h2>Jobstatistics</h2><ul><li>JobsExecuting:0</li><li>JobsWaiting:0</li><li>FailedJobs:0</li><li>WorkerThreads:0</li><li>TotalJobs:0</li></ul><h2>RunningJobs</h2><divid=\"bundles\">Norunningjobs</div><h2>WaitingJobsstatistics</h2><divid=\"bundles\">Nowaitingjobs</div><h2>Jobfailurestatistics</h2><ahref=\"alljobs\">Details...</a><divid=\"bundles\">Nofailures</div>";
    

} 