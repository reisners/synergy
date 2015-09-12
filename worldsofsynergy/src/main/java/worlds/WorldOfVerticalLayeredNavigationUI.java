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
import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;

@Theme("default")
@WorldDescription(prose="Demonstrates an alternative way to render a navigation hierarchy: levels don't nest but are layered from left to right.", tags={"vertical", "hiearchical", "layered", "text"})
public class WorldOfVerticalLayeredNavigationUI extends WorldUI
{
    @WebServlet(value = "/vertical/layered/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = WorldOfVerticalLayeredNavigationUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(WorldOfVerticalLayeredNavigationUI.class);
    
    @Override
    protected void init(VaadinRequest request)
    {
        super.init(request);
        HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSizeFull();
        SynergyView synergyViewTop = new SynergyView(new VerticalFlatSynergyLayoutFactory(Packing.EXPAND), WorldHelper.getNavigationHierarchy());
        synergyViewTop.setWidthUndefined();
        synergyViewTop.setHeight("100%");
        hlayout.addComponent(synergyViewTop);
        hlayout.setExpandRatio(synergyViewTop, 0f);

        SynergyView synergyViewSub = new SynergyView(new VerticalSynergyLayoutFactory(),synergyViewTop );
        synergyViewSub.setSizeUndefined();
        hlayout.addComponent(synergyViewSub);
        hlayout.setExpandRatio(synergyViewSub, 0f);

        hlayout.addComponent(panel);
        hlayout.setExpandRatio(panel, 1f);

        setContent(hlayout);
    }
}
