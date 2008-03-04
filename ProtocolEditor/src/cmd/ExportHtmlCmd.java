package cmd;

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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import tree.DataFieldNode;
import ui.components.ExportDialog;
import util.BareBonesBrowserLaunch;
import util.DefaultExport;
import util.HtmlExport;
import util.IExport;
import util.PreferencesManager;

public class ExportHtmlCmd implements ActionCmd {
	
	String fileExtension = ".html";
	
	JFrame frame = null;
	
	List<DataFieldNode> rootNodes;
	
	public ExportHtmlCmd(List<DataFieldNode> rootNodes) {
		this.rootNodes = rootNodes;
	}
	
	public ExportHtmlCmd(DataFieldNode rootNode) {
		rootNodes = new ArrayList<DataFieldNode>();
		rootNodes.add(rootNode);
	}
	
	public void execute() {
		printToHtml();
	}
	
public void printToHtml() {
		
		// Can't work out how to export xsl file into .jar
		// xmlModel.transformXmlToHtml();
		
		// use this method for now
		
		final JFileChooser fc = new JFileChooser();
		
		File currentLocation = null;
		if (PreferencesManager.getPreference(PreferencesManager.CURRENT_EXPORT_FOLDER) != null) {
			currentLocation = new File(PreferencesManager.getPreference(PreferencesManager.CURRENT_EXPORT_FOLDER));
		} 
		fc.setCurrentDirectory(currentLocation);

		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.getName().endsWith(fileExtension) || (f.isDirectory()))
					return true;
				return false;
			}
			public String getDescription() {
				return fileExtension + " files";
			}
		});
		int returnVal = fc.showSaveDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = fc.getSelectedFile();
		if (file.isDirectory()) {
            JOptionPane.showMessageDialog(frame, "Please choose a file name (not a directory)");
            // try again! 
            printToHtml();
            return;
        }
		
		// remember where the user last exported a file
		PreferencesManager.setPreference(PreferencesManager.CURRENT_EXPORT_FOLDER, file.getParent());
		
		// now, make sure the filename ends with correct file extension. 
        String filePathAndName = file.getAbsolutePath();
        if (!(filePathAndName.endsWith(fileExtension))) {	// if not..
        	filePathAndName = filePathAndName + fileExtension;
        	file = new File(filePathAndName);
        }
		
        
        // set up a Map of output options - used to create a dialog
		LinkedHashMap<String,Boolean> booleanMap = new LinkedHashMap<String,Boolean>();
		booleanMap.put(DefaultExport.SHOW_ALL_FIELDS, false);
		booleanMap.put(DefaultExport.SHOW_DESCRIPTIONS, true);
		booleanMap.put(DefaultExport.SHOW_DEFAULT_VALUES, true);
		booleanMap.put(DefaultExport.SHOW_URL, true);
		booleanMap.put(DefaultExport.SHOW_TABLE_DATA, true);
		booleanMap.put(DefaultExport.SHOW_OTHER_ATTRIBUTES, false);
	
		
		ExportDialog printDialog = new ExportDialog(frame, null, "Print Options", booleanMap);
		printDialog.pack();
		printDialog.setVisible(true);
		
		if (printDialog.getValue().equals(JOptionPane.OK_OPTION)) {
		
			booleanMap = printDialog.getBooleanMap();
		
			export(file, booleanMap);
			
			displayOutput(file);
		}
	}

	public void export(File file, Map<String, Boolean> booleanMap) {
		IExport exporter = new HtmlExport();
		exporter.export(file, rootNodes, booleanMap);
	}

	public void displayOutput(File file) {
		String outputFilePath = file.getAbsolutePath();
        
        if (System.getProperty("os.name").startsWith("Mac OS")) {
        	outputFilePath = "file://" + outputFilePath;
        } else {
        	outputFilePath = "file:///" + outputFilePath;
        }
        
        outputFilePath = outputFilePath.replaceAll(" ", "%20");

        
        BareBonesBrowserLaunch.openURL(outputFilePath);
	}

}
