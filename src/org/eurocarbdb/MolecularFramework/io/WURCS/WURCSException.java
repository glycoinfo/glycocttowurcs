package org.eurocarbdb.MolecularFramework.io.WURCS;


/**
 * Class for WURCS exception
 * @author masaaki
 *
 */
public class WURCSException extends Exception
{
    protected String m_strMessage;

    /**
     * @param message
     */
    public WURCSException(String a_strMessage)
    {
        super(a_strMessage);
        this.m_strMessage = a_strMessage;
    }

    /**
     * @param message
     */
    public WURCSException(String a_strMessage,Throwable a_objThrowable)
    {
        super(a_strMessage,a_objThrowable);
        this.m_strMessage = a_strMessage;
    }

    public String getErrorMessage()
    {
        return this.m_strMessage;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

}