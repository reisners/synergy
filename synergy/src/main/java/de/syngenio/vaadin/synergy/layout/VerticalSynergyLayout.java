package de.syngenio.vaadin.synergy.layout;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView.ItemComponent;
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

    private void layoutComponent(Component component, String height, float expandRatio, Alignment alignment) {
        if (height != null) {
            component.setHeight(height);
        } else {
            component.setHeightUndefined();
        }
        setExpandRatio(component, expandRatio);
        setComponentAlignment(component, alignment);
    }
    
    @Override
    protected void layoutSingularComponent(Component component)
    {
//        component.setHeightUndefined();
//        setExpandRatio(component, 0);
        switch (getPacking()) {
        case EXPAND:
            layoutComponent(component, "100%", 0, Alignment.MIDDLE_RIGHT);
//            component.setHeight("100%");
//            setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
            break;
        case SPACE_AFTER:
            layoutComponent(component, null, 0, Alignment.TOP_RIGHT);
//            setComponentAlignment(component, Alignment.TOP_RIGHT);
            break;
        case SPACE_BEFORE:
            layoutComponent(component, null, 0, Alignment.BOTTOM_RIGHT);
//            setComponentAlignment(component, Alignment.BOTTOM_RIGHT);
            break;
        case SPACE_AROUND:
            layoutComponent(component, null, 0, Alignment.MIDDLE_RIGHT);
//            setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
            break;
        }
    }

    @Override
    protected void layoutFirstComponent(Component component)
    {
//        component.setHeightUndefined();
//        setExpandRatio(component, 0);
//        setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
        switch (getPacking()) {
        case EXPAND:
            layoutComponent(component, "100%", 0, Alignment.MIDDLE_RIGHT);
//            component.setHeight("100%");
            break;
        case SPACE_AROUND:
        case SPACE_BEFORE:
            layoutComponent(component, null, 1, Alignment.BOTTOM_RIGHT);
//            setExpandRatio(component, 1);
//            setComponentAlignment(component, Alignment.BOTTOM_RIGHT);
            break;
        case SPACE_AFTER:
            layoutComponent(component, null, 0, Alignment.MIDDLE_RIGHT);
            break;
        }
    }

    @Override
    protected void layoutIntermediateComponent(Component component)
    {
//        component.setHeightUndefined();
//        setExpandRatio(component, 0);
//        setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
        switch (getPacking()) {
        case EXPAND:
            layoutComponent(component, "100%", 0, Alignment.MIDDLE_RIGHT);
//            component.setHeight("100%");
            break;
        default:
            layoutComponent(component, null, 0, Alignment.MIDDLE_RIGHT);
            break;
        }
    }

    @Override
    protected void layoutLastComponent(Component component)
    {
//        component.setHeightUndefined();
//        setExpandRatio(component, 0);
//        setComponentAlignment(component, Alignment.MIDDLE_RIGHT);
        switch (getPacking()) {
        case EXPAND:
            if (component instanceof ItemComponent) {
                // item
                layoutComponent(component, "100%", 0, Alignment.MIDDLE_RIGHT);
            } else {
                // wrapper
                layoutComponent(component, null, 0, Alignment.MIDDLE_RIGHT);
            }
//            component.setHeight("100%");
            break;
        case SPACE_AFTER:
        case SPACE_AROUND:
            layoutComponent(component, null, 1, Alignment.TOP_RIGHT);
//            setExpandRatio(component, 1);
//            setComponentAlignment(component, Alignment.TOP_RIGHT);
            break;
        case SPACE_BEFORE:
            layoutComponent(component, null, 0, Alignment.MIDDLE_RIGHT);
            break;
        }
    }
}
