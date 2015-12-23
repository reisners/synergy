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
@WorldDescription(prose="Demonstrates a navigation hierarchy in a vertical nested layout. Some of the text items are inlined with an icon. The synergy view has a caption (text and icon). The packing mode can be selected interactively.", tags={"vertical", "hierarchical", "nested", "inline", "caption", "icon"})
public class WorldOfVerticalHierarchicalNavigationUI extends WorldUI
{
    @WebServlet(value = "/vertical/hierarchical/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = WorldOfVerticalHierarchicalNavigationUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(WorldOfVerticalHierarchicalNavigationUI.class);

    private Map<Packing, SynergyView> views = new HashMap<Packing, SynergyView>();

    private HorizontalLayout hlayout;

    private ComboBox select;

    private Panel navbar;
    
    @Override
    protected void init(VaadinRequest request)
    {
        super.init(request);
        VerticalLayout vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        select = new ComboBox("Choose Packing");
        select.setNullSelectionAllowed(false);
        select.setImmediate(true);
        select.addItems(Packing.SPACE_AFTER, Packing.SPACE_AROUND, Packing.SPACE_BEFORE, Packing.EXPAND, Packing.DENSE);
        select.select(Packing.SPACE_AFTER);
        select.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event)
            {
                handleVisibility();
                for (Map.Entry<Packing, SynergyView> entry : views.entrySet()) {
                    Packing packing = entry.getKey();
                    SynergyView synergyView = entry.getValue();
                    synergyView.setVisible(packing.equals(select.getValue()));
                }
            }
        });
        vlayout.addComponent(select);
        vlayout.setExpandRatio(select, 0);
        
        hlayout = new HorizontalLayout();
        hlayout.setSizeFull();
        vlayout.addComponent(hlayout);
        vlayout.setExpandRatio(hlayout, 1);
        
        for (Packing packing : Packing.values()) {
            SynergyView synergyView = new SynergyView(new VerticalSynergyLayout.NestedFactory(packing), WorldHelper.getNavigationHierarchy());
            synergyView.setCaption(packing.name());
            FontAwesome icon = null;
            switch (packing) {
            case SPACE_AFTER:
                icon = FontAwesome.ALIGN_LEFT;
                break;
            case SPACE_AROUND:
                icon = FontAwesome.ALIGN_CENTER;
                break;
            case SPACE_BEFORE:
                icon = FontAwesome.ALIGN_RIGHT;
                break;
            case EXPAND:
                icon = FontAwesome.ALIGN_JUSTIFY;
                break;
            case DENSE:
                icon = FontAwesome.SQUARE;
                break;
            }
            synergyView.setIcon(icon);
            synergyView.setWidthUndefined();
            synergyView.setHeightUndefined();
            synergyView.setVisible(packing.equals(select.getValue()));
            views.put(packing, synergyView);
        }
        
        navbar = new Panel("Navigation");
        navbar.setHeight("100%");
        navbar.setWidthUndefined();
        hlayout.addComponent(navbar);
        hlayout.setComponentAlignment(navbar, Alignment.TOP_LEFT);
        hlayout.setExpandRatio(navbar, 0f);
        hlayout.addComponent(panel);
        hlayout.setExpandRatio(panel, 1f);

        handleVisibility();

        setContent(vlayout);
    }

    private void handleVisibility()
    {
        for (Map.Entry<Packing, SynergyView> entry : views.entrySet()) {
            Packing packing = entry.getKey();
            SynergyView synergyView = entry.getValue();
            boolean visible = packing.equals(select.getValue());

            if (visible) {
                navbar.setContent(synergyView);
            }
        }
    }
}
