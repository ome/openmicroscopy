/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AttachmentPopupMenu 
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.IconManager;

/** 
 * Popup menu used to handle the attachments.
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
class AttachmentPopupMenu 	
	extends JPopupMenu
	implements ActionListener
{

	/** Action to delete the selected elements. */
	private static final int DELETE = 0;
	
	/** Action to edit the selected elements. */
	private static final int DOWNLOAD = 1;
	
	/** Button to delete the selected elements. */
	private JMenuItem 		delete;
	
	/** Button to browse the selected elements. */
	private JMenuItem 		download;
	
	/** Reference to the view. */
	private AttachmentsUI	uiDelegate;
	
	/** Initializes the components. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		delete = new JMenuItem("Remove");
		delete.addActionListener(this);
		delete.setActionCommand(""+DELETE);
		delete.setIcon(icons.getIcon(IconManager.REMOVE));
		delete.setToolTipText("Remove the file.");
		
		download = new JMenuItem("Download");
		download.addActionListener(this);
		download.setActionCommand(""+DOWNLOAD);
		download.setIcon(icons.getIcon(IconManager.DOWNLOAD));
		download.setToolTipText("Download the file.");
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		add(download);
		add(delete);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param uiDelegate The view.
	 */
	AttachmentPopupMenu(AttachmentsUI uiDelegate)
	{
		if (uiDelegate == null)
			throw new IllegalArgumentException("No view.");
		this.uiDelegate = uiDelegate;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Removes the attachment or downloads it.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case DELETE:
				uiDelegate.removeSelectedAttachment(); 
				break;
			case DOWNLOAD:
				uiDelegate.downloadSelectedAttachment();  
		}
	}
	
}
