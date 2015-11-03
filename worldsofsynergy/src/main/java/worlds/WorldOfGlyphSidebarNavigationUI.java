package worlds;

import helpers.WorldHelper;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;
import de.syngenio.vaadin.synergy.layout.VerticalSynergyLayout;

@Theme("default")
@WorldDescription(prose="Demonstrates a single vertical sidebar of large navigation glyphs", tags={"vertical", "stacked", "glyph"})
public class WorldOfGlyphSidebarNavigationUI extends WorldUI
{
    @WebServlet(value = "/vertical/glyphs/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = WorldOfGlyphSidebarNavigationUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(WorldOfGlyphSidebarNavigationUI.class);
    
    @Override
    protected void init(VaadinRequest request)
    {
        super.init(request);
        HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSizeFull();
        SynergyView synergyView = new SynergyView(new VerticalSynergyLayout.NestedFactory(Packing.EXPAND), WorldHelper.getGlyphNavigation());
//        synergyView.setSizeUndefined();
        synergyView.setWidth("200px");
        synergyView.setHeight("100%");
        hlayout.addComponent(synergyView);
        hlayout.setExpandRatio(synergyView, 0f);
        
        VerticalLayout vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        Label label = new Label("Navigation for Vaadin");
        label.setValue("Navigation for Vaadin");
        label.addStyleName("h1");
        label.setSizeFull();
        vlayout.addComponent(label);
        vlayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        vlayout.setExpandRatio(label, 1f);
        hlayout.addComponent(vlayout);
        hlayout.setExpandRatio(vlayout, 1f);

        setContent(hlayout);
    }
}
