package de.syngenio.collaboration;

import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.google.common.eventbus.EventBus;
import com.vaadin.spring.boot.internal.VaadinServletConfiguration;

import de.syngenio.collaboration.ui.AppUI;

@SpringBootApplication
@EnableTransactionManagement
//@EnableScheduling
public class Application
{
    @Configuration
    @ComponentScan(basePackages={"de.syngenio.collaboration"})
    @EnableNeo4jRepositories(basePackages = "de.syngenio.collaboration.data")
    public static class ApplicationConfig extends Neo4jConfiguration {
        public ApplicationConfig() {
            setBasePackage("de.syngenio.collaboration.data");
        }

        @Bean(destroyMethod = "shutdown")
        public GraphDatabaseService graphDatabaseService() {
            return new GraphDatabaseFactory().newEmbeddedDatabase("collaboration.db");
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
        EventBus eventBus()
        {
            return new EventBus();
        }
    }
    
    public static void main(String[] args)
    {
        new SpringApplicationBuilder(Application.class).initializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
            public void initialize(ConfigurableApplicationContext applicationContext)
            {
                ConfigurableEnvironment appEnvironment = applicationContext.getEnvironment();
                Properties props = new Properties();
                props.put(VaadinServletConfiguration.SERVLET_URL_MAPPING_PARAMETER_NAME, AppUI.CONTEXT_PATH+"/*");
                PropertySource< ? > source = new PropertiesPropertySource("vaadin", props);
                appEnvironment.getPropertySources().addFirst(source);
            }
        }).run(args);
    }
}