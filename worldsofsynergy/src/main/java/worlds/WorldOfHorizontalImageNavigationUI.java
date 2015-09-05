package worlds;

import helpers.WorldHelper;

import java.util.ArrayList;
import java.util.List;

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
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;
import de.syngenio.vaadin.synergy.layout.HorizontalSynergyLayoutFactory;

@Theme("default")
public class WorldOfHorizontalImageNavigationUI extends WorldUI
{
    @WebServlet(value = "/horizontal/images/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = WorldOfHorizontalImageNavigationUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(WorldOfHorizontalImageNavigationUI.class);
    
    @Override
    protected void init(VaadinRequest request)
    {
        super.init(request);
        final HierarchicalContainer container = WorldHelper.getImageNavigation2();
        List<Object> itemIds = new ArrayList<Object>(container.getItemIds());
        
        VerticalLayout vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        
        ComboBox selectNumber = new ComboBox("Number of items");
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
                final int number = (int) selectNumber.getValue();
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
                        // TODO Auto-generated method stub
                        return false;
                    }
                    
                });
            }
        });
        vlayout.addComponent(selectNumber);
        
        for (Packing packing : Packing.values()) {
            SynergyView synergyView = new SynergyView(new HorizontalSynergyLayoutFactory(packing), container);
            synergyView.setCaption(packing.name());
//            synergyView.setIcon(FontAwesome.FONT);
            synergyView.setHeightUndefined();
            synergyView.setWidth("100%");
            vlayout.addComponent(synergyView);
            vlayout.setComponentAlignment(synergyView, Alignment.TOP_LEFT);
            vlayout.setExpandRatio(synergyView, 0f);
            
        }

        vlayout.addComponent(panel);
        vlayout.setExpandRatio(panel, 1f);

        setContent(vlayout);
    }
}
