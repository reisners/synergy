package de.syngenio.vaadin.synergy;

import java.net.URI;

import com.vaadin.data.util.HierarchicalContainer;

public class HierarchicalContainerHelper
{
    public static final URI PROPERTY_TARGET_NAVIGATION_STATE = URI.create("http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties/navigationState");

    public static HierarchicalContainer createHierarchicalContainer() {
        final HierarchicalContainer hierarchicalContainer = new HierarchicalContainer();
        hierarchicalContainer.addContainerProperty(PROPERTY_TARGET_NAVIGATION_STATE, String.class, null);
        return hierarchicalContainer;
    }
}
