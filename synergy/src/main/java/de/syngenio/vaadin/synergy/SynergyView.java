package de.syngenio.vaadin.synergy;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontIcon;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView.ItemComponent.State;
import de.syngenio.vaadin.synergy.builder.SynergyBuilder;
import de.syngenio.vaadin.synergy.layout.SynergyLayout;
import de.syngenio.vaadin.synergy.layout.SynergyLayoutFactory;

public class SynergyView extends CustomComponent
{
    public static final String DEFAULT_PRIMARY_STYLE_NAME = "synergy";
    private static final long serialVersionUID = 1L;
    private SynergySelect select;
    private SynergyLayout layout;
    private NavigationHandler navigationHandler = new NavigationHandler();
    
    /**
     * signifies that the view is in inactive state
     */
    private final static String INACTIVE = UUID.randomUUID().toString();
    private String parentId = INACTIVE;
    
    private Component wrapper = this; // default: SynergyView has no extra wrapper

    public Component getWrapper()
    {
        return wrapper;
    }

    public void setWrapper(Component wrapper)
    {
        this.wrapper = wrapper;
    }

    @SuppressWarnings("unused")
    private SynergyView parentView = null;
    private SynergyView subView = null;
    private Map<String, ItemComponent> itemComponents = null;
    private SynergyLayoutFactory layoutFactory;
    private ValueChangeListener viewUpdatingListener;
    private ValueChangeListener navigatingListener;
    
    private final static Logger log = LoggerFactory.getLogger(SynergyView.class);

    public SynergyView(SynergyLayoutFactory layoutFactory)
    {
        this(layoutFactory, SynergyBuilder.createHierarchicalContainer());
    }

    public SynergyView(SynergyLayoutFactory layoutFactory, Container dataSource)
    {
        this(layoutFactory, (SynergyView)null);
        attachToSelect(new SynergySelect(dataSource));
        setParentId(null);
    }
    
