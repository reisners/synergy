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
        log.debug("VaadinUtil.autogenerateMemberComponentIds("+container+")");
        for (Field field : container.getClass().getDeclaredFields()) {
            if (field.getName().startsWith("this$")) { // an inner class' reference to the outer instance
                continue;
            }
            if (!Component.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try
            {
                field.setAccessible(true);
                Component c = (Component) field.get(container);
                if (c == null) {
                    log.debug("component-valued field "+field.getName()+" is null");
                    continue;
                }
                if (c.getId() != null) {
                    log.debug("component-valued field "+field.getName()+" already has id "+c.getId());
                    continue;
                }
                String className = container.getClass().getName().replace('$', '.');
                String id = className+"."+field.getName();
                c.setId(id);
                log.debug("component-valued field "+field.getName()+" assigned id "+id);
            }
            catch (Exception e)
            {
                log.warn("cannot access value of field "+container+"."+field.getName());
            }
        }
    }

}
