
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

package util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldNode;
import tree.IAttributeSaver;
import ui.components.AlarmSetter;
import ui.formFields.FormField;

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
	public String DIV_END = "";
	
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
	 * Exports the given dataField nodes as a String. 
	 * This method works by creating a temporary file, delegating the
	 * export to that file, using {@link #export(File, List)} and then 
	 * converting the file to a String. The temp file is deleted before 
	 * returning the String.
	 * 
	 * @param rootNodes		A list of the nodes to export
	 * @return String		A string representation of the export. 
	 */
	public String exportToString(List<DataFieldNode> rootNodes) 
	{
		File tempFile = new File("temp");
		
		export(tempFile, rootNodes);
		
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(tempFile));
			char[] buf = new char[1024];
			int numRead=0;
			while((numRead=reader.read(buf)) != -1){
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String exportString = fileData.toString();
		
		//tempFile.delete();
		
		return exportString;
	}
	
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
		exportPreferences.put(SHOW_OTHER_ATTRIBUTES, false);
		
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
            	IAttributeSaver protocolField = parentRootNode.getDataField();
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
			IAttributeSaver dataField = child.getDataField();
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
	private void printDataField (IAttributeSaver dataField, PrintWriter outputStream, 
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
			
		HashMap<String, String> allAttributes = new HashMap<String, String>(dataField.getAllAttributes());
        	
		String divHeader = "";
			
		String inputType = dataField.getAttribute(DataFieldConstants.INPUT_TYPE);
        if  ((inputType != null) && (inputType.equals(DataFieldConstants.PROTOCOL_TITLE)))
        	divHeader = DIV_CLASS_PROTOCOL;
        else divHeader = DIV;
        
        String colour = dataField.getAttribute(DataFieldConstants.BACKGROUND_COLOUR);
        if (colour != null) {
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
        	
        if (dataField instanceof DataField) {
	        if (((DataField)dataField).hasChildren()) {
	        	if (subStepsCollapsed) {
	        		outputStream.print(RIGHT_ARROW);
	        	} else {
	        		outputStream.print(DOWN_ARROW);
	        	}
	        }
        }
        outputStream.print(allAttributes.get(DataFieldConstants.ELEMENT_NAME));
        	
        if (value != null) outputStream.print(": " + UNDERLINE + value + UNDERLINE_END);
        if (units != null) outputStream.print(" " + units);
        outputStream.println(SPAN_END);
        	
        if (DataFieldConstants.DATE_TIME_FIELD.equals(inputType)) {
        	String date = "Date: ";
        	String UTCmillisecs = dataField.getAttribute(DataFieldConstants.UTC_MILLISECS);
			if (UTCmillisecs != null) {
				Calendar cal = new GregorianCalendar();
				cal.setTimeInMillis(new Long(UTCmillisecs));
				if (cal.get(Calendar.YEAR) == 1970) {
					int days = cal.get(Calendar.DAY_OF_MONTH) - 1; // 1st of month is 0 days
					date = date + days + " Days.";
				} else {
					SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
					date = date + sdf.format(cal.getTimeInMillis());
				}
			} else {
				date = date + "none set.";
			}
			
			String time = dataField.getAttribute(DataFieldConstants.SECONDS);
			if (time != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				date = date + " at " + sdf.format(new Long(time + "000")) + ".";
			}
			
			String alarmSecs = dataField.getAttribute(DataFieldConstants.ALARM_SECONDS);
			if (alarmSecs != null) {
				date = date + " Alarm: " + AlarmSetter.alarmSecsToString(alarmSecs);
			}
			date = DIV_CLASS_ATTRIBUTE + date + DIV_END;
			
			outputStream.print(date);
        }
        
        if ((showDefaultValues) && (allAttributes.get(DataFieldConstants.DEFAULT) != null)) {
        	outputStream.print(DIV_CLASS_ATTRIBUTE + "Default Value = " +
        			allAttributes.get(DataFieldConstants.DEFAULT) + DIV_END);
        }
        if (showDescriptions) {
        	printDescription(allAttributes.get(DataFieldConstants.DESCRIPTION),
        			outputStream);
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
	
	protected void printDescription(String description, PrintWriter outputStream) {
		if (description != null) {
        	outputStream.print(DIV_CLASS_ATTRIBUTE + 
        			description + DIV_END);
        }
	}
	
	private void printAllAttributes(HashMap<String, String> allAttributes, PrintWriter outputStream) {
		
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
	
	private void printTableData(HashMap<String, String> allAttributes, PrintWriter outputStream) {
		
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
	
	/**
	 * Convenience method for converting html-formatted strings to tag-free
	 * strings.
	 * 
	 * @param withTags		A string containing br, u and b tags. 
	 * @return			The same string, without the br, u and b tags. 
	 */
	public static String removeHtmlTags(String withTags) 
	{
		if (withTags == null) return null;
		
		String noTags = withTags.replace("<br>", "\n");
		noTags = noTags.replace("<br />", "");
		noTags = noTags.replace("<u>", "");
		noTags = noTags.replace("</u>", "");
		noTags = noTags.replace("<b>", "");
		noTags = noTags.replace("</b>", "");
		
		return noTags;
	}

}
