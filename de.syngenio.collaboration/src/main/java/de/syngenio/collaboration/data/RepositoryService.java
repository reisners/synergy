package de.syngenio.collaboration.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.eventbus.EventBus;

import de.syngenio.collaboration.ui.AppUI;

@Component
public class RepositoryService
{
    private static Logger LOG = LoggerFactory.getLogger(RepositoryService.class);
    
    @Autowired
    Neo4jTemplate template;
    
    @Autowired
    GraphDatabaseService graphDb;
    
    @Autowired
    private SheetRepository sheetRepository;
    
    @Autowired
    EventBus eventBus;
    
    public interface HeadUpdateListener {
        void headUpdated(Update newHead);
    }
    
    private Map<Sheet, Collection<HeadUpdateListener>> sheetListenersMap = new HashMap<Sheet, Collection<HeadUpdateListener>>();

    public void addHeadUpdateListener(Sheet sheet, HeadUpdateListener listener) {
        Collection<HeadUpdateListener> listeners = sheetListenersMap.get(sheet);
        if (listeners == null) {
            listeners = new ArrayList<HeadUpdateListener>();
            sheetListenersMap.put(sheet, listeners);
        }
        listeners.add(listener);
        // send the new listener an update right away
        template.fetch(sheet.getHead());
        listener.headUpdated(sheet.getHead());
    }

