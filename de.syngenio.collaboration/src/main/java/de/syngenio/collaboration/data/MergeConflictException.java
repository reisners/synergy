package de.syngenio.collaboration.data;

@SuppressWarnings("serial")
public class MergeConflictException extends Exception
{

    public MergeConflictException()
    {
    }

    public MergeConflictException(String message)
    {
        super(message);
    }

    public MergeConflictException(Throwable cause)
    {
        super(cause);
    }

    public MergeConflictException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MergeConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
