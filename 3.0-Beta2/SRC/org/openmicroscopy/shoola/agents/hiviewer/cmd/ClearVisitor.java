/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ClearVisitor
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.Colors;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Resets the default color of the titleBar.
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
class ClearVisitor
    extends HiViewerVisitor
{
    

    /** 
     * Highlights the titleBar of the specified node. 
     * 
     * @param node The {@link ImageDisplay node} to highlight.
     */
    private void setHighlight(ImageDisplay node)
    {
    	if (model.getBrowser() == null) {
    		node.setHighlight(null);
    		return;
    	}
        if (node.equals(model.getBrowser().getLastSelectedDisplay())) {
            Colors colors = Colors.getInstance();
            node.setHighlight(colors.getColor(Colors.TITLE_BAR_HIGHLIGHT));
        } else node.setHighlight(null);
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer Reference to the model. Mustn't be <code>null</code>.
     */
    ClearVisitor(HiViewer viewer)
    {
        super(viewer);
    }

    /** 
     * Sets the highlight color to <code>null</code>. 
     * @see HiViewerVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node) { setHighlight(node); }

    /** 
     * Sets the highlight color to <code>null</code>. 
     * @see HiViewerVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
        if (node.getParentDisplay() != null) setHighlight(node);
    } 
    
}
