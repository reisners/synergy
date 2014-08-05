package ${package}.beans;

import java.util.Date;
import java.util.UUID;

public class Task
{
    private String id;
    private String subject;
    private Date dueDate;
    
    /**
     * @param id
     * @param subject
     * @param dueDate
     */
    public Task(String subject, Date dueDate)
    {
        this();
        this.subject = subject;
        this.dueDate = dueDate;
    }
    
    public Task()
    {
        this.id = UUID.randomUUID().toString();
    }
    
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    public String getSubject()
    {
        return subject;
    }
    public void setSubject(String subject)
    {
        this.subject = subject;
    }
    public Date getDueDate()
    {
        return dueDate;
    }
    public void setDueDate(Date dueDate)
    {
        this.dueDate = dueDate;
    }
    
    
}
