package de.syngenio.xps;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

import de.syngenio.xps.XPS.Checkpoint;
import de.syngenio.xps.XPS.DoneRecord;
import de.syngenio.xps.XPS.JoinRecord;
import de.syngenio.xps.XPS.KeyValueRecord;
import de.syngenio.xps.XPS.Record;
import de.syngenio.xps.XPS.SplitRecord;

public class GraphAnalyzer implements Closeable
{
    private static final String PROPERTY_PREFIX = "_!";
    private static final String PROPERTY_CHECKPOINT = PROPERTY_PREFIX+"checkpoint";
    private static final String PROPERTY_TIMESTAMP = PROPERTY_PREFIX+"timestamp";
    private static final String PROPERTY_SIGNATURE = PROPERTY_PREFIX+"signature";
    private static final String PROPERTY_ORIGIN = PROPERTY_PREFIX+"origin";

    private static final Logger log = LoggerFactory.getLogger(GraphAnalyzer.class);
    
    private static final String EDGE_LABEL_JOIN = "join";
    private static final String EDGE_LABEL_SPAWN = "spawn";
    private static final String EDGE_LABEL_FOLLOWED_BY = "followed-by";
    
    private RecordLogger recordLogger;
    private Graph graph;
    
    public GraphAnalyzer(RecordLogger recordLogger, String analysisPath) throws IOException {
        this.recordLogger = recordLogger;
        FileUtils.deleteDirectory(new File(analysisPath));
        graph = new Neo4j2Graph(analysisPath);
    }
    
    @SuppressWarnings("serial")
    static class CountingTree<T> extends HashMap<T, CountingTree<T>> {
        private AtomicInteger counter = new AtomicInteger(0);

        public CountingTree() {
            super();
        }
        
        public CountingTree(java.util.Map.Entry<T, CountingTree<T>>... children)
        {
            this();
            for (final java.util.Map.Entry<T, CountingTree<T>> entry : children) {
                this.put(entry.getKey(), entry.getValue());
            }
        }

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

        public JSONObject toJSON() throws JSONException
        {
            return toJSON(true);
        }
        
        private JSONObject toJSON(boolean topLevel) throws JSONException
        {
            JSONObject json = new JSONObject();
            for (java.util.Map.Entry<T, CountingTree<T>> entry : this.entrySet()) {
                JSONObject subjson = entry.getValue().toJSON(false);
                json.put(String.valueOf(entry.getKey()), subjson);
            }
            if (!topLevel) {
                json.put("#", counter);
            }
            return json;
        }

    }
    
    public CountingTree<String> analyze() {
        buildGraph();

        //count paths through which each terminal vertex can be reached
        return GremlinUtil.countPaths(graph);
    }

    private void dumpCountingTree(CountingTree<String> tree, int indent)
    {
        for (Entry<String, CountingTree<String>> entry : tree.entrySet()) {
            System.out.println(StringUtils.repeat(' ', indent)+entry.getKey()+" : "+entry.getValue().getCounter());
            dumpCountingTree(entry.getValue(), indent+2);
        }
    }

