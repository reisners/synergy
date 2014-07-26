package ${package}.ui;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages={"${package}.ui"})
public class ServletSpringConfig {
}