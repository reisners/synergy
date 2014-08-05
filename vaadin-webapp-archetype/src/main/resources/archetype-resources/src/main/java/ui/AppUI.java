package ${package}.ui;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Theme("app")
@Push
@Component
public class AppUI extends UI
{
    private static final Logger log = LoggerFactory.getLogger(AppUI.class);
    
//    @WebServlet(value = "/*", asyncSupported = true, 
//            initParams={
//                @WebInitParam(name = "org.atmosphere.useWebSocketAndServlet3", value = "true"),
//                @WebInitParam(name = "org.atmosphere.cpr.asyncSupport", value="org.atmosphere.container.Servlet30CometSupport")})
    @VaadinServletConfiguration(productionMode = false, ui = AppUI.class, widgetset = "AppWidgetSet")
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
    }

    @Override
    protected void init(VaadinRequest request)
    {
        getPage().setTitle("APP");
        
        // Create a navigator to control the views
        setNavigator(new MyNavigator(this, this));

//        // Create and register the views
//        MainView mainView = new MainView();
//        getNavigator().addView("", mainView);
//        LoginView loginView = new LoginView();
//        getNavigator().addView(LoginView.NAME, loginView);
//        getNavigator().addView(MainView.NAME, mainView);

        getNavigator().addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(ViewChangeEvent event)
            {

                // Check if a user has logged in
                boolean isLoggedIn = getSession().getAttribute("user") != null;
                boolean isLoginView = event.getNewView() instanceof LoginView;

                if (!isLoggedIn && !isLoginView)
                {
                    // Redirect to login view always if a user has not yet
                    // logged in
                    getNavigator().navigateTo(LoginView.NAME);
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
        log.debug("UI initialized");
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

}