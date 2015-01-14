package eu.sqooss.impl.service.webadmin;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.admin.AdminAction;
import eu.sqooss.service.admin.AdminService;
import eu.sqooss.service.cluster.ClusterNodeService;
import eu.sqooss.service.db.*;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.pa.PluginInfo;
import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.updater.Updater;
import eu.sqooss.service.updater.UpdaterService;
import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * A test verifying that the HTML output is unchanged before and after the refactor.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectsControllerTest extends HTMLTest {
    static long failid;
    static long successid;

    @InjectMocks AlitheiaCore core;
    @InjectMocks
    ProjectsController projectsController;

    // Init mock instances here, otherwise null in inserted into the ProjectsView and AlitheiaCore
    @Mock DBService dbService;
    @Mock PluginAdmin pa;
    @Mock AdminService as;
    @Mock ClusterNodeService cns;
    @Mock ProjectDeleteJobFactory pdjf;
    VelocityContext vc;
    @Mock Scheduler shed;
    @Mock UpdaterService updsrv;

    MockHttpServletRequest request;
    ClusterNode node;
    StoredProject proj;

    @Before
    public void setup(){
        vc = new VelocityContext();
        vc.put("tr",new TranslationProxy());
        vc.put("projects", projectsController);

        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setPathInfo("/projects");
        request.addHeader("Accept-Language", "9=nl,en-US;q=0.8,en;q=0.6");

        node = Mockito.mock(ClusterNode.class);
        when(node.getName()).thenReturn("localhost");
        when(node.getProjects()).thenReturn(Collections.<StoredProject>emptySet());
        when(dbService.findObjectsByProperties(eq(ClusterNode.class), anyMap())).thenReturn(Collections.nCopies(1, node));
        proj = mock(StoredProject.class);
        when(proj.getId()).thenReturn(1L);
        when(proj.getName()).thenReturn("some-project");
        when(dbService.findObjectById(eq(StoredProject.class), eq(1L))).thenReturn(proj);

        AdminAction action = mock(AdminAction.class);
        Mockito.when(as.create(anyString())).thenReturn(action);
    }

    @Test
    public void testIndex() {
        projectsController.list(request, vc);
        String result = render(null, request, vc);
        assertWhiteSpaceEqual(index, result);
    }

    @Test
    public void testCreateFrom_AddProject() {
        projectsController.add(request, vc);
        String result = render(null, request, vc);
        assertWhiteSpaceEqual(addProject, result);
    }

    @Test
    public void testRemove(){
        request.setParameter(ProjectsController.REQ_PAR_PROJECT_ID, "1");
        projectsController.deleteConfirm(request, vc);
        String result = render(null, request, vc);
        assertWhiteSpaceEqual(removeProject, result);
    }

    @Test
    public void testUpdate(){
        request.setParameter("updater", "mnem");
        request.setParameter("scope", "single");
        request.setParameter(ProjectsController.REQ_PAR_PROJECT_ID, "1");
//        request.setParameter(ProjectsView.REQ_PAR_ACTION, ProjectsView.ACT_CON_UPD);

        Updater updater = mock(Updater.class);
        when(updsrv.getUpdaters(eq(proj), any(UpdaterService.UpdaterStage.class))).thenReturn(Collections.singleton(updater));
        when(updater.mnem()).thenReturn("updater-mnem");
        when(updater.descr()).thenReturn("updater-descr");

        projectsController.update(request, vc);
        String result = render(null, request, vc);
        assertWhiteSpaceEqual(updateProject, result);
    }

    @Test
    public void testProjectList(){
        Set<StoredProject> list = new LinkedHashSet<>();
        list.add(proj);
        StoredProject other = mock(StoredProject.class);
        when(other.getId()).thenReturn(2L);
        when(other.getName()).thenReturn("other");
        list.add(other);
        when(node.getProjects()).thenReturn(list);

        Mockito.<List<?>>when(dbService.doHQL(contains("ProjectVersion"), anyMap())).thenReturn(
                Collections.singletonList(mock(ProjectVersion.class)),
                Collections.emptyList());

        Mockito.<List<?>>when(dbService.doHQL(contains("MailMessage"), anyMap(), anyInt())).thenReturn(
                Collections.singletonList(mock(MailMessage.class)),
                Collections.emptyList());

        Mockito.<List<?>>when(dbService.doHQL(contains("Bug"), anyMap(), anyInt())).thenReturn(
                Collections.singletonList(mock(Bug.class)),
                Collections.emptyList());

        when(proj.isEvaluated()).thenReturn(true);
        when(other.isEvaluated()).thenReturn(false);

        when(proj.getClusternode()).thenReturn(node);
        when(other.getClusternode()).thenReturn(null);

        testShowLastAppliedVersion();

        projectsController.list(request, vc);
        String result = render(null, request, vc);
        assertWhiteSpaceEqual(projectList, result);
    }

    @Test
    public void testShowProject(){
        request.setParameter(ProjectsController.REQ_PAR_PROJECT_ID, "1");

        when(proj.getName()).thenReturn("ProjName");
        when(proj.getWebsiteUrl()).thenReturn("ProjWebsite");
        when(proj.getContactUrl()).thenReturn("ProjContactUrl");
        when(proj.getBtsUrl()).thenReturn("ProjBTSUrl");
        when(proj.getMailUrl()).thenReturn("ProjMailUrl");
        when(proj.getScmUrl()).thenReturn("ProjSCMUrl");

        projectsController.list(request, vc);
        String result = render(null, request, vc);
        assertWhiteSpaceEqual(showProject, result);
    }

    @Test
    public void testAddProjectForm(){
        projectsController.add(request, vc);
        String result = render(null, request, vc);
        assertWhiteSpaceEqual(addProjectForm, result);
    }

    @Test
    public void testRemoveConfirmation(){
        request.setParameter(ProjectsController.REQ_PAR_PROJECT_ID, "1");

        when(proj.getName()).thenReturn("ProjName");
        when(proj.getWebsiteUrl()).thenReturn("ProjWebsite");
        when(proj.getContactUrl()).thenReturn("ProjContactUrl");
        when(proj.getBtsUrl()).thenReturn("ProjBTSUrl");
        when(proj.getMailUrl()).thenReturn("ProjMailUrl");
        when(proj.getScmUrl()).thenReturn("ProjSCMUrl");

        projectsController.deleteConfirm(request, vc);
        String result = render(null, request, vc);
        assertWhiteSpaceEqual(removeProjectConfirmation, result);
    }

    public void testShowLastAppliedVersion(){
        request.setParameter(ProjectsController.REQ_PAR_PROJECT_ID, "1");

        PluginInfo plugin = mock(PluginInfo.class);
        plugin.installed = true;
        when(plugin.getPluginName()).thenReturn("Some Plugin");
        when(pa.listPlugins()).thenReturn(Collections.nCopies(2, plugin));
    }

    private static final String index = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <table> <thead> <tr class=\"head\"> <td class='head' style='width: 10%;'>Project Id</td> <td class='head' style='width: 35%;'>Project Name</td> <td class='head' style='width: 15%;'>Last Version</td> <td class='head' style='width: 15%;'>Last Email</td> <td class='head' style='width: 15%;'>Last Bug</td> <td class='head' style='width: 10%;'>Evaluated</td> <td class='head' style='width: 10%;'>Host</td> </tr> </thead> <tr> <td colspan=\"6\" class=\"noattr\">No projects found.</td> </tr> <tr class=\"subhead\"> <td>View</td><td colspan=\"6\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Delete project\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'> <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled> <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled> </td> <td colspan=\"2\" align=\"right\"> <input type=\"button\" class=\"install\" value=\"Update all on null\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\"> </td> </tr> </tbody> </table> </fieldset> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value=''> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
    private static final String addProject = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <table> <thead> <tr class=\"head\"> <td class='head' style='width: 10%;'>Project Id</td> <td class='head' style='width: 35%;'>Project Name</td> <td class='head' style='width: 15%;'>Last Version</td> <td class='head' style='width: 15%;'>Last Email</td> <td class='head' style='width: 15%;'>Last Bug</td> <td class='head' style='width: 10%;'>Evaluated</td> <td class='head' style='width: 10%;'>Host</td> </tr> </thead> <tr> <td colspan=\"6\" class=\"noattr\">No projects found.</td> </tr> <tr class=\"subhead\"> <td>View</td><td colspan=\"6\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Delete project\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'> <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled> <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled> </td> <td colspan=\"2\" align=\"right\"> <input type=\"button\" class=\"install\" value=\"Update all on null\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\"> </td> </tr> </tbody> </table> </fieldset> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value=''> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
    private static final String removeProject = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <table> <thead> <tr class=\"head\"> <td class='head' style='width: 10%;'>Project Id</td> <td class='head' style='width: 35%;'>Project Name</td> <td class='head' style='width: 15%;'>Last Version</td> <td class='head' style='width: 15%;'>Last Email</td> <td class='head' style='width: 15%;'>Last Bug</td> <td class='head' style='width: 10%;'>Evaluated</td> <td class='head' style='width: 10%;'>Host</td> </tr> </thead> <tr> <td colspan=\"6\" class=\"noattr\">No projects found.</td> </tr> <tr class=\"subhead\"> <td>View</td><td colspan=\"6\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Delete project\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'> <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled> <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled> </td> <td colspan=\"2\" align=\"right\"> <input type=\"button\" class=\"install\" value=\"Update all on null\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\"> </td> </tr> </tbody> </table> </fieldset> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value=''> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
    private static final String updateProject = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <table> <thead> <tr class=\"head\"> <td class='head' style='width: 10%;'>Project Id</td> <td class='head' style='width: 35%;'>Project Name</td> <td class='head' style='width: 15%;'>Last Version</td> <td class='head' style='width: 15%;'>Last Email</td> <td class='head' style='width: 15%;'>Last Bug</td> <td class='head' style='width: 10%;'>Evaluated</td> <td class='head' style='width: 10%;'>Host</td> </tr> </thead> <tr> <td colspan=\"6\" class=\"noattr\">No projects found.</td> </tr> <tr class=\"subhead\"> <td>View</td><td colspan=\"6\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:window.location='/projects?projectId=1';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Delete project\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'> <select name=\"reqUpd\" id=\"reqUpd\" > <optgroup label=\"Import Stage\"><option value=\"updater-mnem\">updater-descr</option> </optgroup> <optgroup label=\"Parse Stage\"><option value=\"updater-mnem\">updater-descr</option> </optgroup> <optgroup label=\"Inference Stage\"><option value=\"updater-mnem\">updater-descr</option> </optgroup> <optgroup label=\"Default Stage\"><option value=\"updater-mnem\">updater-descr</option> </optgroup> </select> <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\"> <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\"> </td> <td colspan=\"2\" align=\"right\"> <input type=\"button\" class=\"install\" value=\"Update all on null\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\"> </td> </tr> </tbody> </table> </fieldset> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value='1'> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
    private static final String projectList = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <table> <thead> <tr class=\"head\"> <td class='head' style='width: 10%;'>Project Id</td> <td class='head' style='width: 35%;'>Project Name</td> <td class='head' style='width: 15%;'>Last Version</td> <td class='head' style='width: 15%;'>Last Email</td> <td class='head' style='width: 15%;'>Last Bug</td> <td class='head' style='width: 10%;'>Evaluated</td> <td class='head' style='width: 10%;'>Host</td> </tr> </thead> <tbody> <tr class=\"selected\" onclick=\"javascript:document.getElementById('projectId').value='';document.projects.submit();\"> <td class=\"trans\">1</td> <td class=\"trans\"><input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Info\" onclick=\"javascript:document.getElementById('reqAction').value='conShowProject';document.projects.submit();\">&nbsp;some-project</td> <td class=\"trans\">0(null)</td> <td class=\"trans\">null</td> <td class=\"trans\">null</td> <td class=\"trans\">Yes</td> <td class=\"trans\">localhost</td> </tr><tr> <td colspan=\"7\" class=\"noattr\"><input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Synchronise\" onclick=\"javascript:document.getElementById('reqParSyncPlugin').value='null';document.projects.submit();\">&nbsp;Some Plugin</td></tr><tr> <td colspan=\"7\" class=\"noattr\"><input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Synchronise\" onclick=\"javascript:document.getElementById('reqParSyncPlugin').value='null';document.projects.submit();\">&nbsp;Some Plugin</td></tr> <tr class=\"edit\" onclick=\"javascript:document.getElementById('projectId').value='2';document.projects.submit();\"> <td class=\"trans\">2</td> <td class=\"trans\"><img src=\"/edit.png\" alt=\"[Edit]\"/>&nbsp;other</td> <td class=\"trans\">n/a</td> <td class=\"trans\">n/a</td> <td class=\"trans\">n/a</td> <td class=\"trans\">No</td> <td class=\"trans\">(local)</td> </tr> <tr class=\"subhead\"> <td>View</td><td colspan=\"6\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:window.location='/projects?projectId=1';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Delete project\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'> <select name=\"reqUpd\" id=\"reqUpd\" > <optgroup label=\"Import Stage\"> </optgroup> <optgroup label=\"Parse Stage\"> </optgroup> <optgroup label=\"Inference Stage\"> </optgroup> <optgroup label=\"Default Stage\"> </optgroup> </select> <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\"> <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\"> </td> <td colspan=\"2\" align=\"right\"> <input type=\"button\" class=\"install\" value=\"Update all on null\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\"> </td> </tr> </tbody> </table> </fieldset> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value='1'> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
    private static final String showProject = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <fieldset> <legend>Project information</legend> <table class=\"borderless\"> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td> <td class=\"borderless\"> ProjName </td> </tr> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td> <td class=\"borderless\"> ProjWebsite </td> </tr> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td> <td class=\"borderless\"> ProjContactUrl </td> </tr> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td> <td class=\"borderless\"> ProjBTSUrl </td> </tr> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td> <td class=\"borderless\"> ProjMailUrl </td> </tr> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td> <td class=\"borderless\"> ProjSCMUrl </td> </tr> <tr> <td colspan=\"2\" class=\"borderless\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Back\" onclick=\"javascript:document.projects.submit();\"> </td> </tr> </table> </fieldset> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value='1'> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
    private static final String addProjectForm = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <table class=\"borderless\" width='100%'> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td> <td class=\"borderless\"> <input type=\"text\" class=\"form\" id=\"projectName\" name=\"projectName\" value=\"\" size=\"60\"> </td> </tr> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td> <td class=\"borderless\"> <input type=\"text\" class=\"form\" id=\"projectHomepage\" name=\"projectHomepage\" value=\"\" size=\"60\"> </td> </tr> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td> <td class=\"borderless\"> <input type=\"text\" class=\"form\" id=\"projectContact\" name=\"projectContact\" value=\"\" size=\"60\"> </td> </tr> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td> <td class=\"borderless\"> <input type=\"text\" class=\"form\" id=\"projectBL\" name=\"projectBL\" value=\"\" size=\"60\"> </td> </tr> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td> <td class=\"borderless\"> <input type=\"text\" class=\"form\" id=\"projectML\" name=\"projectML\" value=\"\" size=\"60\"> </td> </tr> <tr> <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td> <td class=\"borderless\"> <input type=\"text\" class=\"form\" id=\"projectSCM\" name=\"projectSCM\" value=\"\" size=\"60\"> </td> </tr> <tr> <td colspan=\"2\" class=\"borderless\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add Project\" onclick=\"javascript:document.getElementById('reqAction').value='conAddProject';document.projects.submit();\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Cancel\" onclick=\"javascript:document.projects.submit();\"> </td> </tr> </table> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value=''> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
    private static final String removeProjectConfirmation = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\"> <fieldset> <legend>Delete project: ProjName</legend> <table class=\"borderless\"> <tr> <td class=\"borderless\"><b>Are you sure that you want to completely remove this project?</b></td> </tr> <tr> <td class=\"borderless\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Yes\" onclick=\"javascript:document.getElementById('reqAction').value='conRemProject';document.projects.submit();\"> <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Cancel\" onclick=\"javascript:document.projects.submit();\"> </td> </tr> </table> </fieldset> <input type='hidden' id='reqAction' name='reqAction' value=''> <input type='hidden' id='projectId' name='projectId' value='1'> <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''> </form>";
}
