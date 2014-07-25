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

    public static CountingTree<String> countPaths(Graph graph)
    {
        new GremlinUtil().load()
        
        CountingTree tree = new CountingTree(); 
        Iterator<Vertex> i = graph
        .V
        .filter{it.out().count()==0}
        .iterator();
        while (i.hasNext()) {
            final Vertex b = i.next();
            log.info("processing paths ending in "+b.getProperty(GraphAnalyzer.PROPERTY_SIGNATURE));
            graph
            .V
            .filter{it.in().count() == 0}
            .as("x")
            .out()
            .loop("x"){!it.getObject().getId().equals(b.getId())}
            .filter{it.getId().equals(b.getId())}
            .countingTree(tree){
                Vertex vertex ->
                def props =
                vertex.getPropertyKeys().findAll {!it.startsWith(GraphAnalyzer.PROPERTY_PREFIX)}
                .collectEntries{key -> [(key):String.valueOf(vertex.getProperty(key))]}
                vertex.getProperty(GraphAnalyzer.PROPERTY_SIGNATURE)+(props.size() == 0 ? "" : props.toString())
            }
            .iterate();
        }
        return tree;
    }

    public static void test(Graph graph)
    {
        Iterator<Vertex> i = graph.V().iterator();
        while (i.hasNext()) {
            log.info(i.next().getProperty(GraphAnalyzer.PROPERTY_SIGNATURE));
        }
    }

}
