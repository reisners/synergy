package de.syngenio.xps

import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.groovy.Gremlin
import com.tinkerpop.gremlin.groovy.GroovyPipeFunction
import com.tinkerpop.pipes.Pipe
import com.tinkerpop.pipes.PipeFunction
import com.tinkerpop.pipes.sideeffect.TreePipe;
import com.tinkerpop.pipes.util.FluentUtility;
import com.tinkerpop.pipes.util.structures.Tree

import de.syngenio.xps.GraphAnalyzer.CountingTree

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GremlinUtil
{
    private final static Logger log = LoggerFactory.getLogger(GremlinUtil.class);

    static {
        Gremlin.load()
    }
    
    def load() {
        defineStepCountingTree()
    }
    
    private def defineStepCountingTree() {
        Gremlin.defineStep('countingTree', [Vertex, Pipe], { CountingTree t, p -> new CountingTreePipe(t, new GroovyPipeFunction(p)) })
    }

    public static GraphAnalyzer.Result countPaths(Graph graph)
    {
        new GremlinUtil().load()

        GraphAnalyzer.Result analysisResult = new GraphAnalyzer.Result();
        
        Iterator<Vertex> i = graph
        .V
        .filter{it.out().count()==0}
        .iterator();
        int partitionIndex = 1;
        while (i.hasNext()) {
            final Vertex terminal = i.next();
            String terminalSignature = terminal.getProperty(GraphConstants.PROPERTY_SIGNATURE);
            log.info("processing paths ending in "+terminalSignature);
            CountingTree terminalTree = analysisResult.get(terminalSignature);
            if (terminalTree == null) {
                terminalTree = new CountingTree();
                analysisResult.put(terminalSignature, terminalTree);
            }
            graph
            .V
            .filter{it.in().count() == 0}
            .as("x")
            .out()
            .loop("x"){!it.getObject().getId().equals(terminal.getId())}
            .filter{it.getId().equals(terminal.getId())}
            .countingTree(terminalTree){
                Vertex vertex ->
                def props =
                vertex.getPropertyKeys().findAll {!it.startsWith(GraphConstants.PROPERTY_PREFIX)}
                .collectEntries{key -> [(key):String.valueOf(vertex.getProperty(key))]}
                vertex.getProperty(GraphConstants.PROPERTY_SIGNATURE)+(props.size() == 0 ? "" : props.toString())
            }
            .iterate();
            
            ++partitionIndex;  
        }
        return analysisResult;
    }

    public static void test(Graph graph)
    {
        Iterator<Vertex> i = graph.V().iterator();
        while (i.hasNext()) {
            log.info(i.next().getProperty(GraphConstants.PROPERTY_SIGNATURE));
        }
    }

}
