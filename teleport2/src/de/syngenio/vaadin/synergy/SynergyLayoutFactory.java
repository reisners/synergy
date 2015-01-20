package de.syngenio.vaadin.synergy;

public interface SynergyLayoutFactory
{
    SynergyLayout generateLayout();
    
    SynergyLayoutFactory getSubitemLayoutFactory();
}
