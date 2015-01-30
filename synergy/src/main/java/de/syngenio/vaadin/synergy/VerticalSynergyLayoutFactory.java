package de.syngenio.vaadin.synergy;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class VerticalSynergyLayoutFactory extends AbstractSynergyLayoutFactory
{
    @Override
    public SynergyLayout generateLayout()
    {
        SynergyLayout layout = new SynergyLayout() {

            @Override
            protected AbstractOrderedLayout createLayout()
            {
                VerticalLayout vlayout = new VerticalLayout();
                vlayout.setStyleName(generateStyleName());
                vlayout.setSpacing(true);
                vlayout.setMargin(true);
                vlayout.setWidth("100%");
                return vlayout;
            }

            @Override
            protected void addItemComponent(Component itemComponent)
            {
                itemComponent.setWidth("100%");
                addComponent(itemComponent);
                setComponentAlignment(itemComponent, Alignment.TOP_RIGHT);
            }

            @Override
            protected void addSubview(SynergyView subview, int index)
            {
                subview.setWidth(80, Unit.PERCENTAGE);
                subview.setHeightUndefined();
                addComponent(subview, index);
                setComponentAlignment(subview, Alignment.TOP_RIGHT);
            }
        };
        layout.setCompactArrangement(isCompactArrangement());
        return layout;
    }

    @Override
    public SynergyLayoutFactory getSubitemLayoutFactory()
    {
        return this;
    }

}
