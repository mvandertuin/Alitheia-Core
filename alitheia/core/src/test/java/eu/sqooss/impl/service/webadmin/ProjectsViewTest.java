package eu.sqooss.impl.service.webadmin;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.core.AlitheiaCoreService;
import eu.sqooss.service.admin.AdminAction;
import eu.sqooss.service.admin.AdminService;
import eu.sqooss.service.cluster.ClusterNodeService;
import eu.sqooss.service.db.ClusterNode;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.scheduler.Scheduler;
import junit.framework.ComparisonFailure;
import org.apache.velocity.VelocityContext;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.mail.Store;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * A test verifying that the HTML output is unchanged before and after the refactor.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectsViewTest {
    static long failid;
    static long successid;

    @InjectMocks AlitheiaCore core;
    @InjectMocks ProjectsView projectsView;

    // Init mock instances here, otherwise null in inserted into the ProjectsView and AlitheiaCore
    @Mock DBService dbService;
    @Mock PluginAdmin pa;
    @Mock AdminService as;
    @Mock ClusterNodeService cns;
    @Mock ProjectDeleteJobFactory pdjf;
    @Mock VelocityContext vc;
    @Mock Scheduler shed;

    MockHttpServletRequest request;

    ClusterNode node;

    @Before
    public void setup(){
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setPathInfo("/projects");
        request.addHeader("Accept-Language", "9=nl,en-US;q=0.8,en;q=0.6");

        node = Mockito.mock(ClusterNode.class);
        Mockito.when(node.getName()).thenReturn("localhost");
        Mockito.when(node.getProjects()).thenReturn(Collections.<StoredProject>emptySet());
        when(dbService.findObjectsByProperties(eq(ClusterNode.class), anyMap())).thenReturn(Collections.nCopies(1, node));
        when(dbService.findObjectById(eq(StoredProject.class), eq(1L))).thenReturn(mock(StoredProject.class));
    }

    @Test
    public void testIndex() {
        String result = projectsView.render(request);
        assertWhiteSpaceEqual(index, result);
    }

    @Test
    public void testCreateFrom_AddProject() {
        request.setParameter(ProjectsView.REQ_PAR_ACTION, ProjectsView.ACT_CON_ADD_PROJECT);
        AdminAction action = mock(AdminAction.class);
        Mockito.when(as.create(anyString())).thenReturn(action);
//        Mockito.when(action.results()).thenReturn(Collections.<String,Object>emptyMap());
        String result = projectsView.render(request);
        assertWhiteSpaceEqual(addProject, result);
    }

    @Test
    public void testRemove(){
        request.setParameter(ProjectsView.REQ_PAR_ACTION, ProjectsView.ACT_CON_REM_PROJECT);
        request.setParameter(ProjectsView.REQ_PAR_PROJECT_ID, "1");
        String result = projectsView.render(request);
        assertWhiteSpaceEqual(removeProject, result);
    }

    @Test
    public void testShowLastAppliedVersion(){
        fail();
    }

    private static void assertWhiteSpaceEqual(String expected, String actual){
        if(!reduceWhitespace(expected).equalsIgnoreCase(reduceWhitespace(actual)))
            throw new ComparisonFailure("", expected, actual);
    }

    private static String reduceWhitespace(String in){
        String s = in.replaceAll("\\s+", " ").replaceAll(">\\s", ">").replaceAll(">\\s<", "><").replaceAll("(\\s$|^\\s)", "");
//        String o = "";
//        for(int i = 0; i < s.length(); i+=40)
//            o += s.substring(i, Math.min(i+40, s.length()-1))+"\n";
        return s;//o;
    }

    private static final String index = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <table> <thead> <tr class=\"head\"> <td class='head' style='width: 10%;'>Project Id</td> <td class='head' style='width: 35%;'>Project Name</td> <td class='head' style='width: 15%;'>Last Version</td> <td class='head' style='width: 15%;'>Last Email</td> <td class='head' style='width: 15%;'>Last Bug</td> <td class='head' style='width: 10%;'>Evaluated</td> <td class='head' style='width: 10%;'>Host</td> </tr> </thead> <tr> <td colspan=\"6\" class=\"noattr\">No projects found.</td> </tr> <tr class=\"subhead\"> <td>View</td><td colspan=\"6\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Delete project\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'> <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled> <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled> </td> <td colspan=\"2\" align=\"right\"> <input type=\"button\" class=\"install\" value=\"Update all on null\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\"> </td> </tr> </tbody> </table> </fieldset> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value=''> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
    private static final String addProject = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <table> <thead> <tr class=\"head\"> <td class='head' style='width: 10%;'>Project Id</td> <td class='head' style='width: 35%;'>Project Name</td> <td class='head' style='width: 15%;'>Last Version</td> <td class='head' style='width: 15%;'>Last Email</td> <td class='head' style='width: 15%;'>Last Bug</td> <td class='head' style='width: 10%;'>Evaluated</td> <td class='head' style='width: 10%;'>Host</td> </tr> </thead> <tr> <td colspan=\"6\" class=\"noattr\">No projects found.</td> </tr> <tr class=\"subhead\"> <td>View</td><td colspan=\"6\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Delete project\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'> <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled> <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled> </td> <td colspan=\"2\" align=\"right\"> <input type=\"button\" class=\"install\" value=\"Update all on null\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\"> </td> </tr> </tbody> </table> </fieldset> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value=''> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
    private static final String removeProject = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <table> <thead> <tr class=\"head\"> <td class='head' style='width: 10%;'>Project Id</td> <td class='head' style='width: 35%;'>Project Name</td> <td class='head' style='width: 15%;'>Last Version</td> <td class='head' style='width: 15%;'>Last Email</td> <td class='head' style='width: 15%;'>Last Bug</td> <td class='head' style='width: 10%;'>Evaluated</td> <td class='head' style='width: 10%;'>Host</td> </tr> </thead> <tr> <td colspan=\"6\" class=\"noattr\">No projects found.</td> </tr> <tr class=\"subhead\"> <td>View</td><td colspan=\"6\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Delete project\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'> <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled> <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled> </td> <td colspan=\"2\" align=\"right\"> <input type=\"button\" class=\"install\" value=\"Update all on null\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\"> </td> </tr> </tbody> </table> </fieldset> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value=''> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
}
