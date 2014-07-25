package de.syngenio.xps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

public class TestGraph
{
    private static final String PROP_NODE = "node";

    @Test
    public void test()
    {
        Graph graph = new Neo4j2Graph("test");
        Vertex a = graph.addVertex("a");
        a.setProperty(PROP_NODE, "a");
        System.out.println(a.getPropertyKeys());
        Vertex b = graph.addVertex("b");
        
        Vertex x = graph.getVertex(a.getId());
        GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<>();
        Vertex y = pipe.start(a).filter(new PipeFunction<Vertex, Boolean>() {
            @Override
            public Boolean compute(Vertex argument)
            {
                return argument.getProperty(PROP_NODE).equals("a");
            }
        }).toList().get(0);
        assertEquals(a, x);
        assertEquals(a, y);
    }

}
