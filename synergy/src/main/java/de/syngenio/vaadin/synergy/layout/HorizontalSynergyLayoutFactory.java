package de.syngenio.vaadin.synergy.layout;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.SynergyView.ItemComponent;

public class HorizontalSynergyLayoutFactory extends AbstractSynergyLayoutFactory
{
    private static final String HORIZONTAL = "horizontal";

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
        SynergyLayout layout = new SynergyLayout() {

            @Override
            protected AbstractOrderedLayout createLayout()
            {
                HorizontalLayout hlayout = new HorizontalLayout();
                hlayout.setSpacing(false);
                hlayout.setMargin(false);
                hlayout.setWidth("100%");
//                hlayout.setVisible(false);
                return hlayout;
            }

            @Override
            public void addItemComponent(Component itemComponent)
            {
                if (getPacking() == Packing.EXPAND) {
                    itemComponent.setWidth("100%");
                } else {
                    itemComponent.setWidthUndefined();
                }
                itemComponent.setHeight("100%");
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
                return HorizontalSynergyLayoutFactory.this.getPacking();
            }

            @Override
            protected void layoutSingularComponent(Component itemComponent)
            {
                itemComponent.setWidthUndefined();
                setExpandRatio(itemComponent, 0);
                setComponentAlignment(itemComponent, Alignment.BOTTOM_CENTER);
                switch (getPacking()) {
                case EXPAND:
                    itemComponent.setWidth("100%");
                    break;
                default:
                    // do nothing
                    break;
                }
            }

            @Override
            protected void layoutFirstComponent(Component itemComponent)
            {
                itemComponent.setWidthUndefined();
                setExpandRatio(itemComponent, 0);
                setComponentAlignment(itemComponent, Alignment.BOTTOM_CENTER);
                switch (getPacking()) {
                case EXPAND:
                    itemComponent.setWidth("100%");
                    break;
                case SPACE_BEFORE:
                case SPACE_AROUND:
                    setExpandRatio(itemComponent, 1);
                    setComponentAlignment(itemComponent, Alignment.BOTTOM_RIGHT);
                    break;
                default:
                    // do nothing
                    break;
                }
            }

            @Override
            protected void layoutIntermediateComponent(Component itemComponent)
            {
                itemComponent.setWidthUndefined();
                setExpandRatio(itemComponent, 0);
                setComponentAlignment(itemComponent, Alignment.BOTTOM_CENTER);
                switch (getPacking()) {
                case EXPAND:
                    itemComponent.setWidth("100%");
                    break;
                default:
                    // do nothing
                    break;
                }
            }

            @Override
            protected void layoutLastComponent(Component itemComponent)
            {
                itemComponent.setWidthUndefined();
                setExpandRatio(itemComponent, 0);
                setComponentAlignment(itemComponent, Alignment.BOTTOM_CENTER);
                switch (getPacking()) {
                case EXPAND:
                    itemComponent.setWidth("100%");
                    break;
                case SPACE_AROUND:
                case SPACE_AFTER:
                    setExpandRatio(itemComponent, 1);
                    setComponentAlignment(itemComponent, Alignment.BOTTOM_LEFT);
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
    public String getOrientationStyleName()
    {
        return HORIZONTAL;
    }
}
