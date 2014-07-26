package ${package}.beans;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import ${package}.spring.AppSpringConfig;

@Service
public class AloaService
{
    private static final Logger log = LoggerFactory.getLogger(AloaService.class);
    
    @Autowired
    JmsTemplate jmsPubSubTemplateAloa;

    public void sendAloa(final String text) {

        // Send a message
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(text);
            }
        };
        jmsPubSubTemplateAloa.send(AppSpringConfig.DESTINATION_ALOA, messageCreator);
        log.info("sent message \""+text+"\" to destination "+AppSpringConfig.DESTINATION_ALOA);
    }
}
