/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ViewHierarchyCmd
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;


//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageFinder;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerFactory;

/** 
 * TODO: add comments
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
public class ViewHierarchyCmd
    implements ActionCmd
{
    
    /** Identifies the view in CGCI action. */
    public static final int     IN_CGCI = 0;
    
    /** Identifies the view in PDI action. */
    public static final int     IN_PDI = 1;
    
    /** Reference to the model. */
    private HiViewer    model;
    
    /** One of the constants defined above. */
    private int         index;
    
    /** Creates a new instance.*/
    public ViewHierarchyCmd(HiViewer model, int index)
    {
        if (model == null)
            throw new IllegalArgumentException("no model");
        this.model = model;
        this.index = index;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        ImageDisplay selectedDisplay = model.getBrowser().getSelectedDisplay();
        Set images;
        if (selectedDisplay == null)
            images = model.getBrowser().getImages();
        else {
            ImageFinder visitor = new ImageFinder();
            selectedDisplay.accept(visitor);
            images = visitor.getImages();
        }
        if (images == null || images.size() == 0) return;
        HiViewer viewer = null;
        switch (index) {
            case IN_CGCI:
                viewer = HiViewerFactory.getCGCIViewer(images); 
            break;
            case IN_PDI:
                viewer = HiViewerFactory.getPDIViewer(images); 
        }
        if (viewer != null) viewer.activate();
    }

}
