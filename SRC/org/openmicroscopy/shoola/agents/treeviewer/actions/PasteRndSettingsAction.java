/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.PasteRndSettingsAction
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
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PasteRndSettingsCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

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
public class PasteRndSettingsAction 
	extends TreeViewerAction
{

	/** The name of the action. */
	private static final String NAME = "Paste Settings";

	/** The description of the action. */
	private static final String DESCRIPTION = "Paste the rendering settings.";

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
	 * 
	 * @param selectedDisplay The newly selected display node.
	 */
	protected void onDisplayChange(TreeImageDisplay selectedDisplay)
	{
		if (!model.hasRndSettings()) {
			setEnabled(false);
			return;
		}
		
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
		if (selectedDisplay instanceof TreeImageTimeSet) {
			if (n > 1) setEnabled(false);
			else {
				TreeImageTimeSet timeNode = (TreeImageTimeSet) selectedDisplay;
				setEnabled((timeNode.getNumberItems() > 0));
			}
			return;
		}
		if (!(ho instanceof ImageData || ho instanceof DatasetData || 
				ho instanceof PlateData || ho instanceof ScreenData ||
				ho instanceof ProjectData))
			setEnabled(false);
		else {
			if (n > 1) setEnabled((ho instanceof ImageData));
			else setEnabled(true);
		}
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public PasteRndSettingsAction(TreeViewer model)
	{
		super(model);
		setEnabled(false);
		name = NAME;
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION));
		IconManager im = IconManager.getInstance();
		putValue(Action.SMALL_ICON, im.getIcon(IconManager.PASTE));
	}

	/** 
	 * Creates a  {@link PasteRndSettingsCmd} command to execute the action.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		PasteRndSettingsCmd cmd = new PasteRndSettingsCmd(model, 
										PasteRndSettingsCmd.PASTE);
		cmd.execute();
	}
  
}
