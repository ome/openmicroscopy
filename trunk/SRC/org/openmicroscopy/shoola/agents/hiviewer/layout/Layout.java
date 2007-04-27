/*
 * org.openmicroscopy.shoola.agents.hiviewer.layout.Layout
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

package org.openmicroscopy.shoola.agents.hiviewer.layout;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;

/** 
 * Interface for layout classes.
 * A layout class visits visualization trees and lays their nodes out.
 * This interface extends {@link ImageDisplayVisitor} so that a layout
 * object can be passed to the browser and visit the whole image display.
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
public interface Layout
    extends ImageDisplayVisitor
{
    
    /**
     * Returns a textual description of the layout.
     * Can be used for tooltips.
     * 
     * @return A textual description of the layout.
     */
    public String getDescription();
    
    /** 
     * Returns the index of the layout.
     * 
     * @return See above.
     */
    public int getIndex();
    
    /** Lays out the nodes. */
    public void doLayout();
    
}
