package wos;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.HierarchicalContainer;
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

import de.syngenio.vaadin.synergy.SynergyBuilder;
import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.VerticalSynergyLayoutFactory;

@Theme("default")
public class WorldOfVerticalNavigationUI extends UI
{
    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = WorldOfVerticalNavigationUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(WorldOfVerticalNavigationUI.class);
    
    @Override
    protected void init(VaadinRequest request)
    {
        HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSizeFull();
        SynergyView synergyView = new SynergyView(new VerticalSynergyLayoutFactory(false, false, Alignment.MIDDLE_LEFT), getNavigationHierarchy());
        synergyView.setSizeUndefined();
//        synergyView.setWidth("30%");
        hlayout.addComponent(synergyView);
        hlayout.setExpandRatio(synergyView, 0f);
        
        Panel panel = new Panel();
        panel.setSizeFull();
        hlayout.addComponent(panel);
        hlayout.setExpandRatio(panel, 1f);

        Button b1 = new Button("Button with Long Caption");
        b1.setWidth("100%");
        Button b2 = new Button("Short");
        b2.setWidth("100%");
        VerticalLayout v2 = new VerticalLayout(b1, b2);
        v2.setSizeUndefined();
        hlayout.addComponent(v2);
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

    private HierarchicalContainer getNavigationHierarchy() {
        return new SynergyBuilder() {{
            addItem(
                    button("|Resources").asGroup().withChildren( 
                            button("|Resources|Assets").asGroup().withChildren(
                                    button("|Resources|Assets|Machines").withTargetNavigationState("view/Machines"),
                                    button("|Resources|Assets|Real Estate").withTargetNavigationState("view/Real Estate"),
                                    button("|Resources|Assets|Patents").withTargetNavigationState("view/Patents")),
                            button("|Resources|People").withTargetNavigationState("view/People")));
            addItem(button("|Processes").asGroup().withChildren(
                    button("|Processes|Core").withTargetNavigationState("view/Core Processes"),
                    button("|Processes|Auxiliary").withTargetNavigationState("view/Auxiliary Processes")));
        }}.build();
    }
}
