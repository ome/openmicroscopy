/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.ProgressMessageFormatter
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
 
package org.openmicroscopy.shoola.agents.browser.datamodel;

/**
 * Utility for processing formatted progress messages.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ProgressMessageFormatter
{
    /**
     * Replaces the "%n" in the string with the stepsComplete, and the "%t"
     * in the string with the totalSteps.
     * @param message The original message, with %n and %t tokens.
     * @param stepsComplete The number of steps complete. (%n)
     * @param totalSteps The total number of steps (%t)
     * @return The formatted string.
     */
    public static String format(String message, int stepsComplete, int totalSteps)
    {
        message.replaceAll("%n",String.valueOf(stepsComplete));
        message.replaceAll("%t",String.valueOf(totalSteps));
        return message;
    }
}
