package org.openmicroscopy.shoola.env.event;

/** 
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
