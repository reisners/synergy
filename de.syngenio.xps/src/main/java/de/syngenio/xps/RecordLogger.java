package de.syngenio.xps;

import java.io.Closeable;

import de.syngenio.xps.XPS.Record;

public interface RecordLogger extends Closeable
{

    public abstract void add(Record record);

}