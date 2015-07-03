package de.syngenio.vaadin.synergy;

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
import com.vaadin.event.MouseEvents;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;

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
    private SynergyView parentView;
    private SynergyView subView = null;
    private Map<String, ItemComponent> itemComponents = null;
    private SynergyLayoutFactory layoutFactory;
    
    private final static Logger log = LoggerFactory.getLogger(SynergyView.class);

    public SynergyView(SynergyLayoutFactory layoutFactory)
    {
        this(layoutFactory, SynergyBuilder.createHierarchicalContainer());
    }

    public SynergyView(SynergyLayoutFactory layoutFactory, Container dataSource)
    {
        this(layoutFactory, (SynergyView)null);
        setSelect(new SynergySelect(dataSource));
        setParentId(null);
    }
    
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
        
        if (this.parentView != null) {
            setSelect(parentView.select);
        }
    }
    
    @Override
    public void setCaption(String caption)
    {
        layout.setCaption(caption);
    }

    @Override
    public void setCaptionAsHtml(boolean captionAsHtml)
    {
        layout.setCaptionAsHtml(captionAsHtml);
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
        for (String itemId : getImmediateChildItemIds()) {
            visualizeItem(itemId);
        }
    }

    /**
     * Discards all item components
     */
    private void clear()
    {
        itemComponents = new HashMap<String, ItemComponent>();
//        layout.removeAllComponents();
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
        log.info("updateSelectedVisuals("+itemId+")");
        ItemComponent itemComponent = itemComponents.get(itemId);
        if (itemComponent == null) {
            log.info("no item component found for id "+itemId);
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

    public void setNavigationHandler(NavigationHandler navigationHandler)
    {
        this.navigationHandler = navigationHandler;
    }

    public class NavigationHandler {

        protected void selected(Item item)
        {
            String targetNavigationState = (String) item.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).getValue();
            if (targetNavigationState != null) {
                Navigator navigator = SynergyView.this.getUI().getNavigator();
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

    
    public static class ItemComponentImage extends Image implements ItemComponent {
        private static final String PRIMARY_STYLE_NAME = "synergy-image";
        private Resource source;
        private Resource sourceSelected;
        
        public ItemComponentImage() {
            super();
            setPrimaryStyleName(PRIMARY_STYLE_NAME);
        }
        
        public void setup(final SynergySelect ss, final String itemId)
        {
            Property<String> propertySource = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE);
            String sourceUri = propertySource.getValue();
            if (sourceUri != null) {
                source = createResource(sourceUri);
                setSource(source);
            }
            
            Property<String> propertySourceSelected = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED);
            String sourceSelectedUri = propertySourceSelected.getValue();
            if (sourceSelectedUri != null) {
                sourceSelected = createResource(sourceSelectedUri);
            }
            
            Property<Resource> propertyIcon = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_ICON);
            source = propertyIcon.getValue();
            setSource(source);
            Property<Resource> propertyIconSelected = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_ICON_SELECTED);
            sourceSelected = propertyIconSelected.getValue();
            
            Property<String> propertyCaption = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_CAPTION);
            String caption = propertyCaption.getValue();
            if (caption == null) {
                caption = itemId;
            }
            setCaption(caption);
            Property<String> propertyWidth = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_IMAGE_WIDTH);
            String width = propertyWidth.getValue();
            if (width != null) {
                setWidth(width);
            }
            Property<String> propertyHeight = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_IMAGE_HEIGHT);
            String height = propertyHeight.getValue();
            if (height != null) {
                setHeight(height);
            }
            setImmediate(true);
            addClickListener(new MouseEvents.ClickListener() {
                @Override
                public void click(com.vaadin.event.MouseEvents.ClickEvent event)
                {
                    Object selectedItemId = ss.getValue();
                    if (!itemId.equals(selectedItemId)) {
                        ss.select(itemId);
//                        String targetNavigationState = (String) ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).getValue();
//                        if (targetNavigationState != null) {
//                            UI.getCurrent().getNavigator().navigateTo(targetNavigationState);
//                        }
                    }
                }
            });
            
            setId((String)itemId); // for test automation
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
            Property<String> propertySource = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE);
            String sourceUri = propertySource.getValue();
            if (sourceUri != null) {
                setIcon(createResource(sourceUri));
            }
            addClickListener(new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event)
                {
                    Object selectedItemId = ss.getValue();
                    if (!itemId.equals(selectedItemId)) {
                        ss.select(itemId);
//                        String targetNavigationState = (String) ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).getValue();
//                        if (targetNavigationState != null) {
//                            Navigator navigator = getUI().getNavigator();
//                            if (navigator != null) {
//                                navigator.navigateTo(targetNavigationState);
//                            }
//                        }
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

    protected void setSelect(SynergySelect s)
    {
        this.select = s;
        // add the ValueChangeListener to handle view updates
        this.select.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

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
        });
        // add the ValueChangeListener to handle navigation
        this.select.addValueChangeListener(new ValueChangeListener() {
            
            @Override
            public void valueChange(ValueChangeEvent event)
            {
                String itemId = (String) select.getValue();
                Item item = select.getContainerDataSource().getItem(itemId);
                // delegate to the current NavigationHandler
                navigationHandler.selected(item);
            }
        });
        
        this.select.addItemSetChangeListener(new ItemSetChangeListener() {
            @Override
            public void containerItemSetChange(ItemSetChangeEvent event)
            {
                log.info("parentId="+parentId+" containerItemSetChange");
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
    }

    /**
     * @return this {@code SynergyView}'s item container 
     */
    public Container getContainer() {
        return select.getContainerDataSource();
    }
}
