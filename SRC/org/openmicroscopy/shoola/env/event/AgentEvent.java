package org.openmicroscopy.shoola.env.event;

/** Ancestor of all classes that represent events
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

public abstract class AgentEvent {
    
    private Object source;
    
    public void setSource(Object source) {
        this.source = source;
    }
    
    public Object getSource() {
        return source;
    }
    
    
}
