/*
 * org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent
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

package org.openmicroscopy.shoola.env.data.events;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.StateChangeEvent;

/** 
 * Notifies of the progress of an asynchronous call to the data services.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DSCallFeedbackEvent
    extends StateChangeEvent
{

    /**
     * Conveys how complete the call is.
     * This is normally a percent value, but can be set to a value outside of
     * the range <code>[0, 100]</code> to signify that the percent value wasn't
     * available at the time this event object was generated.
     */
    private final int     percentDone;
    
    /**
     * A textual description of the current state of the call.
     * Can be <code>null</code> if that information wasn't available at the
     * time this event object was generated.
     */
    private final String  status;
    
    
    /**
     * Creates a new instance.
     * 
     * @param percentDone Percent value to convey how complete the call is.
     *                          Set it to <code>-1</code> if that information
     *                          is not available.
     * @param status A textual description of the current state of the call.
     *                  Pass <code>null</code> if not available.
     */
    public DSCallFeedbackEvent(int percentDone, String status)
    {
        this.percentDone = percentDone;
        this.status = status;
        setStateChange(this);
    }
    
    /**
     * Conveys how complete the call is.
     * This is normally a percent value, but can be set to a value outside of
     * the range <code>[0, 100]</code> to signify that the percent value wasn't
     * available at the time this event object was generated.
     * 
     * @return The percent value.
     */
    public int getPercentDone() { return percentDone; }
    
    /**
     * Returns a textual description of the current state of the call.
     * Can be <code>null</code> if that information wasn't available at the
     * time this event object was generated.
     * 
     * @return See above.
     */
    public String getStatus() { return status; }
    
}
