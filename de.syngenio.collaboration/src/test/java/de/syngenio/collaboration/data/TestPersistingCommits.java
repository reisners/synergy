package de.syngenio.collaboration.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeEvent;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;

import de.syngenio.collaboration.Application;
import de.syngenio.collaboration.data.RepositoryService.CommitInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
@Transactional
public class TestPersistingCommits
{
    private static Logger LOG = LoggerFactory.getLogger(TestPersistingCommits.class);
    
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
                LOG.info("value changed to "+event.getProperty().getValue());
                TestHelper.dumpContainer(sheetModel.getContainer());
            }
        };
        itemSetChangeListener = new ItemSetChangeListener() {
            @Override
            public void containerItemSetChange(ItemSetChangeEvent event)
            {
                Container container = event.getContainer();
                addValueChangeListerToContainerProperties(container);
                LOG.info("container item set changed:");
                TestHelper.dumpContainer(container);
            }
        };
        propertySetChangeListener = new PropertySetChangeListener() {
            
            @Override
            public void containerPropertySetChange(PropertySetChangeEvent event)
            {
                final Container container = event.getContainer();
                addValueChangeListerToContainerProperties(container);
                LOG.info("container property set changed:");
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
    public void testSomeCommits() throws MergeConflictException
    {
        Sheet sheet = sheetModel.getSheet();
        
        // fresh database, head is still null
        Update transaction1 = getHead();
        
        // create a new row
        transaction1 = Update.insertOrMoveBeforeRow(sheet, user, transaction1, new Date(), "row1", null);
        // create a new column
        transaction1 = Update.insertOrMoveBeforeColumn(sheet, user, transaction1, new Date(), "column1", null);
        // change its text value
        transaction1 = Update.newTextValue(sheet, user, transaction1, new Date(), "row1", "column1", "Hello World!");
        
        CommitInfo result1 = repository.commit(transaction1);
        assertTrue(result1.getConflicts().isEmpty());
        
        // perform a second transaction (now head is no longer null)
        Update transaction2 = getHead();

        // create a new row
        transaction2 = Update.insertOrMoveBeforeRow(sheet, user, transaction2, new Date(), "row2", null);
        // change its number value
        transaction2 = Update.newNumberValue(sheet, user, transaction2, new Date(), "row2", "column1", Math.PI);

        // change text value of (row1,column1) 
        transaction2 = Update.newTextValue(sheet, user, transaction2, new Date(), "row1", "column1", "Hi there");

        // create a new column
        transaction2 = Update.insertOrMoveBeforeColumn(sheet, user, transaction2, new Date(), "column2", "column1");
        
        CommitInfo result2 = repository.commit(transaction2);
        assertTrue(result2.getConflicts().isEmpty());
    }

    @Test
    public void testConflict() throws MergeConflictException
    {
        Sheet sheet = sheetModel.getSheet();

        Update prepare = getHead();
        // create a new row
        prepare = Update.insertOrMoveBeforeRow(sheet, user, prepare, new Date(), "row1", null);
        // create a new column
        prepare = Update.insertOrMoveBeforeColumn(sheet, user, prepare, new Date(), "column1", null);
        CommitInfo prepareCommitInfo = repository.commit(prepare);
        
        Update transaction1 = getHead();
        
        // change its text value
        transaction1 = Update.newTextValue(sheet, user, transaction1, new Date(), "row1", "column1", "Hello World!");
        
        // start a new transaction while transaction1 is still pending
        Update transaction2 = getHead();
        
        // change text value of (row1,column1) 
        transaction2 = Update.newTextValue(sheet, user, transaction2, new Date(), "row1", "column1", "Hi there");
        
        // finish transaction2
        CommitInfo result2 = repository.commit(transaction2);
        List<MergeConflictException> expected = Collections.emptyList(); 
        assertEquals(expected, result2.getConflicts());
        
        // try to finish transaction1
        CommitInfo result1 = repository.commit(transaction1);
        // now we expect a conflict
        assertFalse(result1.getConflicts().isEmpty());

    }
}
