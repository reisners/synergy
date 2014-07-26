package ${package}.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

public class Receiver
{
    private static final Logger log = LoggerFactory.getLogger(Receiver.class);
    
    public void receiveMessage(final String message) {
        log.info("receiveMessage("+message+")");
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(new Runnable() {
                @Override
                public void run()
                {
                    Notification.show(message, Notification.Type.TRAY_NOTIFICATION);
                }
            });
        }
    }
}
