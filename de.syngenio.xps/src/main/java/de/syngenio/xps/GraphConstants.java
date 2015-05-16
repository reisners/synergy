package de.syngenio.xps;

public class GraphConstants
{
    static final String PROPERTY_PREFIX = "_!";
    static final String PROPERTY_CHECKPOINT = PROPERTY_PREFIX+"checkpoint";
    static final String PROPERTY_TIMESTAMP = PROPERTY_PREFIX+"timestamp";
    static final String PROPERTY_SIGNATURE = PROPERTY_PREFIX+"signature";
    static final String PROPERTY_ORIGIN = PROPERTY_PREFIX+"origin";
    static final String PROPERTY_PARTITION_INDEX = PROPERTY_PREFIX+"partitionIndex";
    
    static final String EDGE_LABEL_JOIN = "join";
    static final String EDGE_LABEL_SPAWN = "spawn";
    static final String EDGE_LABEL_FOLLOWED_BY = "followed-by";
}
