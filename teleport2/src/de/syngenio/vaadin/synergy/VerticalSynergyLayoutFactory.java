package de.syngenio.vaadin.synergy;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class VerticalSynergyLayoutFactory implements SynergyLayoutFactory
{
    @Override
    public SynergyLayout generateLayout()
    {
        SynergyLayout layout = new SynergyLayout() {

            private VerticalLayout vlayout;

            @Override
            Layout createLayout()
            {
                vlayout = new VerticalLayout();
                vlayout.setSpacing(true);
                vlayout.setStyleName("synergy");
                return vlayout;
            }

            @Override
            protected void addItemComponent(Component itemComponent)
            {
                vlayout.addComponent(itemComponent);
                vlayout.setComponentAlignment(itemComponent, Alignment.TOP_RIGHT);
            }

            @Override
            protected void addSubview(SynergyView subview, int index)
            {
                subview.setWidth(80, Unit.PERCENTAGE);
                subview.setHeightUndefined();
                vlayout.addComponent(subview, index);
                vlayout.setComponentAlignment(subview, Alignment.TOP_RIGHT);
            }

            @Override
            public int getComponentIndex(Component c)
            {
                return vlayout.getComponentIndex(c);
            }
        };
        return layout;
    }

    @Override
    public SynergyLayoutFactory getSubitemLayoutFactory()
    {
        return this;
    }

}
