/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ShowTitleBarVisitor
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
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Visits the browser's display to show or hide the title bar of all
 * {@link ImageNode}s below a given node in the display.
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
class ShowTitleBarVisitor
    extends HiViewerVisitor
{

    /** 
     * Tells whether to show (<code>true</code>) or hide (<code>false</code>). 
     */
    private boolean     show;
    
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param show  Tells whether to show (<code>true</code>) or hide 
     *              (<code>false</code>).
     */
    ShowTitleBarVisitor(HiViewer model, boolean show)
    {
        super(model);
        this.show = show;
    }
    
    /** 
     * Sets the title bar to be the small bar (if we're showing the bar) or
     * the header bar (if hiding). 
     */
    public void visit(ImageNode node)
    { 
        if (show) node.setTitleBarType(ImageNode.SMALL_BAR);
        else node.setTitleBarType(ImageNode.HEADER_BAR);
    }

    /** Does nothing. */
    public void visit(ImageSet node) {}

}
