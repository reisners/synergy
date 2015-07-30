package de.syngenio.collaboration.data;

import java.util.List;


/**
 * Given an initial snapshot and a sequence of operations, calculates the resulting snapshot
 */
public class PermutationService
{
    public List<String> update(List<String> snapshot, Iterable<Update> commits) {
        for (Update commit : commits) {
            commit.applyToSnapshot(snapshot);
        }
        return snapshot;
    }
}
