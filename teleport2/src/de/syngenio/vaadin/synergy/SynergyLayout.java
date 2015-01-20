package de.syngenio.vaadin.synergy;

import java.util.Iterator;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;

public abstract class SynergyLayout extends CustomComponent implements Layout
{
    protected SynergyLayout() {
        super();
        setCompositionRoot(createLayout());
    }

    abstract Layout createLayout();
    
    private Layout layout()
    {
        return (Layout)getCompositionRoot();
    }
    
    protected abstract void addItemComponent(Component itemComponent);
    
    /**
     * @param subview the subview to be added.
     * @param index the index of the component position. The components currently in and after the position are shifted forwards.    
     */
    protected abstract void addSubview(SynergyView subview, int index);
    

    @Override
    public void addComponent(Component c)
    {
        layout().addComponent(c);
    }

    @Override
    public void addComponents(Component... components)
    {
        layout().addComponents(components);
    }

    @Override
    public void removeComponent(Component c)
    {
        layout().removeComponent(c);
    }

    @Override
    public void removeAllComponents()
    {
        layout().removeAllComponents();
    }

    @Override
    public void replaceComponent(Component oldComponent, Component newComponent)
    {
        layout().replaceComponent(oldComponent, newComponent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Iterator<Component> getComponentIterator()
    {
        return layout().getComponentIterator();
    }

    @Override
    public void moveComponentsFrom(ComponentContainer source)
    {
        layout().moveComponentsFrom(source);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addListener(ComponentAttachListener listener)
    {
        layout().addListener(listener);
    }

    @Override
    public void removeListener(ComponentAttachListener listener)
    {
        layout().removeListener(listener);
    }

    @Override
    public void addListener(ComponentDetachListener listener)
    {
        layout().addListener(listener);
    }

    @Override
    public void removeListener(ComponentDetachListener listener)
    {
        layout().removeListener(listener);
    }

    @Override
    public void addComponentAttachListener(ComponentAttachListener listener)
    {
        layout().addComponentAttachListener(listener);
    }

    @Override
    public void removeComponentAttachListener(ComponentAttachListener listener)
    {
        layout().removeComponentAttachListener(listener);
    }

    @Override
    public void addComponentDetachListener(ComponentDetachListener listener)
    {
        layout().addComponentDetachListener(listener);
    }

    @Override
    public void removeComponentDetachListener(ComponentDetachListener listener)
    {
        layout().removeComponentDetachListener(listener);
    }

    /**
     * Returns the index of the given component.
     * @param component The component to look up.
     * @return The index of the component or -1 if the component is not a child.
     */
    public abstract int getComponentIndex(Component c);
}
