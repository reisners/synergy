package de.syngenio.vaadin.synergy;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

import de.syngenio.vaadin.synergy.SynergyView.ItemComponentButton;

public class SynergyBuilder
{
    private Map<URI, Object> spec = null; // root does not have spec
    private List<ItemBuilder> itemBuilders = new ArrayList<ItemBuilder>();
    
    private HierarchicalContainer hierarchicalContainer;
    private String parentItemId = null;
    
    public SynergyBuilder() {
        this(createHierarchicalContainer(), null);
    }
    
    public SynergyBuilder(HierarchicalContainer hc, String parentItemId) {
        this.hierarchicalContainer = hc;
        this.parentItemId = parentItemId;
    }
    
    public static final URI PROPERTY_TARGET_NAVIGATION_STATE = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/targetNavigationState");
    /**
     * Fully qualified class name of an implementation of {@code ItemComponent} (optional, defaults to {@code ItemComponentButton}) 
     */
    public static final URI PROPERTY_ITEM_COMPONENT_CLASS = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentClass");
    /**
     * Item caption text (optional)
     */
    public static final URI PROPERTY_ITEM_COMPONENT_CAPTION = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentCaption");
    /**
     * Theme resource URI of image or button icon, respectively (optional)
     * @see {@code SynergyBuilder#PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED}
     */
    public static final URI PROPERTY_ITEM_COMPONENT_SOURCE = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentSource");
    /**
     * Theme resource URI of selected image or button icon, respectively.
     * Optional; if provided, replaces {@code SynergyBuilder#PROPERTY_ITEM_COMPONENT_SOURCE} in selected states
     */
    public static final URI PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentSourceSelected");
    /**
     * Image width (optional; only relevant for {@code ItemComponentImage})
     */
    public static final URI PROPERTY_ITEM_COMPONENT_WIDTH = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentWidth");
    /**
     * Image height (optional; only relevant for {@code ItemComponentImage})
     */
    public static final URI PROPERTY_ITEM_COMPONENT_HEIGHT = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentHeight");
    /**
     * Can be used to inform a filter how to deal with items not having children (optional)  
     */
    public static final URI PROPERTY_ITEM_HIDDEN_IF_EMPTY = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemHiddenIfEmpty");

