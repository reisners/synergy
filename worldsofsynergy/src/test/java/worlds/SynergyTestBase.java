package worlds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;

import de.syngenio.vaadin.synergy.Synergy;
import de.syngenio.vaadin.synergy.SynergyView.ItemComponent.State;

public abstract class SynergyTestBase
{
	private static final Logger log = LoggerFactory.getLogger(SynergyTestBase.class);
	
    private static final int IMPLICIT_WAIT_SECONDS = 10;

    private WebDriver driver;

    private String baseUrl;

    private long timeout = 1000;
    private long waitPeriod = 200;

    protected SynergyTestBase()
    {
//        setupFirefox();
        setupPhantomJs();
        log.info("web driver started");
        turnOnImplicitWait();
    }

	private void setupFirefox()
    {
        driver = new FirefoxDriver();
    }

    private void setupPhantomJs()
    {
    	String phantomJsExecutablePath = System.getProperty("webdriver.phantomjs.bin");
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", true);
        dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomJsExecutablePath);
        driver = new PhantomJSDriver(dCaps);
        driver.manage().window().setSize(new Dimension(1400,1000));
    }

    private void turnOnImplicitWait()
    {
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    private void turnOffImplicitWait()
    {
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
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
            dumpHierarchy(hierarchy);
        }

        private void dumpHierarchy(HierarchicalContainer hierarchy) {
        	for (Object itemId : hierarchy.rootItemIds()) {
        		dumpHierarchyItem(hierarchy, 0, itemId);
        	}
		}

        private void dumpHierarchy(HierarchicalContainer hierarchy, Object parentId, int level) {
        	Collection<?> children = hierarchy.getChildren(parentId);
        	if (children != null) {
				for (Object itemId : children) {
	        		dumpHierarchyItem(hierarchy, level, itemId);
	        	}
        	}
		}

		private void dumpHierarchyItem(HierarchicalContainer hierarchy, int level, Object itemId) {
			log.info(Strings.repeat(" ", level)+itemId);
			dumpHierarchy(hierarchy, itemId, level+1);
		}

		protected void exercise()
        {
            reload();
            sleep(2000);
            String windowHandle = driver.getWindowHandles().iterator().next();
            driver.switchTo().window(windowHandle);
            screenshot();
            // check that all root items are visible
            log.info("root items: "+hierarchy.rootItemIds());
            for (Object rootItemId : hierarchy.rootItemIds()) {
                assertItemVisible((String) rootItemId);
            }
            // check that no other items are visible
            for (Object itemId : hierarchy.getItemIds()) {
                if (!hierarchy.isRoot(itemId)) {
                    assertItemNotVisible((String) itemId);
                }
            }

            // click on each
            final List<String> shuffledRootItemIds = new ArrayList<String>((Collection<String>) hierarchy.rootItemIds());
            Collections.shuffle(shuffledRootItemIds);
            for (String itemId : shuffledRootItemIds) {
                exerciseItem(itemId);
            }
            
            // descend into each subhierarchy
            for (String itemId : shuffledRootItemIds) {
                exerciseItemsBelow(itemId);
            }
            
            screenshot();
        }

        private void exerciseItemsBelow(String parentId)
        {
            List<String> shuffledChildItemIds = childrenOf(parentId);
            if (shuffledChildItemIds.isEmpty()) {
                return;
            }
            log.info("exercising children of "+parentId);
            screenshot();
            click(parentId);
            assertItemState(parentId, State.selected);
            Collections.shuffle(shuffledChildItemIds);
            for (String itemId : shuffledChildItemIds) {
                exerciseItem(itemId);
            }
            // descend further
            for (String itemId : shuffledChildItemIds) {
                exerciseItemsBelow(itemId);
            }
        }

        private void exerciseItem(String itemId)
        {
            log.info("exercising item "+itemId);
            assertItemState(itemId, State.unselected);
            screenshot();
            click(itemId);
            assertItemState(itemId, State.selected);
            // if the item has a target navigation state check that the view is visible
            String targetNavigationState = ((Property<String>) hierarchy.getContainerProperty(itemId, Synergy.PROPERTY_TARGET_NAVIGATION_STATE)).getValue();
            if (targetNavigationState != null) {
                String name = targetNavigationState.split("/")[1];
                assertTrue("content should display "+name, driver.findElement(By.id("content")).getText().contains(name));
            }
            // make sure that all "nephews" (children of siblings) of item are invisible
            List<String> siblings = childrenOf((String) hierarchy.getParent(itemId));
            for (String siblingId : siblings) {
                if (!itemId.equals(siblingId)) {
                    assertItemState(siblingId, State.unselected);
                    for (String nephewId : childrenOf(siblingId)) {
                        assertItemNotVisible(nephewId);
                    }
                }
            }

        }


        private void click(String itemId)
        {
            driver.findElement(By.id(itemId)).click();
            sleep(300);
        }

        private List<String> childrenOf(final String parentId)
        {
            List<String> children = new ArrayList<String>();
            for (Object itemId : hierarchy.getItemIds()) {
                if (equals((String) hierarchy.getParent(itemId), parentId)) {
                    children.add((String) itemId);
                }
            }
            return children;
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
    
    public void screenshot() {
    	try {
    		File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
    		File screenshotDir = new File("screenshots");
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
    		File screenshot = new File(screenshotDir, sdf.format(new Date())+".jpg");
			FileUtils.copyFile(scrFile, screenshot);
			log.info("screenshot saved to "+screenshot.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private void assertCssClassSuffix(String itemId, String cssClassSuffix)
    {
        assertWithinTimeout("css class suffix " + cssClassSuffix + " not found on item "+itemId, itemId, predicateHasCssClassSuffix(cssClassSuffix));
    }

    private Predicate<String> predicateHasCssClassSuffix(final String cssClassSuffix)
    {
        return new Predicate<String>() {
            @Override
            public boolean test(String itemId)
            {
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
        turnOffImplicitWait();
        try {
            assertTrue("item " + itemId + " should not exist", driver.findElements(By.id(itemId)).isEmpty());
        } finally {
            turnOnImplicitWait();
        }
    }

    private void assertItemVisible(String itemId)
    {
    	System.out.println("assertItemVisible("+itemId+")");
    	for (WebElement element : driver.findElements(By.xpath("//*"))) {
    		System.out.println(element.getAttribute("id"));
    	}
        assertEquals("item " + itemId + " should exist", 1, driver.findElements(By.id(itemId)).size());
        checkVisuals(itemId);
    }

    abstract void checkVisuals(String itemId);
}
