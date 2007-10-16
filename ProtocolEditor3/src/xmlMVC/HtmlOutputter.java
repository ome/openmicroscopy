package xmlMVC;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

// crude attempt at getting html from tree of datafields etc. 
// needs some more work to improve look of output!

public class HtmlOutputter {
	
	static FileWriter fileWriter;
	static String outputFileName;
	
	public static final String BROWSER_TOOL_TIP = "title='Please return to Protocol Editor to expand or collapse, then print again'";
	public static final String RIGHT_ARROW = "<img src='http://morstonmud.com/omero/arrow_right.gif' width='15' height='13'" + BROWSER_TOOL_TIP + ">";
	public static final String DOWN_ARROW = "<img src='http://morstonmud.com/omero/arrow_down.gif' width='13' height='15'" + BROWSER_TOOL_TIP + ">";
	
	public static void outputHTML (DataFieldNode parentRootNode, boolean showDescriptions) {
		
		PrintWriter outputStream = null;
		fileWriter = null;
		outputFileName = "print.html";
		
        try {
        	fileWriter = new FileWriter(outputFileName);
        	
            outputStream = new PrintWriter(fileWriter);
            
            outputStream.println("<html><head>");
            outputStream.println("<style type='text/css'>");
            outputStream.println("div {padding: 2px 0px 2px 30px; margin: 0px; font-family: Arial;}");
            outputStream.println(".protocol {background: #dddddd; padding: 5px; font-size: 120%; border: 1px #390d61 solid;}"); 
            outputStream.println(".elementName {font-size: 110%;}"); 
            outputStream.println(".title {background: #dddddd; padding: 5px; font-size: 110%; border-bottom: 1px #390d61 solid;}");
            outputStream.println("h3 {padding: 0px; margin:0px; font-size: 110%;}");
            outputStream.println("</style>");
            outputStream.println("</head><body>");
            
            DataField protocolField = parentRootNode.getDataField();
            printDataField(protocolField, outputStream, showDescriptions);
            	
            printDataFieldTree(outputStream, parentRootNode, showDescriptions);
            
            outputStream.println("</body></html>");
            
            
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
	
	public static void printDataFieldTree(PrintWriter outStream, DataFieldNode parentNode, boolean showDescriptions) {
		
		ArrayList<DataFieldNode> children = parentNode.getChildren();
		if (children.size() == 0) return;
		
		outStream.println("<div>");
		
		for (DataFieldNode child: children) {
			DataField dataField = child.getDataField();
			printDataField(dataField, outStream, showDescriptions);
			
			String hideChildren = dataField.getAttribute(DataField.SUBSTEPS_COLLAPSED);
			if ((hideChildren == null) || (hideChildren.equals("false"))) {
				printDataFieldTree(outStream, child, showDescriptions);
			}
		}
		
		
		outStream.println("</div>");
	}
	
	
	
	public static void printDataField (DataField dataField, PrintWriter outputStream, boolean showDescriptions) {
		
		boolean subStepsCollapsed = false;
		String collapsed = dataField.getAttribute(DataField.SUBSTEPS_COLLAPSED);
		if ((collapsed != null) && (collapsed.equals(DataField.TRUE))) subStepsCollapsed = true;
			
		LinkedHashMap<String, String> allAttributes = dataField.getAllAttributes();
        	
        	if (allAttributes.get(DataField.INPUT_TYPE).equals(DataField.PROTOCOL_TITLE))
        		outputStream.println("<div class='protocol'>");
        	else outputStream.println("<div>");
        	
        	String value = allAttributes.get(DataField.VALUE);
        	String units = allAttributes.get(DataField.UNITS);
        	
        	outputStream.print("<span class='elementName'>");
        	
        	if (dataField.hasChildren()) {
        		if (subStepsCollapsed) {
        			outputStream.print(RIGHT_ARROW);
        		} else {
        			outputStream.print(DOWN_ARROW);
        		}
        	}
        	outputStream.print(allAttributes.get(DataField.ELEMENT_NAME));
        	
        	if (value != null) outputStream.print(": <u>" + value + "</u>");
        	if (units != null) outputStream.print(" " + units);
        	outputStream.println("</span>");
        	
        	if ((showDescriptions) && (allAttributes.get(DataField.DESCRIPTION) != null)) {
        		outputStream.print("<div class='description'>" + 
        				allAttributes.get(DataField.DESCRIPTION) + "</div>");
        	}
        	
        	outputStream.println("</div>");

	}
	
	public static void printAllAttributes(LinkedHashMap<String, String> allAttributes, PrintWriter outputStream) {
		
//		 print any remaining attributes
    	Iterator keyIterator = allAttributes.keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String name = (String)keyIterator.next();
			String value = allAttributes.get(name);

    		if (value == null) value="";
    		if (name == null) name = "";
    		
    		if ((name.equals(DataField.INPUT_TYPE)) || (name.equals(DataField.ELEMENT_NAME))) continue;

    		if (name.equals(DataField.DESCRIPTION) || (name.equals(DataField.VALUE)))
    			name = "";
    		else name = name + ": ";
    		
    		if ((value.length() > 0) &&
    				!(name.equals(DataField.SUBSTEPS_COLLAPSED + ": "))) {
    			outputStream.println(name + value);
    			outputStream.println("<br>");
    		}
    	}
	}

}
