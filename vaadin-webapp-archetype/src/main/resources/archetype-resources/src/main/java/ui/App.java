package ${package}.ui;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

public class App
{
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private boolean isPushing = true;
    
    public App() {
        
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run()
            {
                while (isPushing) {
                    try {
                        Thread.sleep(10000);
                        UI.getCurrent().access(new Runnable() {
                            @Override
                            public void run()
                            {
                                String user = String.valueOf(UI.getCurrent().getSession().getAttribute("user"));
                                if (user != null) {
                                    Notification.show("message for "+user+" at "+new Date(), Notification.Type.TRAY_NOTIFICATION);
                                    log.info("sent push message to "+user+"'s client");
                                } else {
                                    isPushing = false;
                                }
                            }
                        });
                    } catch (Throwable e) {
                        log.error("failed", e);
                        isPushing = false;
                    }
                }
            }});
        thread.setDaemon(true);
        thread.start();
    }
}
