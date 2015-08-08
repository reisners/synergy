package hub;


import helpers.WorldHelper;
import helpers.WorldHelper.WorldBean;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.builder.SynergyBuilder;
import de.syngenio.vaadin.synergy.layout.HorizontalSynergyLayoutFactory;

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
        Grid grid = new Grid("Worlds of Synergy", WorldHelper.getWorlds());
        grid.addSelectionListener(new SelectionListener() {

            @Override
            public void select(SelectionEvent event)
            {
                WorldBean bean = (WorldBean) grid.getSelectedRow();
                if (bean.getPath() != null) {
                    String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
                    getPage().setLocation(contextPath+bean.getPath());
                }
            }
        });
        grid.setSizeFull();
        layout.addComponent(grid);
    }
}
