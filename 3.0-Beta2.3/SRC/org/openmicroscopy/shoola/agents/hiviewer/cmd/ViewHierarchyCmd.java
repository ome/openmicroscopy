/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ViewHierarchyCmd
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageFinder;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewerFactory;

import pojos.ImageData;

/** 
 * Views the selected images in the specified hierarchy type
 * i.e. Project-Dataset or CategoryGroup-Category.
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
    
    /**
     * Checks if the passed index is supported.
     * 
     * @param i The passed index.
     * @return <code>true</code> if the index is supported.
     */
    private boolean checkIndex(int i)
    {
        switch (i) {
            case IN_CGCI:
            case IN_PDI:    
                return true;
        }
        return false;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param index The index. One of the constant defined by this class.
     */
    public ViewHierarchyCmd(HiViewer model, int index)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        if (!checkIndex(index))
            throw new IllegalArgumentException("Index not supported.");
        this.model = model;
        this.index = index;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
    	Browser browser = model.getBrowser();
    	ImageDisplay selectedDisplay = null;
    	if (browser != null) {
    		selectedDisplay = browser.getLastSelectedDisplay();
    	}
         
        Set<ImageData> images;
        if (selectedDisplay == null && browser != null) 
        	images = browser.getImages();  
        else {
            ImageFinder visitor = new ImageFinder();
            selectedDisplay.accept(visitor,
                                ImageDisplayVisitor.IMAGE_NODE_ONLY);
            images = visitor.getImages();
        }
        if (images == null || images.size() == 0) return;
        HiViewer viewer = null;
        switch (index) {
            case IN_CGCI:
                viewer = HiViewerFactory.getCGCIViewer(images,
                			model.getExperimenter()); 
            break;
            case IN_PDI:
                viewer = HiViewerFactory.getPDIViewer(images, 
                			model.getExperimenter());
        }
        if (viewer != null) viewer.activate(model.getUI().getBounds());
    }

}
