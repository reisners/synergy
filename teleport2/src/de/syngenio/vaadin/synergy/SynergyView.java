package de.syngenio.vaadin.synergy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;

public class SynergyView extends CustomComponent
{
    private static final long serialVersionUID = 1L;
    private SynergySelect select;
    private SynergyLayout layout;
    /**
     * signifies that the view is in inactive state
     */
    private final static Object INACTIVE = new Object();
    private Object parentId = INACTIVE;
    @SuppressWarnings("unused")
    private SynergyView parentView;
    private SynergyView subView = null;
    private Map<Object, Component> itemComponents = null;
    private SynergyLayoutFactory layoutFactory;
    

    public SynergyView(SynergyLayoutFactory layoutFactory, Container dataSource)
    {
        this(layoutFactory, (SynergyView)null);
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

    private void setParentId(Object parentId)
    {
        this.parentId = parentId;
        visualizeItems();
    }

    private void visualizeItems()
    {
        clear();
        for (Object itemId : getImmediateChildItemIds()) {
            visualizeItem(itemId);
        }
    }

    private void clear()
    {
        itemComponents = new HashMap<Object, Component>();
        layout.removeAllComponents();
    }

    private Collection<Object> getImmediateChildItemIds() {
        List<Object> children = new ArrayList<Object>();
        for (Object itemId : select.getContainerDataSource().getItemIds()) {
            if (isChild(itemId)) {
                children.add(itemId);
            }
        }
        return children;
    }
    
    private boolean isChild(Object itemId)
    {
        Container container = select.getContainerDataSource();
        if (!(container instanceof HierarchicalContainer)) {
            return true;
        }
        HierarchicalContainer hc = (HierarchicalContainer) container;
        final Object itemParentId = hc.getParent(itemId);
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
    private Object getParentId(Object itemId)
    {
        Container container = select.getContainerDataSource();
        if (!(container instanceof HierarchicalContainer)) {
            return null;
        }
        HierarchicalContainer hc = (HierarchicalContainer) container;
        return hc.getParent(itemId);
    }

    /**
     * Fetch the component for visualizing the item identified by itemId.
     * In addition, if the item has children, creates a nested VerticalSyngergyView
     * @param itemId
     */
    private void visualizeItem(Object itemId)
    {
        Component itemComponent = getItemComponent(itemId);
        itemComponents.put(itemId, itemComponent);
        layout.addItemComponent(itemComponent);
        updateSelectedVisuals(itemId);
    }

    private void updateSelectedVisuals(Object itemId)
    {
        Component itemComponent = itemComponents.get(itemId);
        if (itemComponent == null) {
            return;
        }
        final Object selectedItemId = select.getValue();
        if (itemId.equals(selectedItemId)) {
            itemComponent.setStyleName("synergy-selected");
            replaceSubView(itemId);
        } else if (isAncestorOf(itemId, selectedItemId)) {
            itemComponent.setStyleName("synergy-ancestor-of-selected");
            replaceSubView(itemId);
        } else {
            itemComponent.setStyleName("synergy-unselected");
        }
    }

    private void replaceSubView(Object itemId) {
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
    private boolean isAncestorOf(Object ancestorId, Object itemId) {
        if (ancestorId == null || itemId == null) {
            return false;
        }
        final Object itemParentId = getParentId(itemId);
        return ancestorId.equals(itemParentId) || isAncestorOf(ancestorId, itemParentId); 
    }
   
    private Component getItemComponent(final Object itemId)
    {
        // a very crude default implementation
        final Button button = new Button(itemId.toString());
        final SynergySelect ss = select;
        button.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(ClickEvent event)
            {
                Object selectedItemId = ss.getValue();
                if (!itemId.equals(selectedItemId)) {
                    ss.select(itemId);
                    String targetNavigationState = (String) ss.getContainerProperty(itemId, HierarchicalContainerHelper.PROPERTY_TARGET_NAVIGATION_STATE).getValue();
                    if (targetNavigationState != null) {
                        UI.getCurrent().getNavigator().navigateTo(targetNavigationState);
                    }
                }
            }
        });
        button.setWidth(100, Unit.PERCENTAGE);
        button.setHeightUndefined();
        return button;
    }

    protected void setSelect(SynergySelect select)
    {
        this.select = select;
        this.select.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event)
            {
                for (Object itemId : getImmediateChildItemIds()) {
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
