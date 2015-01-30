package com.example.teleport2;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.AbstractSynergyLayoutFactory;
import de.syngenio.vaadin.synergy.SynergyBuilder;
import de.syngenio.vaadin.synergy.HorizontalSynergyLayoutFactory;
import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.SynergyView.ItemComponentButton;
import de.syngenio.vaadin.synergy.SynergyView.ItemComponentImage;
import de.syngenio.vaadin.synergy.VerticalSynergyLayoutFactory;

@SuppressWarnings("serial")
@Theme("teleport2")
public class Teleport2UI extends UI {
    private static final Logger log = LoggerFactory.getLogger(Teleport2UI.class);
    
	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = Teleport2UI.class)
	public static class Servlet extends VaadinServlet {
	}

    private Panel content;
    private HierarchicalContainer hierarchicalContainer;
    private Item hiVerwaltung;
    private int iOption = 0;

	@Override
	protected void init(VaadinRequest request) {
	    setId("Teleport2UI");
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setId("vLayout");
//		layout.setMargin(true);
//		layout.setSpacing(true);
		setContent(layout);

        buildNavigationHierarchy();

//        Tree tree = new Tree("My Tree");
//        tree.setContainerDataSource(hc);
//        layout.addComponent(tree);
        final AbstractSynergyLayoutFactory layoutFactoryTopLevel = new HorizontalSynergyLayoutFactory();
        layoutFactoryTopLevel.setCompactArrangement(false);
        layoutFactoryTopLevel.setStyleName("h1");
        SynergyView navTopLevel = new SynergyView(layoutFactoryTopLevel, hierarchicalContainer);
        navTopLevel.setWidth("100%");
        layout.addComponent(navTopLevel);
        layout.setExpandRatio(navTopLevel, 0);

        final AbstractSynergyLayoutFactory layoutFactory2ndLevel = new HorizontalSynergyLayoutFactory();
        layoutFactory2ndLevel.setStyleName("h2");
        SynergyView nav2ndLevel = new SynergyView(layoutFactory2ndLevel, navTopLevel);
        nav2ndLevel.setWidth("100%");
        layout.addComponent(nav2ndLevel);
        layout.setExpandRatio(nav2ndLevel, 0);

        navTopLevel.setSubView(nav2ndLevel);
        
        Label greenBar = new Label();
        greenBar.setWidth("100%");
        greenBar.setHeight("3px");
        greenBar.setStyleName("greenbar");
        layout.addComponent(greenBar);
        layout.setExpandRatio(greenBar, 0);
        layout.setComponentAlignment(greenBar, Alignment.TOP_CENTER);
        
        com.vaadin.ui.HorizontalSplitPanel hsplit = new com.vaadin.ui.HorizontalSplitPanel();
        layout.addComponent(hsplit);
        layout.setExpandRatio(hsplit, 1);
        
        SynergyView vsv = new SynergyView(new VerticalSynergyLayoutFactory(), nav2ndLevel);
        vsv.setSizeFull();
        hsplit.setFirstComponent(vsv);
        
        nav2ndLevel.setSubView(vsv);
        
        content = new Panel();
        content.setStyleName("content");
        
        hsplit.setSecondComponent(content);
        hsplit.setSplitPosition(20, Unit.PERCENTAGE);
        
        createAndRegisterViews();
	}

    private void buildNavigationHierarchy()
    {
        hierarchicalContainer = SynergyBuilder.createHierarchicalContainer();

        Item hiStammdaten = hierarchicalContainer.addItem("Stammdaten");
        hiStammdaten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CLASS).setValue(ItemComponentImage.class.getName());
        hiStammdaten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_WIDTH).setValue("64px");
        hiStammdaten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_HEIGHT).setValue("64px");
        hiStammdaten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE).setValue("img/service_desk.png");
        hiStammdaten.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED).setValue("img/service_desk_selected.png");

        Item hiBudget = hierarchicalContainer.addItem("Budget");
        hiBudget.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CLASS).setValue(ItemComponentImage.class.getName());
        hiBudget.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_WIDTH).setValue("64px");
        hiBudget.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_HEIGHT).setValue("64px");
        hiBudget.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE).setValue("img/budget.png");
        hiBudget.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED).setValue("img/budget_selected.png");

        Item hiReporting = hierarchicalContainer.addItem("Reporting");
        hiReporting.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CLASS).setValue(ItemComponentImage.class.getName());
        hiReporting.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_WIDTH).setValue("64px");
        hiReporting.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_HEIGHT).setValue("64px");
        hiReporting.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE).setValue("img/reporting.png");
        hiReporting.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_SOURCE_SELECTED).setValue("img/reporting_selected.png");

        Item hiPerson = hierarchicalContainer.addItem("Person");
        hierarchicalContainer.setParent("Person", "Stammdaten");
        hiPerson.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view1");

        Item hiPersonSuchen = hierarchicalContainer.addItem("Person.Suchen");
        hiPersonSuchen.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view2");
        hiPersonSuchen.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CAPTION).setValue("Suchen");
        hierarchicalContainer.setParent("Person.Suchen", "Person");

        Item hiPersonAnlegen = hierarchicalContainer.addItem("Person.Anlegen");
        hiPersonAnlegen.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view2");
        hiPersonAnlegen.getItemProperty(SynergyBuilder.PROPERTY_ITEM_COMPONENT_CAPTION).setValue("Anlegen");
        hierarchicalContainer.setParent("Person.Anlegen", "Person");

        Item hiPersonAnlegenAnschrift = hierarchicalContainer.addItem("Person.Anlegen.Anschrift");
        hierarchicalContainer.setParent("Person.Anlegen.Anschrift", "Person.Anlegen");
        hiPersonAnlegenAnschrift.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view2");

        Item hiPersonAnlegenBonitaeten = hierarchicalContainer.addItem("Person.Anlegen.Bonitäten");
        hierarchicalContainer.setParent("Person.Anlegen.Bonitäten", "Person.Anlegen");
        hiPersonAnlegenBonitaeten.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view2");

        Item hiKonto = hierarchicalContainer.addItem("Konto");
        hierarchicalContainer.setParent("Konto", "Stammdaten");
        hiKonto.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view3");
        
        hiVerwaltung = hierarchicalContainer.addItem("Verwaltung");
        hierarchicalContainer.setParent("Verwaltung", "Stammdaten");
        hiVerwaltung.getItemProperty(SynergyBuilder.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view4");
    }
	
	private class MyView extends CustomComponent implements View {

	    private String name;
	    
        public MyView(String viewName)
        {
            name = viewName;
            VerticalLayout vlayout = new VerticalLayout();
            vlayout.addComponent(new Label(name));
            vlayout.setSpacing(true);
            vlayout.setMargin(true);
            final String option = "Option"+(++iOption);
            Button b = new Button("Add "+option );
            b.addClickListener(new Button.ClickListener() {
                
                @Override
                public void buttonClick(ClickEvent event)
                {
                    hierarchicalContainer.addItem(option);
                }
            });
            vlayout.addComponent(b);
            setCompositionRoot(vlayout);
        }

        @Override
        public void enter(ViewChangeEvent event)
        {
        }
	}
	
    private void createAndRegisterViews()
    {
        // create and register some views to navigate to
        Navigator navigator = new Navigator(UI.getCurrent(), content);
        navigator.addView("", new MyView("view0"));
        for (int i = 1; i <= 5; ++i) {
            String viewName = "view"+i;
            MyView view = new MyView(viewName);
            navigator.addView(viewName, view);
        }
    }

}