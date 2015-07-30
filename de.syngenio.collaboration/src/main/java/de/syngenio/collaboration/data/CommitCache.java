package de.syngenio.collaboration.data;

import java.util.HashMap;
import java.util.Map;

public class CommitCache<C extends Update>
{
    private Map<String, C> lastCommits = new HashMap<String, C>();
    
    C lastCommitFor(String objectKey) {
        return lastCommits.get(objectKey);
    }
    
    void cache(C commit) {
        lastCommits.put(commit.getRowKey(), commit);
    }
}
