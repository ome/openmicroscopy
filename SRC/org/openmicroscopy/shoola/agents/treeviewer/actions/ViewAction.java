/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.ViewAction
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

package org.openmicroscopy.shoola.agents.treeviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
 * Views or browses the selected node depending on the hierarchy object type.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ViewAction
    extends TreeViewerAction
{

    /** Name of the action when the <code>DataObject</code> is an Image. */
    private static final String VIEW = "View";
    
    /** Name of the action when the <code>DataObject</code> isn't an Image. */
    private static final String BROWSE = "Browse";
    
    /** Description of the action. */
    private static final String DESCRIPTION = "Browse the selected nodes";
    
    /** Description of the action when the selected node is an image. */
    private static final String DESCRIPTION_IMAGE = "View the selected image";
    
    /** Convenience reference to the icon manager. */
    private static IconManager	icons;
    
    /**
     * Sets the action enabled depending on the browser's type and 
     * the currenlty selected node. Sets the name of the action depending on 
     * the <code>DataObject</code> hosted by the currenlty selected node.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            setEnabled(false);
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.BROWSER)); 
            return;
        }
        if (selectedDisplay.getParentDisplay() == null) { //root
            name = BROWSE;
            setEnabled(false);
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.BROWSER)); 
            return;
        }
        Object ho = selectedDisplay.getUserObject();
        if (selectedDisplay instanceof TreeImageTimeSet) {
        	setEnabled(selectedDisplay.containsImages());
        	name = BROWSE;
            putValue(Action.SMALL_ICON, 
            			icons.getIcon(IconManager.BROWSER)); 
            return;
        }
        if (ho == null || !(ho instanceof DataObject) ||
        	ho instanceof ExperimenterData) setEnabled(false);
        else {
            Browser browser = model.getSelectedBrowser();
            if (browser != null) {
                if (browser.getSelectedDisplays().length > 1) {
                    setEnabled(true);
                    name = BROWSE;
                    putValue(Action.SMALL_ICON, 
                    			icons.getIcon(IconManager.BROWSER)); 
                    return;
                }
            }
            if ((ho instanceof ImageData)) {
            	name = VIEW;  
            	description = DESCRIPTION_IMAGE;
            	putValue(Action.SHORT_DESCRIPTION, 
                        UIUtilities.formatToolTipText(description));
            	putValue(Action.SMALL_ICON, icons.getIcon(IconManager.VIEWER)); 
            } else {
            	name = BROWSE;
            	putValue(Action.SMALL_ICON, icons.getIcon(IconManager.BROWSER));
            }
            if (selectedDisplay instanceof TreeImageSet) {
            	setEnabled(
            			((TreeImageSet) selectedDisplay).getNumberItems() > 0);
            } else
            	setEnabled(true);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public ViewAction(TreeViewer model)
    {
        super(model);
        name = BROWSE;
        icons = IconManager.getInstance();
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.BROWSER)); 
    }
    
    /**
     * Creates a  {@link ViewCmd} command to execute the action. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
       ViewCmd cmd = new ViewCmd(model);
       cmd.execute();
    }
    
}
