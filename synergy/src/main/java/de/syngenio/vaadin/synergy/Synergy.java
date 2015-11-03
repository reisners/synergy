package de.syngenio.vaadin.synergy;

import java.net.URI;
import java.util.ResourceBundle;

import com.vaadin.data.Container;
import com.vaadin.ui.AbstractSelect;

@SuppressWarnings("serial")
public class Synergy extends AbstractSelect
{
    private ResourceBundle resourceBundle = null;
    private static final String URI_PREFIX = "http://www.syngenio.de/vaadin/syngergy/hierarchicalContainerProperties";
    public static final URI PROPERTY_TARGET_NAVIGATION_STATE = URI.create(URI_PREFIX+"/targetNavigationState");
    /**
     * Fully qualified class name of an implementation of {@code ItemComponent} (optional, defaults to {@code ItemComponentButton}) 
     */
    public static final URI PROPERTY_ITEM_COMPONENT_CLASS = URI.create(URI_PREFIX+"/itemComponentClass");
    /**
     * Item caption text (optional)
     */
    public static final URI PROPERTY_ITEM_CAPTION = URI.create(URI_PREFIX+"/itemCaption");
    /**
     * Resource for image or button icon (optional).
     * May be either a {@code FontIcon} (glyph), a {@code ThemeResource}, or a {@code ExternalResource}
     * @see {@code SynergyBuilder#PROPERTY_ITEM_COMPONENT_ICON_SELECTED}
     */
    public static final URI PROPERTY_ITEM_ICON = URI.create(URI_PREFIX+"/itemIcon");
    /**
     * Resource URI of selected image or button icon, respectively (optional)
     * Optional; if provided, replaces {@code SynergyBuilder#PROPERTY_ITEM_ICON} in selected states
     */
    public static final URI PROPERTY_ITEM_ICON_SELECTED = URI.create(URI_PREFIX+"/itemIconSelected");
    /**
     * Image width (optional; only relevant for {@code ItemComponentImage})
     */
    public static final URI PROPERTY_ITEM_IMAGE_WIDTH = URI.create(URI_PREFIX+"/itemImageWidth");
    /**
     * Image height (optional; only relevant for {@code ItemComponentImage})
     */
    public static final URI PROPERTY_ITEM_IMAGE_HEIGHT = URI.create(URI_PREFIX+"/itemImageHeight");
    /**
     * font-size for FontIcon resource (optional; only relevant for {@code ItemComponentImage})
     */
    public static final URI PROPERTY_ITEM_GLYPH_SIZE = URI.create(URI_PREFIX+"/itemGlyphSize");
    /**
     * Can be used to inform a filter how to deal with items not having children (optional)  
     */
    public static final URI PROPERTY_ITEM_HIDDEN_IF_EMPTY = URI.create(URI_PREFIX+"/itemHiddenIfEmpty");
    /**
     * Style name to be set on this item's subview (optional)  
     */
    public static final URI PROPERTY_ITEM_SUBVIEW_STYLE = URI.create(URI_PREFIX+"/subviewStyle");
    /**
     * Tool tip to be shown for this item (optional)  
     */
    public static final URI PROPERTY_ITEM_DESCRIPTION = URI.create(URI_PREFIX+"/toolTip");
    /**
     * Action ({@code BiConsumer<com.vaadin.data.Item, com.vaadin.ui.UI>}) to perform when this item is selected (optional)
     */
    public static final URI PROPERTY_ITEM_ACTION = URI.create(URI_PREFIX+"/action");
    
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
