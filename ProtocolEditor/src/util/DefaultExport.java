package util;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldNode;
import ui.formFields.FormField;

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

public class DefaultExport 
	implements IExport {
	
	// attributes for export preferences.
	// each one may or may-not be in the exportPreferences boolean map. 
	public static final String SHOW_ALL_FIELDS = "Show every field (include collapsed fields)";
	public static final String SHOW_DESCRIPTIONS = "Show descriptions";
	public static final String SHOW_DEFAULT_VALUES = "Show default values";
	public static final String SHOW_URL = "Show Url";
	public static final String SHOW_TABLE_DATA = "Show Table Data";
	public static final String SHOW_OTHER_ATTRIBUTES = "Show all other attributes";
	
	
	
	static FileWriter fileWriter;
	
	
	protected String HEADER = "";
	protected String FOOTER = "";
	
	public String DIV = "\n";
	public String DIV_CLASS_PROTOCOL = "\n";
	public String DIV_CLASS_ATTRIBUTE = "\n";
	public String DIV_END = "\n";
	
	public String SPAN_CLASS_ELEMENT_NAME = "";
	public String SPAN_END = "";
	
	public String UNDERLINE = "";
	public String UNDERLINE_END = "";
	
	public String RIGHT_ARROW = "";
	public String DOWN_ARROW = "";
	
	public String TABLE = "";
	public String TABLE_END = "";
	public String TABLE_ROW = "\n";
	public String TABLE_ROW_END = "";
	public String TABLE_DATA = "     ";
	public String TABLE_DATA_END = "";
	
	
	
	/**
	 * Simplest export. Export the given nodes to the file.
	 *  
	 * @param file		Where the file will be written to. 
	 * @param rootNodes	The list of nodes to export (could be a single 'root' node)
	 */
	public void export(File file, List<DataFieldNode> rootNodes) {
		
		Map<String, Boolean> exportPreferences = new HashMap<String, Boolean>();
		
		exportPreferences.put(SHOW_ALL_FIELDS, true);
		exportPreferences.put(SHOW_DESCRIPTIONS, true);
		exportPreferences.put(SHOW_DEFAULT_VALUES, true);
		exportPreferences.put(SHOW_URL, true);
		exportPreferences.put(SHOW_TABLE_DATA, true);
		exportPreferences.put(SHOW_OTHER_ATTRIBUTES, true);
		
		export(file, rootNodes, exportPreferences);
	}
	
	
	/**
	 * Export according to a preference Map. Export the given nodes to the file.
	 *  
	 * @param outputFile		Where the file will be written to. 
	 * @param rootNodes	The list of nodes to export (could be a single 'root' node)
	 * @param exportPreferences		A Map of boolean values. eg printDefaultValue
	 */
	public void export(File outputFile, List<DataFieldNode> rootNodes, Map<String, Boolean> exportPreferences) {
		
		boolean showEveryField = isAttributeTrue(exportPreferences, SHOW_ALL_FIELDS);
		
		PrintWriter outputStream = null;
		fileWriter = null;
		
        try {
        	fileWriter = new FileWriter(outputFile);
        	
            outputStream = new PrintWriter(fileWriter);
            
            outputStream.print(HEADER);
            
            for (DataFieldNode parentRootNode: rootNodes) {
            	DataField protocolField = parentRootNode.getDataField();
            	printDataField(protocolField, outputStream, exportPreferences);
            	
            	if (!protocolField.isAttributeTrue(DataFieldConstants.SUBSTEPS_COLLAPSED) || 
    					(showEveryField)) {
            		printDataFieldTree(outputStream, parentRootNode, exportPreferences);
            	}
            }
            
            outputStream.println(FOOTER);
            
            
        } catch (Exception ex) {
        	ex.printStackTrace();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
        
	}
	
	
	/**
	 * Recursive method that takes a root Node, and writes it to output stream, then processes the
	 * tree below that root.
	 * 
	 * @param outStream		The destination of the output
	 * @param parentNode	The starting (root) node
	 * @param exportPreferences		A boolean map of preferences. 
	 */
	public void printDataFieldTree(PrintWriter outStream, DataFieldNode parentNode, Map<String, Boolean> exportPreferences) {
		
		boolean showEveryField = isAttributeTrue(exportPreferences, SHOW_ALL_FIELDS);

		ArrayList<DataFieldNode> children = parentNode.getChildren();
		if (children.size() == 0) return;
		
		outStream.println(DIV);
		
		for (DataFieldNode child: children) {
			DataField dataField = child.getDataField();
			printDataField(dataField, outStream, exportPreferences);
			
			
			if (!dataField.isAttributeTrue(DataFieldConstants.SUBSTEPS_COLLAPSED) || 
					(showEveryField)) {
				printDataFieldTree(outStream, child, exportPreferences);
			}
		}
		
		
		outStream.println(DIV_END);
	}
	
	
	
	/**
	 * This method writes a single dataField to the output stream. 
	 * 
	 * @param dataField
	 * @param outputStream
	 * @param exportPreferences
	 */
	private void printDataField (DataField dataField, PrintWriter outputStream, 
			Map<String, Boolean> exportPreferences) {
		
		boolean showEveryField = isAttributeTrue(exportPreferences, SHOW_ALL_FIELDS);
		boolean showDescriptions = isAttributeTrue(exportPreferences, SHOW_DESCRIPTIONS);
		boolean showDefaultValues = isAttributeTrue(exportPreferences, SHOW_DEFAULT_VALUES); 
		
		boolean showUrl = isAttributeTrue(exportPreferences, SHOW_URL);
		boolean showAllOtherAttributes = isAttributeTrue(exportPreferences, SHOW_OTHER_ATTRIBUTES);
		boolean printTableData = isAttributeTrue(exportPreferences, SHOW_TABLE_DATA);
		
		boolean subStepsCollapsed = false;
		if (!showEveryField)
			subStepsCollapsed = dataField.isAttributeTrue(DataFieldConstants.SUBSTEPS_COLLAPSED);
			
		LinkedHashMap<String, String> allAttributes = dataField.getAllAttributes();
        	
		String divHeader = "";
			
        if (dataField.isAttributeEqualTo(DataFieldConstants.INPUT_TYPE,DataFieldConstants.PROTOCOL_TITLE))
        	divHeader = DIV_CLASS_PROTOCOL;
        else divHeader = DIV;
        
        if (true) {
        	String colour = dataField.getAttribute(DataFieldConstants.BACKGROUND_COLOUR);
        	Color bgColour = FormField.getColorFromString(colour);
        	if (bgColour != null) {
        		int r = bgColour.getRed();
        		int g = bgColour.getGreen();
        		int b = bgColour.getBlue();
        	
        		String htmlColour = " style='background-color:" + "rgb("+
        			r + "," + g + "," + b + ")'>";
        		divHeader = divHeader.replace(">", htmlColour);
        	}
        }
        
        outputStream.print(divHeader);
        	
        String value = allAttributes.get(DataFieldConstants.VALUE);
        String units = allAttributes.get(DataFieldConstants.UNITS);
        	
        outputStream.print(SPAN_CLASS_ELEMENT_NAME);
        	
        if (dataField.hasChildren()) {
        	if (subStepsCollapsed) {
        		outputStream.print(RIGHT_ARROW);
        	} else {
        		outputStream.print(DOWN_ARROW);
        	}
        }
        outputStream.print(allAttributes.get(DataFieldConstants.ELEMENT_NAME));
        	
        if (value != null) outputStream.print(": " + UNDERLINE + value + UNDERLINE_END);
        if (units != null) outputStream.print(" " + units);
        outputStream.println(SPAN_END);
        	
        if ((showDefaultValues) && (allAttributes.get(DataFieldConstants.DEFAULT) != null)) {
        	outputStream.print(DIV_CLASS_ATTRIBUTE + "Default Value = " +
        			allAttributes.get(DataFieldConstants.DEFAULT) + DIV_END);
        }
        if ((showDescriptions) && (allAttributes.get(DataFieldConstants.DESCRIPTION) != null)) {
        	outputStream.print(DIV_CLASS_ATTRIBUTE + 
        			allAttributes.get(DataFieldConstants.DESCRIPTION) + DIV_END);
        }
        if ((showUrl) && (allAttributes.get(DataFieldConstants.URL) != null)) {
        	outputStream.print(DIV_CLASS_ATTRIBUTE + "URL = " +
        			allAttributes.get(DataFieldConstants.URL) + DIV_END);
        }
        if (showAllOtherAttributes) {
        	printAllAttributes(allAttributes, outputStream);
        }
        if (printTableData) {
        	printTableData(allAttributes, outputStream);
        }
        
        outputStream.println(DIV_END);
	}
	
	
	private void printAllAttributes(LinkedHashMap<String, String> allAttributes, PrintWriter outputStream) {
		
		//		 print any remaining attributes
		Iterator keyIterator = allAttributes.keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String name = (String)keyIterator.next();
			String value = allAttributes.get(name);

			if (value == null) value="";
			if (name == null) name = "";
   		
			if ((name.equals(DataFieldConstants.INPUT_TYPE)) || (name.equals(DataFieldConstants.ELEMENT_NAME))||
   				(name.equals(DataFieldConstants.DESCRIPTION)) || (name.equals(DataFieldConstants.VALUE)) ||
   				(name.equals(DataFieldConstants.URL)) || (name.equals(DataFieldConstants.SUBSTEPS_COLLAPSED)) ||
   				(name.equals(DataFieldConstants.BACKGROUND_COLOUR)) ||
   				(name.equals(DataFieldConstants.DEFAULT)))
   				continue;
			else name = name + ": ";
   		
			outputStream.println(DIV_CLASS_ATTRIBUTE + name + value + DIV_END);
		}
	}
	
	private void printTableData(LinkedHashMap<String, String> allAttributes, PrintWriter outputStream) {
		
		String colNames = allAttributes.get(DataFieldConstants.TABLE_COLUMN_NAMES);
		if (colNames == null) return;
		
		outputStream.println(TABLE);
		outputStream.println(TABLE_ROW);
		String[] colHeaders = colNames.split(",");
		for (int col=0; col<colHeaders.length; col++){
			outputStream.print("<th bgcolor='#eeeeee'>" + colHeaders[col] + "</th>");
		}
		outputStream.println(TABLE_ROW_END);
		
		String rowNumberData = DataFieldConstants.ROW_DATA_NUMBER;
		int rowIndex = 0;
		String rowData = allAttributes.get(rowNumberData + rowIndex);
		
		while(rowData != null) {
			outputStream.println(TABLE_ROW);
			String[] cellData = rowData.split(",");
			for (int col=0; col<cellData.length; col++){
				outputStream.print(TABLE_DATA + cellData[col] + TABLE_DATA_END);
			}
			outputStream.println(TABLE_ROW_END);
			
			rowIndex++;
			rowData = allAttributes.get(rowNumberData + rowIndex);
		}
		
		outputStream.println(TABLE_END);
	}
	
	// simple method to get booleans from the map (returns false if attribute not present (null)).
	private static boolean isAttributeTrue(Map<String, Boolean> map, String attribute) {
		if (map.get(attribute) == null)
			return false;
		else 
			return map.get(attribute);
	}

}
