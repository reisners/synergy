package de.syngenio.ontology.testing.failover.graph;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Element
{

    private Map<String, Object> attributes = new HashMap<String, Object>();

    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    public void set(String key, Object value)
    {
        attributes.put(key, value);
    }

    public void outputAttribute(Writer writer, Entry<String, Object> entry) throws IOException
    {
        writer.append(entry.getKey()+"="+toString(entry.getValue()));
    }
        
    private String toString(Object value) {
        if (value instanceof String) {
            return "\""+value.toString()+"\"";
        } else {
            return value.toString();
        }
    }

    public abstract void output(Writer writer) throws IOException;
}
