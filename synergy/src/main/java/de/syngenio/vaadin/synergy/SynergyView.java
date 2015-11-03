package de.syngenio.vaadin.synergy;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

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
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
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
    private Synergy synergy;
    private SynergyLayout layout;
    
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

    private SynergyView parentView = null;
    private SynergyView subView = null;
    private Map<String, ItemComponent> itemComponents = null;
    private SynergyLayoutFactory layoutFactory;
    private ValueChangeListener viewUpdatingListener;
    private ValueChangeListener selectListener;
    
    private final static Logger log = LoggerFactory.getLogger(SynergyView.class);
    private static final String HAS_CHILDREN = "children";
    
    private Class<? extends ItemComponent> defaultItemComponentClass = ItemComponentButton.class;

    public SynergyView(SynergyLayoutFactory layoutFactory)
    {
        this(layoutFactory, SynergyBuilder.createHierarchicalContainer());
    }

    public SynergyView(SynergyLayoutFactory layoutFactory, Container dataSource)
    {
        this(layoutFactory, (SynergyView)null);
        if (dataSource != null) {
            attachToSynergy(new Synergy(dataSource));
        }
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
    @SuppressWarnings("serial")
    public SynergyView(SynergyLayoutFactory layoutFactory, SynergyView parentView)
    {
        setPrimaryStyleName(DEFAULT_PRIMARY_STYLE_NAME);
        //FIXME: commented code below is ineffective because there can never be a subview style at this stage
//        String subviewStyle = getSubviewStyle();
//        if (subviewStyle != null) {
//            addStyleName(subviewStyle);
//        }
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
        selectListener = new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event)
            {
                String itemId = (String) synergy.getValue();
                // is it an item that this view visualizes?
                if (SynergyBuilder.isChildOf(synergy.getContainerDataSource(), itemId, parentId)) {
                    Item item = synergy.getContainerDataSource().getItem(itemId);
                    UI ui = SynergyView.this.getUI();
                    if (ui != null) {
                        @SuppressWarnings("unchecked")
                        BiConsumer<Item, UI> selectAction = (BiConsumer<Item, UI>) item.getItemProperty(Synergy.PROPERTY_ITEM_ACTION).getValue();
                        if (selectAction == null) {
                            selectAction = defaultSelectAction;
                        }
                        if (selectAction != null) {
                            selectAction.accept(item, ui);
                        }
                    }
                }
            }
        };

        if (this.parentView != null) {
            attachToSynergy(parentView.synergy);
            parentView.setSubView(this);
        }
    }
    
    @SuppressWarnings("unused")
    private String getSubviewStyle()
    {
        if (parentId != null && !parentId.equals(INACTIVE)) {
            return (String) synergy.getContainerDataSource().getContainerProperty(parentId, Synergy.PROPERTY_ITEM_SUBVIEW_STYLE).getValue();
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
     * Called by {@code attach()}, {@code attachToSynergy(Synergy)}, and {@code setParentId(String)}.
     */
    private void visualizeItems()
    {
        if (synergy != null) {
            clear();
            boolean isEmpty = true;
            for (String itemId : getImmediateChildItemIds()) {
                isEmpty = false;
                visualizeItem(itemId);
            }
            setVisible(!isEmpty);
        }
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

    @Override
    public void attach()
    {
        super.attach();
        visualizeItems(); // necessary in case that the container changed while we were detached (TODO why?) 
    }

    private Collection<String> getImmediateChildItemIds() {
        return SynergyBuilder.getChildIdsOf(synergy.getContainerDataSource(), parentId);
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
        final String selectedItemId = (String) synergy.getValue();
        if (itemId.equals(selectedItemId)) {
            itemComponent.setState(State.selected);
            replaceSubView(itemId, State.selected);
        } else if (isAncestorOf(itemId, selectedItemId)) {
            itemComponent.setState(State.ancestorOfSelected);
            replaceSubView(itemId, State.ancestorOfSelected);
        } else {
            itemComponent.setState(State.unselected);
        }
//        JavaScript.getCurrent().execute("vaadin.forceLayout()");
    }

    private void replaceSubView(String itemId, State state) {
        boolean hasChildren = hasChildren(itemId);
        
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
        
        Component itemComponent = itemComponents.get(itemId);
        if (hasChildren) {
            // add it after itemId's component
            int index = layout.getComponentIndex(itemComponent);
            layout.addSubview(subView, index+1);
            subView.setParentState(state);
            if (itemComponent != null) {
                itemComponent.addStyleName(HAS_CHILDREN);
            }
        } else {
            if (itemComponent != null) {
                itemComponent.removeStyleName(HAS_CHILDREN);
            }
        }
    }

    private boolean hasChildren(String itemId)
    {
        boolean hasChildren = false;
        // do we have children? 
        Container container = synergy.getContainerDataSource();
        if (container instanceof HierarchicalContainer) {
            HierarchicalContainer hc = (HierarchicalContainer) container;
            hasChildren = hc.hasChildren(itemId);
        }
        return hasChildren;
    }
    
    private void setParentState(State state)
    {
        state.applyTo(this);
        state.applyTo(layout);
        if (getWrapper() != null && getWrapper() != this) {
            state.applyTo(getWrapper());
        }
    }

    @SuppressWarnings("unused")
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
        return SynergyBuilder.isAncestorOf(synergy.getContainerDataSource(), ancestorId, descendantId); 
    }

    private BiConsumer<Item, UI> defaultSelectAction = new BiConsumer<Item, UI>() { 
        public void accept(Item item, UI ui) {
            String targetNavigationState = (String) item.getItemProperty(Synergy.PROPERTY_TARGET_NAVIGATION_STATE).getValue();
            log.debug("selected item with targetNavigationState="+targetNavigationState);
            if (targetNavigationState != null) {
                Navigator navigator = ui.getNavigator();
                if (navigator != null) {
                    navigator.navigateTo(targetNavigationState);
                }
            }
        }
    };
    
    /**
     * Installs a new default action to perform when an item is selected (normally by the user clicking on it).
     * The initial default action call {@code Navigator#navigateTo(String)} with the value of the item's 
     * {@code SynergyBuilder#PROPERTY_TARGET_NAVIGATION_STATE} property (if set).
     * In case that the selected item has its {@code SynergyBuilder#PROPERTY_ITEM_ACTION} property set, 
     * that action will be performed instead of the defaultSelectAction.
     * @param defaultSelectAction the new default select action
     */
    public void setDefaultSelectAction(BiConsumer<Item, UI> defaultSelectAction)
    {
        this.defaultSelectAction = defaultSelectAction;
    }

    
    /**
     * Sets a class implementing {@code ItemComponent} as default for visualizing items.
     * This default is overridden by an item's {@code SynergyBuilder#PROPERTY_ITEM_COMPONENT_CLASS} property (if set).
     * The initial default is {@code ItemComponentButton}.
     * @param defaultItemComponentClass the new {@code ItemComponent} to use as default
     */
    public void setDefaultItemComponentClass(Class< ? extends ItemComponent> defaultItemComponentClass)
    {
        this.defaultItemComponentClass = defaultItemComponentClass;
    }


    /**
     * Interface implemented by {@code Component}s that visualize items in a {@code SynergyView}
     */
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

        /**
         * Sets up the component to render a specific item
         * @param ss the {@code SynergySelect} (providing the item's properties)
         * @param itemId the item's id
         */
        void setup(final Synergy ss, final String itemId);
        /**
         * Render the item in the given state
         * @param state the item's {@code ItemComponent.State}
         */
        void setState(State state);
    }

    /**
     * Implementation of {@code ItemComponent} that visualizes items as a graphical component
     * stacked on top of a caption.
     * Depending on the type of the source {@code Resource}, the graphical component is either an
     * {@code Image} or a {@code Label}.  
     */
    @SuppressWarnings("serial")
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
        
        public void setup(final Synergy synergy, final String itemId)
        {
            @SuppressWarnings("unchecked")
            Property<Resource> propertyIcon = (Property<Resource>)synergy.getContainerProperty(itemId, Synergy.PROPERTY_ITEM_ICON);
            source = propertyIcon.getValue();
            @SuppressWarnings("unchecked")
            Property<Resource> propertyIconSelected = (Property<Resource>)synergy.getContainerProperty(itemId, Synergy.PROPERTY_ITEM_ICON_SELECTED);
            sourceSelected = propertyIconSelected.getValue();
            
            @SuppressWarnings("unchecked")
            Property<String> propertyCaption = (Property<String>)synergy.getContainerProperty(itemId, Synergy.PROPERTY_ITEM_CAPTION);
            String captionText = propertyCaption.getValue();
            @SuppressWarnings("unchecked")
            Property<String> propertyDescription = (Property<String>)synergy.getContainerProperty(itemId, Synergy.PROPERTY_ITEM_DESCRIPTION);

            layout = new VerticalLayout();
//            layout.setSizeFull();
            layout.setMargin(new MarginInfo(false, true, false, true) );
            //FIXME the following is an ugly hack.
            // The root cause is the behaviour of HorizontalLayout with undefined height (in a horizontal SynergyView) 
            // to set the heights of its children to undefined. We set the height of the synergy-image to 100% via CSS,
            // but it does not seem possible to vertical-align:bottom the wrapped VerticalLayout this way.
            // By adding the CSS class v-align-bottom, however, it works
            layout.addStyleName("v-align-bottom"); 

            
            if (source != null) {
                Alignment imageAlignment = (captionText != null ? Alignment.BOTTOM_CENTER : Alignment.MIDDLE_CENTER);
                if (source instanceof FontIcon) {
                    glyph = new Label("", ContentMode.HTML);
                    glyph.setSizeUndefined();
                    layout.addComponent(glyph);
                    layout.setComponentAlignment(glyph, imageAlignment);
                    layout.setExpandRatio(glyph, 1);
                    @SuppressWarnings("unchecked")
                    Property<String> propertySize = (Property<String>)synergy.getContainerProperty(itemId, Synergy.PROPERTY_ITEM_GLYPH_SIZE);
                    glyphSize  = propertySize.getValue();
                } else {
                    image = new Image();
                    image.setSizeUndefined();
                    layout.addComponent(image);
                    layout.setComponentAlignment(image, imageAlignment);
                    layout.setExpandRatio(image, 1);
                }
            }
            
            final LayoutClickListener clickListener = new LayoutClickListener() {
                @Override
                public void layoutClick(LayoutClickEvent event)
                {
                    Object selectedItemId = synergy.getValue();
                    if (!itemId.equals(selectedItemId)) {
                        synergy.select(itemId);
                    }
                }
            };
            
            setSource(source);
            
            if (captionText != null) {
                Alignment captionAlignment = (source != null ? Alignment.TOP_CENTER : Alignment.MIDDLE_CENTER);
                captionLabel = new Label(synergy.i18n(captionText));
                captionLabel.setSizeUndefined();
                layout.addComponent(captionLabel);
                layout.setComponentAlignment(captionLabel, captionAlignment);
                layout.setExpandRatio(captionLabel, 0);
            }
            String description = propertyDescription.getValue();
            if (description != null) {
                setDescription(synergy.i18n(description));
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
    
    /**
     * Implementation of {@code ItemComponent} that visualizes items as a button with optional
     * caption and icon
     */
    @SuppressWarnings("serial")
    public static class ItemComponentButton extends Button implements ItemComponent {
        private static final String PRIMARY_STYLE_NAME = "synergy-button";

        public ItemComponentButton() {
            super();
            setPrimaryStyleName(PRIMARY_STYLE_NAME);
        }
        
        public void setup(final Synergy synergy, final String itemId)
        {
            @SuppressWarnings("unchecked")
            Property<String> propertyCaption = (Property<String>)synergy.getContainerProperty(itemId, Synergy.PROPERTY_ITEM_CAPTION);
            String caption = propertyCaption.getValue();
            if (caption != null) {
                setCaption(synergy.i18n(caption));
            }
            setImmediate(true);
//            setSizeUndefined();
            @SuppressWarnings("unchecked")
            Property<Resource> propertyIcon = (Property<Resource>)synergy.getContainerProperty(itemId, Synergy.PROPERTY_ITEM_ICON);
            Resource iconResource = propertyIcon.getValue();
            if (iconResource != null) {
                setIcon(iconResource);
            }
            if (caption == null && iconResource == null) {
                setCaption(itemId);
            }
            @SuppressWarnings("unchecked")
            Property<String> propertyToolTip = (Property<String>)synergy.getContainerProperty(itemId, Synergy.PROPERTY_ITEM_DESCRIPTION);
            String toolTip = propertyToolTip.getValue();
            if (toolTip != null) {
                setDescription(synergy.i18n(toolTip));
            }
            addClickListener(new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event)
                {
                    Object selectedItemId = synergy.getValue();
                    if (!itemId.equals(selectedItemId)) {
                        synergy.select(itemId);
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
        Item item = synergy.getItem(itemId);
        @SuppressWarnings("unchecked")
        final Property<Class<? extends ItemComponent>> property = (Property<Class<? extends ItemComponent>>)item.getItemProperty(Synergy.PROPERTY_ITEM_COMPONENT_CLASS);
        Class< ? extends ItemComponent> itemComponentClass = property.getValue();
        if (itemComponentClass == null) {
            itemComponentClass = defaultItemComponentClass;
        }
        try
        {
            Constructor<? extends ItemComponent> defcon = itemComponentClass.getConstructor();
            itemComponent = (ItemComponent) defcon.newInstance();
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
        itemComponent.setup(synergy, itemId);
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

    /**
     * Attaches this SynergyView to the given Synergy instance.
     * Will immediately render the data contained in the Synergy. 
     * @param synergy the Synergy instance to attach to
     */
    @SuppressWarnings("serial")
    public void attachToSynergy(Synergy synergy)
    {
        this.synergy = synergy;
        // add the ValueChangeListener to handle view updates
        this.synergy.addValueChangeListener(viewUpdatingListener);
        // add the ValueChangeListener to handle navigation
        this.synergy.addValueChangeListener(selectListener);
        
        this.synergy.addItemSetChangeListener(new ItemSetChangeListener() {
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
     * Removes listeners from the {@link Synergy}. 
     * Call this method before disposing the {@code SynergyView} to avoid memory leaks.
     */
    public void detachFromSelect() {
        this.synergy.removeValueChangeListener(selectListener);
        this.synergy.removeValueChangeListener(viewUpdatingListener);
    }
    
    /**
     * @return this {@code SynergyView}'s item container 
     */
    public Container getContainer() {
        return synergy.getContainerDataSource();
    }

    private BiConsumer<SynergyView, ViewChangeEvent> syncer = new BiConsumer<SynergyView, ViewChangeEvent>() {
        @Override
        public void accept(SynergyView synergyView, ViewChangeEvent event)
        {
            synergyView.defaultSyncer(event);
        }
    };
    
    private void defaultSyncer(ViewChangeEvent event) {
        String targetNavigationState = extractTargetNavigationState(event);
        final Container container = synergy.getContainerDataSource();
        for (Object itemId : container.getItemIds()) {
            @SuppressWarnings("unchecked")
            Property<String> propertyTargetNavigationState = (Property<String>)container.getContainerProperty(itemId, Synergy.PROPERTY_TARGET_NAVIGATION_STATE);
            if (propertyTargetNavigationState != null && targetNavigationState.equals(propertyTargetNavigationState.getValue())) {
                synergy.select(itemId);
                return;
            }
        }
    };
    
    private String extractTargetNavigationState(ViewChangeEvent event)
    {
        return event.getViewName() + "/" + event.getParameters();
    }
    
    /**
     * Replaces the syncer.
     * @see #syncWith(ViewChangeEvent)
     * @param syncer
     */
    public void setSyncer(BiConsumer<SynergyView, ViewChangeEvent> syncer)
    {
        this.syncer = syncer;
    }

    /**
     * Passes a {@link ViewChangeEvent} to the view's syncer.
     * The default syncer will look for an item in the view's container with
     * matching targetNavigationState and select it. 
     * The default syncer can be replaced using {@link #setSyncer(Syncer)}
     * @param event
     */
    public void syncWith(ViewChangeEvent event)
    {
        if (syncer != null) {
            syncer.accept(this, event);
        }
    }
}
