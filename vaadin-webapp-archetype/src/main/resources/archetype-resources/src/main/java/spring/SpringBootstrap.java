package ${package}.spring;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import test.try14.ui.AppUI;
import test.try14.ui.ServletSpringConfig;

public class SpringBootstrap implements WebApplicationInitializer {
    private static final Logger log = LoggerFactory.getLogger(SpringBootstrap.class);
    
    @Override
    public void onStartup(ServletContext container) {
  
        log.info("onStartup() container="+container.getContextPath());
        
        // Create the 'root' Spring application context
        AnnotationConfigWebApplicationContext rootSpringContext =
          new AnnotationConfigWebApplicationContext();
        rootSpringContext.register(AppSpringConfig.class);
        rootSpringContext.refresh();

        // Manage the lifecycle of the root application context
        container.addListener(new ContextLoaderListener(rootSpringContext));

        // Create the dispatcher servlet's Spring application context
        AnnotationConfigWebApplicationContext servletSpringContext =
          new AnnotationConfigWebApplicationContext();
        servletSpringContext.setParent(rootSpringContext);
        servletSpringContext.register(ServletSpringConfig.class);
        servletSpringContext.refresh();

        // Register and map the vaadin servlet
        AppUI.Servlet servlet = new AppUI.Servlet(servletSpringContext);
        log.info("servlet instantiated");
        ServletRegistration.Dynamic vaadin =
          container.addServlet("vaadin", servlet);
        vaadin.setAsyncSupported(true);
        vaadin.setLoadOnStartup(1);
        vaadin.setInitParameter("org.atmosphere.useWebSocketAndServlet3", "true");
        vaadin.setInitParameter("org.atmosphere.cpr.asyncSupport", "org.atmosphere.container.Servlet30CometSupport");

        vaadin.addMapping("/*");
        
        // necessary for session support to work
        container.addListener(org.atmosphere.cpr.SessionSupport.class);
    }

 }