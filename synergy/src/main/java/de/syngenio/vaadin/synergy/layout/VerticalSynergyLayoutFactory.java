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
    private String indentWidth = "20px";
    
    public VerticalSynergyLayoutFactory() {
        super();
    }
    
    /**
     * @param packing 
     */
    public VerticalSynergyLayoutFactory(Packing packing)
    {
        super(packing);
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
                if (getPacking() == Packing.EXPAND) {
                    itemComponent.setHeight("100%");
                } else {
                    itemComponent.setHeightUndefined();
                }
                itemComponent.addStyleName(getOrientationStyleName());
                addComponent(itemComponent);
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

            @Override
            public Packing getPacking()
            {
                return VerticalSynergyLayoutFactory.this.getPacking();
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
                case SPACE_BEFORE:
                    setExpandRatio(component, 1);
                    setComponentAlignment(component, Alignment.BOTTOM_CENTER);
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
                    setExpandRatio(component, 1);
                    setComponentAlignment(component, Alignment.TOP_RIGHT);
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
