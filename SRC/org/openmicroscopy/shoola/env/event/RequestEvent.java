/*
 * org.openmicroscopy.shoola.env.event.RequestEvent
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.event;

/** 
 * Generic to type to represent a request to execute an asynchronous operation.
 * A concrete subclass encapsulates the actual request.
 * Every <code>RequestEvent</code> object is linked to the processing action 
 * that has to be dispatched upon completion of the asynchronous operation.
 * The <code>RequestEvent</code> class factors out the association as well as 
 * the dispatching logic. The processing action is encapsulated by a class 
 * that implements the <code>CompletionHandler</code> interface.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *              a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$  $Date$
 * @version 2.2
 * @since OME2.2
 */

public abstract class RequestEvent 
    extends AgentEvent
{
    
    private CompletionHandler completionHandler;
    
    public void setCompletionHandler(CompletionHandler cHandler)
    {
        completionHandler = cHandler;
    }
    
    void handleCompletion(ResponseEvent response)
    {
        completionHandler.handle(this, response);
    }
    
    
}
