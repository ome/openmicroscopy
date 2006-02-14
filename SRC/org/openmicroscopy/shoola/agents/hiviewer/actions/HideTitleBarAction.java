/*
 * org.openmicroscopy.shoola.agents.hiviewer.actions.HideTitleBarAction
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

package org.openmicroscopy.shoola.agents.hiviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.ShowTitleBarCmd;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Hides the title bar of all <code>ImageNode</code>s below the 
 * {@link Browser}'s currently selected display.
 * This action is enabled on all display nodes.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class HideTitleBarAction
    extends HiViewerAction
{

    /** The name of the action. */
    private static final String NAME = "Hide Title Bars";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Hides the title bar of every "+
                                    "image below the currently selected node.";

    
    /**
     * Callback to notify of a change in the currently selected display
     * in the {@link Browser}.
     * 
     * @param selectedDisplay The newly selected display node.
     */
    protected void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (selectedDisplay != null) 
            setEnabled(!(model.getBrowser().getSelectedLayout() == 
                        LayoutFactory.TREE_LAYOUT));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public HideTitleBarAction(HiViewer model)
    {
        super(model);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
    }

    /** 
     * Creates a {@link ShowTitleBarCmd} command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        ShowTitleBarCmd cmd = new ShowTitleBarCmd(model, false);
        cmd.execute();
    }

}
