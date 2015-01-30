package de.syngenio.vaadin.synergy;

import java.net.URI;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

public class SynergyBuilder
{
    private HierarchicalContainer hierarchicalContainer;
    private String contextItemId;
    
    public SynergyBuilder() {
        this(createHierarchicalContainer());
    }
    
    public SynergyBuilder(HierarchicalContainer hc) {
        this.hierarchicalContainer = hc;
    }
    
    public SynergyBuilder(HierarchicalContainer hc, String contextItemId) {
        this(hc);
        this.hierarchicalContainer = hc;
        this.contextItemId = contextItemId;
    }
    
    public static final URI PROPERTY_TARGET_NAVIGATION_STATE = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/navigationState");
    public static final URI PROPERTY_ITEM_COMPONENT_CLASS = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentClass");
    public static final URI PROPERTY_ITEM_COMPONENT_CAPTION = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentCaption");
    public static final URI PROPERTY_ITEM_COMPONENT_SOURCE = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentSource");
    public static final URI PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentSourceSelected");
    public static final URI PROPERTY_ITEM_COMPONENT_WIDTH = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentWidth");
    public static final URI PROPERTY_ITEM_COMPONENT_HEIGHT = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/itemComponentHeight");

    public static HierarchicalContainer createHierarchicalContainer() {
        final HierarchicalContainer hierarchicalContainer = new HierarchicalContainer();
        hierarchicalContainer.addContainerProperty(PROPERTY_TARGET_NAVIGATION_STATE, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_CLASS, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_CAPTION, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_SOURCE, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_WIDTH, String.class, null);
        hierarchicalContainer.addContainerProperty(PROPERTY_ITEM_COMPONENT_HEIGHT, String.class, null);
        return hierarchicalContainer;
    }
    
    public HierarchicalContainer build() {
        return hierarchicalContainer;
    }
    
    public Item createItem(String itemId)
    {
        Item hiPersonSuchen = hierarchicalContainer.addItem(itemId);
        hiPersonSuchen.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view2");
        hiPersonSuchen.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CAPTION).setValue("Suchen");
        hierarchicalContainer.setParent("Person.Suchen", "Person");
        return hiPersonSuchen;
    }
}