    protected void buildGraph()
    {
        int recordCount = 0;
        for (Record record : recordLogger.getRecords()) {
            if (record instanceof KeyValueRecord) {
                // just add the key value pair to the referenced vertex
                KeyValueRecord keyValueRecord = (KeyValueRecord) record;
                Vertex vertexReference = findNodeVertex(keyValueRecord.getCheckpoint());
                vertexReference.setProperty(keyValueRecord.getKey(), keyValueRecord.getValue());
                log.debug("added "+keyValueRecord.getKey()+"="+keyValueRecord.getValue()+" to current vertex");
            } else if (record instanceof JoinRecord) {
                Vertex vertex = addVertex(record);
                Checkpoint origin = ((JoinRecord)record).getReferenceCheckpoint();
                Vertex vertexOrigin = findNodeVertex(origin);
                vertex.setProperty(PROPERTY_ORIGIN, origin.toString());
                //find all terminals (DoneRecords) of origin and connect them
                for (Vertex vertexDone : findVerticesDone(vertexOrigin)) {
                    vertexDone.addEdge(EDGE_LABEL_JOIN, vertex);
                    log.debug("added edge from vertex "+vertexDone.getProperty(PROPERTY_CHECKPOINT)+" to current vertex");
                }
            } else if (record instanceof SplitRecord) {
                Vertex vertex = addVertex(record);
                Checkpoint origin = ((SplitRecord)record).getReferenceCheckpoint();
                Vertex vertexPredecessor = findNodeVertex(origin);
                if (vertexPredecessor == null) {
                    throw new AnalysisException("unknown origin "+origin);
                } 
                vertexPredecessor.setProperty(PROPERTY_ORIGIN, origin.toString());
                vertexPredecessor.addEdge(EDGE_LABEL_SPAWN, vertex);
            } else {
                Vertex vertex = addVertex(record);
                if (record instanceof DoneRecord) {
                    vertex.setProperty(PROPERTY_ORIGIN, ((DoneRecord)record).getReferenceCheckpoint().toString());
                }
                Checkpoint predecessor = record.getCheckpoint().previous();
                if (predecessor == null) { // e.g. the very first Record does not have a predecessor
                    continue;
                }
                Vertex vertexPredecessor = findNodeVertex(predecessor);
                if (vertexPredecessor == null) {
                    throw new AnalysisException("unknown predecessor "+predecessor+" for record "+record.getCheckpoint()+" (misplaced done record?)");
                } 
                vertexPredecessor.addEdge(EDGE_LABEL_FOLLOWED_BY, vertex);
                if (log.isDebugEnabled()) {
                    log.debug("added edge from vertex predecessor="+predecessor+" to current vertex");
                }
            }
            ++recordCount;
            if (recordCount % 100 == 0) {
                log.info(recordCount+" records processed");
            }
        }
        log.info("all "+recordCount+" records processed");
    }

    protected Vertex addVertex(Record record)
    {
        Vertex vertex = graph.addVertex(null);
        vertex.setProperty(PROPERTY_CHECKPOINT, record.getCheckpoint().toString());
        vertex.setProperty(PROPERTY_SIGNATURE, record.getSignature());
        vertex.setProperty(PROPERTY_TIMESTAMP, record.getTimestamp());
        log.debug("added vertex for checkpoint "+record.getCheckpoint()+" signature="+record.getSignature());
        return vertex;
    }

    private Vertex findNodeVertex(Checkpoint predecessor)
    {
        Iterator<Vertex> i = new GremlinPipeline<Graph, Vertex>().start(graph).V(PROPERTY_CHECKPOINT, predecessor.toString()).iterator();
        return i.hasNext() ? i.next() : null;
    }

    private List<Vertex> findVerticesDone(Vertex vertexOrigin)
    {
        log.debug("findVerticesDone("+vertexOrigin.getProperty(PROPERTY_CHECKPOINT)+")");
        List<Vertex> verticesDone = new ArrayList<Vertex>();
        
        final String origin = vertexOrigin.getProperty(PROPERTY_ORIGIN);
        new GremlinPipeline<Graph, Vertex>()
            .start(graph)
            .V()
            .filter(new PipeFunction<Vertex, Boolean>() {
                @Override
                public Boolean compute(Vertex argument)
                {
                    Object argorigin = argument.getProperty(PROPERTY_ORIGIN);
                    return ((String)argument.getProperty(PROPERTY_SIGNATURE)).startsWith("done")
                            && argorigin != null && argorigin.equals(origin);
                }
            })
            .fill(verticesDone);
        
        log.debug("-->"+verticesDone);
        return verticesDone;
    }

    public void close() throws IOException
    {
        graph.shutdown();
    }
}
