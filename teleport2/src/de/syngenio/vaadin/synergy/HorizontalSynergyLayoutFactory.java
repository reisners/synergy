package de.syngenio.vaadin.synergy;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;

public class HorizontalSynergyLayoutFactory implements SynergyLayoutFactory
{
    SynergyLayoutFactory subitemLayoutFactory = this;
    
    @Override
    public SynergyLayout generateLayout()
    {
        SynergyLayout layout = new SynergyLayout() {

            private HorizontalLayout hlayout;

            @Override
            Layout createLayout()
            {
                hlayout = new HorizontalLayout();
                hlayout.setSpacing(true);
                hlayout.setStyleName("synergy");
                return hlayout;
            }

            @Override
            protected void addItemComponent(Component itemComponent)
            {
                hlayout.addComponent(itemComponent);
                hlayout.setComponentAlignment(itemComponent, Alignment.MIDDLE_CENTER);
            }

            @Override
            protected void addSubview(SynergyView subview, int index)
            {
                // do nothing
            }

            @Override
            public int getComponentIndex(Component c)
            {
                return hlayout.getComponentIndex(c);
            }
        };
        return layout;
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
