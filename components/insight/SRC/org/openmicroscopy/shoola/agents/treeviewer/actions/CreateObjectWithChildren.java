/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CreateCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

/** 
 * Create a new object and adds the selected nodes to it e.g. dataset
 * and images.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class CreateObjectWithChildren 
	extends TreeViewerAction
{

	/** Indicates to create a <code>Dataset</code>. */
	public static final int DATASET = CreateCmd.DATASET;
	
	/** 
	 * Name of the action if the selected node is a <code>Dataset</code>.
	 */
	private static final String NAME_DATASET = "New Dataset...";
	
	/** 
	 * Description of the action if the selected node is a <code>Dataset</code>.
	 */
	private static final String DESCRIPTION_DATASET = "Create a new Dataset " +
	"and add the selected orphaned images to it.";
	
	/** One of the constants defined by this class.*/
	private int index;
	
	/** 
     * Checks if the passed value is supported.
     * 
     * @param value The value to handle.
     */
    private void checkType(int value)
    {
    	IconManager im = IconManager.getInstance();
    	switch (value) {
			case DATASET:
				name = NAME_DATASET;
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(DESCRIPTION_DATASET));
				putValue(Action.SMALL_ICON, im.getIcon(IconManager.DATASET));
				break;
			default:
				break;
		}
    }
    
    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
        if (browser == null) {
        	setEnabled(false);
        	return;
        }
        switch (browser.getState()) {
            case Browser.LOADING_DATA:
            case Browser.LOADING_LEAVES:
            case Browser.COUNTING_ITEMS:
                setEnabled(false);
                break;
            default:
            	if (browser.getBrowserType() != Browser.ADMIN_EXPLORER)
            		setEnabled(true);
            	else onDisplayChange(browser.getLastSelectedDisplay());
        }
    }
    
    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
    	Browser browser = model.getSelectedBrowser();
        if (browser == null || selectedDisplay == null) {
            setEnabled(false);
            return;
        }
        Object ho = selectedDisplay.getUserObject();
        TreeImageDisplay[] selection = browser.getSelectedDisplays();
        int count = 0;
    	switch (index) {
			case DATASET:
				if (ho instanceof ImageData) {
					for (int i = 0; i < selection.length; i++) {
						if (model.canAnnotate(selection[i].getUserObject()))
								count++;
					}
					setEnabled(count == selection.length);
				} else setEnabled(false);
				
				break;
			default:
				setEnabled(false);
		}
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public CreateObjectWithChildren(TreeViewer model, int index)
    {
        super(model);
        this.index = index;
        checkType(index);
    } 

    /**
     * Creates a {@link CreateCmd} command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	DatasetData d = new DatasetData();
		EditorDialog dialog = new EditorDialog(
				model.getUI(), d, false);
		dialog.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (EditorDialog.CREATE_NO_PARENT_PROPERTY.equals(name) ||
		        		EditorDialog.CREATE_PROPERTY.equals(name)) {
					DataObject object = (DataObject) evt.getNewValue();
		        	model.createDataObjectWithChildren(object);
				}
			}
		});
		UIUtilities.centerAndShow(dialog);
    }

}
