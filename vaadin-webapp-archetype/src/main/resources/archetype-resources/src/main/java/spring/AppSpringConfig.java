package ${package}.spring;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import test.try14.auth.AuthenticationService;
import test.try14.auth.UserProfileRepository;
import test.try14.ui.AppUI;
import test.try14.ui.NavigationHierarchyProvider;
import test.try14.ui.Receiver;
import test.try14.ui.SpringViewCache;
import test.try14.ui.SpringViewCatalogue;
import test.try14.ui.UserManager;

import com.google.common.eventbus.EventBus;

@Configuration
@ComponentScan(basePackageClasses={test.try14.Tag.class})
public class AppSpringConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    UserProfileRepository userProfileRepository() {
        return new UserProfileRepository();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    AuthenticationService authenticationService() {
        return new AuthenticationService();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    AppUI ui() {
        return new AppUI();
    }
    
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    SpringViewCatalogue springViewCatalogue(ApplicationContext applicationContext) {
        return new SpringViewCatalogue(applicationContext);
    }
    
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    UserManager userManager() {
        return new UserManager();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    EventBus eventBus() {
        return new EventBus();
    }
    
//    @Bean
//    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
//    HierarchicalContainer navigationHierarchy(NavigationHierarchyProvider provider)
//    {
//        HierarchicalContainer hc = provider.createHierarchicalContainer();
//        
//        Item hiStammdaten = hc.addItem("Stammdaten");
//        hiStammdaten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CLASS).setValue(ItemComponentImage.class.getName());
//        hiStammdaten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_WIDTH).setValue("64px");
//        hiStammdaten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_HEIGHT).setValue("64px");
//        hiStammdaten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE).setValue("img/service_desk.png");
//        hiStammdaten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED).setValue("img/service_desk_selected.png");
//
//        Item hiBudget = hc.addItem("Budget");
//        hiBudget.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CLASS).setValue(ItemComponentImage.class.getName());
//        hiBudget.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_WIDTH).setValue("64px");
//        hiBudget.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_HEIGHT).setValue("64px");
//        hiBudget.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE).setValue("img/budget.png");
//        hiBudget.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED).setValue("img/budget_selected.png");
//
//        Item hiReporting = hc.addItem("Reporting");
//        hiReporting.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CLASS).setValue(ItemComponentImage.class.getName());
//        hiReporting.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_WIDTH).setValue("64px");
//        hiReporting.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_HEIGHT).setValue("64px");
//        hiReporting.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE).setValue("img/reporting.png");
//        hiReporting.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED).setValue("img/reporting_selected.png");
//
//        Item hiPerson = hc.addItem("Person");
//        hc.setParent("Person", "Stammdaten");
//        hiPerson.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("MyView1");
//
//        Item hiPersonSuchen = hc.addItem("Person.Suchen");
//        hiPersonSuchen.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("MyView2");
//        hiPersonSuchen.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CAPTION).setValue("Suchen");
//        hc.setParent("Person.Suchen", "Person");
//
//        Item hiPersonAnlegen = hc.addItem("Person.Anlegen");
//        hiPersonAnlegen.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("MyView2");
//        hiPersonAnlegen.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CAPTION).setValue("Anlegen");
//        hc.setParent("Person.Anlegen", "Person");
//
//        Item hiPersonAnlegenAnschrift = hc.addItem("Person.Anlegen.Anschrift");
//        hc.setParent("Person.Anlegen.Anschrift", "Person.Anlegen");
//        hiPersonAnlegenAnschrift.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("MyView2");
//        hiPersonAnlegenAnschrift.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CAPTION).setValue("Anschrift");
//
//        Item hiPersonAnlegenBonitaeten = hc.addItem("Person.Anlegen.Bonitäten");
//        hc.setParent("Person.Anlegen.Bonitäten", "Person.Anlegen");
//        hiPersonAnlegenBonitaeten.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("MyView2");
//        hiPersonAnlegenBonitaeten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CAPTION).setValue("Bonitäten");
//
//        Item hiKonto = hc.addItem("Konto");
//        hc.setParent("Konto", "Stammdaten");
//        hiKonto.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("MyView3");
//        
//        Item hiVerwaltung = hc.addItem("Verwaltung");
//        hc.setParent("Verwaltung", "Stammdaten");
//        hiVerwaltung.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("MyView1");
//        
//        return hc;
//    }
    
}