package vaadin.fix.jmeter;

import com.vaadin.server.ClientConnector;
import com.vaadin.ui.Component;

public aspect JMeterStaticComponentIds {
    pointcut createConnectorId(ClientConnector connector) : execution(* com.vaadin.server.VaadinSession.createConnectorId(..)) && args(connector);
    
    String around(ClientConnector connector) : createConnectorId(connector) {
        System.out.println("createConnectorId() called for "+connector);
        if (connector instanceof Component) {
            Component component = (Component) connector;
            if (component.getId() != null) {
                System.out.println("replaced dynamic id by "+component.getId());
                return component.getId();
            }
        }
        return proceed(connector);
    }

    pointcut buildUpDownButton() : execution(* com.example.teleport2.Teleport2UI.buildUpDownButton());
}
