package ${package}.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public aspect PostprocessVaadinContainers {
    private static Logger log = LoggerFactory.getLogger(PostprocessVaadinContainers.class);
    
    /**
     * Application classes.
     */
    pointcut myClasses(): within(test.vaadin.try12.ui..*) && within(com.vaadin.ui.Component+);
    /**
     * The constructors in those classes.
     */
    pointcut myConstructors(): myClasses() && (execution(*.new()) || execution(*.new(..)));

    /**
     * Call VaadinUtil.setIds after executing constructors.
     */
    after(): myConstructors() {
        log.debug(thisJoinPoint.getSignature().toLongString());
        VaadinUtil.autogenerateMemberComponentIds(thisJoinPoint.getTarget());
    }

}
