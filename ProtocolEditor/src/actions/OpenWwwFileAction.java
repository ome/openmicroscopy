package actions;

/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ui.IModel;
import util.FileDownload;
import util.ImageFactory;

public class OpenWwwFileAction 
	extends ProtocolEditorAction {
	
	public OpenWwwFileAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Open file from URL");
		putValue(Action.SHORT_DESCRIPTION, "Open an on-line file");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.WWW_FILE_ICON)); 
	}


	public void actionPerformed(ActionEvent e) {
	
		openWwwFile();
	}
	
	public void openWwwFile() {
		Object[] possibilities = {"http://users.openmicroscopy.org.uk/~will/protocolFiles/Oligofectamine-Invitrogen.pro.xml",
				"http://users.openmicroscopy.org.uk/~will/protocolFiles/BubRI-staining.pro.xml",
				"http://users.openmicroscopy.org.uk/~will/protocolFiles/OME-XML-completeExample.xml",
				"http://users.openmicroscopy.org.uk/~will/protocolFiles/OntologiesExample.pro.xml"};
		String url = (String)JOptionPane.showInputDialog(
                frame,
                "Enter a url for an XML file to open:",
                "Open a www file",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possibilities,
                "");
		
		try {
			File downloadedFile = FileDownload.downloadFile(url);
			model.openThisFile(downloadedFile);
		} catch (MalformedURLException ex) {
			JOptionPane.showMessageDialog(frame, "invalid URL, please try again");
		}

	}
}
