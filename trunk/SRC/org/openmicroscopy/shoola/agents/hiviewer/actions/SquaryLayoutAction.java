/*
 * org.openmicroscopy.shoola.agents.hiviewer.actions.SquaryLayoutAction
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

package org.openmicroscopy.shoola.agents.hiviewer.actions;




//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.LayoutCmd;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Uses the <code>squaryLayout</code> algorithm to layout the thumbnails.
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
public class SquaryLayoutAction
    extends HiViewerAction
{
    
    /** The name of the action. */
    private static final String NAME = "Hierarchical";

    /**
     * Allows to modify the layout when the thumbnails are loaded.
     * @see HiViewerAction#onStateChange()
     */
    protected void onStateChange()
    {
        //setEnabled(model.getState() == HiViewer.READY);
        setEnabled(true);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public SquaryLayoutAction(HiViewer model)
    {
        super(model);
        setEnabled(true);
        putValue(Action.NAME, NAME);
        //String description = 
        //    LayoutFactory.getLayoutDescription(LayoutFactory.SQUARY_LAYOUT);
        putValue(Action.SHORT_DESCRIPTION, "");

        IconManager icons = IconManager.getInstance();
        putValue(Action.SMALL_ICON, 
        		icons.getIcon(IconManager.HIERARCHICAL_LAYOUT));
    }

    /** 
     * Creates a {@link LayoutCmd} command to execute the action. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        LayoutCmd cmd = new LayoutCmd(model, LayoutFactory.SQUARY_LAYOUT);
        cmd.execute();
    }

}
