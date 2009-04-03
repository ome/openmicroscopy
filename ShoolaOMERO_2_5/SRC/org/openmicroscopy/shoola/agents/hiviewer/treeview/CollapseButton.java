/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.CollapseButton
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

package org.openmicroscopy.shoola.agents.hiviewer.treeview;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;

/** 
 * The collapse button in the <code>ToolBar</code>.
 * This is a small MVC component that is aggregated into the bigger MVC set
 * of the {@link TreeView}. The MVC parts of this button are all collapsed
 * in this class.
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
class CollapseButton
	extends JButton
	implements ActionListener
{

    /** The Model this button is working with. */
    private TreeView model;
    
    /**
     * Creates a new instance.
     * 
     * @param model The Model this button is working with.
     *              Mustn't be <code>null</code>.
     */
    CollapseButton(TreeView model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        setRolloverEnabled(true);
        addActionListener(this);
        IconManager im = IconManager.getInstance();
        setIcon(im.getIcon(IconManager.COLLAPSE));
    }
    
    /**
     * Collapses all the nodes of the tree displayed in the model.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    { 
        CollapseVisitor visitor = new CollapseVisitor(model); 
        model.accept(visitor, TreeViewNodeVisitor.IMAGE_SET_ONLY);
    }

}
