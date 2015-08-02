package de.syngenio.ontology.testing.failover.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.syngenio.ontology.testing.failover.graph.Graph.Edge;
import de.syngenio.ontology.testing.failover.graph.Graph.Node;

public class Context
{
    private Map<String, Node> nodes = new HashMap<String, Node>();

    private List<Edge> edges = new ArrayList<Edge>();
    
    public Map<String, Node> getNodes()
    {
        return nodes;
    }

    public List<Edge> getEdges()
    {
        return edges;
    }

    public Node getNode(String id)
    {
        return nodes.get(id);
    }
}