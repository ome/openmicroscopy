/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.PasteRndSettings 
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
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.CategoryData;
import pojos.DatasetData;
import pojos.ImageData;

/** 
 * Action to paste the rendering settings previously copied from
 * another image.
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
public class PasteRndSettings 
	extends TreeViewerAction
{

    /** The name of the action. */
    private static final String NAME = "Paste Settings";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Paste the rendering settings.";
    
    /**
     * Callback to notify of a change in the currently selected display
     * in the currently selected 
     * {@link org.openmicroscopy.shoola.agents.treeviewer.browser.Browser}.
     * 
     * @param selectedDisplay The newly selected display node.
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            setEnabled(false);
            return;
        }
        Object ho = selectedDisplay.getUserObject();
        if (!(ho instanceof ImageData || ho instanceof DatasetData ||
        	ho instanceof CategoryData)) 
        	setEnabled(false);
        else setEnabled(model.hasRndSettings());
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public PasteRndSettings(TreeViewer model)
	{
		super(model);
		name = NAME;
		putValue(Action.SHORT_DESCRIPTION, 
	                UIUtilities.formatToolTipText(DESCRIPTION));
	    IconManager im = IconManager.getInstance();
	    putValue(Action.SMALL_ICON, im.getIcon(IconManager.PASTE));
	}
	
	/** 
	 * Pastes the rendering settings across the collection of images.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
    public void actionPerformed(ActionEvent e)
    {
       model.pasteRndSettings();
    }
    
}
