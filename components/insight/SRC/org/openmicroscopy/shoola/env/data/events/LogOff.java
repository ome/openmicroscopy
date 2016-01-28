/*
 * org.openmicroscopy.shoola.env.data.events.LogOff
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.events;


//Java imports

//Third-party libraries


//Application-internal dependencies
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.RequestEvent;

/** 
 * Event fired by the agents to log off from the current server.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class LogOff 
	extends RequestEvent
{

	/** 
	 * Flag indicating to ask a question to the user before exiting the
	 * application.
	 */
	private boolean askQuestion;

	/** The security context to use to modify the default group.*/
	private SecurityContext ctx;
	
    /** Creates a new instance. */
    public LogOff()
    {
    	this(true);
    }

    /**
     * Creates a new instance.
     * 
     * @param askQuestion Pass <code>true</code> to ask a question before
     * 						closing the application, <code>false</code>
     * 						otherwise.
     */
    public LogOff(boolean askQuestion)
    {
    	this.askQuestion = askQuestion;
    }

    /**
     * Returns <code>true</code> to ask a question before closing the
     * application, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isAskQuestion() { return askQuestion; }

    /**
     * Sets the security context.
     * 
     * @param ctx The value to set.
     */
    public void setSecurityContext(SecurityContext ctx) { this.ctx = ctx; }

    /**
     * Returns the security context.
     * 
     * @return See above.
     */
    public SecurityContext getSecurityContext() { return ctx; }

}
