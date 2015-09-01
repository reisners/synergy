package de.syngenio.vaadin.synergy.builder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.SynergyView.ItemComponentButton;

public class SynergyBuilder
{
    private Map<URI, Object> spec = null; // root does not have spec
    private List<ItemBuilder> itemBuilders = new ArrayList<ItemBuilder>();
    
    private String parentItemId = null;
    private HierarchicalContainer container = null;
    
    /**
     * Constructs a {@code SynergyBuilder}.
     * Its methods {@code #addItem(ItemBuilder)}, {@code #item()} (or {@code group()}) to specify items.
     * <p/>
     * Finally, call its method {@code #build()} to create and populate a new {@code HierarchicalContainer}.
     * <p/>
     * A convenient idiom employs the double-brace syntax:
     * <pre>
     * HierarchicalContainer hc = new SynergyBuilder() {{
     *      addItem(item().withCaption("A"));
     *      addItem(item().withCaption("B"));
     *      addItem(item().withCaption("C"));
     * }}.build();
     * </pre>
     */
    public SynergyBuilder() {
    }
    
    /**
     * Constructs a {@code SynergyBuilder} to add subitems to an existing item of an existing {@code HierarchicalContainer}.
     * <p/>
     * The double-brace syntax can be used as with the default constructor:
     * <pre>
     * HierarchicalContainer hc;
     * new SynergyBuilder(hc, "idOfA") {{
     *      addItem(item().withCaption("A.1"));
     *      addItem(item().withCaption("A.2"));
     * }}.build();
     * </pre>
     * @param container the existing {@code HierarchicalContainer}
     * @param parentItemId id of the item to add subitems to
     */
    public SynergyBuilder(HierarchicalContainer container, String parentItemId) {
        this.container  = container;
        this.parentItemId = parentItemId;
    }
    
    private static final String URI_PREFIX = "http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties";
    
    public static final URI PROPERTY_TARGET_NAVIGATION_STATE = URI.create(URI_PREFIX+"/targetNavigationState");
    /**
     * Fully qualified class name of an implementation of {@code ItemComponent} (optional, defaults to {@code ItemComponentButton}) 
     */
    public static final URI PROPERTY_ITEM_COMPONENT_CLASS = URI.create(URI_PREFIX+"/itemComponentClass");
    /**
     * Item caption text (optional)
     */
    public static final URI PROPERTY_ITEM_CAPTION = URI.create(URI_PREFIX+"/itemCaption");
    /**
     * Resource for image or button icon (optional).
     * May be either a {@code FontIcon} (glyph), a {@code ThemeResource}, or a {@code ExternalResource}
     * @see {@code SynergyBuilder#PROPERTY_ITEM_COMPONENT_ICON_SELECTED}
     */
    public static final URI PROPERTY_ITEM_ICON = URI.create(URI_PREFIX+"/itemIcon");
    /**
     * Resource URI of selected image or button icon, respectively (optional)
     * Optional; if provided, replaces {@code SynergyBuilder#PROPERTY_ITEM_ICON} in selected states
     */
    public static final URI PROPERTY_ITEM_ICON_SELECTED = URI.create(URI_PREFIX+"/itemIconSelected");
    /**
     * Image width (optional; only relevant for {@code ItemComponentImage})
     */
    public static final URI PROPERTY_ITEM_IMAGE_WIDTH = URI.create(URI_PREFIX+"/itemImageWidth");
    /**
     * Image height (optional; only relevant for {@code ItemComponentImage})
     */
    public static final URI PROPERTY_ITEM_IMAGE_HEIGHT = URI.create(URI_PREFIX+"/itemImageHeight");
    /**
     * font-size for FontIcon resource (optional; only relevant for {@code ItemComponentImage})
     */
    public static final URI PROPERTY_ITEM_GLYPH_SIZE = URI.create(URI_PREFIX+"/itemGlyphSize");
    /**
     * Can be used to inform a filter how to deal with items not having children (optional)  
     */
    public static final URI PROPERTY_ITEM_HIDDEN_IF_EMPTY = URI.create(URI_PREFIX+"/itemHiddenIfEmpty");
    /**
     * Style name to be set on this item's subview (optional)  
     */
    public static final URI PROPERTY_ITEM_SUBVIEW_STYLE = URI.create(URI_PREFIX+"/subviewStyle");
    /**
     * Action ({@code BiConsumer<com.vaadin.data.Item, com.vaadin.ui.UI>}) to perform when this item is selected (optional)
     */
    public static final URI PROPERTY_ITEM_ACTION = URI.create(URI_PREFIX+"/action");

