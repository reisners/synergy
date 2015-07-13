package de.syngenio.vaadin.synergy.layout;

import de.syngenio.vaadin.synergy.SynergyView;

public abstract class AbstractSynergyLayoutFactory implements SynergyLayoutFactory
{
    public enum Packing { SPACE_AFTER, SPACE_BEFORE, SPACE_AROUND, EXPAND }
    
    private SynergyLayoutFactory subitemLayoutFactory = this;
    private Packing packing = Packing.SPACE_AFTER;

    protected AbstractSynergyLayoutFactory() {} 

    /**
     * @param packing
     */
    protected AbstractSynergyLayoutFactory(Packing packing)
    {
        this.packing = packing;
    }

    public Packing getPacking()
    {
        return packing;
    }

    public void setPacking(Packing packing)
    {
        this.packing = packing;
    }

    public void setSubitemLayoutFactory(SynergyLayoutFactory subitemLayoutFactory)
    {
        this.subitemLayoutFactory = subitemLayoutFactory;
    }

    @Override
    public SynergyLayoutFactory getSubitemLayoutFactory()
    {
        return subitemLayoutFactory;
    }
}
