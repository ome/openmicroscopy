/*
 * org.openmicroscopy.shoola.agents.browser.ui.UIWrapper
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
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.Component;

/**
 * Specifies the interface for a UI component that wraps a Browser MVC
 * component (such that events do not necessarily go to that component at
 * the top level).  Appropriate for JInternalFrames or JFrames which wrap
 * the browser views.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2.2
 * @since OME2.2
 */
public interface UIWrapper
{   
    /**
     * Returns the component UI that wraps the browser view.
     * @return See above.
     */
    public Component getRealUI();
    
    /**
     * Indicates that the wrapper has been opened.  To be triggered by
     * the wrapper's native open notification event.
     */
    public void wrapperOpened();
    
    /**
     * Indicates that the wrapper has been selected.  To be triggered by
     * the wrapper's native selection notification event.
     */
    public void wrapperSelected();
    
    /**
     * Indicates that the wrapper has been closed.  To be triggered by
     * the wrapper's native closing notification event.
     */
    public void wrapperClosed();
    
    /**
     * Selects the specified wrapper.
     */
    public void select();
}
