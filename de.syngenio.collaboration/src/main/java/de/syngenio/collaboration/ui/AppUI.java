package de.syngenio.collaboration.ui;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.BlurNotifier;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.FieldEvents.FocusNotifier;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.collaboration.data.RepositoryService;
import de.syngenio.collaboration.data.Sheet;
import de.syngenio.collaboration.data.SheetModel;
import de.syngenio.collaboration.data.SheetRepository;

@SuppressWarnings("serial")
@SpringUI
@Theme("valo")
@Title("SYNGENIO Collaboration Tool")
@Push
public class AppUI extends UI
{
    private static final Logger log = LoggerFactory.getLogger(AppUI.class);

    public static final String CONTEXT_PATH = "/ui";

    @Autowired
    public
    SheetRepository sheetRepository;

    @Autowired
    RepositoryService repository;
    
    @Override
    protected void init(VaadinRequest request) {

        setSizeFull();
        HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSizeFull();
        setContent(hlayout);
        Component gridLeft = createTable(repository, "GridSheet");
        hlayout.addComponent(gridLeft);
        Component gridRight = createTable(repository, "GridSheet");
        hlayout.addComponent(gridRight);
    }

    private Component createGrid(RepositoryService repository, String sheetName)
    {
        SheetModel model = createSheetModel(sheetName);
        VerticalLayout vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        Indexed container = model.getContainer();
        log.info("container has "+container.getItemIds().size()+" items");
        Grid grid = new Grid(sheetName, container);
        grid.setSizeFull();
        grid.setEditorEnabled(true);
        grid.addItemClickListener(new ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event)
            {
                if (event.isDoubleClick()) {
                    model.freeze();
                    grid.editItem(event.getItemId());
                }
            }
        });
        vlayout.addComponent(grid);
        //TODO add controls to add/delete rows and columns, and for "time travel"
        return vlayout;
    }

    private Component createTable(RepositoryService repository, String sheetName)
    {
        SheetModel model = createSheetModel(sheetName);
        VerticalLayout vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        Indexed container = model.getContainer();
        log.info("container has "+container.getItemIds().size()+" items");
        Table table = new Table(sheetName, container);
        table.setSizeFull();
        table.setImmediate(true);
        table.setEditable(true);
        table.setTableFieldFactory(new TableFieldFactory() {
            @Override
            public Field< ? > createField(Container container, Object itemId, Object propertyId, Component uiContext)
            {
                Item item = container.getItem(itemId);
                Property<Object> property = item.getItemProperty(propertyId);
                TableCell field = new TableCell(property);
                field.addFocusListener(new FocusListener() {
                    @Override
                    public void focus(FocusEvent event)
                    {
                        model.freeze();
                        field.startEditing();
                    }
                });
                field.addBlurListener(new BlurListener() {
                    @Override
                    public void blur(BlurEvent event)
                    {
                        model.thaw();
                        field.stopEditing();
                    }
                });
                field.addValueChangeListener(new ValueChangeListener() {
                    @Override
                    public void valueChange(ValueChangeEvent event)
                    {
                        model.thaw();
                        field.stopEditing();
                    }
                });
                field.setImmediate(true);
                return field;
            }
        });
        vlayout.addComponent(table);
        //TODO add controls to add/delete rows and columns, and for "time travel"
        return vlayout;
    }

    private class TableCell extends CustomField<Object> implements FocusNotifier, BlurNotifier {

        private class CellComponent extends CustomComponent implements FocusNotifier, BlurNotifier {
            private Set<FocusListener> focusListeners = new HashSet<FocusListener>();
            private Set<BlurListener> blurListeners = new HashSet<BlurListener>();
            
            private CellComponent() {
                readOnlyMode();
            }
            
            private void editMode() {
                TextField field = new TextField(getPropertyDataSource());
                setCompositionRoot(field);
                field.focus();
            }
            
            private void readOnlyMode() {
                CssLayout wrapper = new CssLayout();
                Label label = new Label(getPropertyDataSource());
                wrapper.addComponent(label);
                wrapper.addLayoutClickListener(new LayoutClickListener() {
                    
                    @Override
                    public void layoutClick(LayoutClickEvent event)
                    {
                        for (FocusListener listener : focusListeners) {
                            listener.focus(new FocusEvent(label));
                        }
                    }
                });
                setCompositionRoot(wrapper);
            }

            @Override
            public void addFocusListener(FocusListener listener)
            {
                focusListeners.add(listener);
            }

            @Override
            public void addListener(FocusListener listener)
            {
                focusListeners.add(listener);
            }

            @Override
            public void removeFocusListener(FocusListener listener)
            {
                focusListeners.remove(listener);
            }

            @Override
            public void removeListener(FocusListener listener)
            {
                focusListeners.remove(listener);
            }

            @Override
            public void addBlurListener(BlurListener listener)
            {
                blurListeners.add(listener);
            }

            @Override
            public void addListener(BlurListener listener)
            {
                blurListeners.add(listener);
            }

            @Override
            public void removeBlurListener(BlurListener listener)
            {
                blurListeners.remove(listener);
            }

            @Override
            public void removeListener(BlurListener listener)
            {
                blurListeners.remove(listener);
            }
        }

        private CellComponent cellComponent;
        
        private TableCell(Property<Object> property)
        {
            setPropertyDataSource(property);
            cellComponent = new CellComponent();
        }

        @Override
        protected Component initContent()
        {
            return cellComponent;
        }

        @Override
        public Class< ? extends Object> getType()
        {
            return Object.class;
        }

        @Override
        public void addFocusListener(FocusListener listener)
        {
            cellComponent.addFocusListener(listener);
        }

        @Override
        public void addListener(FocusListener listener)
        {
            cellComponent.addFocusListener(listener);
        }

        @Override
        public void removeFocusListener(FocusListener listener)
        {
            cellComponent.removeFocusListener(listener);
        }

        @Override
        public void removeListener(FocusListener listener)
        {
            cellComponent.removeFocusListener(listener);
        }
        
        private void startEditing() {
            cellComponent.editMode();
        }
        
        private void stopEditing() {
            cellComponent.readOnlyMode();
        }

        @Override
        public void addBlurListener(BlurListener listener)
        {
            cellComponent.addBlurListener(listener);
        }

        @Override
        public void addListener(BlurListener listener)
        {
            cellComponent.addBlurListener(listener);
        }

        @Override
        public void removeBlurListener(BlurListener listener)
        {
            cellComponent.removeBlurListener(listener);
        }

        @Override
        public void removeListener(BlurListener listener)
        {
            cellComponent.removeBlurListener(listener);
        }
    }
    
    private SheetModel createSheetModel(String sheetName)
    {
        Sheet sheet = repository.findOrCreateSheet(sheetName);
        return new SheetModel(repository, sheet);
    }
}