    public void removeHeadUpdateListener(Sheet sheet, HeadUpdateListener listener)
    {
        Collection<HeadUpdateListener> listeners = sheetListenersMap.get(sheet);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private void notifyListeners(Sheet sheet)
    {
        eventBus.post(sheet);
    }

    public RepositoryService()
    {
    }

    public class CommitInfo {
        private Update headBefore;
        private Update headAfter;
        private long started = System.currentTimeMillis();
        private long ended;
        private List<MergeConflictException> conflicts = new ArrayList<MergeConflictException>();

        public CommitInfo(Update head)
        {
            headBefore = head;
            headAfter = head;
        }

        public List<MergeConflictException> getConflicts()
        {
            return conflicts;
        }

        /**
         * Clones a {@code Commit} and chains it into the transaction
         * @param newCommit
         */
        public void add(Update newCommit)
        {
            headAfter = newCommit.clone(headAfter);
            ended = System.currentTimeMillis();
        }

        public long getStarted()
        {
            return started;
        }

        public long getEnded()
        {
            return ended;
        }

        Update getHeadBefore()
        {
            return headBefore;
        }

        Update getHeadAfter()
        {
            return headAfter;
        }

        
    }
    
    /**
     * Takes a new commit chained with zero or more other new commits and finally chained with an existing commit.
     * If the existing commit is still head of its branch, immediately persists the new commits.
     * If not, it performs a merge/rebase operation:
     * <ul>
     * <li>{@code PermutationCommit}s that move/insert/delete a row or column are simply applied on top of the new head</li>
     * <li>{@code CellValueCommit}s that apply to different cells as the already persisted commits are simply applied on top the new head</li>
     * <li>{@code CellValueCommit}s that apply to the same cell as an already persisted commit may be substituted by a 
     * different {@code CellValueCommit} that merges the changes to the cell. However, this only works if the cell type
     * is text. Otherwise, a MergeConflictException is eventually thrown.</li>
     * </ul> 
     * The method processes all commits in the chain and only throws a {@code MergeConflictException} at the very end.
     * @param latestInChain the latest commit, possibly chained with more commits that precede it
     * @return instance of {@code UpdateInfo} with fields headBeforeUpdate, headAfterUpdate and conflicts set
     * @throws MergeConflictException if commits cannot be merged. In this case, the session should inform the user that
     * their changes have been cancelled due to a conflict.
     */
    @Transactional
    public CommitInfo commit(final Update latestInChain) {
        final Sheet sheet = latestInChain.getSheet();
        Node sheetNode = template.getNode(sheet.getId());
        try {
            return performLocked(sheetNode, new Callable<CommitInfo>() {
                @Override
                public CommitInfo call() throws Exception
                {
                    CommitInfo updateInfo = new CommitInfo(sheet.getHead());
                    Update branchingUpdate = findPersistentUpdateInChain(latestInChain);

                    //TODO remove after debugging
                    LOG.info("new updates to be commited:");
                    logChain(latestInChain, branchingUpdate);
                    LOG.info("since branch point "+branchingUpdate.getId()+" already commited updates:");
                    logChain(updateInfo.getHeadBefore(), branchingUpdate);

                    processChain(updateInfo, latestInChain, branchingUpdate);
                    
                    sheet.setHead(updateInfo.headAfter);
                    Sheet savedSheet = sheetRepository.save(sheet);
                    notifyListeners(savedSheet); 
                    return updateInfo;
                }
            });
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private void logChain(Update latestInChain, Update terminal)
    {
        if (latestInChain != null && latestInChain != terminal) {
            logChain(latestInChain.getPrevious(), terminal);
            LOG.info(latestInChain.toString());
        }
    }

    private <T> T performLocked(Node lockNode, Callable<T> callable) throws Exception
    {
        try ( Transaction tx = graphDb.beginTx() ) // this is just a "placebo" transaction that has no effect
        {
            long started = System.currentTimeMillis();
            tx.acquireWriteLock( lockNode );
            long acquired = System.currentTimeMillis();
            LOG.info("acquired lock on "+lockNode.getId()+ " after "+(acquired-started)+"ms");
            T result = callable.call();
            tx.success();
            return result;
        } 
    }

    private void processChain(CommitInfo updateInfo, Update latestInChain, Update branchingCommit)
    {
        if (latestInChain == null || latestInChain == branchingCommit) {
            // nothing to do
            return;
        }
        // process previous commits first
        processChain(updateInfo, latestInChain.getPrevious(), branchingCommit);
        // now process latestInChain
        if (latestInChain.isCellValueCommit()) { // a CellValueCommit is the only possible source of a conflict
            Update persistentCommit = updateInfo.getHeadBefore();
            while (persistentCommit != null && !persistentCommit.equals(branchingCommit)) {
                template.fetch(persistentCommit);
                if (persistentCommit.isCellValueCommit() && persistentCommit.getRowKey().equals(latestInChain.getRowKey()) && persistentCommit.getColumnKey().equals(latestInChain.getColumnKey())) {
                    // oops - conflict!
                    final MergeConflictException e = new MergeConflictException("persistent commit "+persistentCommit+" conflicts with "+latestInChain);
                    LOG.warn("merge conflict detected", e);
                    updateInfo.getConflicts().add(e);
                    return;
                }
                persistentCommit = persistentCommit.getPrevious();
            }
        }
        updateInfo.add(latestInChain);
    }

    private Update findPersistentUpdateInChain(Update latestInChain)
    {
        boolean isPersisted = latestInChain.getId() != null;
        
        if (isPersisted) {
            return latestInChain;
        } else if (latestInChain.getPrevious() != null){
            return findPersistentUpdateInChain(latestInChain.getPrevious());
        } return null;
    }

    @Transactional
    public void fetchChain(Update oldHead, Update latestInChain)
    {
        if (latestInChain != null && !latestInChain.equals(oldHead)) {
            template.fetch(latestInChain);
            fetchChain(oldHead, latestInChain.getPrevious());
            LOG.debug("fetched "+latestInChain);
        }
    }

    @Transactional
    public Sheet findOrCreateSheet(String sheetName)
    {
        Sheet sheet;
        Iterator<Sheet> sheets = sheetRepository.findByName(sheetName).iterator();
        if (sheets.hasNext()) {
            sheet = sheets.next();
        } else {
            sheet = sheetRepository.save(new Sheet(sheetName));
            Update transaction = sheet.getHead();
            transaction = Update.appendOrSwapColumnToEnd(sheet, "user1", transaction, new Date(), "A");
            transaction = Update.appendOrSwapColumnToEnd(sheet, "user1", transaction, new Date(), "B");
            transaction = Update.appendOrSwapRowToEnd(sheet, "user1", transaction, new Date(), "row1");
            transaction = Update.appendOrSwapRowToEnd(sheet, "user1", transaction, new Date(), "row2");
            commit(transaction);
        }
        return sheet;
    }

    public EventBus getEventBus()
    {
        return eventBus;
    }

    @Transactional
    public void requestUpdateFor(Sheet sheet)
    {
        notifyListeners(template.fetch(sheet));
    }
}
