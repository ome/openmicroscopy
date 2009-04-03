 /*
 * cmd.ExportWikiCmd 
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
package cmd;

//Java imports

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

//Third-party libraries

//Application-internal dependencies

import tree.DataFieldNode;
import util.BareBonesBrowserLaunch;
import util.IExport;
import wiki.WikiEdit;
import wiki.WikiExport;

/** 
 * Class for outputting an Editor file to a wiki. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ExportWikiCmd implements ActionCmd {
	
	String fileExtension = ".html";
	
	JFrame frame = null;
	
	List<DataFieldNode> rootNodes;
	
	public ExportWikiCmd(List<DataFieldNode> rootNodes) {
		this.rootNodes = rootNodes;
	}
	
	public ExportWikiCmd(DataFieldNode rootNode) {
		rootNodes = new ArrayList<DataFieldNode>();
		rootNodes.add(rootNode);
	}
	
	public void execute() {
		exportToWiki();
	}
	
	public void exportToWiki() {
		
		String pageName = JOptionPane.showInputDialog(
				"Enter the wiki page to export to:", "Ignore for now!");
		
		if (pageName == null) 
			return;
		
		IExport exporter = new WikiExport();
		String wikiText = exporter.exportToString(rootNodes);
		
		System.out.println(wikiText);
		
		try {
			WikiEdit.edit(wikiText, "Edited by OMERO.editor", false);
			displayWiki();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void displayWiki() {
		
        BareBonesBrowserLaunch.openURL("http://en.wikipedia.org/wiki/User:Will-moore-dundee");
	}

}

