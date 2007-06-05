/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.SaveThumbnailsVisitor
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;



//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Creates an image composed of all thumbnails displayed in the HiViewer.
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
class SaveThumbnailsVisitor
    extends HiViewerVisitor
{

    /** The collection of retrieved {@link Thumbnail}s. */
    private Set thumbnails;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    SaveThumbnailsVisitor(HiViewer model)
    {
        super(model);
        thumbnails = new HashSet();
    }

    /**
     * Returns the collection of retrieved thumbnails.
     * 
     * @return See below.
     */
    Set getThumbnails() { return thumbnails; }
    
    /**
     * Adds to the set the {@link Thumbnail} associated to an
     * {@link ImageNode}.
     * @see HiViewerVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node)
    {
        if (node == null) return;
        Thumbnail thumb = node.getThumbnail();
        thumbnails.add(thumb.getDisplayedImage());
    }
    
}
