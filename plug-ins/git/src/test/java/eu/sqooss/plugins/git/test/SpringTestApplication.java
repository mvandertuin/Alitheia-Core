package eu.sqooss.plugins.git.test;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.impl.service.admin.AdminServiceImpl;
import eu.sqooss.impl.service.cluster.ClusterNodeServiceImpl;
import eu.sqooss.impl.service.db.DBServiceImpl;
import eu.sqooss.impl.service.logging.LogManagerImpl;
import eu.sqooss.impl.service.rest.ResteasyServiceImpl;
import eu.sqooss.impl.service.scheduler.SchedulerServiceImpl;
import eu.sqooss.impl.service.tds.TDSServiceImpl;
import eu.sqooss.impl.service.updater.UpdaterServiceImpl;
import eu.sqooss.impl.service.webadmin.AdminServlet;
import eu.sqooss.impl.service.webadmin.PluginsView;
import eu.sqooss.impl.service.webadmin.ProjectsController;
import eu.sqooss.impl.service.webadmin.WebAdminRenderer;
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
import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import static org.mockito.Mockito.mock;

/**
 * Spring Test Configuration
 */
@Configuration
@Lazy
public class SpringTestApplication {

    public static ConfigurableApplicationContext initialiseSpringTestContext() {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(SpringTestApplication.class);
        context.getBean(AlitheiaCore.class);
        return context;
    }

    @Bean
    BundleContext bundleContext() {
        return null;
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
        return mock(ClusterNodeServiceImpl.class);
    }

    @Bean
    DBService dbService() {
        Properties conProp = new Properties();
        conProp.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        conProp.setProperty("hibernate.connection.url", "jdbc:hsqldb:file:alitheia.db");
        conProp.setProperty("hibernate.connection.username", "sa");
        conProp.setProperty("hibernate.connection.password", "");
        conProp.setProperty("hibernate.connection.host", "localhost");
        conProp.setProperty("hibernate.connection.dialect", "org.hibernate.dialect.HSQLDialect");
        conProp.setProperty("hibernate.connection.provider_class", "org.hibernate.connection.DriverManagerConnectionProvider");

        File root = new File(System.getProperty("user.dir"));
        File config = null;
        while (true) {
            String[] extensions = { "xml" };
            boolean recursive = true;

            Collection files = FileUtils.listFiles(root, extensions, recursive);

            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File file = (File) iterator.next();
                if (file.getName().equals("hibernate.cfg.xml")) {
                    config = file;
                    break;
                }
            }

            if (config == null)
                root = root.getParentFile();
            else
                break;
        }

        try {
            return new DBServiceImpl(conProp, config.toURL() , null);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    FDSService fdsService() {
        return mock(FDSService.class);
    }

    @Bean
    LogManager logManager() {
        return new LogManagerImpl(null);
    }

    @Bean
    MetricActivator metricActivator() {
        return mock(MetricActivator.class);
    }

    @Bean
    PluginAdmin pluginAdmin() {
        return mock(PluginAdmin.class);
    }

    @Bean
    RestService restService() {
        return mock(ResteasyServiceImpl.class);
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
        return mock(WebadminService.class);
    }

    @Bean
    VelocityContext velocityContext() {
        return mock(VelocityContext.class);
    }

    @Bean
    WebAdminRenderer webAdminRenderer() {
        return mock(WebAdminRenderer.class);
    }

    @Bean
    PluginsView pluginsView() {
        return mock(PluginsView.class);
    }

    @Bean
    ProjectsController projectsView() {
        return mock(ProjectsController.class);
    }

    @Bean
    // TODO: refacow tor to remove parameters
    // Maybe create AdminServlet in WebadminService-postconstruct to avoid circular dependency.
    public AdminServlet adminServlet(Logger logger, VelocityEngine ve, WebadminService webadminService) {
        return mock(AdminServlet.class);
    }
}
