
/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import tree.DataFieldConstants;
import tree.DataFieldNode;
import tree.Tree;
import ui.IModel;
import util.ImageFactory;


public class TableImporter 
	extends TextImporter {

	public TableImporter(IModel model) {
		
		initialise(model);
		
		setTitle("Import Table");
		 
		setHeaderMessage("Please paste the table data you wish to import into the text area below. \n" +
			"This should be in tab-delimeted form (eg from Excel).");
		 
		setHeaderIcon(ImageFactory.getInstance().getIcon(ImageFactory.KORGANIZER_ICON));
		
		 
		buildAndDisplayUI();
	}
	
	public void importTextToTree() {
		String wholeText = textArea.getText();
		
		StringReader sr = new StringReader(wholeText);
		
		BufferedReader br = new BufferedReader(sr);
		
		try {
			/*
			 * Create the root node of a tree.
			 */
			
			HashMap<String,String> attributeMap = new HashMap<String,String>();
			attributeMap.put(DataFieldConstants.INPUT_TYPE, DataFieldConstants.PROTOCOL_TITLE);
			attributeMap.put(DataFieldConstants.ELEMENT_NAME, "Imported Table");
			
			DataFieldNode rootNode = new DataFieldNode(attributeMap, tree);
			
			tree.setRootNode(rootNode);
			
			/*
			 * A single child field will hold the table
			 */
			HashMap<String,String> map = new HashMap<String,String>();
			map.put(DataFieldConstants.INPUT_TYPE, DataFieldConstants.TABLE);
			map.put(DataFieldConstants.ELEMENT_NAME, "Table");
			
			String newLine = br.readLine();
			int colCount = 0;
			int rowIndex = 0;
			while (newLine != null) {
				
				/*
				 * For each new line, Take the tab-delimited text and turn into 
				 * comma-delimited text. 
				 */
				if (newLine.length() > 0) {
					String rowData = newLine.replace("\t", ", ");
					System.out.println("rowData: " + rowData);
					int cols = rowData.split(",").length;
					colCount = Math.max(colCount, cols);
					map.put(DataFieldConstants.ROW_DATA_NUMBER + rowIndex, rowData);
				}
				
				newLine = br.readLine();
				rowIndex++;
			}
			
			map.put(DataFieldConstants.TABLE_ROW_COUNT, rowIndex + "");
			
			String columnNames = "";
			for (int i=0; i<colCount; i++) {
				if (i>0)
					columnNames = columnNames + ", ";
				columnNames = columnNames + "column" + (i+1);
			}
			System.out.println("columnNames: " + columnNames);
			map.put(DataFieldConstants.TABLE_COLUMN_NAMES, columnNames);
			
			DataFieldNode newNode = new DataFieldNode(map, tree);
			rootNode.addChild(newNode);
			
			model.openTree((Tree)tree);
			
		} catch (IOException ioEx) {
			// TODO Auto-generated catch block
			ioEx.printStackTrace();
		}
	}

	
}
