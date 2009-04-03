/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.EditorAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
import java.util.List;

import javax.swing.Action;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Action to launch the editor.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class EditorAction 
	extends TreeViewerAction
{

	/** Indicates to open the editor without selection. */
	public static final int		NO_SELECTION = TreeViewer.NO_SELECTION;
	
	/** Indicates to open the editor with a selected file. */
	public static final int		WITH_SELECTION = TreeViewer.WITH_SELECTION;
	
	/** Indicates to launch a new editor with a data object to link to. */
	public static final int		NEW_WITH_SELECTION = 
		TreeViewer.NEW_WITH_SELECTION;
	
	/** The name of the action. */
	private static final String NAME = "Launch Editor...";
	
	/** The name of the action. */
	private static final String NAME_EXPERIMENT = "New Experiment...";
	
	/** The description of the action. */
	private static final String DESCRIPTION = "Launch the Editor.";
	
	/**  One of the constants defined by this class. */						
	private int index;
	
	/**
	 * Controls if the passed index is supported.
	 * 
	 * @param index The value to control.
	 */
	private void checkValue(int index)
	{
		switch (index) {
			case WITH_SELECTION:
			case NO_SELECTION:
			case NEW_WITH_SELECTION:
				this.index = index;
				break;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
	}
	
	/** 
     * Enables the action if the browser is not ready.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
	protected void onDisplayChange(TreeImageDisplay selectedDisplay)
	{
		onBrowserStateChange(model.getSelectedBrowser());
	}
	
	/** 
     * Enables the action if the browser is not ready.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
    	if (browser == null) return;
    	int state = browser.getState();
    	switch (index) {
			case WITH_SELECTION:
				if (state == Browser.READY) {
	    			List l = browser.getSelectedDataObjects();
	    			if (l == null || l.size() != 1) setEnabled(false);
	    			else {
	    				Object object = l.get(0);
	    				setEnabled(object instanceof FileAnnotationData);
	    			}
	    		} else setEnabled(false);
				break;
			case NO_SELECTION:
				setEnabled(state == Browser.READY);
				break;
			case NEW_WITH_SELECTION:
				if (state == Browser.READY) {
	    			List l = browser.getSelectedDataObjects();
	    			if (l == null || l.size() != 1) setEnabled(false);
	    			else {
	    				Object object = l.get(0);
	    				if ((object instanceof ProjectData) ||
	    						(object instanceof DatasetData) || 
	    						(object instanceof ImageData) ||
	    						(object instanceof ScreenData) ||
	    						(object instanceof PlateData))
	    					setEnabled(true);
	    				else setEnabled(false);
	    			}
	    		} else setEnabled(false);
		}
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model	Reference to the Model. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
    public EditorAction(TreeViewer model, int index)
    {
        super(model);
        name = NAME;
        checkValue(index);
        if (index == NEW_WITH_SELECTION)
        	name = NAME_EXPERIMENT;
        IconManager icons = IconManager.getInstance();
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.EDITOR)); 
    }
    
    /**
     * Launches the editor.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    { 
    	model.openEditorFile(index); 
    }
    
}
