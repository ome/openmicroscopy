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

package util;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import tree.DataField;
import tree.DataFieldNode;
import ui.FormField;

// crude attempt at getting html from tree of datafields etc. 
// needs some more work to improve look of output!

public class HtmlOutputter {
	
	static FileWriter fileWriter;
	static String outputFileName;
	
	public static String HEADER = "<html><head> \n" +
		"<style type='text/css'> \n" +
		"div {padding: 2px 0px 2px 30px; margin: 0px; font-family: Arial;} \n" +
		".protocol {background: #dddddd; padding: 5px; font-size: 120%; border: 1px #390d61 solid;} \n" +
		".elementName {font-size: 110%;} \n" +
		".attribute {font-size: 80%;} \n" +
		".title {background: #dddddd; padding: 5px; font-size: 110%; border-bottom: 1px #390d61 solid;} \n" +
		"h3 {padding: 0px; margin:0px; font-size: 110%;} \n" +
		"</style> \n" +
		"</head><body>";
	public static String FOOTER = "</body></html>";
	
	public static String DIV = "<div>";
	public static String DIV_CLASS_PROTOCOL = "<div class='protocol'>";
	public static String DIV_CLASS_ATTRIBUTE = "<div class='attribute'>";
	public static String DIV_END = "</div>";
	
	public static String SPAN_CLASS_ELEMENT_NAME = "<span class='elementName'>";
	public static String SPAN_END = "</span>";
	
	public static String UNDERLINE = "<u>";
	public static String UNDERLINE_END = "</u>";
	
	public static final String BROWSER_TOOL_TIP = "title='Please return to Protocol Editor to expand or collapse, then print again'";
	public static String RIGHT_ARROW = "<img src='http://morstonmud.com/omero/arrow_right.gif' width='15' height='13'" + BROWSER_TOOL_TIP + ">";
	public static String DOWN_ARROW = "<img src='http://morstonmud.com/omero/arrow_down.gif' width='13' height='15'" + BROWSER_TOOL_TIP + ">";
	
