package org.openmicroscopy.shoola.env.event;

/** Abstract class from which agents derive concrete classes to represent static 
 * change notifications
 * The <code>stateChange</code> field can be used to carry all state-change informations
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */
public abstract class StateChangeEvent
    extends AgentEvent {
        
    private Object stateChange;
    
    public void setStateChange(Object stateChange) {
        this.stateChange = stateChange;
    }
    
    public Object getStateChange() {
        return stateChange;
    }
   
    
}
