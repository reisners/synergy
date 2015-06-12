package de.syngenio.vaadin.synergy;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class VerticalSynergyLayoutFactory extends AbstractSynergyLayoutFactory
{
    private boolean hasSpacing;
    private boolean hasMargin;
    private Alignment itemAlignment;
    private String indentWidth = "20px";
    
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
                itemComponent.addStyleName("vertical");
                addComponent(itemComponent);
                setComponentAlignment(itemComponent, itemAlignment);
            }

            @Override
            protected void addSubview(SynergyView subview, int index)
            {
                // if it has been added to this layout before, remove the subview (along with its wrapper) first 
                if (subview.getWrapper() != null) {
                    removeComponent(subview.getWrapper());
                }
                Label label = new Label();
                label.setWidth(indentWidth);
                label.setHeightUndefined();
                HorizontalLayout wrapper = new HorizontalLayout(label, subview);
                wrapper.setWidth("100%");
                wrapper.setHeightUndefined();
                wrapper.setComponentAlignment(subview, Alignment.TOP_LEFT);
                wrapper.setExpandRatio(label, 0);
                wrapper.setExpandRatio(subview, 1);
                subview.setWrapper(wrapper);
                addComponent(wrapper, index);
                setComponentAlignment(wrapper, Alignment.TOP_LEFT);
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
