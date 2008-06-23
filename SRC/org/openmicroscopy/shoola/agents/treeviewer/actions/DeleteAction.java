/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.DeleteAction
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
import org.openmicroscopy.shoola.agents.treeviewer.cmd.DeleteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * Action to delete the selected element and {@link DeleteCmd} is executed.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DeleteAction
    extends TreeViewerAction
{

    /** Name of the action. */
    private static final String NAME = "Delete";//"Remove";
    
    /** Name of the action if the selected items are <code>Project</code>s. */
    private static final String NAME_ROOT_P = "Remove projects";
    
    /** 
     * Name of the action if the selected items are 
     * <code>CategoryGroup</code>s. 
     */
    private static final String NAME_ROOT_CG = "Remove Tag sets";
    
    /** Name of the action if the selected items are <code>Dataset</code>s. */
    private static final String NAME_PROJECT = "Remove from current project";
    
    /** Name of the action if the selected items are <code>Categorie</code>s.*/
    private static final String NAME_CATEGORYGROUP = "Remove from current " +
    												"Tag set";
    
    /** 
     * Name of the action if the selected items are <code>Image</code>s and 
     * the parent is a <code>Dataset</code>.
     */
    private static final String NAME_DATASET = "Remove from current dataset";
    
    /** 
     * Name of the action if the selected items are <code>Image</code>s and 
     * the parent is a <code>Category</code>.
     */
    private static final String NAME_CATEGORY = "Remove current Tag";
    
    
    /** 
     * Description of the action if the selected item is <code>null</code>. 
     */
    private static final String DESCRIPTION = "Delete.";
    
    /** 
     * Description of the action if the selected items are 
     * <code>Project</code>s. 
     */
    private static final String DESCRIPTION_ROOT_P = "Remove the selected " +
    													"project.";
    
    /** 
     * Description of the action if the selected items are 
     * <code>CategoryGroup</code>s. 
     */
    private static final String DESCRIPTION_ROOT_CG = "Remove the selected " +
    													"Tag Set.";
    
    /** 
     * Description of the action if the selected items are 
     * <code>Dataset</code>s. 
     */
    private static final String DESCRIPTION_PROJECT = "Remove the selected " +
    		"datasets from the project.";
    
    /** 
     * Description of the action if the selected items are 
     * <code>Categorie</code>s. 
     */
    private static final String DESCRIPTION_CATEGORYGROUP = "Remove the " +
    		"selected tags from the Tag set.";
    
    /** 
     * Description of the action if the selected items are <code>Image</code>s 
     * and the parent is a <code>Dataset</code>.
     */
    private static final String DESCRIPTION_DATASET = "Remove the selected " +
    		"images from the dataset.";
    
    /** 
     * Description of the action if the selected items are <code>Image</code>s 
     * and  the parent is a <code>Category</code>.
     */
    private static final String DESCRIPTION_CATEGORY = "Remove the Tag " +
    		"from the selected images.";
    
    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
        if (browser == null) return;
        switch (browser.getState()) {
            case Browser.LOADING_DATA:
            case Browser.LOADING_LEAVES:
            case Browser.COUNTING_ITEMS:  
                setEnabled(false);
                break;
            default:
                onDisplayChange(browser.getLastSelectedDisplay());
                break;
        }
    }
    
    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            name = NAME;
            description = DESCRIPTION;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
            setEnabled(false);
            return;
        }
        Browser browser = model.getSelectedBrowser();
        if (browser == null) {
            name = NAME;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
            setEnabled(false);
            description = DESCRIPTION;
            return;
        } 
        if (browser.getBrowserType() == Browser.IMAGES_EXPLORER) {
            name = NAME;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
            setEnabled(false);
            description = DESCRIPTION;
            return;
        }
        Object ho = selectedDisplay.getUserObject(); 
        /*
        if (ho instanceof ProjectData) {
        	name = NAME_ROOT_P;
        	putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_ROOT_P));
            setEnabled(model.isObjectWritable(ho));
        } else if (ho instanceof CategoryGroupData) {
            name = NAME_ROOT_CG;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_ROOT_CG));
            setEnabled(model.isObjectWritable(ho));
        } else if (ho instanceof DatasetData) {
            name = NAME_PROJECT;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_PROJECT));
            TreeImageDisplay parent = selectedDisplay.getParentDisplay();
            if (parent == null) setEnabled(false);
            else {
                if (parent.getUserObject() instanceof ProjectData)
                	setEnabled(model.isObjectWritable(ho));
                else setEnabled(false);
            }
        } else if (ho instanceof CategoryData) {
            name = NAME_CATEGORYGROUP;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_CATEGORYGROUP));
            TreeImageDisplay parent = selectedDisplay.getParentDisplay();
            if (parent == null) setEnabled(false);
            else {
                if (parent.getUserObject() instanceof CategoryGroupData)
                	setEnabled(model.isObjectWritable(ho));
                else setEnabled(false);
            }
        } else if (ho instanceof ImageData) {
        	TreeImageDisplay parent = selectedDisplay.getParentDisplay();
        	if (parent == null) setEnabled(false);
        	else {
                if (parent.getUserObject() instanceof DatasetData) {
                	name = NAME_DATASET;
                	putValue(Action.SHORT_DESCRIPTION, 
                            UIUtilities.formatToolTipText(DESCRIPTION_DATASET));
                } else {
                	name = NAME_CATEGORY;
                	putValue(Action.SHORT_DESCRIPTION, 
                            UIUtilities.formatToolTipText(DESCRIPTION_CATEGORY));
                }
                setEnabled(model.isObjectWritable(ho));
        	}
        } else {
        	setEnabled(false);
        	putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
        }
        */
        if ((ho instanceof DatasetData) || (ho instanceof ProjectData)) {
        	TreeImageDisplay[] selected = browser.getSelectedDisplays();
        	if (selected.length > 1) setEnabled(false);
        	else {
        		setEnabled(model.isObjectWritable(ho));
        	}
        }
        description = (String) getValue(Action.SHORT_DESCRIPTION);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public DeleteAction(TreeViewer model)
    {
        super(model);
        name = NAME;
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.DELETE));
    }

    /**
     * Creates a {@link DeleteCmd} command to execute the action. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        DeleteCmd cmd = new DeleteCmd(model);
        cmd.execute();
    }
    
}
