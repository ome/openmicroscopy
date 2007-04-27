/*
 * org.openmicroscopy.shoola.agents.hiviewer.actions.RemoveAction
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
import java.util.Set;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.RemoveCmd;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Action to remove the selected nodes.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class RemoveAction
    extends HiViewerAction
{

    /** Name of the action. */
    private static final String NAME = "Remove";
    
    /** Name of the action if the selected items are Datasets. */
    private static final String NAME_PROJECT = "Remove from current project";
    
    /** Name of the action if the selected items are Datasets. */
    private static final String NAME_CATEGORYGROUP = 
            "Remove from current category group";
    
    /** 
     * Name of the action if the selected items are images and 
     * the parent is a dataset.
     */
    private static final String NAME_DATASET = "Remove from current dataset";
    
    /** 
     * Name of the action if the selected items are images and
     * the parent is a dataset. 
     */
    private static final String NAME_CATEGORY = "Remove from current category";
    
    
    /** Description of the action. */
    private static final String DESCRIPTION = "Remove the selected element " +
            "from the current container.";
    
    /**
     * Sets the action enabled depending on the currently selected display
     * @see HiViewerAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null || model.getBrowser() == null) {
            setEnabled(false);
            return;
        }
        if (selectedDisplay.getParentDisplay() == null) setEnabled(false);
        else {
            Set nodes = model.getBrowser().getSelectedDisplays();
            if (nodes.size() > 1) setEnabled(false);
            else {
                Object ho = selectedDisplay.getHierarchyObject();
                ImageDisplay parent = selectedDisplay.getParentDisplay();
                Object po = parent.getHierarchyObject();
                if ((ho instanceof ProjectData) ||
                        (ho instanceof CategoryGroupData)) {
                    putValue(Action.NAME, NAME);
                    setEnabled(model.isObjectWritable((DataObject) ho));
                } else if (ho instanceof DatasetData) {
                    putValue(Action.NAME, NAME_PROJECT);
                    if (po instanceof ProjectData) 
                    	setEnabled(model.isObjectWritable((DataObject) ho));
                    else setEnabled(false); 
                } else if (ho instanceof CategoryData) {
                    putValue(Action.NAME, NAME_CATEGORYGROUP);
                    if (po instanceof CategoryGroupData) 
                    	setEnabled(model.isObjectWritable((DataObject) ho));
                    else setEnabled(false); 
                } else if (ho instanceof ImageData) {
                    if (po instanceof DatasetData) {
                    	setEnabled(model.isObjectWritable((DataObject) ho));
                    	putValue(Action.NAME, NAME_DATASET);
                    } else if (po instanceof CategoryData) {
                    	setEnabled(model.isObjectWritable((DataObject) ho));
                    	putValue(Action.NAME, NAME_CATEGORY);
                    } else {
                    	setEnabled(false);
                    	putValue(Action.NAME, NAME);
                    }
                } else setEnabled(false);
            }
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public RemoveAction(HiViewer model)
    {
        super(model);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager icons = IconManager.getInstance();
        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.DELETE));
    }
    
    /**
     * Creates a {@link RemoveCmd} to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        RemoveCmd cmd = new RemoveCmd(model);
        cmd.execute();
    }
    
}
