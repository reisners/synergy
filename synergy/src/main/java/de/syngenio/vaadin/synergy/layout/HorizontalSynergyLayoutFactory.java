package de.syngenio.vaadin.synergy.layout;

import de.syngenio.vaadin.synergy.SynergyView;

public class HorizontalSynergyLayoutFactory extends AbstractSynergyLayoutFactory
{
    public HorizontalSynergyLayoutFactory()
    {
        super();
    }

    public HorizontalSynergyLayoutFactory(Packing packing)
    {
        super(packing);
    }

    @Override
    public SynergyLayout generateLayout()
    {
        SynergyLayout layout = new HorizontalSynergyLayout(getPacking()) {
            @Override
            public void addSubview(SynergyView subview, int index)
            {
                // do nothing
            }
        };
        
        layout.setSizeFull();
        
        return layout;
    }
}
