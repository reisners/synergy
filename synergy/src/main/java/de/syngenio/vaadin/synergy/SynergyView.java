package de.syngenio.vaadin.synergy;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
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
            visualizeItem(itemId);
        }
    }

    private void clear()
    {
        itemComponents = new HashMap<String, ItemComponent>();
        layout.removeAllComponents();
    }

    private Collection<String> getImmediateChildItemIds() {
        List<String> children = new ArrayList<String>();
        for (Object objItemId : select.getContainerDataSource().getItemIds()) {
            String itemId = (String) objItemId;
            if (isChild(itemId)) {
                children.add(itemId);
            }
        }
        return children;
    }
    
    private boolean isChild(String itemId)
    {
        Container container = select.getContainerDataSource();
        if (!(container instanceof HierarchicalContainer)) {
            return true;
        }
        HierarchicalContainer hc = (HierarchicalContainer) container;
        final String itemParentId = (String) hc.getParent(itemId);
        if (parentId == null) {
            return itemParentId == null;
        } else {
            return parentId.equals(itemParentId);
        }
    }
    
    /**
     * If the item identified by itemId has a parent, return that parent's id.
     * Always returns null if the container is not an instance of HierarchicalContainer.
     */
    private String getParentId(String itemId)
    {
        Container container = select.getContainerDataSource();
        if (!(container instanceof HierarchicalContainer)) {
            return null;
        }
        HierarchicalContainer hc = (HierarchicalContainer) container;
        return (String) hc.getParent(itemId);
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
     * @param itemId
     * @return
     */
    private boolean isAncestorOf(String ancestorId, String itemId) {
        if (ancestorId == null || itemId == null) {
            return false;
        }
        final String itemParentId = getParentId(itemId);
        return ancestorId.equals(itemParentId) || isAncestorOf(ancestorId, itemParentId); 
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
            String sourceId = propertySource.getValue();
            if (sourceId != null) {
                source = new ThemeResource(sourceId);
                setSource(source);
            }
            Property<String> propertySourceSelected = ss.getContainerProperty(itemId, SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED);
            String sourceSelectedId = propertySourceSelected.getValue();
            if (sourceSelectedId != null) {
                sourceSelected = new ThemeResource(sourceSelectedId);
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
            String source = propertySource.getValue();
            if (source != null) {
                setIcon(new ThemeResource(source));
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
        final Property<String> property = item.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CLASS);
        String itemComponentClassName = property.getValue();
        if (itemComponentClassName != null) {
            try
            {
                Class itemComponentClass = Class.forName(itemComponentClassName);
                Constructor defcon = itemComponentClass.getConstructor();
                itemComponent = (ItemComponent) defcon.newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if (itemComponent == null) {
            itemComponent = new ItemComponentButton();
        }
        itemComponent.setup(select, itemId);
//        itemComponent.setWidth(100, Unit.PERCENTAGE);
//        itemComponent.setHeightUndefined();
        return itemComponent;
    }

    protected void setSelect(SynergySelect select)
    {
        this.select = select;
        this.select.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event)
            {
                for (String itemId : getImmediateChildItemIds()) {
                    updateSelectedVisuals(itemId);
                }
            }
        });
        
        this.select.addItemSetChangeListener(new ItemSetChangeListener() {
            @Override
            public void containerItemSetChange(ItemSetChangeEvent event)
            {
                visualizeItems();
            }
        });
    }

}
