/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ClearVisitor
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
    

    /** Highlights the titleBar of the specified node. */
    private void setHighlight(ImageDisplay node)
    {
        if (node.equals(model.getBrowser().getSelectedDisplay())) {
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

    /** Sets the highlight color to <code>null</code>. */
    public void visit(ImageNode node) { setHighlight(node); }

    /** Sets the highlight color to <code>null</code>. */
    public void visit(ImageSet node)
    {
        if (node.getParentDisplay() != null) setHighlight(node);
    } 
    
}
