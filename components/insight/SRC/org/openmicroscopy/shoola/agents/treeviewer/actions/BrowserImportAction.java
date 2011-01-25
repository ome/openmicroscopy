/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.BrowserImportAction
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.importer.LoadImporter;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.NodesFinder;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DatasetData;
import pojos.ProjectData;
import pojos.ScreenData;

/**
 * Action to import images.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class BrowserImportAction 
	extends BrowserAction
{

    /** The description of the action. */
    private static final String DESCRIPTION = "Import the selected images.";
    
    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see BrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
    	 switch (model.getState()) {
	    	 case Browser.LOADING_DATA:
	         case Browser.LOADING_LEAVES:
	             setEnabled(false);
	             break;
	         default:
	        	 onDisplayChange(model.getLastSelectedDisplay());
         }
    }
    
    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
        	setEnabled(true);
            return;
        }
        Object ho = selectedDisplay.getUserObject();
        if (ho == null) setEnabled(true);
        else {
        	TreeImageDisplay[] nodes = model.getSelectedDisplays();
            if (nodes != null && nodes.length > 1) {
            	setEnabled(false);
            } else {
            	if (ho instanceof ProjectData || ho instanceof DatasetData ||
            			ho instanceof ScreenData) 
            		setEnabled(model.isUserOwner(ho));
            	else setEnabled(true);
            }
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public BrowserImportAction(Browser model)
	{
		super(model);
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION));
		IconManager im = IconManager.getInstance();
		putValue(Action.SMALL_ICON, im.getIcon(IconManager.IMPORTER));
	}
	
    /**
     * Brings up the importer dialog.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	//No container specified in that case
        //model.showImporter();
    	/*
    	int type = -1;
    	switch (model.getBrowserType()) {
			case Browser.PROJECTS_EXPLORER:
				type = LoadImporter.PROJECT_TYPE;
				break;
			case Browser.SCREENS_EXPLORER:
				type = LoadImporter.SCREEN_TYPE;
		}
    	EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
    	bus.post(new LoadImporter(type));
    	*/
    	TreeImageDisplay[] list = model.getSelectedDisplays();
    	//TreeImageDisplay display = model.getLastSelectedDisplay();
    	LoadImporter event = null;
		Class klass = null;
    	if (list != null && list.length > 0) {
    		List<TreeImageDisplay> containers = new ArrayList<TreeImageDisplay>();
    		TreeImageDisplay node;
    		Object ho;
    		for (int j = 0; j < list.length; j++) {
    			node = list[j];
    			ho = node.getUserObject();
    			if (ho instanceof DatasetData || ho instanceof ScreenData ||
		    			ho instanceof ProjectData) {
					containers.add(node);
					klass = null;
					if (ho instanceof DatasetData) klass = ho.getClass();
				}
			}
    		if (containers.size() > 0) {
    			event = new LoadImporter(containers);
    		}
    	}
    	if (event == null) {
    		int type = -1;
        	switch (model.getBrowserType()) {
    			case Browser.PROJECTS_EXPLORER:
    				klass = DatasetData.class;
    				type = LoadImporter.PROJECT_TYPE;
    				break;
    			case Browser.SCREENS_EXPLORER:
    				klass = null;//ScreenData.class;
    				type = LoadImporter.SCREEN_TYPE;
    		}
        	event = new LoadImporter(type);
    	}
    	if (event != null) {
    		if (klass != null) {
        		NodesFinder finder = new NodesFinder(klass);
        		model.accept(finder);
        		event.setObjects(finder.getNodes());
        	}
    		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
        	bus.post(event);
    	}
    }
    
}
