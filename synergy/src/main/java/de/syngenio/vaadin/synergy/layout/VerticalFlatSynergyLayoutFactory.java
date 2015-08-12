package de.syngenio.vaadin.synergy.layout;

import de.syngenio.vaadin.synergy.SynergyView;

public class VerticalFlatSynergyLayoutFactory extends AbstractSynergyLayoutFactory
{
    public VerticalFlatSynergyLayoutFactory() {
        super();
    }
    
    /**
     * @param packing 
     */
    public VerticalFlatSynergyLayoutFactory(Packing packing)
    {
        super(packing);
    }
    
    @Override
    public SynergyLayout generateLayout()
    {
        SynergyLayout layout = new VerticalSynergyLayout(getPacking()) {
            @Override
            public void addSubview(SynergyView subview, int index)
            {
                // do nothing
            }
        };

        layout.setSizeFull();
        
        return layout;
    }

    @Override
    public SynergyLayoutFactory getSubitemLayoutFactory()
    {
        return this;
    }
}
