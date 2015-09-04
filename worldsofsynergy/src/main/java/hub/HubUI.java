package hub;


import helpers.WorldHelper;
import helpers.WorldHelper.WorldBean;

import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.HtmlRenderer;

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
        layout.setSizeFull();
        setContent(layout);
        Table table = new Table("Worlds of Synergy", WorldHelper.getWorlds());
        table.addGeneratedColumn("description", new ColumnGenerator() {

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId)
            {
                String text = (String) source.getContainerDataSource().getItem(itemId).getItemProperty("description").getValue();
                return new Label(text, ContentMode.HTML);
            }
            
        });
        table.setVisibleColumns("name", "description");
        table.setSelectable(true);
        table.setImmediate(true);
        table.addValueChangeListener(new ValueChangeListener() {
        @Override
        public void valueChange(ValueChangeEvent event)
        {
            WorldBean bean = ((BeanItem<WorldBean>)table.getItem(event.getProperty().getValue())).getBean();
            if (bean.getPath() != null) {
                String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
                getPage().setLocation(contextPath+bean.getPath());
            }
        }
        });
        table.sort(new Object[] {"name"}, new boolean[] {true});
        layout.addComponent(table);
        
//        Grid grid = new Grid("Worlds of Synergy", WorldHelper.getWorlds());
//        grid.setColumnOrder("name", "description");
//        grid.setSortOrder(Lists.asList(new SortOrder("name", SortDirection.ASCENDING), new SortOrder[] {}));
//        grid.getColumn("description").setRenderer(new HtmlRenderer());
//        grid.getColumn("description").setExpandRatio(1);
//        grid.addSelectionListener(new SelectionListener() {
//
//            @Override
//            public void select(SelectionEvent event)
//            {
//                WorldBean bean = (WorldBean) grid.getSelectedRow();
//                if (bean.getPath() != null) {
//                    String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
//                    getPage().setLocation(contextPath+bean.getPath());
//                }
//            }
//        });
//        grid.setSizeFull();
//        layout.addComponent(grid);
    }
}
