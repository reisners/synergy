package de.syngenio.vaadin.synergy;

import java.util.ResourceBundle;

import com.vaadin.data.Container;
import com.vaadin.ui.AbstractSelect;

public class Synergy extends AbstractSelect
{
    private ResourceBundle resourceBundle = null;
    
    public Synergy(Container dataSource)
    {
        super(null, dataSource);
    }

    public ResourceBundle getResourceBundle()
    {
        return resourceBundle;
    }

    public void setResourceBundle(ResourceBundle resourceBundle)
    {
        this.resourceBundle = resourceBundle;
    }

    String i18n(String key)
    {
        return resourceBundle != null ? resourceBundle.getString(key) : key;
    }
}
