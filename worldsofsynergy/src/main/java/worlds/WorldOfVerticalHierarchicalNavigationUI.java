package worlds;

import helpers.WorldHelper;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.layout.VerticalSynergyLayoutFactory;

@Theme("default")
public class WorldOfVerticalHierarchicalNavigationUI extends UI
{
    @WebServlet(value = "/vertical/hierarchical/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = WorldOfVerticalHierarchicalNavigationUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(WorldOfVerticalHierarchicalNavigationUI.class);
    
    @Override
    protected void init(VaadinRequest request)
    {
        HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSizeFull();
        SynergyView synergyView = new SynergyView(new VerticalSynergyLayoutFactory(Alignment.MIDDLE_LEFT), WorldHelper.getNavigationHierarchy());
        synergyView.setSizeUndefined();
//        synergyView.setWidth("30%");
        hlayout.addComponent(synergyView);
        hlayout.setExpandRatio(synergyView, 0f);
        
        Panel panel = new Panel();
        panel.setSizeFull();
        panel.setId("content");
        hlayout.addComponent(panel);
        hlayout.setExpandRatio(panel, 1f);

        setContent(hlayout);
        
        setNavigator(new Navigator(this, panel));
        final NavigationView genericView = new NavigationView();
        getNavigator().addView("", genericView);
        getNavigator().addView("view", genericView);
    }
    
    private static class NavigationView extends Label implements View {
        @Override
        public void enter(ViewChangeEvent event)
        {
            setValue(event.getParameters());
        }
    }
}
