package ${package}.beans;

import org.springframework.stereotype.Component;

@Component
public class MyBean implements MyBeanInterface
{
    @Override
    public String getName()
    {
        return "Hello";
    }

    @Override
    public String getText()
    {
        return "World";
    }
}
