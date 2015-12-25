package worlds;

import helpers.WorldHelper;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;
import de.syngenio.vaadin.synergy.layout.VerticalSynergyLayout;

@Theme("default")
@WorldDescription(prose="Demonstrates a navigation hierarchy in a vertical nested layout. Some of the text items are inlined with an icon. The synergy view has a caption (text and icon). If the contents exceed the window size, scrollbars will appear. Use URL parameter packing to select different layouts.", tags={"vertical", "hierarchical", "nested", "inline", "caption", "icon"})
public class WorldOfVerticalHierarchicalNavigationUI extends WorldUI
{
    @WebServlet(value = "/vertical/hierarchical/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = WorldOfVerticalHierarchicalNavigationUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(WorldOfVerticalHierarchicalNavigationUI.class);

    private Map<Packing, SynergyView> views = new HashMap<Packing, SynergyView>();

    private HorizontalLayout hlayout;

    private Panel navbar;

    private SynergyView synergyView;
    
    @Override
    protected void init(VaadinRequest request)
    {
        super.init(request);
        VerticalLayout vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        
        hlayout = new HorizontalLayout();
        hlayout.setSizeFull();
        vlayout.addComponent(hlayout);
        vlayout.setExpandRatio(hlayout, 1);
        
        synergyView = new SynergyView(new VerticalSynergyLayout.NestedFactory(packing), WorldHelper.getNavigationHierarchy());
        
        navbar = new Panel("Navigation");
        navbar.setHeight("100%");
        navbar.setWidthUndefined();
        navbar.setContent(synergyView);
        hlayout.addComponent(navbar);
        hlayout.setComponentAlignment(navbar, Alignment.TOP_LEFT);
        hlayout.setExpandRatio(navbar, 0f);
        hlayout.addComponent(panel);
        hlayout.setExpandRatio(panel, 1f);

        setContent(vlayout);
    }
}
