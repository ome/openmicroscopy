package org.openmicroscopy.shoola.env.event;

/** Generic to type to represent the completion of an asynchronous operation. A concrete subclass
 * encapsulates the result of the operation.
 * Every <code>ResponseEvent</code> object is linked to the <code>RequestEvent</code> object that 
 * originated it.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

public abstract class ResponseEvent 
    extends AgentEvent {
    
    private RequestEvent    act;
    protected ResponseEvent(RequestEvent act) {
        this.act = act;
    }
    
    public void complete() {
        act.handleCompletion(this);
    }
    
    
}
