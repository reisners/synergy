package de.syngenio.collaboration.data;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.eventbus.EventBus;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeEvent;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;

import de.syngenio.collaboration.Application;
import de.syngenio.collaboration.data.SheetModel.SheetContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@Transactional
public class TestSheetContainer
{
    private static Logger LOG = LoggerFactory.getLogger(TestSheetContainer.class);
    
    @Autowired
    private Neo4jTemplate template;

    @Autowired
    private SheetRepository sheetRepository;
    
    @Autowired
    private RepositoryService repository;
    
    @Autowired
    EventBus eventBus;
    
    private String user = "johndoe";

    private SheetModel sheetModel;
   
    private ItemSetChangeListener itemSetChangeListener;

    private PropertySetChangeListener propertySetChangeListener;

    private ValueChangeListener propertyValueChangeListener;

    public Update getHead()
    {
        return sheetModel.getSheet().getHead();
    }

    @Before
    public void setup() {
        sheetRepository.deleteAll();
        Sheet sheet = sheetRepository.save(new Sheet("MySheet"));
        sheetModel = new SheetModel(repository, sheet);
        eventBus.register(sheetModel);
        
        propertyValueChangeListener = new Property.ValueChangeListener() {
            
            @Override
            public void valueChange(ValueChangeEvent event)
            {
                LOG.info("event: value changed to "+event.getProperty().getValue());
                TestHelper.dumpContainer(sheetModel.getContainer());
            }
        };
        itemSetChangeListener = new ItemSetChangeListener() {
            @Override
            public void containerItemSetChange(ItemSetChangeEvent event)
            {
                Container container = event.getContainer();
                addValueChangeListerToContainerProperties(container);
                LOG.info("event: container item set changed:");
                TestHelper.dumpContainer(container);
            }
        };
        propertySetChangeListener = new PropertySetChangeListener() {
            
            @Override
            public void containerPropertySetChange(PropertySetChangeEvent event)
            {
                final Container container = event.getContainer();
                addValueChangeListerToContainerProperties(container);
                LOG.info("event: container property set changed:");
                TestHelper.dumpContainer(container);
            }
        };
        ((SheetModel.SheetContainer)sheetModel.getContainer()).addItemSetChangeListener(itemSetChangeListener);
        ((SheetModel.SheetContainer)sheetModel.getContainer()).addPropertySetChangeListener(propertySetChangeListener);
    }
    
    private void addValueChangeListerToContainerProperties(Container container)
    {
        Collection< ? > containerPropertyIds = container.getContainerPropertyIds();
        for (Object itemId : container.getItemIds()) {
            for (Object propertyId : containerPropertyIds) {
                ((Property.ValueChangeNotifier)container.getContainerProperty(itemId, propertyId)).addValueChangeListener(propertyValueChangeListener);
            }
        }
    }
    
    @Test
    public void testManipulatingContainer1() throws MergeConflictException
    {
        Container container = sheetModel.getContainer();
        container.addContainerProperty("column1", Object.class, null);
        container.addContainerProperty("column2", Object.class, null);
        Item row1 = container.addItem("row1");
        Item row2 = container.addItem("row2");
        row1.getItemProperty("column1").setValue("R1C1");
        container.getContainerProperty("row1", "column2").setValue("R1C2");
        TestHelper.dumpContainer(container);
    }
    
    @Test
    public void testManipulatingContainer2() throws MergeConflictException
    {
        SheetContainer container = sheetModel.getContainer();
        for (char c = 'A' ; c <= 'Z'; ++c) {
            container.addContainerProperty(String.valueOf(c), Object.class, null);
        }
        for (int i = 0; i < 20; ++i) {
            Object itemId = container.addItem();
            for (char c = 'A' ; c <= 'Z'; ++c) {
                container.getContainerProperty(itemId, String.valueOf(c)).setValue(String.format("%c%d", c, i));
            }
        }
        container.moveItemBefore(container.getIdByIndex(5), container.getIdByIndex(0));
        TestHelper.dumpContainer(container);
    }
}
