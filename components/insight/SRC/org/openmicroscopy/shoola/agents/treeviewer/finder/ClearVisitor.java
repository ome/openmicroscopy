/*
 * org.openmicroscopy.shoola.agents.treeviewer.finder.ClearVisitor
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

package org.openmicroscopy.shoola.agents.treeviewer.finder;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;

/** 
 * Sets the color of each node to <code>null</code>;
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class ClearVisitor
	implements TreeImageDisplayVisitor
{

    /**
     * Resets the highlight color to <code>null</code>
     * and the font to {@link TreeImageDisplay#FONT_PLAIN}.
     * 
     * @param n The node to clear.
     */
    private void clearNode(TreeImageDisplay n)
    {
        n.setHighLight(null);
        n.setFontStyle(TreeImageDisplay.FONT_PLAIN);
    }
    
    /**
     * Sets the color of the node to <code>null</code>.
     * @see TreeImageDisplayVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) { clearNode(node); }

    /**
     * Sets the color of the node to <code>null</code>.
     * @see TreeImageDisplayVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node) { clearNode(node); }

}
