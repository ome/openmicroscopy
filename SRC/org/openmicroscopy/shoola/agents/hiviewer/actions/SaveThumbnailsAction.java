/*
 * org.openmicroscopy.shoola.agents.hiviewer.actions.SaveThumbnailsAction
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
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.cmd.SaveThumbnailsCmd;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Saves the images displayed in an imageSet as a unique thumbnail.
 * This action is enabled if the hierarchy object related to the imageSet is
 * a category or a dataset.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class SaveThumbnailsAction
    extends HiViewerAction
{

    /** The name of the action. */
    private static final String NAME = "Export as";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Save the thumbnails as a " +
            "single image. ";
    
    /**
     * Sets the action enabled depending on the current state.
     * @see HiViewerAction#onStateChange()
     */
    protected void onStateChange()
    {
    	if (model.getState() == HiViewer.READY) {
    		Browser browser = model.getBrowser();
    		if (browser != null) 
    			onDisplayChange(browser.getLastSelectedDisplay());
    		else setEnabled(false);
    	} else setEnabled(false);	
    }
    
    /**
     * Sets the action enabled depending on the currently selected display
     * @see HiViewerAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay selectedDisplay)
    {
    	if (model.getState() != HiViewer.READY) return;
    	if (model.getBrowser().getSelectedLayout() == 
    		LayoutFactory.FLAT_LAYOUT) {
        	setEnabled(true);
        	return;
        }
        if (selectedDisplay == null) {
            setEnabled(false);
            return;
        }	
        if (selectedDisplay.getParentDisplay() == null) setEnabled(false);
        else {
            Object ho = selectedDisplay.getHierarchyObject();
            if (model.getBrowser().getSelectedLayout() == 
            	LayoutFactory.FLAT_LAYOUT && !(ho instanceof ImageData))
            {
            	setEnabled(true);
            	return;
            }
            setEnabled(((ho instanceof CategoryData) || 
            			(ho instanceof DatasetData)));
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public SaveThumbnailsAction(HiViewer model)
    {
        super(model);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.SAVE));
    }

    /**
     * Creates a {@link SaveThumbnailsCmd} command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        SaveThumbnailsCmd cmd = new SaveThumbnailsCmd(model);
        cmd.execute();
    }
    
}
