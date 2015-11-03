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
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;
import de.syngenio.vaadin.synergy.layout.HorizontalSynergyLayout;

@Theme("default")
@WorldDescription(prose="Demonstrates a horizontal image navigation bar.\nThe number of items and the packing mode can be selected interactively.", tags={"horizontal", "image"})
public class WorldOfHorizontalImageNavigationUI extends WorldUI
{
    private final static Logger LOG = LoggerFactory.getLogger(WorldOfHorizontalImageNavigationUI.class);
    
    @WebServlet(value = "/horizontal/images/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = WorldOfHorizontalImageNavigationUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(WorldOfHorizontalImageNavigationUI.class);

    private Map<Packing, SynergyView> views = new HashMap<Packing, SynergyView>();

    private ComboBox selectPacking;

    private VerticalLayout vlayout;

    private HierarchicalContainer container;
    
    @Override
    protected void init(VaadinRequest request)
    {
        super.init(request);
        container = WorldHelper.getImageNavigation2();
        final List<Object> itemIds = new ArrayList<Object>(container.getItemIds());
        
        vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        
        final ComboBox selectNumber = new ComboBox("Number of items");
        selectNumber.addItem(1);
        selectNumber.addItem(2);
        selectNumber.addItem(3);
        selectNumber.addItem(4);
        selectNumber.addItem(5);
        selectNumber.setValue(5);
        selectNumber.addValueChangeListener(new ValueChangeListener() {
            
            @Override
            public void valueChange(ValueChangeEvent event)
            {
                final int number = (Integer) selectNumber.getValue();
                container.removeAllContainerFilters();
                container.addContainerFilter(new Filter() {

                    @Override
                    public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException
                    {
                        return itemIds.indexOf(itemId) < number;
                    }

                    @Override
                    public boolean appliesToProperty(Object propertyId)
                    {
                        return false;
                    }
                    
                });
            }
        });
        vlayout.addComponent(selectNumber);
        vlayout.setExpandRatio(selectNumber, 0);
        
        selectPacking = new ComboBox("Choose Packing");
        selectPacking.setImmediate(true);
        selectPacking.addItems(Packing.SPACE_AFTER, Packing.SPACE_AROUND, Packing.SPACE_BEFORE, Packing.EXPAND);
        selectPacking.select(Packing.SPACE_AFTER);
        selectPacking.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event)
            {
                handleVisibility();
            }
        });
        vlayout.addComponent(selectPacking);
        vlayout.setExpandRatio(selectPacking, 0);

        for (Packing packing : Packing.values()) {
            SynergyView synergyView = new SynergyView(new HorizontalSynergyLayout.Factory(packing), container);
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
            }
            synergyView.setIcon(icon);
            synergyView.setHeightUndefined();
            synergyView.setWidth("100%");

            views.put(packing, synergyView);
        }

        vlayout.addComponent(panel);
        vlayout.setExpandRatio(panel, 1f);
        
        handleVisibility();

        setContent(vlayout);
    }

    private void handleVisibility()
    {
        for (Map.Entry<Packing, SynergyView> entry : views.entrySet()) {
            Packing packing = entry.getKey();
            SynergyView synergyView = entry.getValue();
            boolean visible = packing.equals(selectPacking.getValue());

            if (visible) {
                vlayout.addComponent(synergyView, vlayout.getComponentIndex(panel));
                vlayout.setComponentAlignment(synergyView, Alignment.TOP_LEFT);
                vlayout.setExpandRatio(synergyView, 0f);
            } else {
                vlayout.removeComponent(synergyView);
            }
        }
    }
}
