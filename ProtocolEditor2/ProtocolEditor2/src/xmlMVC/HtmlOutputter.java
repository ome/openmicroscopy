package xmlMVC;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class HtmlOutputter {
	
	static FileWriter fileWriter;
	static String outputFileName;
	
	public static void outputHTML (DataFieldNode parentRootNode) {
		
		PrintWriter outputStream = null;
		fileWriter = null;
		outputFileName = "print.html";
		
	
		
        try {
        	fileWriter = new FileWriter(outputFileName);
        	
            outputStream = new PrintWriter(fileWriter);
            
            outputStream.println("<html><head>");
            outputStream.println("<style type='text/css'>");
            outputStream.println("div {padding: 5px 30px 5px 30px; margin: 5px; font-family: Arial;}");
            outputStream.println(".protocol {background: #dddddd; padding: 5px; font-size: 120%; border: 1px #390d61 solid;}"); 
            outputStream.println(".title {background: #dddddd; padding: 5px; font-size: 110%; border-bottom: 1px #390d61 solid;}");
            outputStream.println("h3 {padding: 0px; margin:0px; font-size: 110%;}");
            outputStream.println("</style>");
            outputStream.println("</head><body>");
            
            DataField protocolField = parentRootNode.getDataField();
            printDataField(protocolField, outputStream);
            	
            printDataFieldTree(outputStream, parentRootNode);
            
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
	
	public static void printDataFieldTree(PrintWriter outStream, DataFieldNode parentNode) {
		
		ArrayList<DataFieldNode> children = parentNode.getChildren();
		if (children.size() == 0) return;
		
		outStream.println("<div style='padding: 5px 30px 5px 30px;'>");
		
		for (DataFieldNode child: children) {
			DataField dataField = child.getDataField();
			printDataField(dataField, outStream);
			
			if (!(dataField.getAttribute(DataField.SUBSTEPS_COLLAPSED).equals("true"))) {
				printDataFieldTree(outStream, child);
			}
		}
		
		
		outStream.println("</div>");
	}
	
	
	
	public static void printDataField (DataField dataField, PrintWriter outputStream) {
        	
		LinkedHashMap<String, String> allAttributes = dataField.getAllAttributes();
        	
        	if (allAttributes.get(DataField.INPUT_TYPE).equals(DataField.PROTOCOL_TITLE))
        		outputStream.println("<div class='protocol'>");
        	else outputStream.println("<div>");
        	//outputStream.println("<p>");
        	
        	outputStream.println("<h3>" + allAttributes.get(DataField.ELEMENT_NAME) + "</h3>");
        	            	
        	// print any remaining attributes
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

        	outputStream.println("</div>");

	}

}
