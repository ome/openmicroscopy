package org.openmicroscopy.shoola.env.event;

/** Generic to type to represent a request to execute an asynchronous operation.
 * A concrete subclass encapsulates the actual request.
 * Every <code>RequestEvent</code> object is linked to the processing action that has
 * to be dispatched upon completion of the asynchronous operation.
 * The <code>RequestEvent</code> class factors out the association as well as the dispatching
 * logic. The processing action is encapsulated by a class that implements the 
 * <code>CompletionHandler</code> interface.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

public abstract class RequestEvent 
    extends AgentEvent {
    
    private CompletionHandler completionHandler;
    
    public void setCompletionHandler(CompletionHandler cHandler) {
        completionHandler = cHandler;
    }
    void handleCompletion(ResponseEvent response) {
        completionHandler.handle(this, response);
    }
    
    
}
