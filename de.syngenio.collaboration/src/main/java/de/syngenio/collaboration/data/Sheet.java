package de.syngenio.collaboration.data;

public class Sheet extends GraphEntity
{
    private String name;

    public Sheet() {}
    
    public Sheet(String name)
    {
        setName(name);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    private Update head;

    public Update getHead()
    {
        return head;
    }

    public void setHead(Update head)
    {
        this.head = head;
    }
}
