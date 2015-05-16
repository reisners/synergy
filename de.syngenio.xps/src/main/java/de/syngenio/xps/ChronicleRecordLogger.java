package de.syngenio.xps;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.higherfrequencytrading.chronicle.Excerpt;
import com.higherfrequencytrading.chronicle.impl.IndexedChronicle;
import com.higherfrequencytrading.chronicle.tools.ChronicleTools;

import de.syngenio.xps.XPS.Record;

public class ChronicleRecordLogger implements RecordLogger, Iterable<byte[]>
{
    private final static Logger log = LoggerFactory.getLogger(ChronicleRecordLogger.class);
    private String chroniclePath;
    private IndexedChronicle chronicle;
    private Excerpt excerpt;
    private long maxIndex = -1;
    
    public ChronicleRecordLogger() throws IOException {
        File tempDir = Files.createTempDir();
        chroniclePath = tempDir.getAbsolutePath()+File.separatorChar+"chronicle";
        ChronicleTools.deleteOnExit(chroniclePath);

        chronicle = new IndexedChronicle(chroniclePath);
        excerpt = chronicle.createExcerpt();
    }
    
    @Override
    public void add(final Record record)
    {
        byte[] bytes = record.serialize();
        excerpt.startExcerpt(bytes.length+4);
        excerpt.writeInt(bytes.length);
        excerpt.write(bytes);
        excerpt.finish();
        maxIndex = excerpt.index();
        log.info("index "+getMaxIndex()+" written to "+getChroniclePath());
    }

    @Override
    public void close() throws IOException
    {
        chronicle.close();
    }

    public long getMaxIndex() {
        return maxIndex ;
    }

    public String getChroniclePath() {
        return chroniclePath;
    }
    
    public Iterator<byte[]> iterator() {
        try
        {
            return new Iterator<byte[]>() {
                private IndexedChronicle readingChronicle = new IndexedChronicle(getChroniclePath());
                private Excerpt readingExcerpt = readingChronicle.createExcerpt();

                @Override
                public boolean hasNext()
                {
                    boolean hasNext = readingExcerpt.nextIndex() && readingExcerpt.index() <= getMaxIndex();
                    if (!hasNext) {
                        readingChronicle.close();
                    }
                    return hasNext;
                }

                @Override
                public byte[] next()
                {
                    int n = readingExcerpt.readInt();
                    ByteBuffer bb = ByteBuffer.allocate(n);
                    readingExcerpt.read(bb);
                    readingExcerpt.finish();
                    return bb.array();
                }

                @Override
                public void remove()
                {
                    throw new Error("not supported");
                }
            };
        }
        catch (IOException e)
        {
            throw new Error("fatal", e);
        }
    }
}
