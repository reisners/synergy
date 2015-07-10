package de.syngenio.vaadin.synergy.layout;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import de.syngenio.vaadin.synergy.SynergyView;

public class HorizontalSynergyLayoutFactory extends AbstractSynergyLayoutFactory
{
    private static final String HORIZONTAL = "horizontal";

    @Override
    public SynergyLayout generateLayout()
    {
        SynergyLayout layout = new SynergyLayout() {


            @Override
            protected AbstractOrderedLayout createLayout()
            {
                HorizontalLayout hlayout = new HorizontalLayout();
//                hlayout.setSpacing(true);
//                hlayout.setMargin(true);
                hlayout.setWidth("100%");
                hlayout.setVisible(false);
                return hlayout;
            }

            @Override
            public void addItemComponent(Component itemComponent)
            {
                if (isCompactArrangement()) {
                    itemComponent.setWidthUndefined();
                } else {
                    itemComponent.setWidth("100%");
                }
                itemComponent.addStyleName(getOrientationStyleName());
                addComponent(itemComponent);
                setComponentAlignment(itemComponent, isCompactArrangement() ? Alignment.MIDDLE_LEFT : Alignment.MIDDLE_CENTER);
            }

            @Override
            public void addSubview(SynergyView subview, int index)
            {
                // do nothing
            }
        };
        layout.setCompactArrangement(isCompactArrangement());
        return layout;
    }

    @Override
    public String getOrientationStyleName()
    {
        return HORIZONTAL;
    }
}
