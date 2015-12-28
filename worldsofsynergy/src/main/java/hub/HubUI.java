package hub;


import helpers.WorldHelper;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.layout.VerticalSynergyLayout;

@Theme("default")
public class HubUI extends UI
{
    @WebServlet(value = { "/*", "/VAADIN/*" }, asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = HubUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(HubUI.class);
    
    @Override
    protected void init(VaadinRequest request)
    {
        Panel panel = new Panel("Worlds of Synergy");
        
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        
        panel.setContent(layout);
        
        setContent(panel);
        
        SynergyView synergyView = new SynergyView(new VerticalSynergyLayout.NestedFactory(), WorldHelper.getWorldsNavigation());
        layout.addComponent(synergyView);
        
    }
}