    /**
     * Creates a subview of a parent view.
     * This causes the parent view to visualize just a single
     * layer of the navigation hierarchy, while the children of a selected item
     * will be visualized in this subview.
     * @param layoutFactory
     * @param parentView
     */
    public SynergyView(SynergyLayoutFactory layoutFactory, SynergyView parentView)
    {
        setPrimaryStyleName(DEFAULT_PRIMARY_STYLE_NAME);
        String subviewStyle = getSubviewStyle();
        if (subviewStyle != null) {
            addStyleName(subviewStyle);
        }
        this.layoutFactory = layoutFactory;
        this.parentView = parentView;
        layout = layoutFactory.generateLayout();
        setCompositionRoot(layout);
        addStyleName(layout.getOrientationStyleName());
        
        // will be added to the SynergySelect later
        viewUpdatingListener = new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event)
            {
                log.debug("parentId="+parentId+" valueChange");
                final UI ui = getUI();
                if (ui != null) {
                    ui.access(new Runnable() {
                        public void run()
                        {
                            for (String itemId : getImmediateChildItemIds()) {
                                updateSelectedVisuals(itemId);
                            }
//                            ui.push();
                        }

                    });
                }
            }
        };
        // will be added to the SynergySelect later
        navigatingListener = new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event)
            {
                String itemId = (String) select.getValue();
                // is it an item that this view visualizes?
                if (SynergyBuilder.isChildOf(select.getContainerDataSource(), itemId, parentId)) {
                    Item item = select.getContainerDataSource().getItem(itemId);
                    UI ui = SynergyView.this.getUI();
                    if (ui != null && navigationHandler != null) { 
                        // delegate to the current NavigationHandler
                        navigationHandler.selected(item, ui);
                    }
                }
            }
        };

        if (this.parentView != null) {
            attachToSelect(parentView.select);
            parentView.setSubView(this);
        }
    }
    
    private String getSubviewStyle()
    {
        if (parentId != null && !parentId.equals(INACTIVE)) {
            return (String) select.getContainerDataSource().getContainerProperty(parentId, SynergyBuilder.PROPERTY_ITEM_SUBVIEW_STYLE).getValue();
        }
        return null;
    }

    private void setParentId(String parentId)
    {
        this.parentId = parentId;
        visualizeItems();
    }

    /**
     * Empties the layout and then calls {@code visualizeItem} on each immediate child item 
     * Called either when the view has been moved to a new parent
     * or upon receiving a containerItemSetChange event to update the view's item components.
     */
    private void visualizeItems()
    {
        clear();
        boolean isEmpty = true;
        for (String itemId : getImmediateChildItemIds()) {
            isEmpty = false;
            visualizeItem(itemId);
        }
        setVisible(!isEmpty);
    }

    /**
     * Discards all item components and create a fresh {@code SynergyLayout}
     */
    private void clear()
    {
        itemComponents = new HashMap<String, ItemComponent>();
        // create a fresh SynergyLayout (emptying and reusing the existing layout caused issues)
        layout = layoutFactory.generateLayout();
        setCompositionRoot(layout);
    }

    private Collection<String> getImmediateChildItemIds() {
        return SynergyBuilder.getChildIdsOf(select.getContainerDataSource(), parentId);
    }

    /**
     * Creates the component for visualizing an immediate child item of this view and adds it to the layout.
     * Then calls updateSelectedVisuals for the child item
     * @param itemId
     */
    private void visualizeItem(String itemId)
    {
        ItemComponent itemComponent = getItemComponent(itemId);
        itemComponents.put(itemId, itemComponent);
        layout.addItemComponent(itemComponent);
        updateSelectedVisuals(itemId);
    }

    /**
     * Updates the visualization of an item
     * @param itemId item id
     */
    private void updateSelectedVisuals(String itemId)
    {
        log.trace("updateSelectedVisuals("+itemId+")");
        ItemComponent itemComponent = itemComponents.get(itemId);
        if (itemComponent == null) {
            log.debug("no item component found for id "+itemId);
            return;
        }
        final String selectedItemId = (String) select.getValue();
        if (itemId.equals(selectedItemId)) {
            itemComponent.setState(State.selected);
            replaceSubView(itemId, State.selected);
        } else if (isAncestorOf(itemId, selectedItemId)) {
            itemComponent.setState(State.ancestorOfSelected);
            replaceSubView(itemId, State.ancestorOfSelected);
        } else {
            itemComponent.setState(State.unselected);
        }
    }

    private void replaceSubView(String itemId, State state) {
        boolean hasChildren = false;
        // do we have children? 
        Container container = select.getContainerDataSource();
        if (container instanceof HierarchicalContainer) {
            HierarchicalContainer hc = (HierarchicalContainer) container;
            hasChildren = hc.hasChildren(itemId);
        }
        
        // no subView present
        if (subView == null) {
            // do we need a subview actually?
            if (hasChildren) {
                // create a new subView to render the children of the current item
                setSubView(new SynergyView(layoutFactory.getSubitemLayoutFactory(), this));
                subView.setParentId(itemId);
            }
        } else if (subView.isVisible() && equals(subView.parentId, itemId)) { // anything to do at all?
            // the subView's parent hasn't changed
            // check if the subView's items have changed
            if (subView.itemComponents != null && asSet(getImmediateChildItemIds()).equals(subView.itemComponents.keySet())) {
                // nothing to do
                return;
            }
            // rebuild the subView
            if (hasChildren) {
                subView.setParentId(itemId);
            } else {
                subView.setParentId(INACTIVE);
            }
        } else {
            // remove (wrapped) subView from its previous place
            layout.removeComponent(subView.getWrapper());
            subView.replaceSubView(INACTIVE, state);
            if (hasChildren) {
                subView.setParentId(itemId);
            } else {
                subView.setParentId(INACTIVE);
            }
        }
        
        if (hasChildren) {
            // add it after itemId's component
            Component itemComponent = itemComponents.get(itemId);
            int index = layout.getComponentIndex(itemComponent);
            layout.addSubview(subView, index+1);
            subView.setParentState(state);
        }
    }
    
    private void setParentState(State state)
    {
        state.applyTo(this);
        state.applyTo(layout);
        if (getWrapper() != null && getWrapper() != this) {
            state.applyTo(getWrapper());
        }
    }

    private static void setStateStyleOn(Component component, State state)
    {
        for (State value : State.values()) {
            component.removeStyleName(value.getCssClass());
        }
        component.addStyleName(state.getCssClass());
    }

    private <T> Set<T> asSet(Collection<T> collection)
    {
        return new HashSet<T>(collection);
    }

    private boolean equals(Object itemId1, Object itemId2)
    {
        return itemId1 == null ? itemId2 == null : itemId1.equals(itemId2);
    }

    public void setSubView(SynergyView subView)
    {
        this.subView = subView;
    }

    /**
     * Checks if the item identified by ancestorId is an ancestor of the item identified by itemId
     * @param ancestorId
     * @param descendantId
     * @return true if the item identified by ancestorId is an ancestor of the item identified by descendantId
     */
    private boolean isAncestorOf(String ancestorId, String descendantId) {
        return SynergyBuilder.isAncestorOf(select.getContainerDataSource(), ancestorId, descendantId); 
    }

    /**
     * Sets a different {@code NavigationHandler} to the view.
     * @param navigationHandler
     */
    public void setNavigationHandler(NavigationHandler navigationHandler)
    {
        this.navigationHandler = navigationHandler;
    }

    /**
     * The method {@code selected} of this class or a subclass is called when the user selects an item.
     * The default behaviour is to fetch the UI's {@code Navigator} and navigate to the selected item's
     * target navigation state property. 
     * Install your own behaviour by passing an instance {@code NavigationHandler} 
     * to {@code SynergyView.setNavigationHandler(NavigationHandler)}.
     */
    public class NavigationHandler implements Serializable {

        /**
         * Gets the given UI's {@code Navigator} and passes the item's targetNavigationState to {@code Navigator.navigateTo()}.
         * @param item the selected item
         * @param ui the UI where the event originated (never null)
         */
        protected void selected(Item item, UI ui)
        {
            String targetNavigationState = (String) item.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).getValue();
            log.debug("selected item with targetNavigationState="+targetNavigationState);
            if (targetNavigationState != null) {
                Navigator navigator = ui.getNavigator();
                if (navigator != null) {
                    navigator.navigateTo(targetNavigationState);
                }
            }
        }
    }
    
    public interface ItemComponent extends Component {
        enum State {
            unselected, selected, ancestorOfSelected("ancestor-of-selected");
            public final String cssClassSuffix;

            State()
            {
                this.cssClassSuffix = this.name();
            }

            public void applyTo(Component component)
            {
                for (State value : values()) {
                    component.removeStyleName(value.getCssClass());
                }
                component.addStyleName(getCssClass());
            }

            State(String cssClassSuffix)
            {
                this.cssClassSuffix = cssClassSuffix;
            }

            /**
             * @return the css class suffix associated with this state
             */
            public String getCssClassSuffix()
            {
                return cssClassSuffix;
            }
            
            public String getCssClass() {
                return DEFAULT_PRIMARY_STYLE_NAME + "-" + getCssClassSuffix();
            }
        };

        void setup(final SynergySelect ss, final String itemId);
        void setState(State state);
    }

    
    
    /**
     * Creates a {@code VerticalLayout} of the graphical component and optionally a caption {@code Label}.
     * Depending on the type of the source {@code Resource}, the graphical component is either an
     * {@code Image} or a {@code Label}.  
     */
    public static class ItemComponentImage extends CustomComponent implements ItemComponent {
        private static final String PRIMARY_STYLE_NAME = "synergy-image";
        private VerticalLayout layout;
        private Resource source;
        private Resource sourceSelected;
        private Image image = null;
        private Label glyph = null;
        private Label captionLabel;
        private String glyphSize = null;
        
        public ItemComponentImage() {
            super();
            setPrimaryStyleName(PRIMARY_STYLE_NAME);
        }
        
        public void setup(final SynergySelect synergySelect, final String itemId)
        {
            layout = new VerticalLayout();
//            layout.setSizeFull();
            layout.setMargin(new MarginInfo(false, true, false, true) );
            //FIXME the following is an ugly hack.
            // The root cause is the behaviour of HorizontalLayout with undefined height (in a horizontal SynergyView) 
            // to set the heights of its children to undefined. We set the height of the synergy-image to 100% via CSS,
            // but it does not seem possible to vertical-align:bottom the wrapped VerticalLayout this way.
            // By adding the CSS class v-align-bottom however, it works
            layout.addStyleName("v-align-bottom"); 

            Property<Resource> propertyIcon = synergySelect.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_ICON);
            source = propertyIcon.getValue();
            Property<Resource> propertyIconSelected = synergySelect.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_ICON_SELECTED);
            sourceSelected = propertyIconSelected.getValue();

            if (source instanceof FontIcon) {
                glyph = new Label("", ContentMode.HTML);
                glyph.setSizeUndefined();
                layout.addComponent(glyph);
                layout.setComponentAlignment(glyph, Alignment.BOTTOM_CENTER);
                layout.setExpandRatio(glyph, 1);
                Property<String> propertySize = synergySelect.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_GLYPH_SIZE);
                glyphSize  = propertySize.getValue();
            } else {
                image = new Image();
                image.setSizeUndefined();
                layout.addComponent(image);
                layout.setComponentAlignment(image, Alignment.BOTTOM_CENTER);
                layout.setExpandRatio(image, 1);
                
                Property<String> propertyWidth = synergySelect.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_IMAGE_WIDTH);
                String width = propertyWidth.getValue();
                if (width != null) {
                    image.setWidth(width);
                }
                Property<String> propertyHeight = synergySelect.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_IMAGE_HEIGHT);
                String height = propertyHeight.getValue();
                if (height != null) {
                    image.setHeight(height);
                }
            }
            
            final LayoutClickListener clickListener = new LayoutClickListener() {
                @Override
                public void layoutClick(LayoutClickEvent event)
                {
                    Object selectedItemId = synergySelect.getValue();
                    if (!itemId.equals(selectedItemId)) {
                        synergySelect.select(itemId);
                    }
                }
            };
            
            setSource(source);
            
            Property<String> propertyCaption = synergySelect.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_CAPTION);
            String captionText = propertyCaption.getValue();
            if (captionText != null) {
                captionLabel = new Label(captionText);
                captionLabel.setSizeUndefined();
                layout.addComponent(captionLabel);
                layout.setComponentAlignment(captionLabel, Alignment.TOP_CENTER);
                layout.setExpandRatio(captionLabel, 0);
            }

            layout.addLayoutClickListener(clickListener);

            setCompositionRoot(layout);
            setImmediate(true);

            setId((String)itemId); // for test automation
        }

        private void setSource(Resource source)
        {
            if (image != null) {
                image.setSource(source);
            }
            if (glyph != null) {
                glyph.setValue(generateGlyphHtml(source));
            }
        }

        protected String generateGlyphHtml(Resource source)
        {
            String html = ((FontIcon)source).getHtml();
            // if glyphSize is set, add a font-size style to the HTML string 
            if (glyphSize != null) {
                html = html.replaceAll("(?=font-family)", "font-size:"+glyphSize+";");
            }
            return html;
        }

        @Override
        public void setState(State state)
        {
            // set the appropriate style name (CSS class) on the image 
            state.applyTo(this);
            
            // in addition and if applicable, handle switching icons
            switch (state) {
            case selected:
            case ancestorOfSelected:
                if (sourceSelected != null) {
                    setSource(sourceSelected);
                }
                break;
            case unselected:
                setSource(source);
                break;
            }
        }
    }
    
    
    public static class ItemComponentButton extends Button implements ItemComponent {
        private static final String PRIMARY_STYLE_NAME = "synergy-button";

        public ItemComponentButton() {
            super();
            setPrimaryStyleName(PRIMARY_STYLE_NAME);
        }
        
        public void setup(final SynergySelect ss, final String itemId)
        {
            Property<String> propertyCaption = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_CAPTION);
            String caption = propertyCaption.getValue();
            if (caption == null) {
                caption = itemId;
            }
            setCaption(caption);
            setImmediate(true);
//            setSizeUndefined();
            Property<Resource> propertyIcon = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_ICON);
            Resource iconResource = propertyIcon.getValue();
            if (iconResource != null) {
                setIcon(iconResource);
            }
            addClickListener(new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event)
                {
                    Object selectedItemId = ss.getValue();
                    if (!itemId.equals(selectedItemId)) {
                        ss.select(itemId);
                    }
                }
            });

            setId((String)itemId); // for test automation
        }

        @Override
        public void setState(State state)
        {
            // set the appropriate style name (CSS class) on the button
            state.applyTo(this);
        }
    }
    
    private ItemComponent getItemComponent(final String itemId)
    {
        ItemComponent itemComponent = null;
        Item item = select.getItem(itemId);
        final Property<Class> property = item.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CLASS);
        Class<ItemComponent> itemComponentClass = property.getValue();
        try
        {
            Constructor defcon = itemComponentClass.getConstructor();
            itemComponent = (ItemComponent) defcon.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        itemComponent.setup(select, itemId);
//        itemComponent.setWidth(100, Unit.PERCENTAGE);
//        itemComponent.setHeightUndefined();
        return itemComponent;
    }

    public static Resource createResource(String sourceUri)
    {
        if (sourceUri.matches("^\\w+://.*")) {
            return new ExternalResource(sourceUri);
        } else {
            return new ThemeResource(sourceUri);
        }
    }

    protected void attachToSelect(SynergySelect s)
    {
        this.select = s;
        // add the ValueChangeListener to handle view updates
        this.select.addValueChangeListener(viewUpdatingListener);
        // add the ValueChangeListener to handle navigation
        this.select.addValueChangeListener(navigatingListener);
        
        this.select.addItemSetChangeListener(new ItemSetChangeListener() {
            @Override
            public void containerItemSetChange(ItemSetChangeEvent event)
            {
                log.debug("parentId="+parentId+" containerItemSetChange");
                final UI ui = getUI();
                if (ui != null) {
                    ui.access(new Runnable() {
                        public void run()
                        {
                            visualizeItems();
//                            ui.push();
                        }
                    });
                }
            }
        });
        
        visualizeItems();
    }

    /**
     * Removes listeners from the {@code SynergySelect}. 
     * Call this method before disposing the {@code SynergyView} to avoid memory leaks.
     */
    protected void detachFromSelect() {
        this.select.removeValueChangeListener(navigatingListener);
        this.select.removeValueChangeListener(viewUpdatingListener);
    }
    
    /**
     * @return this {@code SynergyView}'s item container 
     */
    public Container getContainer() {
        return select.getContainerDataSource();
    }
}
