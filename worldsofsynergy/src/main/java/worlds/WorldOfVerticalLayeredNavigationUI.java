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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.layout.VerticalFlatSynergyLayoutFactory;
import de.syngenio.vaadin.synergy.layout.VerticalSynergyLayoutFactory;

@Theme("default")
public class WorldOfVerticalLayeredNavigationUI extends UI
{
    @WebServlet(value = "/vertical/layered/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = WorldOfVerticalLayeredNavigationUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(WorldOfVerticalLayeredNavigationUI.class);
    
    @Override
    protected void init(VaadinRequest request)
    {
        HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSizeFull();
        SynergyView synergyViewTop = new SynergyView(new VerticalFlatSynergyLayoutFactory(), WorldHelper.getNavigationHierarchy());
        synergyViewTop.setSizeUndefined();
        hlayout.addComponent(synergyViewTop);
        hlayout.setExpandRatio(synergyViewTop, 0f);

        SynergyView synergyViewSub = new SynergyView(new VerticalSynergyLayoutFactory(),synergyViewTop );
        synergyViewSub.setSizeUndefined();
        hlayout.addComponent(synergyViewSub);
        hlayout.setExpandRatio(synergyViewSub, 0f);

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
