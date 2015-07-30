package de.syngenio.collaboration.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractInMemoryContainer;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.PropertysetItem;

import de.syngenio.collaboration.data.RepositoryService.CommitInfo;
import de.syngenio.collaboration.data.SheetModel.SheetContainer.SheetProperty;

public class SheetModel
{
    private static Logger LOG = LoggerFactory.getLogger(SheetModel.class);
    
    private Sheet sheet;
    private String user = "johndoe";
    private SheetContainer container = new SheetContainer();
    private EventList<String> rowSnapshot = new BasicEventList<String>();
    private EventList<String> columnSnapshot = new BasicEventList<String>(); 
    private Update oldHead = null;

    private RepositoryService repository;

    private Update frozenHead = null;

    @SuppressWarnings("serial")
    class SheetContainer extends AbstractInMemoryContainer<String, Object, Item> implements Container.PropertySetChangeNotifier {

        private Map<Object, PropertysetItem> data = new HashMap<Object, PropertysetItem>();
        
        @Override
        public Collection<String> getContainerPropertyIds()
        {
            return Collections.unmodifiableList(columnSnapshot);
        }

        @Override
        public List<String> getAllItemIds()
        {
            return Collections.unmodifiableList(rowSnapshot);
        }


        @Override
        public Property getContainerProperty(Object itemId, Object propertyId)
        {
            return getUnfilteredItem(itemId).getItemProperty(propertyId);
        }

        @Override
        public Class< ? > getType(Object propertyId)
        {
            return Object.class;
        }

        @Override
        protected Item getUnfilteredItem(Object itemId)
        {
            Item item = data.get(itemId);
            return item;
        }

        class SheetProperty extends AbstractProperty<Object> {

            private Object itemId;
            private Object id;
            private Object value = null;

            /**
             * @param itemId
             * @param id
             */
            protected SheetProperty(Object itemId, Object id)
            {
                this.itemId = itemId;
                this.id = id;
            }

            
            @Override
            public String toString()
            {
                return super.toString();
            }


            @Override
            public Object getValue()
            {
                return value;
            }

            @Override
            public void setValue(Object newValue) throws com.vaadin.data.Property.ReadOnlyException
            {
                if (newValue != null && !newValue.equals(value) || newValue == null && value != null) {
                    if (newValue instanceof String) {
                        commit(Update.newTextValue(sheet, null, getFrozenHead(), new Date(), (String)itemId, (String)id, (String)newValue));
                    } else {
                        throw new Error("not implemented yet");
                    }
                }
            }

            @Override
            public Class< ? extends Object> getType()
            {
                return Object.class;
            }

            @Override
            public boolean isReadOnly()
            {
                return false;
            }

            @Override
            public void setReadOnly(boolean newStatus)
            {
                // ignored
            }

            private void internallySetValue(Object newValue)
            {
                if (newValue != null && !newValue.equals(value) || newValue == null && value != null) {
                    value = newValue;
                    
                    fireValueChange();
                    
                    TestHelper.dumpContainer(container);
                }
            }
        }
        
        private String generateItemId()
        {
            return UUID.randomUUID().toString();
        }

        
        @Override
        public Object addItemAt(int index)
        {
            Object newItemId = generateItemId(); 
            return addItemAt(index, newItemId);
        }

        @Override
        public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException
        {
            Object newItemId = generateItemId(); 
            return addItemAfter(previousItemId, newItemId);
        }

        @Override
        public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException
        {
            int index = previousItemId == null ? 0 : rowSnapshot.indexOf(previousItemId);
            if (index == -1) {
                return null;
            }
            return addItemAt(index, newItemId);
        }

        @Override
        public Item addItem(Object itemId) throws UnsupportedOperationException
        {
            return addItemAt(rowSnapshot.size(), itemId);
        }

        @Override
        public Object addItem() throws UnsupportedOperationException
        {
            Object newItemId = generateItemId();
            Item newItem = addItem(newItemId);
            return newItem != null ? newItemId: null;
        }

        @Override
        public Item addItemAt(final int index, final Object newItemId)
        {
            String targetKey = index < rowSnapshot.size() ? rowSnapshot.get(index) : null;
            CommitInfo commitInfo = commit(Update.insertOrMoveBeforeRow(sheet, user, getFrozenHead(), new Date(), (String)newItemId, targetKey ));
            if (!commitInfo.getConflicts().isEmpty()) {
                return null;
            }
            final Item newItem = getUnfilteredItem(newItemId);
            return newItem;
        }

