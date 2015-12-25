package worlds;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;

import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;

@SuppressWarnings("serial")
public abstract class WorldUI extends UI
{
    protected Panel panel;
    protected Packing packing;

    @Override
    protected void init(VaadinRequest request)
    {
        String[] packings = request.getParameterMap().get("packing");
        String packingName;
        if (packings != null && packings.length > 0) {
            packingName = packings[0].toUpperCase();
        } else {
            packingName = Packing.SPACE_AFTER.name();
        }
        packing = Packing.valueOf(packingName);
        
        panel = new Panel();
        panel.setSizeFull();
        panel.setId("content");
        
        setNavigator(new Navigator(this, panel));
        final NavigationView genericView = new NavigationView();
        getNavigator().addView("", genericView);
        getNavigator().addView("view", genericView);
    }
    
    protected static class NavigationView extends Label implements View {
        @Override
        public void enter(ViewChangeEvent event)
        {
            setValue(event.getParameters());
        }
    }

}
