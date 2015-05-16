package de.syngenio.xps;

import static de.syngenio.xps.GraphConstants.EDGE_LABEL_FOLLOWED_BY;
import static de.syngenio.xps.GraphConstants.EDGE_LABEL_JOIN;
import static de.syngenio.xps.GraphConstants.EDGE_LABEL_SPAWN;
import static de.syngenio.xps.GraphConstants.PROPERTY_CHECKPOINT;
import static de.syngenio.xps.GraphConstants.PROPERTY_ORIGIN;
import static de.syngenio.xps.GraphConstants.PROPERTY_SIGNATURE;
import static de.syngenio.xps.GraphConstants.PROPERTY_TIMESTAMP;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
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

public class GraphUtil
{
    private final static Logger log = LoggerFactory.getLogger(GraphUtil.class);
    
    public static Neo4j2Graph load(ChronicleRecordLogger recordLogger, String graphPath) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        try
        {
            FileUtils.deleteDirectory(new File(graphPath));
        }
        catch (IOException e)
        {
            throw new Error("fatal", e);
        }
        Neo4j2Graph graph;
        graph = new Neo4j2Graph(graphPath);
        
        for (byte[] recordBytes : recordLogger) {
            Record record = Record.deserialize(recordBytes);
            if (log.isTraceEnabled()) {
                log.trace(record.toString());
            }
            loadRecord(record, graph);
        }
        
        return graph;
    }

    private static void loadRecord(Record record, Neo4j2Graph graph)
    {
        if (record instanceof KeyValueRecord) {
            // just add the key value pair to the referenced vertex
            KeyValueRecord keyValueRecord = (KeyValueRecord) record;
            Vertex vertexReference = findNodeVertex(keyValueRecord.getCheckpoint(), graph);
            vertexReference.setProperty(keyValueRecord.getKey(), keyValueRecord.getValue());
            log.debug("added "+keyValueRecord.getKey()+"="+keyValueRecord.getValue()+" to current vertex");
        } else if (record instanceof JoinRecord) {
            Vertex vertex = addVertex(record, graph);
            Checkpoint origin = ((JoinRecord)record).getReferenceCheckpoint();
            Vertex vertexOrigin = findNodeVertex(origin, graph);
            vertex.setProperty(PROPERTY_ORIGIN, origin.toString());
            //find all terminals (DoneRecords) of origin and connect them
            for (Vertex vertexDone : findVerticesDone(vertexOrigin, graph)) {
                vertexDone.addEdge(EDGE_LABEL_JOIN, vertex);
                log.debug("added edge from vertex "+vertexDone.getProperty(PROPERTY_CHECKPOINT)+" to current vertex");
            }
        } else if (record instanceof SplitRecord) {
            Vertex vertex = addVertex(record, graph);
            Checkpoint origin = ((SplitRecord)record).getReferenceCheckpoint();
            Vertex vertexPredecessor = findNodeVertex(origin, graph);
            if (vertexPredecessor == null) {
                throw new AnalysisException("unknown origin "+origin);
            } 
            vertexPredecessor.setProperty(PROPERTY_ORIGIN, origin.toString());
            vertexPredecessor.addEdge(EDGE_LABEL_SPAWN, vertex);
        } else {
            Vertex vertex = addVertex(record, graph);
            if (record instanceof DoneRecord) {
                vertex.setProperty(PROPERTY_ORIGIN, ((DoneRecord)record).getReferenceCheckpoint().toString());
            }
            Checkpoint predecessor = record.getCheckpoint().previous();
            if (predecessor != null) { // e.g. the very first Record does not have a predecessor
                Vertex vertexPredecessor = findNodeVertex(predecessor, graph);
                if (vertexPredecessor == null) {
                    throw new AnalysisException("unknown predecessor "+predecessor+" for record "+record.getCheckpoint()+" (misplaced done record?)");
                } 
                vertexPredecessor.addEdge(EDGE_LABEL_FOLLOWED_BY, vertex);
                if (log.isDebugEnabled()) {
                    log.debug("added edge from vertex predecessor="+predecessor+" to current vertex");
                }
            }
        }
    }

    private static Vertex addVertex(Record record, Neo4j2Graph graph)
    {
        Vertex vertex = graph.addVertex(null);
        vertex.setProperty(PROPERTY_CHECKPOINT, record.getCheckpoint().toString());
        vertex.setProperty(PROPERTY_SIGNATURE, record.getSignature());
        vertex.setProperty(PROPERTY_TIMESTAMP, record.getTimestamp());
        log.debug("added vertex for checkpoint "+record.getCheckpoint()+" signature="+record.getSignature());
        return vertex;
    }

    private static Vertex findNodeVertex(Checkpoint predecessor, Neo4j2Graph graph)
    {
        Iterator<Vertex> i = new GremlinPipeline<Graph, Vertex>().start(graph).V(PROPERTY_CHECKPOINT, predecessor.toString()).iterator();
        return i.hasNext() ? i.next() : null;
    }

    private static List<Vertex> findVerticesDone(Vertex vertexOrigin, Neo4j2Graph graph)
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
}