	public static String TABLE = "<table cellspacing='1' cellpadding='5' bgcolor='black'>";
	public static String TABLE_END = "</table>";
	public static String TABLE_ROW = "<tr>";
	public static String TABLE_ROW_END = "</tr>";
	public static String TABLE_DATA = "<td bgcolor='#eeeeee'>";
	public static String TABLE_DATA_END = "</td>";
	
	
	/**
	 * Output the tree file as an html document. 
	 * @param parentRootNode	the root of the tree
	 * @param showEveryField	if false, only show children of non-collapsed nodes. If true, show all
	 * @param showDescriptions	if true, show the description attribute
	 * @param showDefaultValues	if true, show the default value attribute
	 * @param showUrl			if true, show the url attribute
	 * @param showAllOtherAttributes	if true, show any other additional attributes
	 */
	public static void outputHTML (DataFieldNode parentRootNode, boolean showEveryField, 
			boolean showDescriptions, boolean showDefaultValues, 
			boolean showUrl, boolean showAllOtherAttributes, boolean printTableData) {

		ArrayList<DataFieldNode> nodes = new ArrayList<DataFieldNode>();
		nodes.add(parentRootNode);
		
		outputHTML (nodes, showEveryField, showDescriptions, showDefaultValues, 
				showUrl, showAllOtherAttributes, printTableData);
	}
	
	
	/**
	 * Output the tree file as an html document. 
	 * @param rootNodes			the a list of nodes you want to print
	 * @param showEveryField	if false, only show children of non-collapsed nodes. If true, show all
	 * @param showDescriptions	if true, show the description attribute
	 * @param showDefaultValues	if true, show the default value attribute
	 * @param showUrl			if true, show the url attribute
	 * @param showAllOtherAttributes	if true, show any other additional attributes
	 */
	public static void outputHTML (ArrayList<DataFieldNode> rootNodes, boolean showEveryField, 
			boolean showDescriptions, boolean showDefaultValues, 
			boolean showUrl, boolean showAllOtherAttributes, boolean printTableData) {
		
		PrintWriter outputStream = null;
		fileWriter = null;
		outputFileName = "print.html";
		
        try {
        	fileWriter = new FileWriter(outputFileName);
        	
            outputStream = new PrintWriter(fileWriter);
            
            outputStream.print(HEADER);
            
            for (DataFieldNode parentRootNode: rootNodes) {
            	DataField protocolField = parentRootNode.getDataField();
            	printDataField(protocolField, outputStream, showEveryField, showDescriptions, showDefaultValues, 
					 showUrl,  showAllOtherAttributes, printTableData);
            	
            	if (!protocolField.isAttributeTrue(DataField.SUBSTEPS_COLLAPSED) || 
    					(showEveryField)) {
            		printDataFieldTree(outputStream, parentRootNode, showEveryField, showDescriptions, showDefaultValues, 
            				showUrl,  showAllOtherAttributes, printTableData);
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
        
        
        File findMyDirectory = new File("");
        String currentDirectory = findMyDirectory.getAbsolutePath();
        
        String outputFilePath = "file://" + currentDirectory + "/" + outputFileName;
        
        outputFilePath = outputFilePath.replaceAll(" ", "%20");
        
        BareBonesBrowserLaunch.openURL(outputFilePath);
		
	}
	
	public static void printDataFieldTree(PrintWriter outStream, DataFieldNode parentNode, boolean showEveryField, 
			boolean showDescriptions, boolean showDefaultValues, 
			boolean showUrl, boolean showAllOtherAttributes, boolean printTableData) {
		
		ArrayList<DataFieldNode> children = parentNode.getChildren();
		if (children.size() == 0) return;
		
		outStream.println(DIV);
		
		for (DataFieldNode child: children) {
			DataField dataField = child.getDataField();
			printDataField(dataField, outStream, showEveryField, showDescriptions, showDefaultValues, 
					 showUrl,  showAllOtherAttributes, printTableData);
			
			
			if (!dataField.isAttributeTrue(DataField.SUBSTEPS_COLLAPSED) || 
					(showEveryField)) {
				printDataFieldTree(outStream, child, showEveryField, showDescriptions, showDefaultValues, 
						 showUrl,  showAllOtherAttributes, printTableData);
			}
		}
		
		
		outStream.println(DIV_END);
	}
	
	
	
	public static void printDataField (DataField dataField, PrintWriter outputStream, 
			boolean showEveryField, 
			boolean showDescriptions, boolean showDefaultValues, 
			boolean showUrl, boolean showAllOtherAttributes, boolean printTableData) {
		
		boolean subStepsCollapsed = dataField.isAttributeTrue(DataField.SUBSTEPS_COLLAPSED);
		if (showEveryField)
			subStepsCollapsed = false;
			
		LinkedHashMap<String, String> allAttributes = dataField.getAllAttributes();
        	
		String divHeader = "";
			
        if (dataField.isAttributeEqualTo(DataField.INPUT_TYPE,DataField.PROTOCOL_TITLE))
        	divHeader = DIV_CLASS_PROTOCOL;
        else divHeader = DIV;
        
        if (true) {
        	String colour = dataField.getAttribute(DataField.BACKGROUND_COLOUR);
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
        	
        String value = allAttributes.get(DataField.VALUE);
        String units = allAttributes.get(DataField.UNITS);
        	
        outputStream.print(SPAN_CLASS_ELEMENT_NAME);
        	
        if (dataField.hasChildren()) {
        	if (subStepsCollapsed) {
        		outputStream.print(RIGHT_ARROW);
        	} else {
        		outputStream.print(DOWN_ARROW);
        	}
        }
        outputStream.print(allAttributes.get(DataField.ELEMENT_NAME));
        	
        if (value != null) outputStream.print(": " + UNDERLINE + value + UNDERLINE_END);
        if (units != null) outputStream.print(" " + units);
        outputStream.println(SPAN_END);
        	
        if ((showDefaultValues) && (allAttributes.get(DataField.DEFAULT) != null)) {
        	outputStream.print(DIV_CLASS_ATTRIBUTE + "Default Value = " +
        			allAttributes.get(DataField.DEFAULT) + DIV_END);
        }
        if ((showDescriptions) && (allAttributes.get(DataField.DESCRIPTION) != null)) {
        	outputStream.print(DIV_CLASS_ATTRIBUTE + 
        			allAttributes.get(DataField.DESCRIPTION) + DIV_END);
        }
        if ((showUrl) && (allAttributes.get(DataField.URL) != null)) {
        	outputStream.print(DIV_CLASS_ATTRIBUTE + "URL = " +
        			allAttributes.get(DataField.URL) + DIV_END);
        }
        if (showAllOtherAttributes) {
        	printAllAttributes(allAttributes, outputStream);
        }
        if (printTableData) {
        	printTableData(allAttributes, outputStream);
        }
        
        outputStream.println(DIV_END);

	}
	
	public static void printAllAttributes(LinkedHashMap<String, String> allAttributes, PrintWriter outputStream) {
		
//		 print any remaining attributes
    	Iterator keyIterator = allAttributes.keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String name = (String)keyIterator.next();
			String value = allAttributes.get(name);

    		if (value == null) value="";
    		if (name == null) name = "";
    		
    		if ((name.equals(DataField.INPUT_TYPE)) || (name.equals(DataField.ELEMENT_NAME))||
    				(name.equals(DataField.DESCRIPTION)) || (name.equals(DataField.VALUE)) ||
    				(name.equals(DataField.URL)) || (name.equals(DataField.SUBSTEPS_COLLAPSED)) ||
    				(name.equals(DataField.BACKGROUND_COLOUR)) ||
    				(name.equals(DataField.DEFAULT)))
    				continue;
    		else name = name + ": ";
    		
    		outputStream.println(DIV_CLASS_ATTRIBUTE + name + value + DIV_END);
    	}
	}
	
	public static void printTableData(LinkedHashMap<String, String> allAttributes, PrintWriter outputStream) {
		
		String colNames = allAttributes.get(DataField.TABLE_COLUMN_NAMES);
		if (colNames == null) return;
		
		outputStream.println(TABLE);
		outputStream.println(TABLE_ROW);
		String[] colHeaders = colNames.split(",");
		for (int col=0; col<colHeaders.length; col++){
			outputStream.print("<th bgcolor='#eeeeee'>" + colHeaders[col] + "</th>");
		}
		outputStream.println(TABLE_ROW_END);
		
		String rowNumberData = DataField.ROW_DATA_NUMBER;
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

}
