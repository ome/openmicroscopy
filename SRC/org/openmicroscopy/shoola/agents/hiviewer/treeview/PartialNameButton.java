/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.PartialNameButton 
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
package org.openmicroscopy.shoola.agents.hiviewer.treeview;




//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JToggleButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The partial name button in the <code>ToolBar</code>.
 * This is a small MVC component that is aggregated into the bigger MVC set
 * of the {@link TreeView}. The MVC parts of this button are all collapsed
 * in this class.
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
class PartialNameButton 
	extends JToggleButton
	implements ActionListener
{

	/** Description of the button. */
    private static final String DESCRIPTION = "Show the full name of " +
    									"the image";

    /** The Model this button is working with. */
    private TreeView model;
    
    /**
     * Creates a new instance.
     * 
     * @param model The Model this button is working with.
     *              Mustn't be <code>null</code>.
     */
    PartialNameButton(TreeView model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        setRolloverEnabled(true);
        addActionListener(this);
        IconManager im = IconManager.getInstance();
        setIcon(im.getIcon(IconManager.PARTIAL_NAME));
        setToolTipText(UIUtilities.formatToolTipText(DESCRIPTION));
    }
    
    /**
     * Displays the full or a truncated version of the <code>Image</code>'s
     * name.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    { 
        PartialNameVisitor visitor = new PartialNameVisitor(!isSelected()); 
        model.accept(visitor, TreeViewNodeVisitor.IMAGE_NODE_ONLY);
        model.repaint();
    }
    
}
