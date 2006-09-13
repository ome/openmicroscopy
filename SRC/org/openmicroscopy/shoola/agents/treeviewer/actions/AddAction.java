/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.AddAction
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
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ClassifyCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 *  Adds existing objects to the selected <code>DataObject</code>.
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
public class AddAction
    extends TreeViewerAction
{

    /** The default name of the action. */
    private static final String NAME = "Add existing";
    
    /** The name of the action to add existing <code>Datasets</code>. */
    private static final String NAME_DATASET = "Add existing Dataset...";
    
    /** The name of the action to add existing <code>Categories</code>. */
    private static final String NAME_CATEGORY = "Add existing Category...";
    
    /** The name of the action to add existing <code>Images</code>. */
    private static final String NAME_IMAGE = "Add existing Image...";
    
    /** Description of the action. */
    private static final String DESCRIPTION = "Add existing elements to the " +
                                                "selected container.";
    
    /**
     * Modifies the name of the action and sets it enabled depending on
     * the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            setEnabled(false);
            return;
        }
        Object ho = selectedDisplay.getUserObject();
        if (ho instanceof String) { // root
            setEnabled(false);
            putValue(Action.NAME, NAME);  
        } else if (ho instanceof ProjectData) {
            setEnabled(model.isObjectWritable((DataObject) ho));
            putValue(Action.NAME, NAME_DATASET); 
        } else if (ho instanceof CategoryGroupData) {
            setEnabled(model.isObjectWritable((DataObject) ho));
            putValue(Action.NAME, NAME_CATEGORY);
        } else if (ho instanceof CategoryData) {
            setEnabled(model.isObjectWritable((DataObject) ho));
            putValue(Action.NAME, NAME_IMAGE);
        } else if (ho instanceof DatasetData) {
            setEnabled(model.isObjectWritable((DataObject) ho));
            putValue(Action.NAME, NAME_IMAGE);
        } else {
            setEnabled(false);
            putValue(Action.NAME, NAME);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public AddAction(TreeViewer model)
    {
        super(model);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.CREATE));
    }

    /**
     * Adds existing items to the currently selected node.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        Browser b = model.getSelectedBrowser();
        if (b == null) return;
        TreeImageDisplay d = b.getLastSelectedDisplay();
        if (d == null) return;
        Object ho = d.getUserObject();
        if ((ho instanceof ProjectData) || (ho instanceof CategoryGroupData) ||
            (ho instanceof DatasetData))
            model.addExistingObjects((DataObject) ho);
        else if (ho instanceof CategoryData) {
            ClassifyCmd cmd = new ClassifyCmd(model, ClassifyCmd.CLASSIFY);
            cmd.execute();
        }
    }
    
}
