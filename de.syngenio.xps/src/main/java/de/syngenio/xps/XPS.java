/***********************************************************************
 * (C)opyright 2014 by syngenio AG München, Germany
 * [All rights reserved]. This product and related documentation are
 * protected by copyright restricting its use, copying, distribution,
 * and decompilation. No part of this product or related documentation
 * may be reproduced in any form by any means without prior written
 * authorization of syngenio or its partners, if any. Unless otherwise
 * arranged, third parties may not have access to this product or
 * related documentation.
 **********************************************************************/

/***********************************************************************
 *    $Author$
 *   $RCSfile$
 *  $Revision$
 *        $Id$
 **********************************************************************/

package de.syngenio.xps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to record execution paths for later quantitative analysis.
 * 
 * The system created here must be able to deal with paths splitting up and joining together again,
 * and it must be able to cope with exceptions occurring anywhere aborting paths before completing normally.
 * Paths may begin in one thread and branch off into another thread. Eventually the path should return to
 * the thread where it originated, but this cannot be guarranteed.
 * Therefore, the recovery of the path graph must rely solely on the information that is saved to the record log. 
 */
public class XPS
{
    private static final Logger log = LoggerFactory.getLogger(XPS.class);
    
    private static final ThreadLocal<XPS> threadXPS= new ThreadLocal<XPS>() {
        protected XPS initialValue() {
            return new XPS();
         }
    };

    public static void configure(RecordLogger recordLogger) {
        init();
        getInstance().setRecordLogger(recordLogger);
    }
    private static void init()
    {
        threadXPS.remove();
    }
    private static XPS getInstance() {
        return threadXPS.get();
    }
    
    private RecordLogger recordLogger = null;
    
    private void setRecordLogger(RecordLogger recordLogger) {
        this.recordLogger = recordLogger;
    }

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private Checkpoint currentCheckpoint = new Checkpoint();
    
    public static class Checkpoint {
        private UUID threadUUID;
        private long chainNo;
        private long iSeq;
        private Checkpoint() {
            threadUUID = UUID.randomUUID();
            chainNo = 0;
            iSeq = -1;
        }
        private Checkpoint(UUID threadUUID, long chainNo, long iSeq) {
            this.threadUUID = threadUUID;
            this.chainNo = chainNo;
            this.iSeq = iSeq;
        }
        public UUID getThreadUUID() {
            return threadUUID;
        }
        public long getChainNo() {
            return chainNo;
        }
        public long getiSeq() {
            return iSeq;
        }
        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this, false);
        }
        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj, false);
        }
        @Override
        public String toString()
        {
            return threadUUID+"-"+chainNo+"-"+iSeq;
        }
        public Checkpoint previous()
        {
            return iSeq > 0 ? new Checkpoint(threadUUID, chainNo, iSeq-1) : null;
        }
        public Checkpoint next()
        {
            return new Checkpoint(threadUUID, chainNo, iSeq+1);
        }
        public Checkpoint nextChain()
        {
            return new Checkpoint(threadUUID, chainNo+1, 0);
        }
        
        public Checkpoint set(String key, Object value) {
            return XPS.set(this, key, value);
        }
        public Checkpoint set(String key, int value)
        {
            return set(key, Integer.valueOf(value));
        }
        public Checkpoint set(String key, double value)
        {
            return set(key, Double.valueOf(value));
        }
    }
    
    private Checkpoint nextCheckpoint() {
        currentCheckpoint = currentCheckpoint.next();
        return currentCheckpoint;
    }
    
    private static Checkpoint set(Checkpoint reference, String key, Object value)
    {
        return getInstance().addKeyValueRecord(reference, key, value);
    }
    
    private Checkpoint addKeyValueRecord(Checkpoint reference, String key, Object value) {
        return add(new KeyValueRecord(currentCheckpoint, reference, key, value));
    }
    
    private Checkpoint newChain() {
        currentCheckpoint = currentCheckpoint.nextChain();
        return currentCheckpoint;
    }
    
    public abstract class Record {
        private transient long timestamp = System.currentTimeMillis();
        private transient Checkpoint checkpoint;
        
        protected Record(Checkpoint node) {
            this.checkpoint = node;
        }
        public Checkpoint getCheckpoint() {
            return checkpoint;
        }
        public long getTimestamp() {
            return timestamp;
        }
        protected abstract String getLabel();
        public String getSignature() {
            return getLabel();
        }

        @Override
        public String toString()
        {
            return checkpoint.toString()+"@"+simpleDateFormat.format(new Date(getTimestamp()))+": "+getSignature();
        }
    }
    
    class PointRecord extends Record {
        private String pointName;

        private PointRecord(Checkpoint node, String pointName) {
            super(node);
            this.pointName = pointName;
        }

        protected String getPointName()
        {
            return pointName;
        }

        public String getLabel()
        {
            return getPointName();
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this, false);
        }

        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj, false);
        }
    }
    
    abstract class ReferenceRecord extends Record {
        private Checkpoint referenceCheckpoint;

        private ReferenceRecord(Checkpoint checkpoint, Checkpoint reference) {
            super(checkpoint);
            this.referenceCheckpoint = reference;
        }
        public Checkpoint getReferenceCheckpoint() {
            return referenceCheckpoint;
        }
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this, false);
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj, false);
        }
    }
    
    class KeyValueRecord extends Record {
        private String key;
        private Object value;
        
        private KeyValueRecord(Checkpoint checkpoint, Checkpoint origin, String key, Object value) {
            super(checkpoint);
            this.key = key;
            this.value = value;
        }
        @Override
        protected String getLabel() {
            return null;
        }
        public String getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
    }
    
    class SplitRecord extends ReferenceRecord {
        private SplitRecord(Checkpoint node, Checkpoint predecessor) {
            super(node, predecessor);
        }
        protected String getLabel() {
            return "split";
        }
    }
    
    class DoneRecord extends ReferenceRecord {
        private DoneRecord(Checkpoint node, Checkpoint origin) {
            super(node, origin);
        }
        protected String getLabel() {
            return "done";
        }
    }
    
    class JoinRecord extends ReferenceRecord {
        private JoinRecord(Checkpoint node, Checkpoint origin) {
            super(node, origin);
        }
        protected String getLabel() {
            return "join";
        }
    }

    public static Checkpoint point(String pointName) {
        return getInstance().addPoint(pointName);
    }
    
    private Checkpoint addPoint(String pointName) {
        return add(new PointRecord(nextCheckpoint(), pointName));
    }
    
    public static Checkpoint split(Checkpoint predecessor) {
        return getInstance().addSplit(predecessor);
    }
    
    private Checkpoint addSplit(Checkpoint predecessor)
    {
        return add(new SplitRecord(newChain(), predecessor));
    }
    
    private Checkpoint add(Record record) {
        recordLogger.add(record);
        if (log.isDebugEnabled()) {
            log.debug(record.toString());
        }
        return record.getCheckpoint();
    }

    public static Checkpoint done(Checkpoint origin)
    {
        return getInstance().addDone(origin);
    }
    
    private Checkpoint addDone(Checkpoint origin)
    {
        return add(new DoneRecord(nextCheckpoint(), origin));
    }
    public static Checkpoint join(Checkpoint origin)
    {
        return getInstance().addJoin(origin);
    }
    
    private Checkpoint addJoin(Checkpoint origin)
    {
        return add(new JoinRecord(nextCheckpoint(), origin));
    }
}
