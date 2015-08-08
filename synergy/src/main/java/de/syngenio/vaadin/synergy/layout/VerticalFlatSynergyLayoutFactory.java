package de.syngenio.vaadin.synergy.layout;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView;

public class VerticalFlatSynergyLayoutFactory extends AbstractSynergyLayoutFactory
{
    private static final String VERTICAL = "vertical";
    
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
        SynergyLayout layout = new SynergyLayout() {

            @Override
            protected AbstractOrderedLayout createLayout()
            {
                VerticalLayout vlayout = new VerticalLayout();
                vlayout.setSpacing(false);
                vlayout.setMargin(false);
                vlayout.setWidth("100%");
                return vlayout;
            }

            @Override
            public void addItemComponent(Component itemComponent)
            {
                itemComponent.setWidth("100%");
                if (getPacking() == Packing.EXPAND) {
                    itemComponent.setHeight("100%");
                } else {
                    itemComponent.setHeightUndefined();
                }
                itemComponent.addStyleName(getOrientationStyleName());
                addComponent(itemComponent);
            }

            @Override
            public void addSubview(SynergyView subview, int index)
            {
                // do nothing
            }

            @Override
            public Packing getPacking()
            {
                return VerticalFlatSynergyLayoutFactory.this.getPacking();
            }

            @Override
            protected void layoutSingularComponent(Component component)
            {
                component.setHeightUndefined();
                setExpandRatio(component, 0);
                setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
                switch (getPacking()) {
                case EXPAND:
                    component.setHeight("100%");
                    break;
                default:
                    // do nothing
                    break;
                }
            }

            @Override
            protected void layoutFirstComponent(Component component)
            {
                component.setHeightUndefined();
                setExpandRatio(component, 0);
                setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
                switch (getPacking()) {
                case EXPAND:
                    component.setHeight("100%");
                    break;
                case SPACE_BEFORE:
                    setExpandRatio(component, 1);
                    setComponentAlignment(component, Alignment.BOTTOM_CENTER);
                    break;
                default:
                    // do nothing
                    break;
                }
            }

            @Override
            protected void layoutIntermediateComponent(Component component)
            {
                component.setHeightUndefined();
                setExpandRatio(component, 0);
                setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
                switch (getPacking()) {
                case EXPAND:
                    component.setHeight("100%");
                    break;
                default:
                    // do nothing
                    break;
                }
            }

            @Override
            protected void layoutLastComponent(Component component)
            {
                component.setHeightUndefined();
                setExpandRatio(component, 0);
                setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
                switch (getPacking()) {
                case EXPAND:
                    component.setHeight("100%");
                    break;
                case SPACE_AFTER:
                    setExpandRatio(component, 1);
                    setComponentAlignment(component, Alignment.TOP_RIGHT);
                    break;
                default:
                    // do nothing
                    break;
                }
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

    @Override
    public String getOrientationStyleName()
    {
        return VERTICAL;
    }

}
