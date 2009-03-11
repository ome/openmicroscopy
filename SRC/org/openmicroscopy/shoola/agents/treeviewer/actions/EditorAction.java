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

import pojos.FileAnnotationData;

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

	/** The name of the action. */
	private static final String NAME = "Launch Editor...";
	
	/** The description of the action. */
	private static final String DESCRIPTION = "Launch the Editor.";
	
	/** 
	 * Flag indicating to open the editor with a selected item if 
	 * <code>true</code>, to open with a blank document if <code>false</code>
	 */						
	private boolean forSelected;
	
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
    	if (forSelected) {
    		if (state == Browser.READY) {
    			List l = browser.getSelectedDataObjects();
    			if (l == null || l.size() != 1) setEnabled(false);
    			else {
    				Object object = l.get(0);
    				setEnabled(object instanceof FileAnnotationData);
    			}
    		} else setEnabled(false);
    	} else {
    		setEnabled(state == Browser.READY);
    	}
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model 		Reference to the Model. 
     * 						Mustn't be <code>null</code>.
     * @param forSelected 	Pass <code>true</code> to open the editor 
     * 						with a selected item, <code>false</code> to open	
     * 						with a blank document.
     */
    public EditorAction(TreeViewer model, boolean forSelected)
    {
        super(model);
        name = NAME;
        this.forSelected = forSelected;
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
    	model.openEditorFile(forSelected); 
    }
    
}
