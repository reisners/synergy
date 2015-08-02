package de.syngenio.ontology.testing.failover.graph;


public abstract class IdentifiedElement extends Element
{
    private String id;
    protected IdentifiedElement(String id)
    {
        this.id = id;
    }
    
    public String getId()
    {
        return id;
    }
    
    
}
