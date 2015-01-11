package eu.sqooss.test.service.scheduler;

import com.google.inject.Guice;
import com.google.inject.Injector;
import eu.sqooss.impl.service.logging.LogManagerModule;
import eu.sqooss.impl.service.scheduler.SchedulerServiceModule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.sqooss.impl.service.scheduler.SchedulerServiceImpl;
import eu.sqooss.service.scheduler.SchedulerException;
import org.mockito.Mock;

public class SchedulerTests {

    static SchedulerServiceImpl sched;

    @BeforeClass
    public static void setUp() {
        Injector injector = Guice.createInjector(new LogManagerModule(), new SchedulerServiceModule());
        sched = injector.getInstance(SchedulerServiceImpl.class);
        sched.startExecute(2);
    }

    @Test
    public void testJobYield() throws SchedulerException {
        
        TestJob j1 = new TestJob(20, "Test");
        sched.enqueue(j1);
        TestJob j2 = new TestJob(20, "Test");
        sched.enqueue(j2);
        TestJob j3 = new TestJob(20, "Test");
        sched.enqueue(j3);
        TestJob j4 = new TestJob(20, "Test");
        sched.enqueue(j4);
    }
    
    @AfterClass
    public static void tearDown() {
        while (sched.getSchedulerStats().getWaitingJobs() > 0)
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            
        sched.stopExecute();
    }
}