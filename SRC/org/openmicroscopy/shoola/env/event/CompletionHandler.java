package org.openmicroscopy.shoola.env.event;

/** 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 */

public interface CompletionHandler {
    
    public void handle(RequestEvent request, ResponseEvent response);
    
}
