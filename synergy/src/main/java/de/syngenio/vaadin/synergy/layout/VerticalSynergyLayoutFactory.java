package de.syngenio.vaadin.synergy.layout;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.SynergyView;

public class VerticalSynergyLayoutFactory extends AbstractSynergyLayoutFactory
{
    private static final String VERTICAL = "vertical";
    private Alignment itemAlignment;
    private String indentWidth = "20px";
    
    /**
     * Default constructor
     */
    public VerticalSynergyLayoutFactory()
    {
        super();
    }
    
    /**
     * @param itemAlignment set this to {@code Alignment.MIDDLE_LEFT} if items form a hierarchy. Otherwise, {@code Alignment.MIDDLE_CENTER} is recommended.
     */
    public VerticalSynergyLayoutFactory(Alignment itemAlignment)
    {
        super();
        this.itemAlignment = itemAlignment;
    }

    public void setIndentWidth(String indentWidth)
    {
        this.indentWidth = indentWidth;
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
                if (isCompactArrangement()) {
                    itemComponent.setHeightUndefined();
                } else {
                    itemComponent.setHeight("100%");
                }
                itemComponent.addStyleName(getOrientationStyleName());
                addComponent(itemComponent);
                setComponentAlignment(itemComponent, itemAlignment);
            }

            @Override
            public void addSubview(SynergyView subview, int index)
            {
                // if it has been added to this layout before, remove the subview (along with its wrapper) first 
                if (subview.getWrapper() != null) {
                    removeComponent(subview.getWrapper());
                }
                Label label = new Label();
                label.setWidth(indentWidth);
                label.setHeightUndefined();
                HorizontalLayout wrapper = new HorizontalLayout(label, subview);
                wrapper.addStyleName("wrapper");
                wrapper.setWidth("100%");
                wrapper.setHeightUndefined();
                wrapper.setComponentAlignment(subview, Alignment.TOP_LEFT);
                wrapper.setExpandRatio(label, 0);
                wrapper.setExpandRatio(subview, 1);
                subview.setWrapper(wrapper);
                addComponent(wrapper, index);
                setComponentAlignment(wrapper, Alignment.TOP_RIGHT);
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

    @Override
    public String getOrientationStyleName()
    {
        return VERTICAL;
    }

}
