package de.syngenio.vaadin.synergy;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;

import de.syngenio.vaadin.synergy.SynergyView.ItemComponent.State;

public class SynergyView extends CustomComponent
{
    private static final String STYLE_NAME = "synergy";
    private static final String STYLE_NAME_UNSELECTED = STYLE_NAME+"-unselected";
    private static final String STYLE_NAME_ANCESTOR_OF_SELECTED = STYLE_NAME+"-ancestor-of-selected";
    private static final String STYLE_NAME_SELECTED = STYLE_NAME+"-selected";
    private static final long serialVersionUID = 1L;
    private SynergySelect select;
    private SynergyLayout layout;
    /**
     * signifies that the view is in inactive state
     */
    private final static String INACTIVE = UUID.randomUUID().toString();
    private String parentId = INACTIVE;
    
    private final static String PRIMARY_STYLE_NAME = STYLE_NAME+"view";
    
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
        setPrimaryStyleName(PRIMARY_STYLE_NAME);
        setSelect(new SynergySelect(dataSource));
        setParentId(null);
    }
    
    public SynergyView(SynergyLayoutFactory layoutFactory, SynergyView parentView)
    {
        this.layoutFactory = layoutFactory;
        this.parentView = parentView;
        layout = layoutFactory.generateLayout();
        setCompositionRoot(layout);
        
        if (this.parentView != null) {
            setSelect(parentView.select);
        }
    }

    private void setParentId(String parentId)
    {
        this.parentId = parentId;
        visualizeItems();
    }

    private void visualizeItems()
    {
        clear();
        for (String itemId : getImmediateChildItemIds()) {
            if ("|Tools|Collaboration|Chat".equals(parentId)) {
                log.info("visualizing SynergyView "+((Object)this).toString()+" item "+itemId);
            }
            visualizeItem(itemId);
        }
    }

    private void clear()
    {
        itemComponents = new HashMap<String, ItemComponent>();
        layout.removeAllComponents();
    }

    private Collection<String> getImmediateChildItemIds() {
        return SynergyBuilder.getChildIdsOf(select.getContainerDataSource(), parentId);
    }

    /**
     * Fetch the component for visualizing the item identified by itemId.
     * In addition, if the item has children, creates a nested VerticalSyngergyView
     * @param itemId
     */
    private void visualizeItem(String itemId)
    {
        ItemComponent itemComponent = getItemComponent(itemId);
        itemComponents.put(itemId, itemComponent);
        layout.addItemComponent(itemComponent);
        updateSelectedVisuals(itemId);
    }

    private void updateSelectedVisuals(String itemId)
    {
        ItemComponent itemComponent = itemComponents.get(itemId);
        if (itemComponent == null) {
            return;
        }
        final String selectedItemId = (String) select.getValue();
        if (itemId.equals(selectedItemId)) {
            itemComponent.setState(State.selected);
            replaceSubView(itemId);
        } else if (isAncestorOf(itemId, selectedItemId)) {
            itemComponent.setState(State.ancestorOfSelected);
            replaceSubView(itemId);
        } else {
            itemComponent.setState(State.unselected);
        }
    }

    private void replaceSubView(String itemId) {
        boolean hasChildren = false;
        // do we need a subview actually?
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
            // the subView's parent hasn't changed, so nothing to do
            return;
        } else {
            // remove subView from its previous place
            layout.removeComponent(subView);
            subView.replaceSubView(INACTIVE);
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
        }
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

    public interface ItemComponent extends Component {
        enum State { unselected, selected, ancestorOfSelected }; 
        void setup(final SynergySelect ss, final Object itemId);
        void setState(State state);
    }

    
    public static class ItemComponentImage extends Image implements ItemComponent {
        private Resource source;
        private Resource sourceSelected;
        
        public ItemComponentImage() {
            super();
        }
        
        public void setup(final SynergySelect ss, final Object itemId)
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
            Property<String> propertyCaption = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_COMPONENT_CAPTION);
            String caption = propertyCaption.getValue();
            if (caption == null) {
                caption = itemId.toString();
            }
            setCaption(caption);
            Property<String> propertyWidth = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_COMPONENT_WIDTH);
            String width = propertyWidth.getValue();
            if (width != null) {
                setWidth(width);
            }
            Property<String> propertyHeight = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_COMPONENT_HEIGHT);
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
                        String targetNavigationState = (String) ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).getValue();
                        if (targetNavigationState != null) {
                            UI.getCurrent().getNavigator().navigateTo(targetNavigationState);
                        }
                    }
                }
            });
        }

        @Override
        public void setState(State state)
        {
            switch (state) {
            case selected:
                setStyleName(STYLE_NAME_SELECTED);
                if (sourceSelected != null) {
                    setSource(sourceSelected);
                }
                break;
            case ancestorOfSelected:
                setStyleName(STYLE_NAME_ANCESTOR_OF_SELECTED);
                if (sourceSelected != null) {
                    setSource(sourceSelected);
                }
                break;
            case unselected:
                setStyleName(STYLE_NAME_UNSELECTED);
                setSource(source);
                break;
            }
        }
    }

    public static class ItemComponentButton extends Button implements ItemComponent {
        public ItemComponentButton() {
            super();
        }
        
        public void setup(final SynergySelect ss, final Object itemId)
        {
            Property<String> propertyCaption = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_COMPONENT_CAPTION);
            String caption = propertyCaption.getValue();
            if (caption == null) {
                caption = itemId.toString();
            }
            setCaption(caption);
            setImmediate(true);
            setSizeUndefined();
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
                        String targetNavigationState = (String) ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).getValue();
                        if (targetNavigationState != null) {
                            getUI().getNavigator().navigateTo(targetNavigationState);
                        }
                    }
                }
            });
        }

        @Override
        public void setState(State state)
        {
            switch (state) {
            case selected:
                setStyleName(STYLE_NAME_SELECTED);
                break;
            case ancestorOfSelected:
                setStyleName(STYLE_NAME_ANCESTOR_OF_SELECTED);
                break;
            case unselected:
                setStyleName(STYLE_NAME_UNSELECTED);
                break;
            }
        }
    }
    
    private ItemComponent getItemComponent(final Object itemId)
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

    protected void setSelect(SynergySelect select)
    {
        this.select = select;
        this.select.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event)
            {
//                log.info("valueChange("+event+") called for "+((Object)this).toString()+", subView="+((Object)subView).toString());
                for (String itemId : getImmediateChildItemIds()) {
                    updateSelectedVisuals(itemId);
                }
            }
        });
        
        this.select.addItemSetChangeListener(new ItemSetChangeListener() {
            @Override
            public void containerItemSetChange(ItemSetChangeEvent event)
            {
                final VaadinSession session = UI.getCurrent().getSession();
                if (session != null) {
                    session.lock();
                }
                try {
                   visualizeItems();
                } finally {
                    if (session != null) {
                        session.unlock();
                    }
                }
            }
        });
    }

    public SynergySelect getSelect()
    {
        return select;
    }
}
