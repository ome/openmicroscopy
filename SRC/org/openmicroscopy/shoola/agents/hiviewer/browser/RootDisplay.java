/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.RootDisplay
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.Colors;

/** 
 * UI component that contains all the visualization trees hosted by the
 * {@link Browser}.
 * This class takes on the role of the browser's View (as in MVC).
 * 
 * @see BrowserModel
 * @see BrowserControl
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
class RootDisplay
    extends ImageSet
{
    
    /**
     * A placeholder to simulate an hierarchy object.
     * This is required because every {@link ImageDisplay} must have one,
     * but it's not actually used.  (It only avoids a <code>NPE</code> in
     * the constructor.)
     */
    static final Object FAKE_HIERARCHY_OBJECT = new Object();
    
    /**
     * Creates the View to use as UI delegate for this component.
     * 
     * @return The UI delegate for this component.
     */
    //protected TinyFrameUI createUIDelegate()
    //{ 
    //    return new TinyFrameStaticUI(this); 
    //}
    
    /**
     * Creates a new root display.
     */
    RootDisplay()  
    {
        super("", FAKE_HIERARCHY_OBJECT);
        Colors colors = Colors.getInstance();
        setHighlight(colors.getColor(Colors.TITLE_BAR));
        setTitleBarType(STATIC_BAR);
        setListenToBorder(false);
    }
    
}
