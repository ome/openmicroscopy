/*
 * org.openmicroscopy.shoola.env.event.RequestEvent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.event;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Generic to type to represent a request to execute an asynchronous operation.
 * A concrete subclass encapsulates the actual request.
 * Every <code>RequestEvent</code> object is linked to the processing action 
 * that has to be dispatched upon completion of the asynchronous operation.
 * The <code>RequestEvent</code> class factors out the association as well as 
 * the dispatching logic.The processing action is encapsulated by a class 
 * that implements the <code>CompletionHandler</code> interface.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public abstract class RequestEvent 
    extends AgentEvent
{
    
    /** Reference to the completion Handler. */
    private CompletionHandler completionHandler;
    
    /**
     * Handles the completion of the asynchronous operation.
     * 
     * @param response The response.
     */
    void handleCompletion(ResponseEvent response)
    {
        if (completionHandler != null)
            completionHandler.handle(this, response);
    }
    
    /**
     * Sets the completion handler.
     * 
     * @param cHandler The object to set.
     */
    public void setCompletionHandler(CompletionHandler cHandler)
    {
        completionHandler = cHandler;
    }
    
}