    /**
     * Creates a new {@code HierarchicalContainer} with synergy-specific container properties:
     * @return
     */
    public static HierarchicalContainer createHierarchicalContainer() {
        final HierarchicalContainer hierarchicalContainer = new HierarchicalContainer();
        hierarchicalContainer.addContainerProperty(PROPERTY_TARGET_NAVIGATION_STATE, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_CLASS, Class.class, ItemComponentButton.class);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_CAPTION, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_ICON, Resource.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_ICON_SELECTED, Resource.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_IMAGE_WIDTH, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_IMAGE_HEIGHT, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_GLYPH_SIZE, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_HIDDEN_IF_EMPTY, Boolean.class, Boolean.FALSE);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_SUBVIEW_STYLE, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_ACTION, BiConsumer.class, null);
        return hierarchicalContainer;
    }

    /**
     * Populates the {@code HierarchicalContainer}, after creating it if necessary.
     * @return the populated {@code HierarchicalContainer}
     */
    public HierarchicalContainer build() {
        if (container == null) {
            container = createHierarchicalContainer();
            parentItemId = null;
        }
        buildItems(container, parentItemId);
        return container;
    }
    
    private void buildItems(HierarchicalContainer hc, String parentItemId) {
        for (ItemBuilder itemBuilder : itemBuilders) {
            itemBuilder.build(hc, parentItemId);
        }
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
     * @param navigationHierarchy navigation hierarchy
     * @param itemId id of item in navigationHierarchy
     * @param parentItemId id of another item "parent" in navigationHierarchy or null
     * @return true if item is a child of parent, or if parentItemId==null and item is a root item. Returns always false if navigationHierarchy is not a {@code HierarchicalContainer} 
     */
    public static boolean isChildOf(Container navigationHierarchy, String itemId, final String parentItemId)
    {
        if (!(navigationHierarchy instanceof HierarchicalContainer)) {
            return false;
        }
        HierarchicalContainer hc = (HierarchicalContainer) navigationHierarchy;
        String itemParentId = (String) hc.getParent(itemId);
        if (parentItemId == null) {
            return itemParentId == null;
        } else {
            return parentItemId.equals(itemParentId);
        }
    }

    public static class ItemBuilder {
        public enum Mode { inline, stacked }
        
        private final String id;
        private Class<? extends Component> componentClass = null;
        private String caption = null;
        private String targetNavigationState = null;
        private boolean hiddenIfEmpty = false;
        private SynergyBuilder childrenBuilder = null;
        private String imageWidth = null;
        private String imageHeight = null;
        private Resource icon = null;
        private Resource iconSelected = null;
        private Mode mode = null;
        private String glyphSize = null;
        private String subviewStyle;

        /**
         * Create a builder for an item with given id.
         * Will set the item's targetNavigationState to the id.
         * If the id looks hierarchical (starts with a non-alphanumeric character, e.g. |Root|Level|...|Level|Item),
         * will set the item's caption to the last element of the hierarchical id as a default.
         * @param id of the item to be created
         * @param componentClass subclass of {@code com.vaadin.ui.Component} to visualize the item 
         */
        public ItemBuilder(String id, Class<? extends Component> componentClass) {
            this.id = id;
            this.componentClass = componentClass;
            setDefaultCaption();
        }

        /**
         * @param id
         * @return
         */
        protected void setDefaultCaption()
        {
            final char first = id.charAt(0);
            if (!Character.isAlphabetic(first) && !Character.isDigit(first)) {
                this.caption = id.replaceAll("^.*\\"+first+"(?=[^"+first+"]+)", "");
            }
        }
        
        @SuppressWarnings("unchecked")
        private void build(HierarchicalContainer hc, String parentItemId) {
            Item item = hc.addItem(id);
            if (parentItemId != null) {
                hc.setParent(id, parentItemId);
            }
            inferMode();
            inferComponentClass();
            item.getItemProperty(PROPERTY_ITEM_COMPONENT_CLASS).setValue(componentClass);
            item.getItemProperty(PROPERTY_ITEM_CAPTION).setValue(caption);
            item.getItemProperty(PROPERTY_ITEM_ICON).setValue(icon);
            item.getItemProperty(PROPERTY_ITEM_ICON_SELECTED).setValue(iconSelected);
            item.getItemProperty(PROPERTY_TARGET_NAVIGATION_STATE).setValue(targetNavigationState);
            item.getItemProperty(PROPERTY_ITEM_HIDDEN_IF_EMPTY).setValue(hiddenIfEmpty);
            item.getItemProperty(PROPERTY_ITEM_IMAGE_WIDTH).setValue(imageWidth);
            item.getItemProperty(PROPERTY_ITEM_IMAGE_HEIGHT).setValue(imageHeight);
            item.getItemProperty(PROPERTY_ITEM_GLYPH_SIZE).setValue(glyphSize);
            item.getItemProperty(PROPERTY_ITEM_SUBVIEW_STYLE).setValue(subviewStyle);
            if (childrenBuilder != null) {
                childrenBuilder.buildItems(hc, id);
            }
        }
        
        /**
         * If mode has not been set yet, try to guess it from what the user specified
         */
        private void inferMode() {
            if (mode == null) {
                if (imageWidth != null || imageHeight != null) {
                    mode = Mode.stacked;
                } else {
                    mode = Mode.inline;
                }
            }
        }
        
        /**
         * If componentClass has not been set yet, try to guess it from what the user specified
         */
        private void inferComponentClass()
        {
            if (componentClass == null) {
                if (Mode.stacked == mode && icon != null) {
                    componentClass = SynergyView.ItemComponentImage.class;
                } else {
                    componentClass = SynergyView.ItemComponentButton.class;
                }
            }
        }

        /**
         * Set the ItemBuilder's caption
         * @param caption
         * @return the ItemBuilder
         */
        public ItemBuilder withCaption(String caption) {
            this.caption = caption;
            return this;
        }

        /**
         * Set the ItemBuilder's subviewStyle
         * @param subviewStyle
         * @return the ItemBuilder
         */
        public ItemBuilder withSubviewStyle(String subviewStyle) {
            this.subviewStyle = subviewStyle;
            return this;
        }
        
        /**
         * Set the ItemBuilder's mode, which determines the relative placement of icon and caption
         * @param mode
         * @return the ItemBuilder
         */
        public ItemBuilder withMode(Mode mode) {
            this.mode = mode;
            return this;
        }
        
        /**
         * Set the ItemBuilder's mode to inline
         * @return the ItemBuilder
         */
        public ItemBuilder inline() {
            this.mode = Mode.inline;
            return this;
        }
        
        /**
         * Set the ItemBuilder's mode to stacked
         * @return the ItemBuilder
         */
        public ItemBuilder stacked() {
            this.mode = Mode.stacked;
            return this;
        }
        
        /**
         * Set the ItemBuilder's icon resource
         * @param icon
         * @return the ItemBuilder
         */
        public ItemBuilder withIcon(Resource icon) {
            this.icon = icon;
            return this;
        }
        
        /**
         * Set the ItemBuilder's icon resource when in selected state
         * @param iconSelected
         * @return the ItemBuilder
         */
        public ItemBuilder withIconSelected(Resource iconSelected) {
            this.iconSelected = iconSelected;
            return this;
        }

        /**
         * Set the ItemBuilder's image width
         * @param imageWidth
         * @return the ItemBuilder
         */
        public ItemBuilder withImageWidth(String imageWidth) {
            this.imageWidth = imageWidth;
            return this;
        }

        /**
         * Set the ItemBuilder's image height
         * @param imageHeight
         * @return the ItemBuilder
         */
        public ItemBuilder withImageHeight(String imageHeight) {
            this.imageHeight = imageHeight;
            return this;
        }

        /**
         * Set the ItemBuilder's glyph size
         * @param glyphSize
         * @return the ItemBuilder
         */
        public ItemBuilder withGlyphSize(String glyphSize) {
            this.glyphSize = glyphSize;
            return this;
        }
        
        /**
         * Sets the ItemBuilder's target navigation state
         * @param targetNavigationState
         * @return the ItemBuilder
         */
        public ItemBuilder withTargetNavigationState(String targetNavigationState) {
            this.targetNavigationState = targetNavigationState;
            return this;
        }
        
        /**
         * Marks the item to be created as a grouping item that 
         * does not have a target navigation state of its own
         * and that is only visible as long as it has visible child items
         * @return the ItemBuilder
         */
        public ItemBuilder asGroup() {
            this.hiddenIfEmpty = true;
            this.targetNavigationState = null;
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
    
    public SynergyBuilder addItem(ItemBuilder itemBuilder)
    {
        itemBuilders.add(itemBuilder);
        return this;
    }

    /**
     * Creates a new {@code ItemBuilder} with a given id.
     * @param id given id
     * @return the {@code ItemBuilder}
     */
    public ItemBuilder item(String id)
    {
        return new ItemBuilder(id, null);
    }

    /**
     * Creates a new {@code ItemBuilder} with a generated unique id
     * @return the {@code ItemBuilder}
     */
    public ItemBuilder item()
    {
        return item(UUID.randomUUID().toString());
    }

    /**
     * Creates a new {@code ItemBuilder} with a given id, configured as a group builder.
     * Yields the same result as {@code item(id).asGroup()}. 
     * @param id user-provided id
     * @return the {@code ItemBuilder}
     */
    public ItemBuilder group(String id)
    {
        return item(id).asGroup();
    }

    /**
     * Creates a new {@code ItemBuilder} with a generated unique id, configured as a group builder.
     * Yields the same result as {@code item().asGroup()}. 
     * @return the {@code ItemBuilder}
     */
    public ItemBuilder group()
    {
        return item().asGroup();
    }
}
