package org.openmicroscopy.shoola.env.event;

/** 
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
