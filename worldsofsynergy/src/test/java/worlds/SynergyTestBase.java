package worlds;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;

import de.syngenio.vaadin.synergy.SynergyBuilder;
import de.syngenio.vaadin.synergy.SynergyView.ItemComponent.State;

public abstract class SynergyTestBase
{
    private WebDriver driver;

    private String baseUrl;

    private long timeout = 1000;
    private long waitPeriod = 200;

    protected SynergyTestBase()
    {
        setupFirefox();
//        setupPhantomJs();
    }

    private void setupFirefox()
    {
        driver = new FirefoxDriver();
    }

    private void setupPhantomJs()
    {
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", true);
        dCaps.setCapability("phantomjs.binary.path", "D:/Users/sre/Documents/Technologie/phantomjs-2.0.0-windows/bin/phantomjs.exe");
        driver = new PhantomJSDriver(dCaps);
    }

    public WebDriver getDriver()
    {
        return driver;
    }

    public void setDriver(WebDriver driver)
    {
        this.driver = driver;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    private void reload()
    {
        driver.get(baseUrl);
    }

    protected class HierarchyExercise
    {
        private HierarchicalContainer hierarchy;
        private final Logger log = LoggerFactory.getLogger(HierarchyExercise.class);

        HierarchyExercise(HierarchicalContainer hierarchy)
        {
            this.hierarchy = hierarchy;
        }

        protected void exercise()
        {
            reload();
            // check that all root items are visible
            hierarchy.rootItemIds().forEach(rootItemId -> assertItemVisible((String) rootItemId));
            // check that no other items are visible
            hierarchy.getItemIds().forEach(itemId -> {
                if (!hierarchy.isRoot(itemId))
                {
                    assertItemNotVisible((String) itemId);
                }
            });

            // click on each
            final List<String> shuffledRootItemIds = new ArrayList<String>((Collection<String>) hierarchy.rootItemIds());
            Collections.shuffle(shuffledRootItemIds);
            shuffledRootItemIds.forEach(itemId -> exerciseItem(itemId));
            
            // descend into each subhierarchy
            shuffledRootItemIds.forEach(itemId -> exerciseItemsBelow(itemId));
        }

        private void exerciseItemsBelow(String parentId)
        {
            log.info("exercising children of "+parentId);
            click(parentId);
            List<String> shuffledChildItemIds = childrenOf(parentId);
            Collections.shuffle(shuffledChildItemIds);
            shuffledChildItemIds.forEach(itemId -> exerciseItem(itemId));
            // descend further
            shuffledChildItemIds.forEach(itemId -> exerciseItemsBelow(itemId));
        }

        private void exerciseItem(String itemId)
        {
            log.info("exercising item "+itemId);
            assertItemState(itemId, State.unselected);
            click(itemId);
            assertItemState(itemId, State.selected);
            // if the item has a target navigation state check that the view is visible
            String targetNavigationState = ((Property<String>) hierarchy.getContainerProperty(itemId, SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE)).getValue();
            if (targetNavigationState != null) {
                String name = targetNavigationState.split("/")[1];
                assertTrue("content should display "+name, driver.findElement(By.id("content")).getText().contains(name));
            }
        }


        private void click(String itemId)
        {
            driver.findElement(By.id(itemId)).click();
            sleep(300);
        }

        private List<String> childrenOf(final String parentId)
        {
            return (List<String>) hierarchy.getItemIds().stream().filter(itemId -> equals((String) hierarchy.getParent(itemId), parentId))
                    .collect(Collectors.toList());
        }

        private boolean equals(String id1, String id2)
        {
            return id1 != null && id1.equals(id2) || id1 == id2;
        }
    }

    private void assertItemState(String itemId, State state)
    {
        assertCssClassSuffix(itemId, state.getCssClassSuffix());
    }
    
    private void assertCssClassSuffix(String itemId, String cssClassSuffix)
    {
        assertWithinTimeout("css class suffix " + cssClassSuffix + " not found on item "+itemId, itemId, predicateHasCssClassSuffix(cssClassSuffix));
    }

    private Predicate<String> predicateHasCssClassSuffix(String cssClassSuffix)
    {
        return itemId -> {
            try
            {
                final WebElement item = driver.findElement(By.id(itemId));
                final String cssClass = item.getAttribute("class");
                return cssClass.matches(".*\\b" + cssClassSuffix + "\\b.*");
            }
            catch (NoSuchElementException e)
            {
                return false;
            }
        };
    }
    
    private void assertWithinTimeout(String message, String itemId, Predicate<String> predicate) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeout ) {
            boolean result = predicate.test(itemId);
            if (result) {
                return;
            }
            sleep(waitPeriod);
        }
        assertTrue(message, false);
    }

    private void sleep(long delay)
    {
        try
        {
            Thread.sleep(delay);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    private void assertItemNotVisible(String itemId)
    {
        assertTrue("item " + itemId + " should not be visible", driver.findElements(By.id(itemId)).isEmpty());
    }

    private void assertItemVisible(String itemId)
    {
        assertNotNull("item " + itemId + " should be visible", driver.findElement(By.id(itemId)));
        checkVisuals(itemId);
    }

    abstract void checkVisuals(String itemId);
}
