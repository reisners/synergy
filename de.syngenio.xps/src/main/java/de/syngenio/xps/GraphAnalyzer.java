package de.syngenio.xps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;

public class GraphAnalyzer
{

    private static final Logger log = LoggerFactory.getLogger(GraphAnalyzer.class);
    
    @SuppressWarnings("serial")
    static class CountingTree<T> extends HashMap<T, CountingTree<T>> {
        private final String id = UUID.randomUUID().toString();
        private AtomicInteger counter = new AtomicInteger(0);
        private final int partitionIndex = 0;

        public CountingTree() {
            super();
        }
        
        @SafeVarargs
        public CountingTree(final java.util.Map.Entry<T, CountingTree<T>>... children)
        {
            this();
            for (final java.util.Map.Entry<T, CountingTree<T>> entry : children) {
                this.put(entry.getKey(), entry.getValue());
            }
        }

        @SafeVarargs
        public CountingTree(final T... children) {
            this();
            for (final T t : children) {
                this.put(t, new CountingTree<T>());
            }
        }

        public AtomicInteger getCounter()
        {
            return counter;
        }

        public String getId()
        {
            return id;
        }

        public int getPartitionIndex()
        {
            return partitionIndex;
        }

        @Override
        public String toString()
        {
            return super.toString()+"#"+counter.get();
        }
        
        public void dump(int indent)
        {
            for (java.util.Map.Entry<T, CountingTree<T>> entry : this.entrySet()) {
                System.out.println(StringUtils.repeat(' ', indent)+entry.getKey()+" : "+entry.getValue().getCounter());
                entry.getValue().dump(indent+2);
            }
        }
    }
    
    public static Result analyze(Neo4j2Graph graph) {
        //count paths through which each terminal vertex can be reached
        return GremlinUtil.countPaths(graph);
    }
    
    static class Result extends HashMap<String, CountingTree<String>> {

        /**
         * This method create a CSV file that can be imported into an R dataframe 
         * and processed into a tree map using gvisTreeMap.
         * @return JSON
         * @throws JSONException
         */
        public void toCSV(File csvFile) throws IOException
        {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
                writer.write("id,parent,size,color\n");
                Set<String> uniqueIds = new HashSet<String>();
                toCSV(writer, uniqueIds, "root", null);
            }
        }
        
        private void toCSV(BufferedWriter writer, Set<String> uniqueIds, String id, String parentId) throws IOException
        {
            for (java.util.Map.Entry<String, CountingTree<String>> entry : this.entrySet()) {
                CountingTree<String> countingTree = entry.getValue();
                int i = 0;
                String id1 = id+" *"+String.valueOf(countingTree.getCounter());
                while (uniqueIds.contains(id1)) {
                    ++i;
                    id1 = id+"."+i+" *"+String.valueOf(countingTree.getCounter());
                }
                uniqueIds.add(id1);
                writer.write(String.format("%s,%s,%s,%d\n", id1, parentId != null ? parentId : "", String.valueOf(countingTree.getCounter()), countingTree.getPartitionIndex()));
//                entry.getValue().toCSV(writer, uniqueIds, String.valueOf(entry.getKey()), id1);
            }
        }
        
    }
}
