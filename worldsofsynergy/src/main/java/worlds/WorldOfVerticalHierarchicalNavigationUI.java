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
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;
import de.syngenio.vaadin.synergy.layout.VerticalSynergyLayoutFactory;

@Theme("default")
@WorldTags({"vertical", "hierarchical", "inline", "caption", "icon"})
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
        SynergyView synergyViewSpaceAfter = new SynergyView(new VerticalSynergyLayoutFactory(Packing.SPACE_AFTER), WorldHelper.getNavigationHierarchy());
        synergyViewSpaceAfter.setCaption(Packing.SPACE_AFTER.name());
        synergyViewSpaceAfter.setIcon(FontAwesome.ALIGN_LEFT);
        synergyViewSpaceAfter.setWidthUndefined();
        synergyViewSpaceAfter.setHeight("100%");
//        synergyView.setWidth("30%");
        hlayout.addComponent(synergyViewSpaceAfter);
        hlayout.setExpandRatio(synergyViewSpaceAfter, 0f);
        
        SynergyView synergyViewSpaceAround = new SynergyView(new VerticalSynergyLayoutFactory(Packing.SPACE_AROUND), WorldHelper.getNavigationHierarchy());
        synergyViewSpaceAround.setCaption(Packing.SPACE_AROUND.name());
        synergyViewSpaceAround.setIcon(FontAwesome.ALIGN_CENTER);
        synergyViewSpaceAround.setWidthUndefined();
        synergyViewSpaceAround.setHeight("100%");
//        synergyView.setWidth("30%");
        hlayout.addComponent(synergyViewSpaceAround);
        hlayout.setExpandRatio(synergyViewSpaceAround, 0f);
        
        SynergyView synergyViewSpaceBefore = new SynergyView(new VerticalSynergyLayoutFactory(Packing.SPACE_BEFORE), WorldHelper.getNavigationHierarchy());
        synergyViewSpaceBefore.setCaption(Packing.SPACE_BEFORE.name());
        synergyViewSpaceBefore.setIcon(FontAwesome.ALIGN_RIGHT);
        synergyViewSpaceBefore.setWidthUndefined();
        synergyViewSpaceBefore.setHeight("100%");
//        synergyView.setWidth("30%");
        hlayout.addComponent(synergyViewSpaceBefore);
        hlayout.setExpandRatio(synergyViewSpaceBefore, 0f);
        
        SynergyView synergyViewExpand = new SynergyView(new VerticalSynergyLayoutFactory(Packing.EXPAND), WorldHelper.getNavigationHierarchy());
        synergyViewExpand.setCaption(Packing.EXPAND.name());
        synergyViewExpand.setIcon(FontAwesome.ALIGN_JUSTIFY);
        synergyViewExpand.setWidthUndefined();
        synergyViewExpand.setHeight("100%");
//        synergyView.setWidth("30%");
        hlayout.addComponent(synergyViewExpand);
        hlayout.setExpandRatio(synergyViewExpand, 0f);
        
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
