package de.syngenio.collaboration.data;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import de.syngenio.collaboration.data.Update;
import de.syngenio.collaboration.data.Sheet;

public class TestApplyingPermutationCommitsToSnapshot
{
    Sheet sheet = new Sheet();
    String user = "TESTUSER";
    List<String> snapshot;
    private Update head;
    private Random random = new Random();
    
    @Before
    public void setup() {
        snapshot = Update.cloneSnapshot(Arrays.asList(
                "aedafeb3-e24d-4543-8735-752982972141", 
                "2109be54-c2b6-46a4-973b-e7a7a51d5c95", 
                "0c4678bb-7519-41c2-baf4-781aa9905ffa",
                "e451ea4a-60e9-4b04-99df-1dd6fa5c47e8"
                ));
    }
    
    @Test
    public void testInsertAtEnd()
    {
        assertArrayEquals( new String[] {
                "aedafeb3-e24d-4543-8735-752982972141", 
                "2109be54-c2b6-46a4-973b-e7a7a51d5c95", 
                "0c4678bb-7519-41c2-baf4-781aa9905ffa",
                "e451ea4a-60e9-4b04-99df-1dd6fa5c47e8",
                "e8f87dae-14e9-422c-874a-b73b2bd8cf6c"
                }, 
                permutationCommit("e8f87dae-14e9-422c-874a-b73b2bd8cf6c", null)
                .applyToImmutableSnapshot(snapshot).toArray());
    }

    @Test
    public void testInsertBefore1()
    {
        assertArrayEquals( new String[] {
                "e8f87dae-14e9-422c-874a-b73b2bd8cf6c",
                "aedafeb3-e24d-4543-8735-752982972141", 
                "2109be54-c2b6-46a4-973b-e7a7a51d5c95", 
                "0c4678bb-7519-41c2-baf4-781aa9905ffa",
                "e451ea4a-60e9-4b04-99df-1dd6fa5c47e8"
                }, 
                permutationCommit("e8f87dae-14e9-422c-874a-b73b2bd8cf6c", "aedafeb3-e24d-4543-8735-752982972141")
                .applyToImmutableSnapshot(snapshot).toArray());
    }

    @Test
    public void testInsertBefore2()
    {
        assertArrayEquals( new String[] {
                "aedafeb3-e24d-4543-8735-752982972141", 
                "e8f87dae-14e9-422c-874a-b73b2bd8cf6c",
                "2109be54-c2b6-46a4-973b-e7a7a51d5c95", 
                "0c4678bb-7519-41c2-baf4-781aa9905ffa",
                "e451ea4a-60e9-4b04-99df-1dd6fa5c47e8"
                }, 
                permutationCommit("e8f87dae-14e9-422c-874a-b73b2bd8cf6c", "2109be54-c2b6-46a4-973b-e7a7a51d5c95")
                .applyToImmutableSnapshot(snapshot).toArray());
    }

    @Test
    public void testMoveToEnd()
    {
        assertArrayEquals( new String[] {
                "2109be54-c2b6-46a4-973b-e7a7a51d5c95", 
                "0c4678bb-7519-41c2-baf4-781aa9905ffa",
                "e451ea4a-60e9-4b04-99df-1dd6fa5c47e8",
                "aedafeb3-e24d-4543-8735-752982972141"
                }, 
                permutationCommit("aedafeb3-e24d-4543-8735-752982972141", null)
                .applyToImmutableSnapshot(snapshot).toArray());
    }

    @Test
    public void testMoveToFront()
    {
        assertArrayEquals( new String[] {
                "2109be54-c2b6-46a4-973b-e7a7a51d5c95",
                "aedafeb3-e24d-4543-8735-752982972141", 
                "0c4678bb-7519-41c2-baf4-781aa9905ffa",
                "e451ea4a-60e9-4b04-99df-1dd6fa5c47e8"
                }, 
                permutationCommit("2109be54-c2b6-46a4-973b-e7a7a51d5c95", "aedafeb3-e24d-4543-8735-752982972141")
                .applyToImmutableSnapshot(snapshot).toArray());
    }

    @Test
    public void testMoveToBack()
    {
        assertArrayEquals( new String[] {
                "aedafeb3-e24d-4543-8735-752982972141",
                "0c4678bb-7519-41c2-baf4-781aa9905ffa", 
                "2109be54-c2b6-46a4-973b-e7a7a51d5c95",
                "e451ea4a-60e9-4b04-99df-1dd6fa5c47e8"
                }, 
                permutationCommit("2109be54-c2b6-46a4-973b-e7a7a51d5c95", "e451ea4a-60e9-4b04-99df-1dd6fa5c47e8")
                .applyToImmutableSnapshot(snapshot).toArray());
    }

    @Test
    public void testDelete()
    {
        for (int indexOfObject = 0; indexOfObject < snapshot.size(); ++indexOfObject) {
            List<String> deleted = Update.cloneSnapshot(snapshot);
            deleted.remove(indexOfObject);
            String[] expected = deleted.toArray(new String[0]);
            final String objectKey = snapshot.get(indexOfObject);
            assertArrayEquals(expected, 
            permutationCommit(objectKey, Update.TRASH_KEY)
            .applyToImmutableSnapshot(snapshot).toArray());
        }
    }
    
    @Test
    public void testPerformance()
    {
        long start = System.currentTimeMillis();
        int n = 1000000;
        int maxsize = 0;
        int avgsize = 0;
        for (int i = 0; i < n; ++i) {
            randomOperation().applyToSnapshot(snapshot);
            maxsize = Math.max(maxsize, snapshot.size());
            avgsize += snapshot.size();
        }
        long stop = System.currentTimeMillis();
        System.out.println("applying "+n+" commits took "+(stop-start)+" ms");
        System.out.println("snapshot size: avg "+avgsize/n+", max "+maxsize);
    }

    private Update randomOperation()
    {
        int indexObjectKey = snapshot.isEmpty() ? 0 : random.nextInt(snapshot.size()*2);
        int indexTargetKey = snapshot.isEmpty() ? 0 : random.nextInt(snapshot.size()*2);
        String objectKey = indexObjectKey < snapshot.size() ? snapshot.get(indexObjectKey) : UUID.randomUUID().toString();
        String targetKey = indexTargetKey < snapshot.size() ? snapshot.get(indexTargetKey) : (snapshot.isEmpty() ? null : Update.TRASH_KEY);
        return permutationCommit(objectKey, targetKey);
    }

    private Update permutationCommit(String objectKey, String swapWithObjectKey)
    {
        Update permutationCommit = Update.rowPermutation(sheet, user, head, new Date(), objectKey, swapWithObjectKey);
        head = permutationCommit;
        return permutationCommit;
    }
}
