 /*
 * search.TemplateSearch 
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
package search;

//Java imports

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//Third-party libraries

//Application-internal dependencies

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldNode;
import tree.IAttributeSaver;
import tree.Tree;
import tree.Tree.Actions;
import ui.FormDisplay;
import ui.components.CustomDialog;
import util.XMLMethods;

import cmd.OpenFileCmd;


/** 
 * A template search uses a Protocol file as a search form.
 * All the fields of the experiment are blank, and the user fills 
 * a small number to look for other experiments that have the
 * same values in the same fields. 
 * Currently, the position of the field in the hierarchy is ignored.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TemplateSearch 
	extends CustomDialog {
	
	/**
	 * A bound property of this class. Change notified when the search
	 * completes, in order that the SearchController can obtain the 
	 * results and display them. 
	 */
	public static final String TEMPLATE_SEARCH_DONE = "templateSearchDone";
	
	/**
	 * The Tree model that is created from the template file chosen by the
	 * user. Root of this is passed to the UI to display.
	 * When search starts, this tree is used to get filled fields.
	 * to build search query.
	 */
	Tree templateTree;
	
	/**
	 * The UI for displaying the Tree, allowing user to fill fields.
	 */
	FormDisplay treeDisplay;
	
	/**
	 * The result Lucene Hits are filtered to match the search fields, then
	 *  used to build result text objects 
	 * eg SearchResultHtml, added to the results list. 
	 */
	List<Object> results = new ArrayList<Object>();
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * Calls super() to create a new Dialog. This also calls 
	 * setHeaderComponents() and displays the dialog.
	 * 
	 * Then the user is shown a FileChooser, to pick an Ediotr file
	 * to use for template search. This is displayed with all fields cleared. 
	 * 
	 */
	public TemplateSearch() {
	
		super("Template Search");
		
		/*
		 * Get a file from user...
		 */
		File templateFile = OpenFileCmd.getFileFromUser();
		
		Document doc = null;
		try {
			// .. convert to DOM document
			doc = XMLMethods.readXMLtoDOM(templateFile);
			
		} catch (SAXException e) {
			
			JOptionPane.showMessageDialog(null, "Could not read template", 
					"Error reading template", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			
		}
		
		if (doc != null) {
			/*
			 * And create the Tree data-structure. 
			 */
			templateTree = new Tree(doc);
		} 
		else templateTree = new Tree();
		
		
		treeDisplay = new FormDisplay(templateTree.getRootNode());
		
		/*
		 * Clear the fields in the Tree, ready for user to enter search terms
		 */
		templateTree.editTree(Actions.CLEAR_FIELDS);
		
		JScrollPane treeScroller = new JScrollPane(treeDisplay,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		/*
		 * And display the Tree in the Dialog. 
		 */
		setDialogContent(treeScroller);
	}

	/**
	 * After the user hits the Search button...
	 * 
	 * This iterates through the Tree template, checking which fields are
	 * filled, using dataField.isFieldFilled();
	 * Filled fields are used to compile a Lucene search string, which 
	 * will return documents with all the words from the name and value
	 * of the filled fields.
	 * 
	 * The returned documents are filtered, for ones that match the 
	 * filled fields of the template (name and value), and these are used
	 * to build a List of results (Objects that have a toString() for display). 
	 * A propertyChange event is fired with this as the newValue.
	 */
	public void actionPerformed(ActionEvent e) {
		
		/*
		 * 
		 */
		results.clear();
		
		ArrayList<IAttributeSaver> searchFields = 
			new ArrayList<IAttributeSaver>();
		
		Iterator<DataFieldNode> iterator = 
			templateTree.getRootNode().iterator();
		
		String searchQuery = "";
		
		while (iterator.hasNext()) {
			DataField field = (DataField)iterator.next().getDataField();
			
			if (field.isFieldFilled()) {
				/*
				 * Ignore fixed steps and table fields (too complex!).
				 */
				String fieldType = field.getAttribute(DataFieldConstants.INPUT_TYPE);
				if (DataFieldConstants.FIXED_PROTOCOL_STEP.equals(fieldType) ||
						DataFieldConstants.TABLE.equals(fieldType) ||
						DataFieldConstants.PROTOCOL_TITLE.equals(fieldType)) 
					continue;
				
				String fieldName = field.getAttribute(
						DataFieldConstants.ELEMENT_NAME);
				String[] fieldValues = field.getValues();
				String values = "";
				for (int i=0; i<fieldValues.length; i++) {
					String val = fieldValues[i];
					if (val != null) {
						/*
						 * Concatenate values, with a space
						 */
						if (values.length() > 0) values = values + " ";
						values = values + val;
						
						/*
						 * Keep a list of fields used in the search
						 */
						searchFields.add(field);
					}
				}
				
				/*
				 * Concatenate search terms, with a space
				 */
				if (searchQuery.length() > 0) searchQuery = searchQuery + " ";
				searchQuery = searchQuery + fieldName + " " + values;
				
			}	
		}
		
		searchQuery = "+" + searchQuery.replace(" ", " +");
		
		System.out.println("TemplateSearch searchQuery = " + searchQuery);
		
		IndexReader reader = SearchFiles.getIndexReader(IndexFiles.INDEX_PATH);
		
		/*
		 * If no search index found, and user decided not to create one...
		 */
		if (reader == null) 
			return;
		
		try {
			Hits hits = SearchFiles.getHits(searchQuery, reader);
			
			
			org.apache.lucene.document.Document doc = null;
        	
			Date startTime = new Date();
			
			for (int i=0; i<hits.length(); i++) {
				doc = hits.doc(i);
				String path = doc.get("path");
				String date = doc.get("modified");
		        String name = doc.get("name");

		        File file = new File(path);
		        
		        String[] attsToMatch = new String[] {
						DataFieldConstants.ELEMENT_NAME,
						DataFieldConstants.VALUE };
		       // int fieldMatches = getFieldMatches(file, searchFields);
		       int fieldMatches = getFieldMatches(file, searchFields, attsToMatch);
		        
		      //   System.out.println("TemplateSearch File: " + path + "" +
		     //   		" has: " + fieldMatches + " matches." + saxMatches);
		        
		        if (fieldMatches >= searchFields.size()) {
		        	results.add(new SearchResultHtml(doc, searchQuery));
		        }
			}
			this.firePropertyChange(TEMPLATE_SEARCH_DONE, null, results);
			
			Date endTime = new Date();
        	
			System.out.println("TemplateSearch millisecs: " +
					(endTime.getTime() - startTime.getTime()));
			
			/*
			 * Need to close reader AFTER reading docs from hits. 
			 */
			reader.close();
			
		} catch (CorruptIndexException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	/**
	 * This method delegates to XMLMethods.getFieldMatches, which uses a 
	 * SAX parser to query the XML file for elements with the attributes
	 * in attsToMatch. 
	 * If the values of these match those of the field.getAttribute(string), 
	 * then this is a "FieldMatch".
	 * The whole XML file is checked for each field in the list and the 
	 * total number of matches is returned. 
	 * 
	 * @param xmlFile	XML file to search	
	 * @param fields	A list of fields where getAttribute(String name) is called
	 * @param attsToMatch	The list of attributes that must match for each field.
	 * @return		Total matches for all fields through the whole XML file.
	 */
	public int getFieldMatches(File xmlFile, List<IAttributeSaver> fields,
			String[] attsToMatch) {
		return XMLMethods.getFieldMatches(xmlFile, fields, attsToMatch);
	}
	
	/**
	 * This uses a DOM document to achieve the same functionality as for
	 * getFieldMatches. The attributes checked are elementName and value. 
	 * 
	 * @param xmlFile
	 * @param fields
	 * @return
	 */
	public int getFieldMatches(File xmlFile, List<IAttributeSaver> fields) {
		
		int matches = 0;
		
		try {
			Document domDoc = XMLMethods.readXMLtoDOM(xmlFile);
			
			String elementType;
			String[] attsToCheck = new String[] {
					DataFieldConstants.ELEMENT_NAME,
					DataFieldConstants.VALUE };
			
			for (IAttributeSaver field : fields) {
				elementType = field.getAttribute(DataFieldConstants.INPUT_TYPE);
				
				NodeList nodes = domDoc.getElementsByTagName(elementType);
				for (int i=0; i<nodes.getLength(); i++) {
					Element element = (Element)nodes.item(i);
					if (fieldsMatch(field, element, attsToCheck)) {
						matches++;
					}
				}
				
			}
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return matches;
	}
	
	/**
	 * Do two fields match, based on the named attributes? 
	 * 
	 * @param field1
	 * @param field2
	 * @param attsToCheck
	 * @return	true if the values of the named attributes are the same in both
	 * fields. 
	 */
	public boolean fieldsMatch(IAttributeSaver field1, Element field2,
			String[] attsToCheck) {
		boolean allMatch = true;
		String val1;
		String val2;
		for (int i=0; i<attsToCheck.length; i++) {
			val1 = field1.getAttribute(attsToCheck[i]);
			val2 = field2.getAttribute(attsToCheck[i]);
			if (val1 == null) {
				if (val2 == null) {
					continue;
				} else {
					return false;
				}
			}
			// val1 not null
			else {
				if (! val1.equals(val2)) {
					return false;
				}
			}
		}
		
		return allMatch;
	}
	

	@Override
	/**
	 * When the dialog first appears, constructor will call this. 
	 * Returns a new JPanel.
	 */
	public JComponent getDialogContent() {
		
		return new JPanel();
	}

	@Override
	/**
	 * Constructor calls this to display dialog. 
	 * Sets the text of the "OK" button to "Search" and sets Header message. 
	 */
	public void setHeaderComponents() {
		
		setOkButtonText("Search");
		
		setHeaderMessage("Choose a template file to use as a search form.\n" +
				"Fill in the required fields for search, and hit search.");
	}

}
