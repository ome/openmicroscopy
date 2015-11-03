package omero.gateway.facility;

/**
 * A callback handle for asynchronous method calls
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class Callback {

    /** Cancel request flag */
    private boolean cancelled = false;

    /** Finished flag */
    private boolean finished = false;

    /** Reference to the result object **/
    protected Object result = null;
    
    /**
     * Set the cancel request flag to <code>true</code>
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Returns if the cancel request flag has been set
     * 
     * @return See above
     */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Returns if the call has been finished
     * 
     * @return See above
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Set the result
     * 
     * @param result
     *            The result object
     */
    protected void setResult(Object result) {
        finished = true;
        this.result = result;
        handleResult(result);
    }

    /**
     * Indicate that fatal exception has occurred
     * 
     * @param t
     *            The exception
     */
    protected void setException(Throwable t) {
        finished = true;
        handleException(t);
    }

    /**
     * Override this in order to deal with the result
     * 
     * @param result
     *            The result object
     */
    public void handleResult(Object result) {

    }

    /**
     * Override this in order to deal with an exception
     * 
     * @param t
     *            The exception
     */
    public void handleException(Throwable t) {

    }
}
