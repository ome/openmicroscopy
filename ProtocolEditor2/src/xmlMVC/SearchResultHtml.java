package xmlMVC;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class SearchResultHtml {
	
	String path;
	SearchPanel searchPanel;
	
	String paneText;
	
	
	public SearchResultHtml(Document doc, String searchString) {
		
		String[] searchTerms = searchString.split(" ");
		
		String name = null;
		path = doc.get("path");
		String date = doc.get("modified");
        name = doc.get("name");

        File file = new File(path);
		
        if (file.exists()){
        	paneText = "<a href='http:/" + path + "'>"+ name + "</a> <br>";
        
        	paneText = paneText + date + "<br>";
        } else {
        	paneText = "<u>" + name + "</u> <br>";
        }
        
        
        
        try {
        	ArrayList<HashMap> elements = new XMLMethods().getAllXmlFileAttributes(file);
        	
        	for (HashMap element: elements) {
        		//System.out.println("Element has " + element.size() + " attributes");
        		//System.out.println("SearchResultsHtml name: " + nameValuePair[0] + " value: " + nameValuePair[1]);
			
        		Iterator attributeIterator = element.keySet().iterator();
        		while (attributeIterator.hasNext()) {
        			
        			Object key = attributeIterator.next();
        			String attributeName = (String)key;
        			String value = (String)element.get(key);
        			
        			// look for each search term within the document. 
        			// if it's there, present it in context
        			for (int i=0; i<searchTerms.length; i++) {
            		
        				if (value.contains(searchTerms[i])) {
        					String labelText = value;
        					
//        					 take some context - either side of the searchTerm
        					int startIndex = labelText.indexOf(searchTerms[i]);
        					int endIndex = startIndex + searchTerms[i].length();
        					startIndex = startIndex - 12;
        					endIndex = endIndex + 12;
        					
        					if (startIndex < 0) {
        						startIndex = 0;
        					} else {
        						startIndex = labelText.indexOf(" ", startIndex);
        					}
        					
        					if (endIndex > labelText.length()) {
        						endIndex = labelText.length();
        					} else {
        						int nextSpace = labelText.indexOf(" ", endIndex);
        						if (nextSpace > endIndex) endIndex = nextSpace;
        					}
        					
        					labelText = labelText.substring(startIndex, endIndex);
        					
        					labelText = labelText.replace(searchTerms[i], "<b>" + searchTerms[i] + "</b>");
        					
        					// remove the attribute name from the elementPath
        					int lastSlashIndex = attributeName.lastIndexOf("/");
        					if (lastSlashIndex > 1)
        						attributeName = attributeName.substring(0, lastSlashIndex);
        					
        					paneText = paneText + attributeName + ": " + labelText + "<br>";
        				}
        			}
            	}
        	}
        	 
        } catch (FileNotFoundException ex) {
        	paneText = paneText + "File not found<br>";
        }
	}
	
	public String getPaneText() {
		return paneText;
	}
}
