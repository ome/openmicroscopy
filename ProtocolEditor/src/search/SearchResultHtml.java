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

package search;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.xml.sax.SAXParseException;

import util.XMLMethods;

// each Lucene doc returned in search is used to construct one of this class
// html for the search result is build from the original document 

public class SearchResultHtml {
	
	String path;
	String name;
	String date;
	
	SearchPanel searchPanel;
	
	String resultHtml;
	String[] searchTerms;
	
	File file;
	
	public SearchResultHtml(Document doc, String searchString) {
		
		searchTerms = searchString.split(" ");
		
		name = null;
		path = doc.get("path");
		date = doc.get("modified");
        name = doc.get("name");

        file = new File(path);

	}
	
	public String getHtmlText() {
		
		// if file not found...
        if (!file.exists()){
        	resultHtml = "<u>" + name + "</u> <br>" + 
        	"File not found at " + path + "<br>";
        	return resultHtml;
        }
        
        resultHtml = "<a href='http:/" + path + "'>"+ name + "</a> <br>";
        
    	resultHtml = resultHtml + date + "<br>";
        
    	// if html file, 
        if ((file.getName().endsWith(".html")) || (file.getName().endsWith(".htm"))) {
        	resultHtml = resultHtml + "To view the html page in a browser, click the link above.<br>";
        } else {
        	// try parsing xml to get context...
		
			try {
				
				// this returns a hashMap of attributes for each element. 
				// key is elementPath/attributeName, value is attribute value.
	        	ArrayList<HashMap> elements = new XMLMethods().getAllXmlFileAttributes(file);
	        	
	        	for (HashMap element: elements) {
	    
	        		Iterator attributeIterator = element.keySet().iterator();
	        		while (attributeIterator.hasNext()) {
	        			
	        			Object key = attributeIterator.next();
	        			String attributePathAndName = (String)key;
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
	        						if (startIndex < 0) startIndex = 0;
	        					}
	        					
	        					int labelTextLength = labelText.length();
	        					if (endIndex > labelTextLength) {
	        						endIndex = labelTextLength;
	        					} else {
	        						int nextSpace = labelText.indexOf(" ", endIndex);
	        						if (nextSpace > endIndex) endIndex = nextSpace;
	        					}
	        					
	        					labelText = labelText.substring(startIndex, endIndex);
	        					
	        					labelText = labelText.replace(searchTerms[i], "<b>" + searchTerms[i] + "</b>");
	        					
	        					// remove the attribute name from the elementPath
	        					int lastSlashIndex = attributePathAndName.lastIndexOf("/");
	        					if (lastSlashIndex > 1)
	        						attributePathAndName = attributePathAndName.substring(0, lastSlashIndex);
	        					
	        					resultHtml = resultHtml + attributePathAndName + ": " + labelText + "<br>";
	        				}
	        			}
	            	}
	        	}
	        	 
	        } catch (FileNotFoundException ex) {
	        	resultHtml = resultHtml + "File not found<br>";
	        } catch (SAXParseException ex) {
	        	resultHtml = "<u>" + name + "</u> <br>" + date + "<br>Cannot read file at " + path + ", due to badly-formed XML<br>";
	        }
        }
        
		return resultHtml;
	}
}
