package helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinServlet;

import de.syngenio.vaadin.synergy.builder.SynergyBuilder;

public class WorldHelper
{
    private final static Logger LOG = LoggerFactory.getLogger(WorldHelper.class);

    public static HierarchicalContainer getNavigationHierarchy()
    {
        return new SynergyBuilder() {
            {
                addItem(item().withCaption("First"));
                addItem(group()
                        .withCaption("Resources")
                        .withDescription("Click here for Resources navigation choices")
                        .withChildren(
                                group().withCaption("Assets").withChildren(
                                        item().withCaption("Machines").withIcon(FontAwesome.COGS).withDescription("Click here to navigate to Machines view")
                                                .withTargetNavigationState("view/Machines"),
                                        item().withCaption("Real Estate").withIcon(FontAwesome.HOME)
                                                .withDescription("Click here to navigate to Real Estate view").withTargetNavigationState("view/Real Estate"),
                                        item().withCaption("Patents").withIcon(FontAwesome.SHIELD).withDescription("Click here to navigate to Patents view")
                                                .withTargetNavigationState("view/Patents")),
                                item().withCaption("People").withIcon(FontAwesome.USERS).withDescription("Click here to navigate to People view")
                                        .withTargetNavigationState("view/People")));
                addItem(item().withCaption("Something"));
                addItem(group()
                        .withCaption("Processes")
                        .withDescription("Click here for Processes navigation choices")
                        .withChildren(
                                item().withCaption("Core").withDescription("Click here to navigate to Core Processes view")
                                        .withTargetNavigationState("view/Core Processes"),
                                item().withCaption("Auxiliary").withDescription("Click here to navigate to Auxiliary Processes view")
                                        .withTargetNavigationState("view/Auxiliary Processes")));
                addItem(item().withCaption("More"));
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
                    WorldBean bean = new WorldBean();
                    final Class worldClass = webServletClass.getEnclosingClass();
                    WorldDescription description = (WorldDescription) worldClass.getAnnotation(WorldDescription.class);
                    if (description == null)
                    {
                        continue;
                    }
                    bean.name = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(worldClass.getSimpleName().replaceAll("UI$", "")), ' ');
                    bean.path = path;
                    bean.tags = new HashSet<String>(Arrays.asList(description.tags()));
                    bean.description = description.prose();
                    addItem(item().stacked().withCaption(bean.name).withIcon(FontAwesome.GLOBE).withGlyphSize("2em").withDescription(bean.description)
                            .withTargetNavigationState(bean.path).withAction((item, ui) -> {
                                Property<String> propertyTargetNavigationState = item.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE);
                                String targetNavigationState = propertyTargetNavigationState.getValue();
                                if (targetNavigationState != null)
                                {
                                    String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
                                    ui.getPage().setLocation(contextPath + bean.getPath());
                                }
                            }));

                }
            }
        }.build();
    }

}
