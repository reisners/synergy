package ${package}.util;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;

public class VaadinUtil
{
    private static Logger log = LoggerFactory.getLogger(VaadinUtil.class);
    
    public static void autogenerateMemberComponentIds(Object container)
    {
        log.debug("VaadinUtil.setIds("+container+")");
        for (Field field : container.getClass().getDeclaredFields()) {
            if (!Component.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try
            {
                field.setAccessible(true);
                Component c = (Component) field.get(container);
                if (c != null && c.getId() == null) {
                    c.setId(container.getClass().getCanonicalName()+"."+field.getName());
                }
            }
            catch (Exception e)
            {
                log.warn("cannot access value of field "+container+"."+field.getName());
            }
        }
    }

}
