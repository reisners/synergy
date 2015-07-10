package hub;


import helpers.WorldHelper;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.builder.SynergyBuilder;
import de.syngenio.vaadin.synergy.layout.HorizontalSynergyLayoutFactory;
import de.syngenio.vaadin.synergy.layout.VerticalSynergyLayoutFactory;

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
        VerticalLayout layout = new VerticalLayout();
        setContent(layout);
        SynergyView synergyView = new SynergyView(new HorizontalSynergyLayoutFactory(), WorldHelper.getHubNavigation());
        synergyView.setNavigationHandler(synergyView.new NavigationHandler() {
            @Override
            protected void selected(Item item)
            {
                String targetNavigationState = (String) item.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).getValue();
                if (targetNavigationState != null) {
                    getPage().setLocation(request.getContextPath()+targetNavigationState); // this results in a NPE sometimes (not always)
                }
            }
        });
        synergyView.setSizeFull();
        layout.addComponent(synergyView);
        layout.addComponent(new Button("Test Button"));
    }
}
