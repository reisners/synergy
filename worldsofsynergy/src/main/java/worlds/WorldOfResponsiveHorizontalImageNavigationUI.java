package worlds;

import helpers.WorldHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Container;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.Synergy;
import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;
import de.syngenio.vaadin.synergy.layout.HorizontalSynergyLayoutFactory;
import de.syngenio.vaadin.synergy.layout.VerticalSynergyLayoutFactory;

@Theme("default")
@WorldDescription(prose="Demonstrates a horizontal image navigation bar.\nThe number of items and the packing mode can be selected interactively.", tags={"horizontal", "image"})
public class WorldOfResponsiveHorizontalImageNavigationUI extends WorldUI
{
    private final static Logger LOG = LoggerFactory.getLogger(WorldOfResponsiveHorizontalImageNavigationUI.class);
    
    @WebServlet(value = "/horizontal/responsive/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = WorldOfResponsiveHorizontalImageNavigationUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(WorldOfResponsiveHorizontalImageNavigationUI.class);

    private Map<Packing, SynergyView> views = new HashMap<Packing, SynergyView>();

    private ComboBox selectPacking;

    private VerticalLayout vlayout;

    private HierarchicalContainer container;
    
    @Override
    protected void init(VaadinRequest request)
    {
        super.init(request);
        
        addStyleName("toggledisplay");
        
        container = WorldHelper.getImageNavigation2();
        List<Object> itemIds = new ArrayList<Object>(container.getItemIds());
        
        vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        
        Synergy synergy = new Synergy(container);
        
        SynergyView synergyViewEnoughSpace = new SynergyView(new HorizontalSynergyLayoutFactory(Packing.EXPAND), (Container)null);
        synergyViewEnoughSpace.attachToSynergy(synergy);
        synergyViewEnoughSpace.setHeightUndefined();
        synergyViewEnoughSpace.setWidth("100%");
        synergyViewEnoughSpace.addStyleName("enoughspace");
        vlayout.addComponent(synergyViewEnoughSpace);
        
        SynergyView synergyViewNotEnoughSpace = new SynergyView(new VerticalSynergyLayoutFactory(Packing.EXPAND), (Container)null);
        synergyViewNotEnoughSpace.attachToSynergy(synergy);
        synergyViewNotEnoughSpace.setWidthUndefined();
        synergyViewNotEnoughSpace.setHeight("100%");
        synergyViewNotEnoughSpace.addStyleName("notenoughspace");
        
        HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSizeFull();
        hlayout.addComponent(synergyViewNotEnoughSpace);
        
        hlayout.addComponent(panel);
        hlayout.setExpandRatio(panel, 1f);
        
        vlayout.addComponent(hlayout);
        vlayout.setExpandRatio(hlayout, 1f);
        
        setContent(vlayout);
    }
}
