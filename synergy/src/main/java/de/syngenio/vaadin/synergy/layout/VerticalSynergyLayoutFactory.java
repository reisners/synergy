package de.syngenio.vaadin.synergy.layout;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import de.syngenio.vaadin.synergy.SynergyView;

public class VerticalSynergyLayoutFactory extends AbstractSynergyLayoutFactory
{
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
        SynergyLayout layout = new VerticalSynergyLayout(getPacking()) {
            @Override
            public void addSubview(SynergyView subview, int index)
            {
                // if it has been added to this layout before, remove the subview (along with its wrapper) first 
                if (subview.getWrapper() != null) {
                    removeComponent(subview.getWrapper());
                }
//                Label label = new Label();
//                label.setWidth(indentWidth);
//                label.setHeightUndefined();
                HorizontalLayout wrapper = new HorizontalLayout(subview);
                wrapper.setMargin(new MarginInfo(false, false, true, true));
                wrapper.addStyleName("wrapper");
                wrapper.setWidth("100%");
                wrapper.setHeightUndefined();
                wrapper.setComponentAlignment(subview, Alignment.TOP_LEFT);
//                wrapper.setExpandRatio(label, 0);
//                wrapper.setExpandRatio(subview, 1);
                subview.setWrapper(wrapper);
                subview.setSizeUndefined();
                addComponent(wrapper, index);
                setComponentAlignment(wrapper, Alignment.TOP_RIGHT);
                setExpandRatio(wrapper, 0);
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
}
