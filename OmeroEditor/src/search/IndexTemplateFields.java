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
 *
 * This code is adapted from the Apache demo classes:
 * (see below)
 */

package search;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.lucene.index.IndexWriter;
import org.xml.sax.SAXParseException;

import tree.DataFieldConstants;
import util.PreferencesManager;
import util.XMLMethods;
import xmlMVC.ConfigConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFileChooser;

// almost all Lucene demo code for indexing files. 

/** Index all text files under a directory. */
public class IndexTemplateFields {
  
  private IndexTemplateFields() {}

  public static final String INDEX_TEMPLATE_PATH = ConfigConstants.OMERO_EDITOR_FILE + File.separator + "indexTemplate";
  
  // this index is separate from the keyword search index
  static final File INDEX_DIR = new File(INDEX_TEMPLATE_PATH);
  
  static DateFormat fDateFormat = DateFormat.getDateInstance (DateFormat.LONG);
  static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
  
  /** Index all xml files that contain elements and attributes that match a template*/
  
  public static void indexFiles(String rootFolder) {
    
    if (INDEX_DIR.exists()) {
    	System.out.println("Index already exists. Writing over old index...>!");
      //System.out.println("Cannot save index to '" +INDEX_DIR+ "' directory, please delete it first");
      //System.exit(1);
    }
    
    final File docDir = new File(rootFolder);
    if (!docDir.exists() || !docDir.canRead()) {
      System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    
    Date start = new Date();
    try {
      IndexWriter writer = new IndexWriter(INDEX_DIR, new StandardAnalyzer(), true);
      System.out.println("Indexing to directory '" +INDEX_DIR+ "'...");
      indexDocs(writer, docDir);
      System.out.println("Optimizing...");
      writer.optimize();
      writer.close();

      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }

  static void indexDocs(IndexWriter writer, File file)
    throws IOException {
    // do not try to index files that cannot be read
    if (file.canRead()) {
      if (file.isDirectory()) {
        String[] files = file.list();
        // an IO error could occur
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            indexDocs(writer, new File(file, files[i]));
          }
        }
      } else {
        System.out.println("IndexFiles: adding " + file + "......");
        try {
        	
            // make a new, empty document
            Document doc = new Document();
            
//          Add the url as a field named "path".  Use a field that is 
    		// indexed (i.e. searchable), but don't tokenize the field into words.
    		doc.add(new Field("path", file.getAbsolutePath(), Field.Store.YES,
    				Field.Index.UN_TOKENIZED));
    		
    		doc.add(new Field("name", file.getName(), Field.Store.YES,
    				Field.Index.UN_TOKENIZED));

    		// Add the last modified date of the file a field named "modified".  
    		// Use a field that is indexed (i.e. searchable), but don't tokenize
    		// the field into words.
    		doc.add(new Field("modified",
            		fDateFormat.format(file.lastModified()) + " " + timeFormat.format(file.lastModified()),
                    Field.Store.YES, Field.Index.UN_TOKENIZED));
    		
    		// the whole doc is read with a File Reader, not saved, used for searching.
    		//doc.add(new Field("contents", new FileReader(file)));
    		
    		// need a "snippet" field for clustering search results with Carrot2
    		// this should be a String????
    		// but for clustering, want all of the document (or a large chunk?)
    		
    		
        	try{
        		XMLMethods xmlMethods = new XMLMethods();
        		ArrayList<HashMap<String, String>> elements = xmlMethods.getAllXmlFileAttributes(file, xmlMethods.new ElementAttributesHashMapHandler());
        	
        		// got through the whole doc (not sure that this improves clustering over eg 10 elements)
        		for (HashMap<String, String> element: elements) {
    
        				String attributeName = element.get(DataFieldConstants.ELEMENT_NAME);
        				String value = element.get(DataFieldConstants.VALUE);
        				
        				if ((value != null) && (attributeName != null)){
        					
        					attributeName = attributeName.replace(" ", "");
        					System.out.println("IndexTemplateFields   " + attributeName + "    " + value);
        					
        					doc.add(new Field(attributeName, value, Field.Store.YES,
        		    				Field.Index.UN_TOKENIZED));
        				}
        			
        		}
        	} catch (FileNotFoundException ex) {
	        } catch (SAXParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
    		
            writer.addDocument(doc);
            
            System.out.println("IndexFiles.indexDocs: Added document to writer.");
        //  writer.addDocument(FileDocument.Document(file));
        }
        // at least on windows, some temporary files raise this exception with an "access denied" message
        // checking if the file can be read doesn't help
        catch (FileNotFoundException fnfe) {
        	System.out.println("Exception - doc not added to writer.");
        }
      }
    }
  }
  
  
  public static void indexTemplateFields() {
		 //Create a file chooser
		final JFileChooser fc = new JFileChooser();
			
		File rootFolderLocation = null;
		if (PreferencesManager.getPreference(PreferencesManager.ROOT_FILES_FOLDER) != null) {
			rootFolderLocation = new File(PreferencesManager.getPreference(PreferencesManager.ROOT_FILES_FOLDER));
		} else if (PreferencesManager.getPreference(PreferencesManager.CURRENT_FILES_FOLDER) != null) {
			rootFolderLocation = new File(PreferencesManager.getPreference(PreferencesManager.CURRENT_FILES_FOLDER));
		}
		fc.setCurrentDirectory(rootFolderLocation);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File folderToIndex = fc.getSelectedFile();
			String path = folderToIndex.getAbsolutePath();
			// remember this location
			PreferencesManager.setPreference(PreferencesManager.ROOT_FILES_FOLDER, path);
	            
			indexFiles(path);
		}
	}
  
}
