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
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.syngenio.vaadin.synergy.HierarchicalContainerHelper;
import de.syngenio.vaadin.synergy.HorizontalSynergyLayoutFactory;
import de.syngenio.vaadin.synergy.SynergyView;
import de.syngenio.vaadin.synergy.VerticalSynergyLayoutFactory;

@SuppressWarnings("serial")
@Theme("teleport2")
public class Teleport2UI extends UI {
    private static final Logger log = LoggerFactory.getLogger(Teleport2UI.class);
    
	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = Teleport2UI.class, widgetset = "com.example.teleport2.Teleport2Widgetset")
	public static class Servlet extends VaadinServlet {
	}

	public class UpDownButton extends Button {
	    boolean toggleUp = true;
	    boolean isUp() {return toggleUp;}
	    void setUp(boolean up) {
	        toggleUp = up;
	        setStyleName(toggleUp ? "up": "down");
	    }
	    public UpDownButton(String caption, boolean initialUp) {
	        super(caption);
	        setUp(initialUp);
	    }
	}

    private Panel content;
    private HierarchicalContainer hierarchicalContainer;
    private Item hiVerwaltung;
    private int iOption = 0;

	@Override
	protected void init(VaadinRequest request) {
	    setId("Teleport2UI");
		final VerticalLayout layout = new VerticalLayout();
		layout.setId("vLayout");
		layout.setMargin(true);
		setContent(layout);

		final UpDownButton button = buildUpDownButton();
		button.setId("upDownButton");
		button.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				button.setUp(!button.isUp());
			}
		});
		layout.addComponent(button);
		
        buildNavigationHierarchy();

//        Tree tree = new Tree("My Tree");
//        tree.setContainerDataSource(hc);
//        layout.addComponent(tree);
        SynergyView navTopLevel = new SynergyView(new HorizontalSynergyLayoutFactory(), hierarchicalContainer);
        layout.addComponent(navTopLevel);

        SynergyView nav2ndLevel = new SynergyView(new HorizontalSynergyLayoutFactory(), navTopLevel);
        layout.addComponent(nav2ndLevel);

        navTopLevel.setSubView(nav2ndLevel);
        
        com.vaadin.ui.HorizontalSplitPanel hsplit = new com.vaadin.ui.HorizontalSplitPanel();
        layout.addComponent(hsplit);
        
        SynergyView vsv = new SynergyView(new VerticalSynergyLayoutFactory(), nav2ndLevel);
        vsv.setSizeFull();
        hsplit.setFirstComponent(vsv);
        
        nav2ndLevel.setSubView(vsv);
        
        content = new Panel();
        
        hsplit.setSecondComponent(content);
        
        createAndRegisterViews();
	}

    private void buildNavigationHierarchy()
    {
        hierarchicalContainer = HierarchicalContainerHelper.createHierarchicalContainer();

        Item hiPerson = hierarchicalContainer.addItem("Person");
        hierarchicalContainer.setChildrenAllowed("Person", true);
        hiPerson.getItemProperty(HierarchicalContainerHelper.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view1");

        Item hiPersonSuchen = hierarchicalContainer.addItem("Person.Suchen");
        hierarchicalContainer.setParent("Person.Suchen", "Person");
        hiPersonSuchen.getItemProperty(HierarchicalContainerHelper.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view2");

        Item hiPersonAnlegen = hierarchicalContainer.addItem("Person.Anlegen");
        hierarchicalContainer.setParent("Person.Anlegen", "Person");
        hiPersonAnlegen.getItemProperty(HierarchicalContainerHelper.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view2");

        Item hiPersonAnlegenAnschrift = hierarchicalContainer.addItem("Person.Anlegen.Anschrift");
        hierarchicalContainer.setParent("Person.Anlegen.Anschrift", "Person.Anlegen");
        hiPersonAnlegenAnschrift.getItemProperty(HierarchicalContainerHelper.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view2");

        Item hiPersonAnlegenBonitaeten = hierarchicalContainer.addItem("Person.Anlegen.Bonitäten");
        hierarchicalContainer.setParent("Person.Anlegen.Bonitäten", "Person.Anlegen");
        hiPersonAnlegenBonitaeten.getItemProperty(HierarchicalContainerHelper.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view2");

        Item hiKonto = hierarchicalContainer.addItem("Konto");
        hierarchicalContainer.setChildrenAllowed("Konto", true);
        hiKonto.getItemProperty(HierarchicalContainerHelper.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view3");
        
        hiVerwaltung = hierarchicalContainer.addItem("Verwaltung");
        hiVerwaltung.getItemProperty(HierarchicalContainerHelper.PROPERTY_TARGET_NAVIGATION_STATE).setValue("view4");
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

    public UpDownButton buildUpDownButton()
    {
        return new UpDownButton("Toggle Me", true);
    }

}