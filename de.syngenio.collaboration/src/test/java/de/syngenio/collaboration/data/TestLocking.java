package de.syngenio.collaboration.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vaadin.data.Container;

import de.syngenio.collaboration.Application;
import de.syngenio.collaboration.data.RepositoryService.CommitInfo;
import de.syngenio.collaboration.data.RepositoryService.HeadUpdateListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class TestLocking
{
    private static Logger LOG = LoggerFactory.getLogger(TestLocking.class);
    
    @Autowired
    private Neo4jTemplate template;
    
    @Autowired
    private SheetRepository sheetRepository;
    
    @Autowired
    private RepositoryService repository;
    private Sheet sheet;
    private String user = "johndoe";
    
    private HeadUpdateListener listener;
    
    private Container container;
    
    private SheetModel model;
    
    public Update getHead()
    {
        return sheet.getHead();
    }

    @Before
    public void setup() {
        sheetRepository.deleteAll();
        sheet = sheetRepository.save(new Sheet("MySheet"));
        
        model = new SheetModel(repository, sheet);
    }

    @Test
    public void concurrentTransactionsAreSerialized() throws MergeConflictException, InterruptedException, ExecutionException {
        Update setupTransaction = getHead();
        // add a row and three columns
        setupTransaction = Update.insertOrMoveBeforeRow(sheet, user, setupTransaction, new Date(), "row", null);
        setupTransaction = Update.insertOrMoveBeforeColumn(sheet, user, setupTransaction, new Date(), "columnPreceding", null);
        setupTransaction = Update.insertOrMoveBeforeColumn(sheet, user, setupTransaction, new Date(), "columnLong", null);
        setupTransaction = Update.insertOrMoveBeforeColumn(sheet, user, setupTransaction, new Date(), "columnShort", null);
        CommitInfo setupCommitInfo = repository.commit(setupTransaction);
        
        // long and short transaction start off the same head
        Update longTransaction = getHead();
        Update shortTransaction = getHead();
        
        // first perform a transaction with many value updates up front
        Update precedingTransaction = getHead();
        // add columns and set values
        for (int i = 0; i < 10; ++i) {
            precedingTransaction = Update.newNumberValue(sheet, user, precedingTransaction, new Date(), "row", "columnPreceding", i);
        }
        // commit it
        CommitInfo precedingCommitInfo = repository.commit(precedingTransaction);
        
        // perform many updates to (row, column2) in longTransaction
        for (int i = 0; i < 100; ++i) {
            longTransaction = Update.newNumberValue(sheet, user, longTransaction, new Date(), "row", "columnLong", i);
        }
        
        // perform a single update to (row, column2) in shortTransaction
        shortTransaction = Update.newNumberValue(sheet, user, shortTransaction, new Date(), "row", "columnShort", -1);
        
        // construct callables for each commit
        final Update finalLong = longTransaction;
        Callable<CommitInfo> callableLong = () -> repository.commit(finalLong);
        final Update finalShort = shortTransaction;
        Callable<CommitInfo> callableShort = () -> repository.commit(finalShort);
        
        ExecutorService executionService = Executors.newFixedThreadPool(2);
        // start both commits in parallel, giving the long one a head start
        Future<CommitInfo> futureLong = executionService.submit(callableLong);
        Thread.sleep(100);
        Future<CommitInfo> futureShort = executionService.submit(callableShort);
        
        CommitInfo longCommitInfo = futureLong.get();
        CommitInfo shortCommitInfo = futureShort.get();
        
        assertEquals(0, longCommitInfo.getConflicts().size());
        assertEquals(0, shortCommitInfo.getConflicts().size());
        
        // verify that locking worked:
        // the commits did not overlap in time
        assertTrue(shortCommitInfo.getStarted() >= longCommitInfo.getEnded());
        // shortCommitInfo.headBeforeCommit must equal longCommitInfo.headAfterCommit
        assertEquals(longCommitInfo.getHeadAfter(), shortCommitInfo.getHeadBefore());
        // longCommitInfo.headBeforeCommit must equal precedingCommitInfo.headAfterCommit
        assertEquals(precedingCommitInfo.getHeadAfter(), longCommitInfo.getHeadBefore());
    }
}
