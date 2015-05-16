package de.syngenio.vaadin.synergy;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class VerticalSynergyLayoutFactory extends AbstractSynergyLayoutFactory
{
    private boolean hasSpacing;
    private boolean hasMargin;
    private Alignment itemAlignment;
    
    /**
     * @param hasSpacing whether to add spacing between item components
     * @param hasMargin whether to put a margin around the view
     * @param itemAlignment set this to {@code Alignment.MIDDLE_LEFT} if items form a hierarchy. Otherwise, {@code Alignment.MIDDLE_CENTER} is recommended.
     */
    public VerticalSynergyLayoutFactory(boolean hasSpacing, boolean hasMargin, Alignment itemAlignment)
    {
        super();
        this.hasSpacing = hasSpacing;
        this.hasMargin = hasMargin;
        this.itemAlignment = itemAlignment;
    }

    @Override
    public SynergyLayout generateLayout()
    {
        SynergyLayout layout = new SynergyLayout() {

            @Override
            protected AbstractOrderedLayout createLayout()
            {
                VerticalLayout vlayout = new VerticalLayout();
                vlayout.setStyleName(generateStyleName());
                vlayout.setSpacing(hasSpacing);
                vlayout.setMargin(hasMargin);
                vlayout.setWidth("100%");
                return vlayout;
            }

            @Override
            protected void addItemComponent(Component itemComponent)
            {
                itemComponent.setWidth("100%");
                addComponent(itemComponent);
                setComponentAlignment(itemComponent, itemAlignment);
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
