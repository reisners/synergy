package helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.servlet.annotation.WebServlet;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import worlds.WorldDescription;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.FontIcon;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

import de.syngenio.vaadin.synergy.Synergy;
import de.syngenio.vaadin.synergy.builder.SynergyBuilder;

public class WorldHelper
{
    private final static Logger LOG = LoggerFactory.getLogger(WorldHelper.class);

    public static HierarchicalContainer getNavigationHierarchy()
    {
        return new SynergyBuilder() {
            {
                addItem(item("1").withCaption("First"));
                addItem(group("2")
                        .withCaption("Resources")
                        .withDescription("Click here for Resources navigation choices")
                        .withChildren(
                                group("2.1").withCaption("Assets").withChildren(
                                        item("2.1.1").withCaption("Machines").withIcon(FontAwesome.COGS).withDescription("Click here to navigate to Machines view")
                                                .withTargetNavigationState("view/Machines"),
                                        item("2.1.2").withCaption("Real Estate").withIcon(FontAwesome.HOME)
                                                .withDescription("Click here to navigate to Real Estate view").withTargetNavigationState("view/Real Estate"),
                                        item("2.1.3").withCaption("Patents").withIcon(FontAwesome.SHIELD).withDescription("Click here to navigate to Patents view")
                                                .withTargetNavigationState("view/Patents")),
                                item("2.2").withCaption("People").withIcon(FontAwesome.USERS).withDescription("Click here to navigate to People view")
                                        .withTargetNavigationState("view/People")));
                addItem(item("3").withCaption("Something"));
                addItem(group("4")
                        .withCaption("Processes")
                        .withDescription("Click here for Processes navigation choices")
                        .withChildren(
                                item("4.1").withCaption("Core").withDescription("Click here to navigate to Core Processes view")
                                        .withTargetNavigationState("view/Core Processes"),
                                item("4.2").withCaption("Auxiliary").withDescription("Click here to navigate to Auxiliary Processes view")
                                        .withTargetNavigationState("view/Auxiliary Processes")));
                addItem(item("5").withCaption("More"));
                addItem(group("6")
                        .withCaption("Extras")
                        .withChildren(
                                item("6.1").withCaption("Options")));
            }
        }.build();
    }

    public static HierarchicalContainer getNavigationHierarchyWithStyle()
    {
        return new SynergyBuilder() {
            {
                addItem(group().withCaption("Administration").withChildren(item().withCaption("Bridge"), item().withCaption("Quarters")));
                addItem(group().withCaption("Engineering").withSubviewStyle("engineering")
                        .withChildren(item().withCaption("Reactor"), item().withCaption("Life Support")));
                addItem(group().withCaption("Weapons").withSubviewStyle("weapons")
                        .withChildren(item().withCaption("Phasers"), item().withCaption("Photon Torpedos")));
            }
        }.build();
    }

    public static HierarchicalContainer getImageNavigation()
    {
        return new SynergyBuilder() {
            {
                addItem(item().withCaption("Bookmark").stacked().withIcon(FontAwesome.BOOKMARK).withGlyphSize("2.3em").withIconSelected(FontAwesome.BOOKMARK_O)
                        .withTargetNavigationState("view/Bookmark"));
                addItem(item().withCaption("Bullhorn").withIcon(new ThemeResource("img/bullhorn.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/bullhorn_selected.png")).withTargetNavigationState("view/Bullhorn"));
                addItem(item().withCaption("Bullseye").withIcon(new ThemeResource("img/bullseye.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/bullseye_selected.png")).withTargetNavigationState("view/Bullseye"));
                addItem(item().withCaption("Credit Card").withIcon(new ThemeResource("img/cc.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/cc_selected.png")).withTargetNavigationState("view/Credit Card"));
                addItem(item().withCaption("Desktop").withIcon(new ThemeResource("img/desktop.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/desktop_selected.png")).withTargetNavigationState("view/Desktop"));
                addItem(item().withCaption("Download").withIcon(new ThemeResource("img/download.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/download_selected.png")).withTargetNavigationState("view/Download"));
                addItem(item().withCaption("Money").withIcon(new ThemeResource("img/money.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/money_selected.png")).withTargetNavigationState("view/Money"));
                addItem(item().withCaption("Mortar-Board").withIcon(new ThemeResource("img/mortar-board.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/mortar-board_selected.png")).withTargetNavigationState("view/Mortar-Board"));
                addItem(item().withCaption("Paper-Plane").withIcon(new ThemeResource("img/paper-plane.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/paper-plane_selected.png")).withTargetNavigationState("view/Paper-Plane"));
                addItem(item().withCaption("Paw").withIcon(new ThemeResource("img/paw.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/paw_selected.png")).withTargetNavigationState("view/Paw"));
                addItem(item().withCaption("Rocket").withIcon(new ThemeResource("img/rocket.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/rocket_selected.png")).withTargetNavigationState("view/Rocket"));
                addItem(item().withCaption("Shekel").withIcon(new ThemeResource("img/shekel.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/shekel_selected.png")).withTargetNavigationState("view/Shekel"));
                addItem(item().withCaption("Tachometer").withIcon(new ThemeResource("img/tachometer.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/tachometer_selected.png")).withTargetNavigationState("view/Tachometer"));
            }
        }.build();
    }

