package ${package}.ui;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import test.try14.ui.main.MainView;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Theme("try14")
@Title("try14")
@Push
@Component
public class AppUI extends UI
{
    private static final Logger log = LoggerFactory.getLogger(AppUI.class);
    
    @Autowired
    private SpringViewCatalogue springViewCatalogue;
    
    @Autowired
    private SpringViewCache springViewCache;
    
//    @WebServlet(value = "/*", asyncSupported = true, 
//            initParams={
//                @WebInitParam(name = "org.atmosphere.useWebSocketAndServlet3", value = "true"),
//                @WebInitParam(name = "org.atmosphere.cpr.asyncSupport", value="org.atmosphere.container.Servlet30CometSupport")})
    @VaadinServletConfiguration(productionMode = false, ui = AppUI.class)
    public static class Servlet extends VaadinServlet
    {
       /**
        * Prefix for the ServletContext attribute for the WebApplicationContext.
        * The completion is the servlet name.
        */
        public static final String SERVLET_CONTEXT_PREFIX = Servlet.class.getName() + ".CONTEXT.";
        
        private WebApplicationContext webApplicationContext;

        public Servlet() {
            throw new Error("default constructor called");
        }
        
        public Servlet(WebApplicationContext webApplicationContext)
        {
            this.webApplicationContext = webApplicationContext;
            log.debug("Servlet instantiated");
        }

        public static Servlet getCurrent() {
            return (Servlet) VaadinServlet.getCurrent();
        }
        
        public String getServletContextAttributeName() {
            return SERVLET_CONTEXT_PREFIX + getServletName();
        }

        @Override
        public void init(ServletConfig servletConfig) throws ServletException
        {
            super.init(servletConfig);
            getServletContext().setAttribute(getServletContextAttributeName(), webApplicationContext);
            log.debug("Servlet initialized");
        }
        
        @Override
        protected void servletInitialized() throws ServletException
        {
            super.servletInitialized();
            getService().addSessionInitListener(
                    new SessionInitListener() {
                @Override
                public void sessionInit(SessionInitEvent event)
                        throws ServiceException {
                    event.getSession().addUIProvider(new UIProvider() {
                        @Override
                        public Class< ? extends UI> getUIClass(UIClassSelectionEvent event)
                        {
                            return AppUI.class;
                        }

                        @Override
                        public UI createInstance(UICreateEvent event)
                        {
                            return (UI) webApplicationContext.getBean("ui");
                        }
                    });
                }
            });
        }

        @Override
        public void destroy()
        {
            // clean up resources
            
            super.destroy();
        }

        public WebApplicationContext getWebApplicationContext()
        {
            return webApplicationContext;
        }
    }

    public AppUI() {
        super();
    }
    
    @Override
    public void close()
    {
        try
        {
            super.close();
        }
        catch (RuntimeException e)
        {
            log.warn("caught while closing", e);
        }
    }

    @Override
    protected void init(VaadinRequest request) {
        setId("try14");

        // keep the navigation hierarchy in the session
        HierarchicalContainer hierarchicalContainer = new NavigationHierarchyProvider(springViewCatalogue).createHierarchicalContainer();
        getSession().setAttribute("navigationHierarchy", hierarchicalContainer);
        
        setupAuthentication();
        
        setSizeFull();
//        setContent(mainView);
    }
    
    private void setupAuthentication() {
        final ViewDisplay viewDisplay = createViewDisplay();
        final Navigator navigator = new Navigator(this, viewDisplay);
        View login = springViewCache.getView(LoginView.NAME);
        View main = springViewCache.getView(MainView.NAME);
        log.info("setupAuthentication: ui="+this+" viewDisplay="+viewDisplay+", navigator="+navigator+", springViewCache="+springViewCache+", login="+login+", main="+main);
        
        navigator.addProvider(springViewCache);
        navigator.addViewChangeListener(new ViewChangeListener() {
            @Override
            public boolean beforeViewChange(ViewChangeEvent event)
            {
                // Check if a user has logged in
                boolean isLoggedIn = getSession().getAttribute("userProfile") != null;
                boolean isLoginView = event.getNewView() instanceof LoginView;

                if (!isLoggedIn && !isLoginView)
                {
                    // Redirect to login view always if a user has not yet
                    // logged in
                    navigator.navigateTo(LoginView.NAME);
                    return false;

                }
                else if (isLoggedIn && isLoginView)
                {
                    // If someone tries to access to login view while logged in,
                    // then cancel
                    return false;
                }

                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event)
            {
            }
        });
    }

    private ViewDisplay createViewDisplay()
    {
        return new ViewDisplay() {

            @Override
            public void showView(View view)
            {
                if (view instanceof MainView 
                        || view instanceof LoginView) {
                    AppUI.this.setContent((com.vaadin.ui.Component) view);
                } else {
                    MainView mainView = (MainView) springViewCache.getView("");
                    AppUI.this.setContent(mainView);
                    mainView.getContentPanel().setContent((com.vaadin.ui.Component) view);
                }
            }
            
        };
    }
}