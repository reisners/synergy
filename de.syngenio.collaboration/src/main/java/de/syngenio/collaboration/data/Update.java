package de.syngenio.collaboration.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Update extends GraphEntity
{
    public enum Type { VALUE_TEXT, VALUE_NUMBER, VALUE_DATE, ROW, COLUMN }
    
    public final static String TRASH_KEY = "TRASH";

    private Type type;
    private Sheet sheet;
    private String user;
    private Update previous;
    private Date timestamp;
    private String rowKey;
    private String columnKey;
    private Type newValueType;
    private String newTextValue;
    private Double newNumberValue;
    private Date newDateValue;
    private String targetKey;

    /**
     * Required by Spring Data
     */
    private Update() {}
    
    /**
     * @param id TODO
     * @param type indicates the type of commit
     * @param sheet {@code Sheet} that this commit belongs to
     * @param user committing user
     * @param previous previous {@code Commit} within the same sheet
     * @param timestamp time of commit
     * @param rowKey key of row that this commit refers to, or null
     * @param columnKey key of column that this commit refers to, or null
     * @param targetKey key of target object (row or column), or null
     * @param newTextValue new text value or null
     * @param newDateValue new date value or null
     * @param newNumberValue new number value of null
     */
    private Update(Long id, Type type, Sheet sheet, String user, Update previous, Date timestamp, String rowKey, String columnKey, String targetKey, String newTextValue, Date newDateValue, Double newNumberValue)
    {
        this.id = id;
        this.type = type;
        this.sheet = sheet;
        this.user = user;
        this.previous = previous;
        this.timestamp = timestamp;
        this.rowKey = rowKey;
        this.columnKey = columnKey;
        this.targetKey = targetKey;
        this.newTextValue = newTextValue;
        this.newDateValue = newDateValue;
        this.newNumberValue = newNumberValue;
    }

    /**
     * Creates a new row permutation commit
     * @see Update
     * @param sheet
     * @param user
     * @param previous
     * @param timestamp
     * @param rowKey the key of the row to move. If this row key was not previously known, an insert operation is assumed
     * @param targetKey the row key where to move the row. null means move/insert at the end. An existing row key means move/insert before that row. The special value {@code Commit.TRASH_KEY} means delete
     */
    public static Update rowPermutation(Sheet sheet, String user, Update previous, Date timestamp, String rowKey, String targetKey)
    {
        return new Update(null, Type.ROW, sheet, user, previous, timestamp, rowKey, null, targetKey, null, null, null);
    }

    /**
     * Creates a new column permutation commit
     * @see Update
     * @param sheet
     * @param user
     * @param previous
     * @param timestamp
     * @param columnKey the key of the column to move. If this column key was not previously known, an insert operation is assumed
     * @param targetKey the column key where to move the column. null means move/insert at the end. An existing column key means move/insert before that column. The special value {@code Commit.TRASH_KEY} means delete
     */
    public static Update columnPermutation(Sheet sheet, String user, Update previous, Date timestamp, String columnKey, String targetKey)
    {
        return new Update(null, Type.COLUMN, sheet, user, previous, timestamp, null, columnKey, targetKey, null, null, null);
    }

    /**
     * Creates a new text cell value commit 
     * @param sheet
     * @param user
     * @param previous
     * @param timestamp
     * @param rowKey
     * @param columnKey
     * @param newTextValue
     */
    public static Update newTextValue(Sheet sheet, String user, Update previous, Date timestamp, String rowKey, String columnKey, String newTextValue)
    {
        return new Update(null, Type.VALUE_TEXT, sheet, user, previous, timestamp, rowKey, columnKey, null, newTextValue, null, null);
    }
    
    /**
     * Creates a new number cell value commit 
     * @param sheet
     * @param user
     * @param previous
     * @param timestamp
     * @param rowKey
     * @param columnKey
     * @param newNumberValue
     */
    public static Update newNumberValue(Sheet sheet, String user, Update previous, Date timestamp, String rowKey, String columnKey, double newNumberValue)
    {
        return new Update(null, Type.VALUE_NUMBER, sheet, user, previous, timestamp, rowKey, columnKey, null, null, null, newNumberValue);
    }

    /**
     * Creates a new date cell value commit 
     * @param sheet
     * @param user
     * @param previous
     * @param timestamp
     * @param rowKey
     * @param columnKey
     * @param newDateValue
     */
    public static Update newDateValue(Sheet sheet, String user, Update previous, Date timestamp, String rowKey, String columnKey, Date newDateValue)
    {
        return new Update(null, Type.VALUE_DATE, sheet, user, previous, timestamp, rowKey, columnKey, null, null, newDateValue, null);
    }


    public Update clone(Update previous)
    {
        return new Update(
                getId(),
                getType(), 
                getSheet(), 
                getUser(), 
                previous, 
                getTimestamp(), 
                getRowKey(), 
                getColumnKey(), 
                getTargetKey(), 
                getNewTextValue(),
                getNewDateValue(), 
                getNewNumberValue());
    }
    
    public Sheet getSheet()
    {
        return sheet;
    }
    public String getUser()
    {
        return user;
    }
    public Update getPrevious()
    {
        return previous;
    }
    
    public Date getTimestamp()
    {
        return timestamp;
    }
    public String getRowKey()
    {
        return rowKey;
    }

    public String getColumnKey()
    {
        return columnKey;
    }

    public Type getNewValueType()
    {
        return newValueType;
    }

    public String getNewTextValue()
    {
        return newTextValue;
    }

    public Double getNewNumberValue()
    {
        return newNumberValue;
    }

    public Date getNewDateValue()
    {
        return newDateValue;
    }

    public String getTargetKey()
    {
        return targetKey;
    }

    public Type getType()
    {
        return type;
    }
    
    /**
     * @param originalSnapshot the snapshot to apply this commit to
     * @return snapshot resulting from applying the commit to originalSnapshot 
     * @throws IllegalArgumentException if snapshot does not contain {@code swapWithObjectKey != PermutationCommit.TRASH_KEY}  
     */
    public List<String> applyToImmutableSnapshot(List<String> originalSnapshot) throws IllegalArgumentException
    {
        if (type != Type.ROW && type != Type.COLUMN) {
            return originalSnapshot;
        }
        List<String> snapshot = cloneSnapshot(originalSnapshot);
        applyToSnapshot(snapshot);
        return snapshot;
    }

    private String getObjectKey() {
        switch (type) {
        case ROW:
            return rowKey;
        case COLUMN:
            return columnKey;
        default:
            throw new IllegalStateException();
        }
    }
    
    /**
     * Modify the given snapshot by applying this update to it. 
     * @param snapshot the snapshot to apply this commit to (will be modified)
     * @throws IllegalArgumentException if snapshot does not contain {@code swapWithObjectKey != PermutationCommit.TRASH_KEY}  
     */
    public void applyToSnapshot(List<String> snapshot)
    {
        if (type != Type.ROW && type != Type.COLUMN) {
            return;
        }
        int index = snapshot.indexOf(getObjectKey());
        int targetIndex = snapshot.indexOf(getTargetKey());
        if (Update.TRASH_KEY.equals(getTargetKey())) { 
            // a delete operation
            if (index != -1) {
                snapshot.remove(index);
            } // otherwise ignore the operation
        } else { 
            // a insert or swap operation
            if (getTargetKey() == null) { // insert at or swap to end
                if (index != -1) { 
                    // move to end, i.e. remove it from its former place before adding it at the end again
                    snapshot.remove(index);
                }
                snapshot.add(getObjectKey());
            } else if (targetIndex != -1) {
                if (index != -1) { 
                    // move to index of targetObjectKey, i.e.
                    // first remove it from its current position
                    snapshot.remove(index);
                    // then re-insert it before targetObjectKey
                    // note: if index < targetIndex, the targetIndex is now one less because it was removed
                    int insertionIndex = targetIndex - (index < targetIndex ? 1 : 0);
                    snapshot.add(insertionIndex, getObjectKey());
                } else {
                    // insert at index of targetObjectKey
                    snapshot.add(targetIndex, getObjectKey());
                }
            } else {
                throw new IllegalArgumentException("unknown target "+type.name()+" "+getTargetKey());
            }
        }
    }

    public static List<String> cloneSnapshot(List<String> originalSnapshot)
    {
        return new ArrayList<String>(originalSnapshot); // does LinkedList perform better? tests so far are inconclusive
    }
    
    public static Update appendOrSwapRowToEnd(Sheet sheet, String user, Update previous, Date timestamp, String rowKey) {
        return rowPermutation(sheet, user, previous, timestamp, rowKey, null);
    }
    
    public static Update appendOrSwapColumnToEnd(Sheet sheet, String user, Update previous, Date timestamp, String columnKey) {
        return columnPermutation(sheet, user, previous, timestamp, columnKey, null);
    }
    
    public static Update deleteRow(Sheet sheet, String user, Update previous, Date timestamp, String rowKey) {
        return rowPermutation(sheet, user, previous, timestamp, rowKey, TRASH_KEY);
    }
    
    public static Update deleteColumn(Sheet sheet, String user, Update previous, Date timestamp, String columnKey) {
        return columnPermutation(sheet, user, previous, timestamp, columnKey, TRASH_KEY);
    }

    /**
     * Inserts a new row or moves an existing row
     * @param sheet
     * @param user
     * @param previous
     * @param timestamp
     * @param rowKey the key of the row to move. If this row key was not previously known, an insert operation is assumed
     * @param targetKey the row key where to move the row. null means move/insert at the end. An existing row key means move/insert before that row.
     */
    public static Update insertOrMoveBeforeRow(Sheet sheet, String user, Update previous, Date timestamp, String rowKey, String targetKey) {
        return rowPermutation(sheet, user, previous, timestamp, rowKey, targetKey);
    }

    public static Update insertOrMoveBeforeColumn(Sheet sheet, String user, Update previous, Date timestamp, String columnKey, String targetKey) {
        return columnPermutation(sheet, user, previous, timestamp, columnKey, targetKey);
    }

    public boolean isCellValueCommit()
    {
        switch (type) {
        case VALUE_DATE:
        case VALUE_NUMBER:
        case VALUE_TEXT:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (id == null) return false;

        if (! (other instanceof Update)) return false;

        return id.equals(((Update) other).id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }
    
    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(clone(null), ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * @return the Update's value (depending on its type) or null if the Update is a row or column permutation
     */
    public Object getNewValue()
    {
        switch (type) {
        case VALUE_DATE:
            return newDateValue;
        case VALUE_NUMBER:
            return newNumberValue;
        case VALUE_TEXT:
            return newTextValue;
        default:
            return null;
        }
    }
    
}