    public static HierarchicalContainer getImageNavigation2()
    {
        return new SynergyBuilder() {
            {
                addItem(item().withCaption("Bookmark").stacked().withIcon(FontAwesome.BOOKMARK).withGlyphSize("2.3em").withIconSelected(FontAwesome.BOOKMARK_O)
                        .withTargetNavigationState("view/Bookmark"));
                addItem(item().withCaption("Bullhorn").withIcon(new ThemeResource("img/bullhorn.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/bullhorn_selected.png")).withTargetNavigationState("view/Bullhorn"));
                addItem(item().withCaption("Bullseye").withIcon(new ThemeResource("img/bullseye.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/bullseye_selected.png")).withTargetNavigationState("view/Bullseye"));
                addItem(item().withCaption("Credit Card").withIcon(new ThemeResource("img/cc.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/cc_selected.png")).withTargetNavigationState("view/Credit Card"));
                addItem(item().withCaption("Desktop").withIcon(new ThemeResource("img/desktop.png")).withImageWidth("64px").withImageHeight("64px")
                        .withIconSelected(new ThemeResource("img/desktop_selected.png")).withTargetNavigationState("view/Desktop"));
            }
        }.build();
    }

    private static class AlphaShapesCircles implements FontIcon {
        private int codepoint;
        private String fontFamily = "AlphaShapesCirclesFont";
        /**
         * @param codePoint
         */
        protected AlphaShapesCircles(int codePoint)
        {
            this.codepoint = codePoint;
        }

        /**
         * Unsupported: {@link FontIcon} does not have a MIME type and is not a
         * {@link Resource} that can be used in a context where a MIME type would be
         * needed.
         */
        @Override
        public String getMIMEType() {
            throw new UnsupportedOperationException(FontIcon.class.getSimpleName()
                    + " should not be used where a MIME type is needed.");
        }

        @Override
        public String getFontFamily()
        {
            return fontFamily;
        }

        @Override
        public int getCodepoint()
        {
            return codepoint;
        }

        @Override
        public String getHtml()
        {
            return "<span class=\"v-icon\" style=\"font-family: " + fontFamily
                    + ";\">&#x" + Integer.toHexString(codepoint) + ";</span>";
        }
    }
    
    public static HierarchicalContainer getGlyphNavigation()
    {
        return new SynergyBuilder() {
            {
                addItem(item().stacked().withIcon(new AlphaShapesCircles((int)'s')).withGlyphSize("6vh").withDescription("letter \"s\""));
                addItem(item().stacked().withIcon(new AlphaShapesCircles((int)'y')).withGlyphSize("6vh").withDescription("letter \"y\""));
                addItem(item().stacked().withIcon(new AlphaShapesCircles((int)'n')).withGlyphSize("6vh").withDescription("letter \"n\""));
                addItem(item().stacked().withIcon(new AlphaShapesCircles((int)'e')).withGlyphSize("6vh").withDescription("letter \"e\""));
                addItem(item().stacked().withIcon(new AlphaShapesCircles((int)'r')).withGlyphSize("6vh").withDescription("letter \"r\""));
                addItem(item().stacked().withIcon(new AlphaShapesCircles((int)'g')).withGlyphSize("6vh").withDescription("letter \"g\""));
                addItem(item().stacked().withIcon(new AlphaShapesCircles((int)'y')).withGlyphSize("6vh").withDescription("letter \"y\""));
            }
        }.build();
    }