        private PropertysetItem internallyAddItemAt(final int index, final Object newItemId)
        {
            PropertysetItem newItem = new PropertysetItem();
            
            for (Object propertyId : columnSnapshot) {
                newItem.addItemProperty(propertyId, new SheetProperty(newItemId, propertyId));
            }
            
            data.put(newItemId, newItem);
            fireItemAdded(index, (String) newItemId, newItem);
            return newItem;
        }

        @Override
        public boolean removeItem(Object itemId) throws UnsupportedOperationException
        {
            CommitInfo commitInfo = commit(Update.deleteRow(sheet, user, getFrozenHead(), new Date(), (String)itemId));
            if (!commitInfo.getConflicts().isEmpty()) {
                return false;
            }
            fireItemSetChange(new ItemSetChangeEvent() {
                @Override
                public Container getContainer()
                {
                    return SheetContainer.this;
                }
            });
            return true;
        }
        
        private boolean internallyRemoveItem(Object itemId)
        {
            final boolean success = data.remove(itemId) != null;
            if (!success) {
                return false;
            }
            fireItemSetChange(new ItemSetChangeEvent() {
                @Override
                public Container getContainer()
                {
                    return SheetContainer.this;
                }
            });
            return true;
        }

        private void internallyAddContainerProperty(Object propertyId)
        {
            container.data.forEach((rowKey, item) -> {
                SheetProperty property = new SheetProperty(rowKey, propertyId);
                property.setReadOnly(false);
                item.addItemProperty(propertyId, property);
            });
            
            fireContainerPropertySetChange(new PropertySetChangeEvent() {
                @Override
                public Container getContainer()
                {
                    return SheetContainer.this;
                }
            });
        }
        
        private void internallyRemoveContainerProperty(String propertyId)
        {
            container.data.forEach((rowKey, item) -> {
               item.removeItemProperty(propertyId) ;
            });
            
            fireContainerPropertySetChange(new PropertySetChangeEvent() {
                @Override
                public Container getContainer()
                {
                    return container;
                }
            });
        }


        public boolean addContainerProperty(Object propertyId, Class< ? > type, Object defaultValue) throws UnsupportedOperationException
        {
            CommitInfo commitInfo = commit(Update.appendOrSwapColumnToEnd(sheet, user, getFrozenHead(), new Date(), (String) propertyId));
            if (!commitInfo.getConflicts().isEmpty()) {
                return false;
            }
            fireContainerPropertySetChange();
            return true;
        }

        @Override
        public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException
        {
            data.forEach((rowKey, item) -> item.removeItemProperty(propertyId));
            fireContainerPropertySetChange();
            return true;
        }

        @Override
        public void addPropertySetChangeListener(
                Container.PropertySetChangeListener listener) {
            super.addPropertySetChangeListener(listener);
        }

        /**
         * @deprecated As of 7.0, replaced by
         *             {@link #addPropertySetChangeListener(com.vaadin.data.Container.PropertySetChangeListener)}
         **/
        @Deprecated
        @Override
        public void addListener(Container.PropertySetChangeListener listener) {
            addPropertySetChangeListener(listener);
        }

        @Override
        public void removePropertySetChangeListener(
                Container.PropertySetChangeListener listener) {
            super.removePropertySetChangeListener(listener);
        }

        /**
         * @deprecated As of 7.0, replaced by
         *             {@link #removePropertySetChangeListener(com.vaadin.data.Container.PropertySetChangeListener)}
         **/
        @Deprecated
        @Override
        public void removeListener(Container.PropertySetChangeListener listener) {
            removePropertySetChangeListener(listener);
        }

        public boolean moveItemBefore(String sourceId, String targetId)
        {
            CommitInfo commitInfo = commit(Update.insertOrMoveBeforeRow(getSheet(), user, getFrozenHead(), new Date(), sourceId, targetId));
            if (!commitInfo.getConflicts().isEmpty()) {
                return false;
            }
            fireItemSetChange();
            return true;
        }

    }
    
