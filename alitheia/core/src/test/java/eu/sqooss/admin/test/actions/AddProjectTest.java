package eu.sqooss.admin.test.actions;

import com.google.inject.Inject;
import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.impl.service.admin.AdminServiceImpl;
import eu.sqooss.impl.service.tds.ProjectDataAccessorImpl;
import eu.sqooss.impl.service.webadmin.ProjectsView;
import eu.sqooss.service.admin.AdminAction;
import eu.sqooss.service.admin.actions.AddProject;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.tds.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AddProjectTest {

    static final String PROJECT_NAME = "Alitheia";
    static final String PROJECT_HOME = "https://github.com/delftsre/Alitheia-Core";
    static final String PROJECT_SCM  = "git-file:///Projects/SoftwareReengineering/Alitheia-Core";

    @Mock
    private DBService db;
    @Mock
    private LogManager log;
    @Mock
    private TDSService tds;
    @Mock
    private BTSAccessor bds;

    private AdminServiceImpl impl;

    @InjectMocks
    AlitheiaCore core;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        impl = new AdminServiceImpl(db);
        impl.registerAdminAction(new AddProject().mnemonic(), AddProject.class);
    }

    @Test
    public void testAddProject() throws Exception {
        AddProject action = new AddProject();

        HashMap < String, Object > args = new HashMap<String, Object>();
        args.put("name", PROJECT_NAME);
        args.put("scm", PROJECT_SCM);
        args.put("web", PROJECT_HOME);
        action.setArgs(args);

        ProjectAccessor a = Mockito.mock(ProjectAccessor.class); //new ProjectDataAccessorImpl(1L, PROJECT_NAME, "", "", PROJECT_SCM)
        SCMAccessor scm = Mockito.mock(SCMAccessor.class);

        Mockito.when(tds.getAccessor(anyLong())).thenReturn(a);
        Mockito.when(tds.isURLSupported(PROJECT_SCM)).thenReturn(true);
        Mockito.stub(a.getSCMAccessor()).toReturn(scm);
        Mockito.when(scm.getHeadRevision()).thenReturn(Mockito.mock(Revision.class));

        action.execute();

        Mockito.verify(a, never()).getBTSAccessor();
        Mockito.verify(a, never()).getMailAccessor();
        Mockito.verify(a, atLeastOnce()).getSCMAccessor();

        assertEquals(action.status(), AdminAction.AdminActionStatus.FINISHED);
        assertNull(action.errors());
    }
}
