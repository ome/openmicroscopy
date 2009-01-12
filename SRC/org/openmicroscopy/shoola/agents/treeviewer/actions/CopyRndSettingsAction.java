/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.CopyRndSettingsAction 
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
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageData;

/** 
 * Copies the id of the pixels set to copy the rendering settings from.
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
	extends TreeViewerAction
{

	/** The name of the action. */
	private static final String NAME = "Copy Settings";

	/** The description of the action. */
	private static final String DESCRIPTION = "Copy the image's " +
			"rendering settings.";

	/**
	 * Callback to notify of a change of state in the currently selected 
	 * browser.
	 * @see TreeViewerAction#onBrowserStateChange(Browser)
	 */
	protected void onBrowserStateChange(Browser browser)
	{
		if (browser != null) 
			onDisplayChange(browser.getLastSelectedDisplay());
	}
	
	/**
	 * Callback to notify of a change in the currently selected display
	 * in the currently selected 
	 * {@link org.openmicroscopy.shoola.agents.treeviewer.browser.Browser}.
	 * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
	 */
	protected void onDisplayChange(TreeImageDisplay selectedDisplay)
	{
		if (selectedDisplay == null) {
			setEnabled(false);
			return;
		}
		Object ho = selectedDisplay.getUserObject();
		Browser browser = model.getSelectedBrowser();
		if (ho == null || browser == null) {
			setEnabled(false);
			return;
		}
		int n = browser.getSelectedDisplays().length;
		
		if (n > 1) {
			setEnabled(false);
			return;
		}
		setEnabled(ho instanceof ImageData);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public CopyRndSettingsAction(TreeViewer model)
	{
		super(model);
		name = NAME;
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION));
		IconManager im = IconManager.getInstance();
		putValue(Action.SMALL_ICON, im.getIcon(IconManager.COPY));
	}

	/** 
	 * Notifies the model to stored the id of the pixels set to copy 
	 * the rendering settings from.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		model.copyRndSettings(null);
	}

}
