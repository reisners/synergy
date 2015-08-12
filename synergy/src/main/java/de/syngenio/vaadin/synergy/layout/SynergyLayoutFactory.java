package de.syngenio.vaadin.synergy.layout;

import java.io.Serializable;

public interface SynergyLayoutFactory extends Serializable
{
    /**
     * @return a new instance of SynergyLayout
     */
    SynergyLayout generateLayout();
    
    /**
     * @return the LayoutFactory for the subitems
     */
    SynergyLayoutFactory getSubitemLayoutFactory();
}
