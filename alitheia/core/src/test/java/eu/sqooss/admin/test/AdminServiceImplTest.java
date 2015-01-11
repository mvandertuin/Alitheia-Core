package eu.sqooss.admin.test;

import static org.junit.Assert.*;

import java.util.Set;

import com.google.inject.Inject;
import eu.sqooss.service.db.DBService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.sqooss.impl.service.admin.AdminServiceImpl;
import eu.sqooss.impl.service.admin.AdminServiceImpl.ActionContainer;
import eu.sqooss.service.admin.AdminAction;
import eu.sqooss.service.admin.AdminAction.AdminActionStatus;
import eu.sqooss.service.admin.actions.RunTimeInfo;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AdminServiceImplTest {

    @Mock
    private DBService db;

    private AdminServiceImpl impl;
    static long failid;
    static long successid;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        impl = new AdminServiceImpl(db);
        registerAdminActions();
    }

    @Test
    public void testAdminServiceImpl() {
        assertNotNull(impl);
    }

    // Relying on tests running in sequence is Bad Practice,
    // however for now AdminServiceImpl has a static list of actions
    public void registerAdminActions() {
        if(impl.getAdminActions().size() == 3)
            return;

        RunTimeInfo rti = new RunTimeInfo();
        impl.registerAdminAction(rti.mnemonic(), RunTimeInfo.class);
        assertEquals(1, impl.getAdminActions().size());

        FailingAction fa = new FailingAction();
        impl.registerAdminAction(fa.mnemonic(), FailingAction.class);
        assertEquals(2, impl.getAdminActions().size());

        SucceedingAction su = new SucceedingAction();
        impl.registerAdminAction(su.mnemonic(), SucceedingAction.class);
        assertEquals(3, impl.getAdminActions().size());
    }

    @Test
    public void testGetAdminActions() {
        Set<AdminAction> actions = impl.getAdminActions();
        for (AdminAction aa : actions)
            assertNotNull (aa);
    }
    
    @Test
    public void testCreate() {
        AdminAction fail = impl.create("blah");
        assertNull(fail);

        fail = impl.create("fail");
        assertNotNull(fail);
        ActionContainer ac = impl.liveactions().get(1L);
        assertNotNull(ac);
        assertEquals(-1, ac.end);

        assertEquals(AdminActionStatus.CREATED, fail.status());
        assertNull(fail.errors());
        assertNull(fail.results());
        failid = fail.id();
    }

    // The following tests need this repeatedly
    private AdminAction success(){
        AdminAction success = impl.create("win");
        if(success != null)
            impl.execute(success);
        return success;
    }

    // The following tests need this repeatedly
    private AdminAction fail(){
        AdminAction fail = impl.create("fail");
        if(fail != null)
            impl.execute(fail);
        return fail;
    }
    
    @Test
    public void testExecute() {
        AdminAction success = success();
        assertNotNull(success);

        assertNull(success.errors());
        assertEquals("#win", success.results().get("1"));
        assertEquals(AdminActionStatus.FINISHED, success.status());

        AdminAction fail = fail();
        assertNotNull(fail);

        assertNull(fail.results());
        assertEquals("#fail", fail.errors().get("1"));
        assertEquals(AdminActionStatus.ERROR, fail.status());
        failid = fail.id();
    }
    
    @Test
    public void testShow() {
        AdminAction aa = impl.show(fail().id());
        assertNotNull(aa);
        
        aa = impl.show(success().id());
        assertNotNull(aa);
    }
    
    @Test
    public void testGC() {
        impl.registerAdminAction(new FailingAction().mnemonic(), FailingAction.class);
        fail();
        fail();

        try {
            Thread.sleep (300);
        } catch (InterruptedException e) {}
        int collected = impl.gc(1);
        
        assertEquals(collected, 2);
        
        AdminAction aa = impl.show(failid);
        assertNull(aa);
    }
}
