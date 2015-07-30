package de.syngenio.collaboration.data;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

public class TestHelper
{
    private static Logger LOG = LoggerFactory.getLogger(TestHelper.class);
    
    static void dumpContainer(Container container)
    {
        Collection< ? > propertyIds = container.getContainerPropertyIds();
        StringBuilder sbHead = new StringBuilder(String.format("%20.20s", ""));
        for (Object propertyId : propertyIds) {
            sbHead.append(String.format("| %20.20s", propertyId));
        }
        LOG.info(sbHead.toString());
        LOG.info(sbHead.toString().replaceAll(".", "-"));
        for (Object itemId : container.getItemIds()) {
            StringBuilder sb = new StringBuilder(String.format("%20.20s", itemId.toString()));
            Item item = container.getItem(itemId);
            for (Object propertyId : propertyIds) {
                Object value = item.getItemProperty(propertyId).getValue();
                sb.append(String.format("| %20.20s", value));
            }
            LOG.info(sb.toString());
        }
        LOG.info(sbHead.toString().replaceAll(".", "="));
    }
}
