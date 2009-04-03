/*
 * org.openmicroscopy.shoola.agents.hiviewer.actions.CopyRndSettingsAction 
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
package org.openmicroscopy.shoola.agents.hiviewer.actions;




//Java imports
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageData;

/** 
 * Action to copy the rendering settings.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CopyRndSettingsAction 
	extends HiViewerAction
{

	/** The name of the action. */
    private static final String NAME = "Copy Settings";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Copy the rendering settings.";

    /**
     * Callback to notify a change of state.
     * @see HiViewerAction#onStateChange()
     */
    protected void onStateChange()
    {
    	Browser browser = model.getBrowser();
    	if (browser != null)
    		onDisplayChange(browser.getLastSelectedDisplay());
    }
    
    /**
     * Sets the action enabled depending on the currently selected display.
     * @see HiViewerAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null || model.getBrowser() == null) {
            setEnabled(false);
            return;
        }
        Set nodes = model.getBrowser().getSelectedDisplays();
    	if (nodes.size() > 1) setEnabled(false);
    	else {
    		Object ho = selectedDisplay.getHierarchyObject();
    		setEnabled(ho instanceof ImageData);
    	}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public CopyRndSettingsAction(HiViewer model)
    {
        super(model);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.PASTE));
    }
    
    /** 
     * Posts an event to notify listeners of a copy rnd settings event.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	Browser browser = model.getBrowser();
    	if (browser == null) return;
    	ImageDisplay node = browser.getLastSelectedDisplay();
    	Object ho = node.getHierarchyObject();
    	if (!(ho instanceof ImageData)) return;
    	ImageData img = (ImageData) ho;
    	EventBus bus = HiViewerAgent.getRegistry().getEventBus();
    	bus.post(new CopyRndSettings(img.getDefaultPixels().getId()));
    }
    
}
