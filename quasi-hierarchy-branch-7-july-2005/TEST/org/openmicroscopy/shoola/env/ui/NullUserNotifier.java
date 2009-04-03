/*
 * org.openmicroscopy.shoola.env.ui.NullUserNotifier
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

package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.awt.Component;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies

/** 
 * Implements the {@link UserNotifier} interface to be a Null Object, that is
 * to do nothing.
 * So this implementation has no UI associated with it.
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
public class NullUserNotifier
    implements UserNotifier
{

    /**
     * @see UserNotifier#notifyError(String, String, Throwable)
     */
    public void notifyError(String title, String summary, Throwable detail) {}

    /**
     * @see UserNotifier#notifyError(String, String, String)
     */
    public void notifyError(String title, String summary, String detail) {}

    /**
     * @see UserNotifier#notifyError(String, String, java.awt.Component)
     */
    public void notifyError(String title, String summary, Component c) {}

    /**
     * @see UserNotifier#notifyError(String, String)
     */
    public void notifyError(String title, String message) {}

    /**
     * @see UserNotifier#notifyWarning(String, String, Throwable)
     */
    public void notifyWarning(String title, String summary, Throwable detail) {}

    /**
     * @see UserNotifier#notifyWarning(String, String, String)
     */
    public void notifyWarning(String title, String summary, String detail) {}

    /**
     * @see UserNotifier#notifyWarning(String, String, java.awt.Component)
     */
    public void notifyWarning(String title, String summary, Component c) {}

    /**
     * @see UserNotifier#notifyWarning(String, String)
     */
    public void notifyWarning(String title, String message) {}
    
    /**
     * @see UserNotifier#notifyInfo(String, String)
     */
    public void notifyInfo(String title, String message) {}

    /**
     * @see UserNotifier#notifyInfo(String, String, javax.swing.Icon)
     */
    public void notifyInfo(String title, String message, Icon icon) {}

}
