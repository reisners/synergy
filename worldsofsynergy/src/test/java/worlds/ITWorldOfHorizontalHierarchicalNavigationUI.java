package worlds;

import helpers.WorldHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

public class ITWorldOfHorizontalHierarchicalNavigationUI extends SynergyTestBase
{
    private String baseUrl = "http://localhost:8080/worldsofsynergy/horizontal/hierarchical";
    
    public ITWorldOfHorizontalHierarchicalNavigationUI()
    {
        super();
    }
    
    @Before
    public void setUp() {
        setBaseUrl(baseUrl);
    }

    @Test
    public void test()
    {
        new HierarchyExercise(WorldHelper.getNavigationHierarchy()).exercise();
    }

    @Override
    void checkVisuals(String itemId)
    {
    }

    @After
    public void tearDown() {
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        getDriver().close();
        System.out.println("windows remaining: "+getDriver().getWindowHandles());
    }
}
