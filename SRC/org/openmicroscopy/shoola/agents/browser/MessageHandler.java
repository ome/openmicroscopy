/*
 * org.openmicroscopy.shoola.agents.browser.MessageHandler
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface MessageHandler
{
    /**
     * Report an error that was caused by programming mistakes entirely within
     * this OME system.
     * 
     * @param message The error message to send.
     */
    public void reportInternalError(String message);

    /**
     * Report an detail an error that was caused by programming mistakes
     * entirely within the OME system.
     * 
     * @param message The error message to send.
     * @param e The details of the error (Exception thrown, if any)
     */
    public void reportInternalError(String message, Exception e);

    /**
     * Report an error with the specified message.
     * @param message The error message to report.
     */
    public void reportError(String message);

    /**
     * Report an error with the specified message and Exception details.
     * @param message The error message to report.
     * @param e The exception detail to include.
     */
    public void reportError(String message, Exception e);

    /**
     * Warn the user of something with the specified message.
     * @param message The warning message to report.
     */
    public void reportWarning(String message);

    /**
     * Inform the user of something with specified message.
     * @param message The information message to report (could be good!)
     */
    public void reportInformation(String message);
}