    public static Container getHubNavigation()
    {
        return new SynergyBuilder() {
            {
                // find World UIs
                Reflections reflections = new Reflections(worlds.PackageTag.class.getPackage().getName(), new SubTypesScanner(), new TypeAnnotationsScanner());
                for (Class webServletClass : reflections.getTypesAnnotatedWith(WebServlet.class))
                {
                    WebServlet ws = (WebServlet) webServletClass.getAnnotation(WebServlet.class);
                    String path = ws.value()[0].replaceAll("/\\*$", "");
                    addItem(item(path).withCaption(webServletClass.getEnclosingClass().getSimpleName()).withTargetNavigationState(path));
                }
            }
        }.build();
    }

    public static class WorldBean implements Serializable
    {
        private String name;

        private String description;

        private String path;

        private Set<String> tags;

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getPath()
        {
            return path;
        }

        public Set<String> getTags()
        {
            return tags;
        }
    }

    public static Indexed getWorlds()
    {
        BeanItemContainer<WorldBean> indexed = new BeanItemContainer<WorldBean>(WorldBean.class);
        // find World UIs
        Reflections reflections = new Reflections(worlds.PackageTag.class.getPackage().getName(), new SubTypesScanner(), new TypeAnnotationsScanner());
        for (Class webServletClass : reflections.getTypesAnnotatedWith(WebServlet.class))
        {
            WebServlet ws = (WebServlet) webServletClass.getAnnotation(WebServlet.class);
            String path = ws.value()[0].replaceAll("/\\*$", "");
            WorldBean bean = new WorldBean();
            final Class worldClass = webServletClass.getEnclosingClass();
            WorldDescription description = (WorldDescription) worldClass.getAnnotation(WorldDescription.class);
            if (description == null)
            {
                continue;
            }
            bean.name = worldClass.getSimpleName();
            bean.path = path;
            bean.tags = new HashSet<String>(Arrays.asList(description.tags()));
            bean.description = description.prose();
            indexed.addBean(bean);
        }
        return indexed;
    }

    public static HierarchicalContainer getWorldsNavigation()
    {
        return new SynergyBuilder() {
            {

                // find World UIs
                Reflections reflections = new Reflections(worlds.PackageTag.class.getPackage().getName(), new SubTypesScanner(), new TypeAnnotationsScanner());
                List<Class< ? >> classes = new ArrayList<Class< ? >>(reflections.getTypesAnnotatedWith(WebServlet.class));
                Collections.sort(classes, new Comparator<Class< ? >>() {
                    @Override
                    public int compare(Class< ? > o1, Class< ? > o2)
                    {
                        return o1.getEnclosingClass().getSimpleName().compareTo(o2.getEnclosingClass().getSimpleName());
                    }
                });
                for (Class webServletClass : classes)
                {
                    WebServlet ws = (WebServlet) webServletClass.getAnnotation(WebServlet.class);
                    String path = ws.value()[0].replaceAll("/\\*$", "");
                    final WorldBean bean = new WorldBean();
                    final Class worldClass = webServletClass.getEnclosingClass();
                    WorldDescription description = (WorldDescription) worldClass.getAnnotation(WorldDescription.class);
                    if (description == null)
                    {
                        continue;
                    }
                    if (!description.include()) {
                        continue;
                    }
                    bean.name = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(worldClass.getSimpleName().replaceAll("UI$", "")), ' ');
                    bean.path = path;
                    bean.tags = new HashSet<String>(Arrays.asList(description.tags()));
                    bean.description = description.prose();
                    addItem(item().stacked().withCaption(bean.name).withIcon(FontAwesome.GLOBE).withGlyphSize("2em").withDescription(bean.description)
                            .withTargetNavigationState(bean.path).withAction(new BiConsumer<Item, UI>() {
                                public void accept(Item item, UI ui) {
                                    Property<String> propertyTargetNavigationState = (Property<String>)item.getItemProperty(Synergy.PROPERTY_TARGET_NAVIGATION_STATE);
                                    String targetNavigationState = propertyTargetNavigationState.getValue();
                                    if (targetNavigationState != null)
                                    {
                                        String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
                                        ui.getPage().setLocation(contextPath + bean.getPath());
                                    }
                                }
                            }));
                }
            }
        }.build();
    }

}
