package de.syngenio.collaboration.data;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public abstract class GraphEntity
{
    @GraphId
    protected Long id;

    public boolean equals(Object other) {
        if (this == other) return true;

        if (id == null) return false;

        if (! (other instanceof GraphEntity)) return false;

        return id.equals(((GraphEntity) other).id);
    }

    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }

    public Long getId()
    {
        return id;
    }
}
