package de.syngenio.vaadin.synergy.layout;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;

public abstract class VerticalSynergyLayout extends SynergyLayout
{
    private static final String VERTICAL = "vertical";
    
    protected VerticalSynergyLayout(Packing packing) {
        super(packing);
    }
    
    @Override
    public String getOrientationStyleName()
    {
        return VERTICAL;
    }

    @Override
    protected AbstractOrderedLayout createLayout()
    {
        VerticalLayout vlayout = new VerticalLayout();
        vlayout.setSpacing(true);
        vlayout.setMargin(false);
        vlayout.setWidth("100%");
        vlayout.setHeight("100%");
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
        super.addItemComponent(itemComponent);
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
        case SPACE_AROUND:
        case SPACE_BEFORE:
            setExpandRatio(component, 1);
            setComponentAlignment(component, Alignment.BOTTOM_RIGHT);
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
        case SPACE_AROUND:
            setExpandRatio(component, 1);
            setComponentAlignment(component, Alignment.TOP_RIGHT);
            break;
        default:
            // do nothing
            break;
        }
    }
}
