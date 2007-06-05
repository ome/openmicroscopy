/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardFactory
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;




//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Factory to create {@link ClipBoard} components.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ClipBoardFactory
{

    /**
     * Creates a new {@link ClipBoard}.
     * 
     * @param parentModel   A reference to the {@link HiViewer} view hosting 
     *                      the {@link ClipBoard} component.
     *                      Mustn't be <code>null</code>.
     * @return See above
     */
    public static ClipBoard createClipBoard(HiViewer parentModel)
    {
        if (parentModel == null)
            throw new NullPointerException("No parent model.");
        ClipBoardModel model = new ClipBoardModel(parentModel);
        ClipBoardComponent component = new ClipBoardComponent(model);
        model.initialize(component);
        component.initialize();
        return component;
    }
    
}
