package de.syngenio.ontology.testing.failover.graph;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Graph extends IdentifiedElement
{
    private Context context = new Context();

    public Graph(String id) {
        super(id);
    }
    
    public Node getNode(String id) {
        Node node = context.getNode(id);
        if (node != null) {
            return node;
        }
        for (Subgraph subgraph : subgraphs.values()) {
            node = subgraph.getNode(id);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    public Graph graphOf(String id)
    {
        return graphOf(getNode(id));
    }

    public Graph graphOf(Node node)
    {
        if (context.getNodes().containsKey(node.getId())) {
            return this;
        } else {
            for (Subgraph subgraph : subgraphs.values()) {
                Graph graph = subgraph.graphOf(node);
                if (graph != null) {
                    return graph;
                }
            }
        }
        return null;
    }

    public class Edge extends Element {
        private Node from;
        private Node to;
        
        private Edge(Node from, Node to)
        {
            this.from = from;
            this.to = to;
        }
        
        @Override
        public void output(Writer writer) throws IOException {
            writer.append(from.getId()+" -> "+to.getId());
            if (!getAttributes().isEmpty()) {
                writer.append(" [");
                boolean first = true;
                for (Entry<String, Object> entry : getAttributes().entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        writer.append(',');
                    }
                    outputAttribute(writer, entry);
                }
                writer.append("]");
            }
        }
    }
    
    public class Node extends IdentifiedElement {
        protected Node(String id) {
            super(id);
        }
        
        @Override
        public void output(Writer writer) throws IOException {
            writer.append(getId());
            if (!getAttributes().isEmpty()) {
                writer.append(" [");
                boolean first = true;
                for (Entry<String, Object> entry : getAttributes().entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        writer.append(',');
                    }
                    outputAttribute(writer, entry);
                }
                writer.append("]");
            }
        }
    }
    
    public class Subgraph extends Graph {
        public Subgraph(String id)
        {
            super(id);
        }
    }
    
    private Map<String, Subgraph> subgraphs = new HashMap<String, Subgraph>();

    public Node addNode(Node node) {
        if (context.getNodes().containsKey(node.getId())) {
            throw new IllegalArgumentException("graph already contains node with id "+node.getId());
        }
        context.getNodes().put(node.getId(), node);
        return node;
    }
    
    public Node addNode(String id) {
        return addNode(new Node(id));
    }
    
    public Edge addEdge(Edge edge) {
        context.getEdges().add(edge);
        return edge;
    }
    
    public Edge addEdge(Node from, Node to) {
        return addEdge(new Edge(from, to));
    }
    
    public Edge addEdge(String fromId, String toId) {
        final Node from = getNode(fromId);
        if (from == null) {
            throw new IllegalArgumentException("node "+fromId+" not found");
        }
        final Node to = getNode(toId);
        if (to == null) {
            throw new IllegalArgumentException("node "+toId+" not found");
        }
        return addEdge(from, to);
    }
    
    public Subgraph addSubgraph(Subgraph subgraph) {
        subgraphs.put(subgraph.getId(), subgraph);
        return subgraph;
    }
    
    public Subgraph addSubgraph(String id) {
        return addSubgraph(new Subgraph(id));
    }

    public Subgraph getSubgraph(String id)
    {
        return subgraphs.get(id);
    }

    public void output(Writer writer) throws IOException
    {
        writer.append(this.getClass().getSimpleName().toLowerCase()+" "+getId()+" {");
        
        for (Entry<String, Object> entry : getAttributes().entrySet()) {
            outputAttribute(writer, entry);
            writer.append(";\n");
        }

        for (Subgraph subgraph : subgraphs.values()) {
            subgraph.output(writer);
        }
        for (Node node : context.getNodes().values()) {
            node.output(writer);
            writer.append(";\n");
        }
        for (Edge edge : context.getEdges()) {
            edge.output(writer);
            writer.append(";\n");
        }
        writer.append("}\n");
    }
}
