package eu.sqooss.core;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import eu.sqooss.impl.service.admin.AdminServiceImpl;
import eu.sqooss.impl.service.cluster.ClusterNodeServiceImpl;
import eu.sqooss.impl.service.db.DBServiceImpl;
import eu.sqooss.impl.service.fds.FDSServiceImpl;
import eu.sqooss.impl.service.logging.LogManagerImpl;
import eu.sqooss.impl.service.metricactivator.MetricActivatorImpl;
import eu.sqooss.impl.service.pa.PAServiceImpl;
import eu.sqooss.impl.service.rest.ResteasyServiceImpl;
import eu.sqooss.impl.service.scheduler.SchedulerServiceImpl;
import eu.sqooss.impl.service.tds.TDSServiceImpl;
import eu.sqooss.impl.service.updater.UpdaterServiceImpl;
import eu.sqooss.impl.service.webadmin.*;
import eu.sqooss.service.admin.AdminService;
import eu.sqooss.service.cluster.ClusterNodeService;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.fds.FDSService;
import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.rest.RestService;
import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.tds.TDSService;
import eu.sqooss.service.updater.UpdaterService;
import eu.sqooss.service.webadmin.WebadminService;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.io.InputStream;

/**
 * Spring Configuration
 *
 * OSGi doesn't allow us to use @ComponentScan, so we have to include all beans manually.
 */
@Configuration
@Lazy
public class SpringApplication {

    public static ConfigurableApplicationContext initialiseSpringContext(BundleContext bc) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(SpringApplication.class);
        context.getBean(SpringApplication.BundleContextHolder.class).setBundleContext(bc);
        AlitheiaCore core = context.getBean(AlitheiaCore.class);
        return context;
    }

    public static void initialiseLogger() throws IOException {
        // Initialize logging immediately
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            loggerContext.reset();
            InputStream config = SpringApplication.class.getClassLoader().getResources("logback.xml").nextElement().openStream();
            configurator.doConfigure(config);
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
        Logger logger = LoggerFactory.getLogger(SpringApplication.class);
        logger.info("Logger initialised");
    }

    public static class BundleContextHolder {
        private BundleContext bundleContext;

        public void setBundleContext(BundleContext bundleContext) {
            assert this.bundleContext == null;
            this.bundleContext = bundleContext;
        }

        public BundleContext getBundleContext() {
            assert bundleContext != null;
            return bundleContext;
        }
    }

    @Bean
    BundleContextHolder bundleContextHolder() {
        return new BundleContextHolder();
    }

    @Bean
    BundleContext bundleContext() {
        return bundleContextHolder().getBundleContext();
    }

    @Bean
    AlitheiaCore alitheiaCore() {
        return new AlitheiaCore();
    }

    @Bean
    AdminService adminService() {
        return new AdminServiceImpl();
    }

    @Bean
    ClusterNodeService clusterNodeService() {
        return new ClusterNodeServiceImpl(bundleContext(), dbService(), updaterService());
    }

    @Bean
    DBService dbService() {
        return new DBServiceImpl(bundleContext());
    }

    @Bean
    FDSService fdsService() {
        return new FDSServiceImpl(bundleContext(), tdsService());
    }

    @Bean
    LogManager logManager() {
        return new LogManagerImpl(bundleContext());
    }

    @Bean
    MetricActivator metricActivator() {
        return new MetricActivatorImpl(bundleContext(), pluginAdmin(), dbService(), scheduler());
    }

    @Bean
    PluginAdmin pluginAdmin() {
        return new PAServiceImpl(bundleContext(), dbService());
    }

    @Bean
    RestService restService() {
        return new ResteasyServiceImpl(bundleContext());
    }

    @Bean
    Scheduler scheduler() {
        return new SchedulerServiceImpl();
    }

    @Bean
    TDSService tdsService() {
        return new TDSServiceImpl();
    }

    @Bean
    UpdaterService updaterService() {
        return new UpdaterServiceImpl(bundleContext(), dbService());
    }

    @Bean
    WebadminService webadminService() {
        return new WebadminServiceImpl(bundleContext(), dbService(), this);
    }

    @Bean
    VelocityContext velocityContext() {
        return new VelocityContext();
    }

    @Bean
    WebAdminRenderer webAdminRenderer() {
        return new WebAdminRenderer(bundleContext());
    }

    @Bean
    PluginsView pluginsView() {
        return new PluginsView(bundleContext());
    }

    @Bean
    ProjectsController projectsView() {
        return new ProjectsController(bundleContext());
    }

    @Bean
    // TODO: refactor to remove parameters
    // Maybe create AdminServlet in WebadminService-postconstruct to avoid circular dependency.
    public AdminServlet adminServlet(Logger logger, VelocityEngine ve, WebadminService webadminService) {
        return new AdminServlet(bundleContext(), logger, ve, webadminService);
    }
}