    public static HierarchicalContainer createHierarchicalContainer() {
        final HierarchicalContainer hierarchicalContainer = new HierarchicalContainer();
        hierarchicalContainer.addContainerProperty(PROPERTY_TARGET_NAVIGATION_STATE, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_CLASS, Class.class, ItemComponentButton.class);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_CAPTION, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_SOURCE, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_WIDTH, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_HEIGHT, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_HIDDEN_IF_EMPTY, Boolean.class, Boolean.FALSE);
        return hierarchicalContainer;
    }

    public HierarchicalContainer build() {
        return build(hierarchicalContainer, null);
    }
    
    private HierarchicalContainer build(HierarchicalContainer hc, String parentItemId) {
        for (ItemBuilder itemBuilder : itemBuilders) {
            itemBuilder.build(hc, null);
            if (itemBuilder.childrenBuilder != null) {
                itemBuilder.childrenBuilder.build(hc, itemBuilder.id);
            }
            if (parentItemId != null) {
                hc.setParent(itemBuilder.id, parentItemId);
            }
        }
        return hierarchicalContainer;
    }
    
    /**
     * Checks if the item identified by ancestorId is an ancestor of the item identified by itemId
     * @param container navigation hierarchy
     * @param ancestorId id of an item nearer to the roots of the hierarchy (may be null)
     * @param descendantId id of an item deeper down the hiearchy
     * @return true if the item identified by ancestorId is an ancestor of the item identified by descendantId
     */
    public static boolean isAncestorOf(Container container, String ancestorId, String descendantId)
    {
        if (ancestorId == null) {
            return true;
        }
        if (descendantId == null) {
            return false;
        }
        final String itemParentId = getParentId(container, descendantId);
        return ancestorId.equals(itemParentId) || isAncestorOf(container, ancestorId, itemParentId);
    }

    public static String getParentId(Container container, String itemId)
    {
        if (!(container instanceof HierarchicalContainer)) {
            return null;
        }
        HierarchicalContainer hc = (HierarchicalContainer) container;
        return (String) hc.getParent(itemId);
    }

    public static Collection<String> getChildIdsOf(Container container, String parentItemId)
    {
        if (container instanceof HierarchicalContainer) {
            HierarchicalContainer hc = (HierarchicalContainer) container;
            List<String> children = new ArrayList<String>();
            for (Object objItemId : hc.getItemIds()) {
                String itemId = (String) objItemId;
                if (isChildOf(hc, itemId, parentItemId)) {
                    children.add(itemId);
                }
            }
            return children;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Tests for a parent-child relationship or a root item
     * @param hc navigation hierarchy
     * @param itemId id of item in hc
     * @param parentItemId id of another item "parent" in hc or null
     * @return true if item is a child of parent, or if parentItemId==null and item is a root item 
     */
    public static boolean isChildOf(HierarchicalContainer hc, String itemId, final String parentItemId)
    {
        String itemParentId = (String) hc.getParent(itemId);
        if (parentItemId == null) {
            return itemParentId == null;
        } else {
            return parentItemId.equals(itemParentId);
        }
    }

    public static class ItemBuilder {
        protected final String id;
        protected String caption = null;
        protected String targetNavigationState = null;
        protected boolean hiddenIfEmpty = false;
        protected String imageSource = null;
        protected String imageSourceSelected = null;
        protected SynergyBuilder childrenBuilder = null;

        protected ItemBuilder(String id, String caption, String imageSource, String imageSourceSelected, String targetNavigationState, boolean hiddenIfEmpty, SynergyBuilder childrenBuilder) {
            this.id = id;
            this.caption = caption;
            this.imageSource = imageSource;
            this.imageSourceSelected = imageSourceSelected;
            this.targetNavigationState = targetNavigationState;
            this.hiddenIfEmpty = hiddenIfEmpty;
            this.childrenBuilder = childrenBuilder;
        }
        
        protected ItemBuilder(String id) {
            this.id = id;
            this.caption = id.replaceAll("^.*|(?!.*|)", ""); // default caption to the part of the id after the last pipe (|)
        }
        
        protected Item build(HierarchicalContainer hc, String parentItemId) {
            Item item = hc.addItem(id);
            item.getItemProperty(PROPERTY_ITEM_COMPONENT_CAPTION).setValue(caption);
            item.getItemProperty(PROPERTY_ITEM_COMPONENT_SOURCE).setValue(imageSource);
            item.getItemProperty(PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED).setValue(imageSourceSelected);
            item.getItemProperty(PROPERTY_TARGET_NAVIGATION_STATE).setValue(targetNavigationState);
            item.getItemProperty(PROPERTY_ITEM_HIDDEN_IF_EMPTY).setValue(hiddenIfEmpty);
            hc.setParent(id, parentItemId);
            return item;
        }
        
        public ItemBuilder withCaption(String id) {
            this.caption = caption;
            return this;
        }
        
        public ItemBuilder withImageSource(String imageSource) {
            this.imageSource = imageSource;
            return this;
        }
        
        public ItemBuilder withImageSourceSelected(String imageSourceSelected) {
            this.imageSourceSelected = imageSourceSelected;
            return this;
        }
        
        public ItemBuilder withTargetNavigationState(String targetNavigationState) {
            this.targetNavigationState = targetNavigationState;
            return this;
        }
        
        public ItemBuilder withHiddenIfEmpty() {
            this.hiddenIfEmpty = true;
            return this;
        }
        
        public ItemBuilder withChildren(SynergyBuilder childrenBuilder) {
            this.childrenBuilder = childrenBuilder;
            return this;
        }
        
        public ItemBuilder withChildren(ItemBuilder... childBuilders) {
            SynergyBuilder myChildrenBuilder = new SynergyBuilder();
            for (ItemBuilder childBuilder : childBuilders) {
                myChildrenBuilder.addItem(childBuilder);
            }
            return withChildren(myChildrenBuilder);
        }
    }
    
    public static class ButtonItemBuilder extends ItemBuilder {
        public ButtonItemBuilder(String id, String caption, String imageSource, String targetNavigationState, boolean hiddenIfEmpty, SynergyBuilder childrenBuilder) {
            super(id, caption, imageSource, null, targetNavigationState, hiddenIfEmpty, childrenBuilder);
        }
        
        public ButtonItemBuilder(String id) {
            super(id);
        }
        
        protected Item build(HierarchicalContainer hc, String parentItemId) {
            return super.build(hc, parentItemId);
        }
    }

    public SynergyBuilder addItem(ItemBuilder itemBuilder)
    {
        itemBuilders.add(itemBuilder);
        return this;
    }

    public ItemBuilder button(String id)
    {
        return new ButtonItemBuilder(id);
    }
    
}
