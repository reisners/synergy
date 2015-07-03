package helpers;

import javax.servlet.annotation.WebServlet;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import worlds.PackageTag;

import com.vaadin.data.Container;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.ThemeResource;

import de.syngenio.vaadin.synergy.builder.SynergyBuilder;

public class WorldHelper
{

    public static HierarchicalContainer getNavigationHierarchy() {
        return new SynergyBuilder() {{
            addItem(item().withCaption("First"));
            addItem(
                    item().asGroup().withCaption("Resources").withChildren( 
                            item().asGroup().withCaption("Assets").withChildren(
                                    item().withCaption("Machines").withTargetNavigationState("view/Machines"),
                                    item().withCaption("Real Estate").withTargetNavigationState("view/Real Estate"),
                                    item().withCaption("Patents").withTargetNavigationState("view/Patents")),
                            item().withCaption("People").withTargetNavigationState("view/People")));
            addItem(item().withCaption("Something"));
            addItem(item().asGroup().withCaption("Processes").withChildren(
                    item().withCaption("Core").withTargetNavigationState("view/Core Processes"),
                    item().withCaption("Auxiliary").withTargetNavigationState("view/Auxiliary Processes")));
            addItem(item().withCaption("More"));
        }}.build();
//        return new SynergyBuilder() {{
//            addItem(
//                    button("|Resources").asGroup().withChildren( 
//                            button("|Resources|Assets").asGroup().withChildren(
//                                    button("|Resources|Assets|Machines").withTargetNavigationState("view/Machines"),
//                                    button("|Resources|Assets|Real Estate").withTargetNavigationState("view/Real Estate"),
//                                    button("|Resources|Assets|Patents").withTargetNavigationState("view/Patents")),
//                            button("|Resources|People").withTargetNavigationState("view/People")));
//            addItem(button("|Processes").asGroup().withChildren(
//                    button("|Processes|Core").withTargetNavigationState("view/Core Processes"),
//                    button("|Processes|Auxiliary").withTargetNavigationState("view/Auxiliary Processes")));
//        }}.build();
    }

    public static HierarchicalContainer getImageNavigation()
    {
        return new SynergyBuilder() {{
            addItem(item().withCaption("Bullhorn").withIcon(new ThemeResource("img/bullhorn_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/bullhorn_93f100_64.png")).withTargetNavigationState("view/Bullhorn"));
            addItem(item().withCaption("Bullseye").withIcon(new ThemeResource("img/bullseye_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/bullseye_93f100_64.png")).withTargetNavigationState("view/Bullseye"));
            addItem(item().withCaption("Credit Card").withIcon(new ThemeResource("img/cc_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/cc_93f100_64.png")).withTargetNavigationState("view/Credit Card"));
            addItem(item().withCaption("Desktop").withIcon(new ThemeResource("img/desktop_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/desktop_93f100_64.png")).withTargetNavigationState("view/Desktop"));
            addItem(item().withCaption("Download").withIcon(new ThemeResource("img/download_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/download_93f100_64.png")).withTargetNavigationState("view/Download"));
            addItem(item().withCaption("Money").withIcon(new ThemeResource("img/money_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/money_93f100_64.png")).withTargetNavigationState("view/Money"));
            addItem(item().withCaption("Mortar-Board").withIcon(new ThemeResource("img/mortar-board_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/mortar-board_93f100_64.png")).withTargetNavigationState("view/Mortar-Board"));
            addItem(item().withCaption("Paper-Plane").withIcon(new ThemeResource("img/paper-plane_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/paper-plane_93f100_64.png")).withTargetNavigationState("view/Paper-Plane"));
            addItem(item().withCaption("Paw").withIcon(new ThemeResource("img/paw_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/paw_93f100_64.png")).withTargetNavigationState("view/Paw"));
            addItem(item().withCaption("Rocket").withIcon(new ThemeResource("img/rocket_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/rocket_93f100_64.png")).withTargetNavigationState("view/Rocket"));
            addItem(item().withCaption("Shekel").withIcon(new ThemeResource("img/shekel_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/shekel_93f100_64.png")).withTargetNavigationState("view/Shekel"));
            addItem(item().withCaption("Tachometer").withIcon(new ThemeResource("img/tachometer_ffffff_64.png")).withImageWidth("64px").withImageHeight("64px").withIcon(new ThemeResource("img/tachometer_93f100_64.png")).withTargetNavigationState("view/Tachometer"));
        }}.build();
//        return new SynergyBuilder() {{
//            addItem(image("|Bullhorn").withImageSource("img/bullhorn_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/bullhorn_93f100_64.png").withTargetNavigationState("view/Bullhorn"));
//            addItem(image("|Bullseye").withImageSource("img/bullseye_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/bullseye_93f100_64.png").withTargetNavigationState("view/Bullseye"));
//            addItem(image("|Credit Card").withImageSource("img/cc_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/cc_93f100_64.png").withTargetNavigationState("view/Credit Card"));
//            addItem(image("|Desktop").withImageSource("img/desktop_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/desktop_93f100_64.png").withTargetNavigationState("view/Desktop"));
//            addItem(image("|Download").withImageSource("img/download_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/download_93f100_64.png").withTargetNavigationState("view/Download"));
//            addItem(image("|Money").withImageSource("img/money_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/money_93f100_64.png").withTargetNavigationState("view/Money"));
//            addItem(image("|Mortar-Board").withImageSource("img/mortar-board_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/mortar-board_93f100_64.png").withTargetNavigationState("view/Mortar-Board"));
//            addItem(image("|Paper-Plane").withImageSource("img/paper-plane_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/paper-plane_93f100_64.png").withTargetNavigationState("view/Paper-Plane"));
//            addItem(image("|Paw").withImageSource("img/paw_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/paw_93f100_64.png").withTargetNavigationState("view/Paw"));
//            addItem(image("|Rocket").withImageSource("img/rocket_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/rocket_93f100_64.png").withTargetNavigationState("view/Rocket"));
//            addItem(image("|Shekel").withImageSource("img/shekel_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/shekel_93f100_64.png").withTargetNavigationState("view/Shekel"));
//            addItem(image("|Tachometer").withImageSource("img/tachometer_ffffff_64.png").withImageWidth("64px").withImageHeight("64px").withImageSourceSelected("img/tachometer_93f100_64.png").withTargetNavigationState("view/Tachometer"));
//        }}.build();
    }

    public static Container getHubNavigation()
    {
        return new SynergyBuilder() {{
            // find World UIs
            Reflections reflections = new Reflections(PackageTag.class.getPackage().getName(), new SubTypesScanner(), new TypeAnnotationsScanner());
            for (Class webServletClass : reflections.getTypesAnnotatedWith(WebServlet.class)) {
                WebServlet ws = (WebServlet) webServletClass.getAnnotation(WebServlet.class);
                String path = ws.value()[0];
                addItem(button(path).withCaption(webServletClass.getEnclosingClass().getSimpleName()).withTargetNavigationState(path));
            }
        }}.build();
    }

}
