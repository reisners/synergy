package helpers;

import com.vaadin.data.util.HierarchicalContainer;

import de.syngenio.vaadin.synergy.SynergyBuilder;

public class WorldHelper
{

    public static HierarchicalContainer getNavigationHierarchy() {
        return new SynergyBuilder() {{
            addItem(
                    button("|Resources").asGroup().withChildren( 
                            button("|Resources|Assets").asGroup().withChildren(
                                    button("|Resources|Assets|Machines").withTargetNavigationState("view/Machines"),
                                    button("|Resources|Assets|Real Estate").withTargetNavigationState("view/Real Estate"),
                                    button("|Resources|Assets|Patents").withTargetNavigationState("view/Patents")),
                            button("|Resources|People").withTargetNavigationState("view/People")));
            addItem(button("|Processes").asGroup().withChildren(
                    button("|Processes|Core").withTargetNavigationState("view/Core Processes"),
                    button("|Processes|Auxiliary").withTargetNavigationState("view/Auxiliary Processes")));
        }}.build();
    }

}
