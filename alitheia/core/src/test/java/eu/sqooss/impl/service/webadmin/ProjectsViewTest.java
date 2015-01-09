package eu.sqooss.impl.service.webadmin;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.core.AlitheiaCoreService;
import eu.sqooss.service.db.ClusterNode;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.logging.LogManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.mail.Store;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProjectsViewTest {
    static long failid;
    static long successid;

    @Mock
    DBService dbService;

    @InjectMocks
    ProjectsView projectsView;

    @InjectMocks
    AlitheiaCore core;

    @Test
    public void testCreateFrom_AddProject() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(ProjectsView.REQ_PAR_ACTION, ProjectsView.ACT_CON_REM_PROJECT);
        request.setParameter(ProjectsView.ACT_CON_ADD_PROJECT, "project-id");

        when(dbService.findObjectsByProperties(eq(ClusterNode.class), anyMap())).thenReturn(Collections.nCopies(1, new ClusterNode("localhost")));

        String result = projectsView.render(request);
        assertEquals(result, "");
    }

//    @Test
//    public void testCreateFrom_AddProject() {
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setParameter(ProjectsView.REQ_PAR_ACTION, ProjectsView.ACT_CON_REM_PROJECT);
//        request.setParameter(ProjectsView.ACT_CON_ADD_PROJECT, "project-id");
//    }
//
//    @Test
//    public void testCreateFrom_AddProject() {
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setParameter(ProjectsView.REQ_PAR_ACTION, ProjectsView.ACT_CON_UPD);
//        request.setParameter(ProjectsView.ACT_CON_ADD_PROJECT, "project-id");
//    }

}
