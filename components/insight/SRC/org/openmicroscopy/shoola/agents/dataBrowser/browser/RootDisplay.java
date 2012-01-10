/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.RootDisplay 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.browser;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.Colors;

/** 
 * UI component that contains all the visualization trees hosted by the
 * {@link Browser}.
 * This class takes on the role of the browser's View (as in MVC).
 * 
 * @see BrowserModel
 * @see BrowserControl
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class RootDisplay
	extends ImageSet
{

    /**
     * A place holder to simulate an hierarchy object.
     * This is required because every {@link ImageDisplay} must have one,
     * but it's not actually used.  (It only avoids a <code>NPE</code> in
     * the constructor.)
     */
    static final Object FAKE_HIERARCHY_OBJECT = new Object();
    
    /** Creates a new root display. */
    RootDisplay()  
    {
    	 super("", FAKE_HIERARCHY_OBJECT);
         Colors colors = Colors.getInstance();
         setHighlight(colors.getColor(Colors.TITLE_BAR));
         setTitleBarType(STATIC_BAR);
         setListenToBorder(false);
    }

}
