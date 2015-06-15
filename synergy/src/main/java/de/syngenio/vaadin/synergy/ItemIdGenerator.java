package de.syngenio.vaadin.synergy;

public interface ItemIdGenerator
{
    String generateId(String parentItemId, String caption, String imageSource, String targetNavigationState);
}
