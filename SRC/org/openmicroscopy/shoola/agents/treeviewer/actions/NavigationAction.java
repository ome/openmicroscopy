/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.NavigationAction
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

package org.openmicroscopy.shoola.agents.treeviewer.actions;




//Java imports
import java.awt.event.ActionEvent;

import javax.swing.Action;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ImageData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class NavigationAction
    extends BrowserAction
{

    /** Description of the action. */
    private static final String DESCRIPTION_FORWARD = "Go into selected node.";
    
    /** Description of the action. */
    private static final String DESCRIPTION_BACKWARD = "Go back to the tree.";
    
    /** Flag to indicate the navigation orientation. */
    private boolean v;
    
    /**
     * Enables the action depending on the selected node.
     * @see BrowserAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            setEnabled(false);
            return;
        }
        Object ho = selectedDisplay.getUserObject();
        setEnabled(!(ho == null || (ho instanceof ImageData)));
    }
    
    /**
     * Enables the action depending on the current state.
     * @see BrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
        switch (model.getState()) {
            case Browser.LOADING_DATA:
            case Browser.LOADING_LEAVES:
            case Browser.COUNTING_ITEMS:  
                setEnabled(false);
                break;
            default:
                setEnabled(true);
                onDisplayChange(model.getLastSelectedDisplay());
                break;
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model     Reference to the Model. Mustn't be <code>null</code>.
     * @param forward   Pass <code>true</code> to navigate forward,
     *                  <code>false</code> to navigate backward.
     */
    public NavigationAction(Browser model, boolean forward)
    {
        super(model);
        //setEnabled(true);
        v = forward;
        IconManager im = IconManager.getInstance();
        if (forward) {
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_FORWARD));
            putValue(Action.SMALL_ICON, im.getIcon(IconManager.FORWARD_NAV));
        } else {
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_BACKWARD));
            putValue(Action.SMALL_ICON, im.getIcon(IconManager.BACKWARD_NAV));
        }     
    }
    
    /**
     * Displays the main tree or go into the selected node.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        model.navigate(!v);
    }
    
}
