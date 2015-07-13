package de.syngenio.vaadin.synergy.layout;

import java.util.Iterator;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;

import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.SynergyView.ItemComponent;
import de.syngenio.vaadin.synergy.layout.AbstractSynergyLayoutFactory.Packing;

public abstract class SynergyLayout extends CustomComponent implements Layout, Layout.AlignmentHandler
{
    private AbstractOrderedLayout layout;

    protected SynergyLayout() {
        super();
        layout = createLayout();
        setCompositionRoot(layout);
    }

    public abstract Packing getPacking();
    
    private void updateItemLayout()
    {
        int componentCount = getComponentCount();
        if (componentCount > 0) {
            for (int i = 0; i < componentCount; ++i) {
                final Component itemComponent = getComponent(i);
                if (componentCount == 1) {
                    layoutSingularComponent(itemComponent);
                } else if (i == 0) {
                    layoutFirstComponent(itemComponent);
                } else if (i == componentCount - 1) {
                    layoutLastComponent(itemComponent);
                } else {
                    layoutIntermediateComponent(itemComponent);
                }
            }
        }
    }
    
    abstract protected void layoutSingularComponent(Component itemComponent);
    abstract protected void layoutFirstComponent(Component itemComponent);
    abstract protected void layoutIntermediateComponent(Component itemComponent);
    abstract protected void layoutLastComponent(Component itemComponent);
    
    abstract protected AbstractOrderedLayout createLayout();

    /**
     * Adds a component representing an item to the layout
     * @param itemComponent
     */
    public abstract void addItemComponent(Component itemComponent);
    
    /**
     * Adds a nested SynergyView at the given position to the layout
     * @param subview subview to be added.
     * @param index position for insertion. The components currently in and after the position are shifted to higher indices.    
     */
    public abstract void addSubview(SynergyView subview, int index);
    

    @Override
    public void addComponent(Component c)
    {
        layout.addComponent(c);
        updateItemLayout();
    }

    public void addComponent(Component c, int index)
    {
        layout.addComponent(c, index);
        updateItemLayout();
    }

    @Override
    public void addComponents(Component... components)
    {
        layout.addComponents(components);
        updateItemLayout();
    }

    @Override
    public void removeComponent(Component c)
    {
        layout.removeComponent(c);
        updateItemLayout();
    }

    @Override
    public void removeAllComponents()
    {
        layout.removeAllComponents();
        updateItemLayout();
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent)
    {
        layout.replaceComponent(oldComponent, newComponent);
        updateItemLayout();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Iterator<Component> getComponentIterator()
    {
        return layout.getComponentIterator();
    }

    @Override
    public void moveComponentsFrom(ComponentContainer source)
    {
        layout.moveComponentsFrom(source);
        updateItemLayout();
    }

    @Deprecated
    @Override
    public void addListener(ComponentAttachListener listener)
    {
        layout.addListener(listener);
    }

    @Deprecated
    @Override
    public void removeListener(ComponentAttachListener listener)
    {
        layout.removeListener(listener);
    }

    @Deprecated
    @Override
    public void addListener(ComponentDetachListener listener)
    {
        layout.addListener(listener);
    }

    @Deprecated
    @Override
    public void removeListener(ComponentDetachListener listener)
    {
        layout.removeListener(listener);
    }

    @Override
    public void addComponentAttachListener(ComponentAttachListener listener)
    {
        layout.addComponentAttachListener(listener);
    }

    @Override
    public void removeComponentAttachListener(ComponentAttachListener listener)
    {
        layout.removeComponentAttachListener(listener);
    }

    @Override
    public void addComponentDetachListener(ComponentDetachListener listener)
    {
        layout.addComponentDetachListener(listener);
    }

    @Override
    public void removeComponentDetachListener(ComponentDetachListener listener)
    {
        layout.removeComponentDetachListener(listener);
    }

    public int getComponentIndex(Component c) {
        return layout.getComponentIndex(c);
    }

    public void setExpandRatio(Component component, float ratio) {
        layout.setExpandRatio(component, ratio);
    }

    public float getExpandRatio(Component component) {
        return layout.getExpandRatio(component);
    }
    
    @Override
    public void setComponentAlignment(Component childComponent, Alignment alignment) {
        layout.setComponentAlignment(childComponent, alignment);
    }

    public Component getComponent(int index) {
        return layout.getComponent(index);
    }
    
    @Override
    public int getComponentCount()
    {
        return layout.getComponentCount();
    }

    @Override
    public Alignment getComponentAlignment(Component childComponent)
    {
        return layout.getComponentAlignment(childComponent);
    }

    @Override
    public void setDefaultComponentAlignment(Alignment defaultComponentAlignment)
    {
        layout.setDefaultComponentAlignment(defaultComponentAlignment);
    }

    @Override
    public Alignment getDefaultComponentAlignment()
    {
        return layout.getDefaultComponentAlignment();
    }
}