    public SheetModel(RepositoryService repository, Sheet sheet) {
        this.repository = repository;
        this.sheet = sheet;
        
        // add a ListEventListener that will add or remove items from the container according to the modifications to the rowSnapshot
        rowSnapshot.addListEventListener(new ListEventListener<String>() {
            @Override
            public void listChanged(ListEvent<String> listChanges)
            {
                while (listChanges.next()) {
                    final int index = listChanges.getIndex();
                    switch (listChanges.getType()) {
                    case ListEvent.INSERT:
                        container.internallyAddItemAt(index, rowSnapshot.get(index)); // will fire ItemSetChangedEvent
                        break;
                    case ListEvent.DELETE:
                        container.internallyRemoveItem(listChanges.getOldValue()); // will fire ItemSetChangedEvent
                        break;
                    case ListEvent.UPDATE:
                        throw new Error("broken contract");
                    }
                    LOG.info("processed row snapshot change "+listChanges.getType());
                    TestHelper.dumpContainer(container);
                }
            }
        });
        
        // add a ListEventListener that will add or remove properties from the container according to the modifications to the columnSnapshot
        columnSnapshot.addListEventListener(new ListEventListener<String>() {
            @Override
            public void listChanged(ListEvent<String> listChanges)
            {
                while (listChanges.next()) {
                    final int index = listChanges.getIndex();
                    switch (listChanges.getType()) {
                    case ListEvent.INSERT:
                        container.internallyAddContainerProperty(columnSnapshot.get(index)); // will fire PropertySetChangedEvent
                        break;
                    case ListEvent.DELETE:
                        container.internallyRemoveContainerProperty(listChanges.getOldValue()); // will fire PropertySetChangedEvent
                        break;
                    case ListEvent.UPDATE:
                        throw new Error("broken contract");
                    }
                    LOG.info("processed column snapshot change "+listChanges.getType());
                    TestHelper.dumpContainer(container);
                }
            }
        });
        
        repository.getEventBus().register(this);
        
        // replay the sheet's entire history so that the container reflects its current state
        repository.fetchChain(oldHead, sheet.getHead());
        update(sheet.getHead());
        LOG.info("initialized");
    }
    
    public SheetContainer getContainer()
    {
        return container;
    }
    
    Sheet getSheet()
    {
        return sheet;
    }

    @Subscribe
    public void headUpdated(Sheet sheet) {
        try {
            Update newHead = sheet.getHead();
            if (newHead != null) {
                LOG.info("headUpdated: id "+newHead.getId()+", oldHead "+oldHead);
                repository.fetchChain(oldHead, newHead); // completely fetch each of the new Updates 
                update(newHead);
                LOG.info("synchronized");
            }
        } catch (Throwable e) {
            LOG.error("a problem occurred", e);
            throw e;
        }
    }

    /**
     * Update the {@code SheetModel} by incorporating all {@code Update}s between oldHead and lastestInChain.
     * TODO optimize this to ignore irrelevant parts of history:
     * <ul>
     * <li>all value updates preceding the last one</li>
     * <li>row or column permutations preceding a snapshot commit (not implemented yet), 
     * snapshotting the whole permutation state</li>
     * </ul>
     * @param latestInChain
     */
    private void update(Update latestInChain) {
        if (frozenHead != null) {
            LOG.info("update ignored because frozenHead = "+frozenHead);
            return;
        }
        LOG.info("latestInChain = "+latestInChain+", oldHead = "+oldHead);
        if (latestInChain == null || latestInChain.equals(oldHead)) {
            return;
        }
        
        // recurse
        update(latestInChain.getPrevious());
        
        apply(latestInChain);
        oldHead = latestInChain;
        LOG.info("update set oldHead = "+oldHead);
    }

    private void apply(Update latestInChain)
    {
        switch (latestInChain.getType()) {
        case ROW:
            latestInChain.applyToSnapshot(rowSnapshot); // rowSnapshot will fire ListEvents
            break;
        case COLUMN:
            latestInChain.applyToSnapshot(columnSnapshot); // columnSnapshot will fire ListEvents
            break;
        case VALUE_TEXT:
        case VALUE_DATE:
        case VALUE_NUMBER:
            SheetProperty property = (SheetProperty) container.getContainerProperty(latestInChain.getRowKey(), latestInChain.getColumnKey());
            property.internallySetValue(latestInChain.getNewValue());
            break;
        }
    }

    public void freeze()
    {
        frozenHead = oldHead;
        LOG.info("frozenHead = "+frozenHead);
    }
    
    private Update getFrozenHead() {
        if (frozenHead == null) {
            throw new IllegalStateException("freeze() was not called");
        }
        return frozenHead;
    }

    /**
     * Unfreeze the head and then commit the given {@code Update} through the {@code RepositoryService}
     * @param update
     */
    private CommitInfo commit(final Update update)
    {
        frozenHead = null;
        return repository.commit(update);
    }

    public void thaw()
    {
        if (frozenHead != null) {
            repository.requestUpdateFor(sheet);
            frozenHead = null;
        }
    }
}
