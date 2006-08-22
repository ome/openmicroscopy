/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.ViewAction
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
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
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
    private static final String DESCRIPTION = "View the selected image or" +
            "browse the selected project, dataset, categoryGroup or category";
    
    /**
     * Sets the action enabled depending on the browser's type and 
     * the currenlty selected node. Sets the name of the action depending on 
     * the <code>DataObject</code> hosted by the currenlty selected node.
     * @see ViewAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            setEnabled(false);
            return;
        }
        if (selectedDisplay.getParentDisplay() == null) { //root
            putValue(Action.NAME, BROWSE);
            setEnabled(false);
            //setEnabled(model.getSelectedBrowser().getBrowserType() == 
            //    Browser.IMAGES_EXPLORER);
            return;
        }
        Object ho = selectedDisplay.getUserObject();
        if (ho == null || !(ho instanceof DataObject)) setEnabled(false);
        else {
            if ((ho instanceof ImageData)) putValue(Action.NAME, VIEW);   
            else putValue(Action.NAME, BROWSE);
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
        setEnabled(true);
        putValue(Action.NAME, VIEW);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.VIEWER)); 
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
