package worlds;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.FontIcon;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;

@Theme("default")
public class TestLayoutUI extends UI
{
    @WebServlet(value = "/test/layout/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = TestLayoutUI.class)
    public static class Servlet extends VaadinServlet {
    }

    private static final Logger log = LoggerFactory.getLogger(TestLayoutUI.class);
    
    @Override
    protected void init(VaadinRequest request)
    {
        VerticalLayout vlayout = new VerticalLayout();
        vlayout.setSizeFull();

        for (Packing packing : Packing.values()) {
            vlayout.addComponent(new Label(packing.name()));
            vlayout.addComponent(createHorizontalLayout(packing));
        }
        
        setContent(vlayout);
    }

    private Component createHorizontalLayout(Packing packing)
    {
        HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setWidth("100%");
        for (int i = 0; i < 3; ++i) {
            VerticalLayout item = new VerticalLayout();
            item.setMargin(new MarginInfo(false, true, false, true));
            String html = FontAwesome.IMAGE.getHtml().replaceAll("(?=font-family)", "font-size:4em;");
            Label glyph = new Label(html, ContentMode.HTML);
            glyph.setWidthUndefined();
            item.addComponent(glyph);
            item.setComponentAlignment(glyph, Alignment.BOTTOM_CENTER);
            final Label label = new Label("Label"+i);
            label.setWidthUndefined();
            item.addComponent(label);
            item.setComponentAlignment(label, Alignment.TOP_CENTER);
            hlayout.addComponent(item);
            switch (i) {
            case 0:
                layoutFirstComponent(hlayout, item, packing);
                break;
            case 1:
                layoutIntermediateComponent(hlayout, item, packing);
                break;
            case 2:
                layoutLastComponent(hlayout, item, packing);
                break;
            }
        }
        return hlayout;
    }

    protected void layoutFirstComponent(HorizontalLayout layout, Component itemComponent, Packing packing)
    {
        itemComponent.setWidthUndefined();
        layout.setExpandRatio(itemComponent, 0);
        layout.setComponentAlignment(itemComponent, Alignment.BOTTOM_CENTER);
        switch (packing) {
        case EXPAND:
            itemComponent.setWidth("100%");
            break;
        case SPACE_BEFORE:
        case SPACE_AROUND:
            layout.setExpandRatio(itemComponent, 1);
            layout.setComponentAlignment(itemComponent, Alignment.BOTTOM_RIGHT);
            break;
        default:
            // do nothing
            break;
        }
    }

    protected void layoutIntermediateComponent(HorizontalLayout layout, Component itemComponent, Packing packing)
    {
        itemComponent.setWidthUndefined();
        layout.setExpandRatio(itemComponent, 0);
        layout.setComponentAlignment(itemComponent, Alignment.BOTTOM_CENTER);
        switch (packing) {
        case EXPAND:
            itemComponent.setWidth("100%");
            break;
        default:
            // do nothing
            break;
        }
    }

    protected void layoutLastComponent(HorizontalLayout layout, Component itemComponent, Packing packing)
    {
        itemComponent.setWidthUndefined();
        layout.setExpandRatio(itemComponent, 0);
        layout.setComponentAlignment(itemComponent, Alignment.BOTTOM_CENTER);
        switch (packing) {
        case EXPAND:
            itemComponent.setWidth("100%");
            break;
        case SPACE_AROUND:
        case SPACE_AFTER:
            layout.setExpandRatio(itemComponent, 1);
            layout.setComponentAlignment(itemComponent, Alignment.BOTTOM_LEFT);
            break;
        default:
            // do nothing
            break;
        }
    }
}
