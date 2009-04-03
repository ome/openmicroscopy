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
 *  parts of this code have been adapted from the Lucene demo classes. 
 *
 */package search;

 
 
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.similar.MoreLikeThis;

import util.ExceptionHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

// most code from Lucene demo
// each doc in search results is used to make a SearchResultHtml object. 
// List of these is returned

/** Simple command-line based search demo. */
public class SearchFiles {
	
	public static int HITS_PER_PAGE = 10;
	
	public static String index = IndexFiles.INDEX_PATH;
	public static String field = "contents";

  
	/**
	 * This method searches the Lucene index specified by the IndexReader,
	 * using the searchString to create a query. 
	 * NB. This method does not close the reader, so the index can be 
	 * queried to get the documents for each hit BEFORE closing the
	 * IndexReader.
	 * 
	 * @param searchString		A Lucene search string
	 * @param reader		An open IndexReader, to read the Lucene search index
	 * @return		A series of Hits (documents in the Lucene index).
	 * 
	 * @throws CorruptIndexException
	 * @throws ParseException
	 * @throws IOException
	 */
	public static Hits getHits(String searchString, IndexReader reader) throws 
		CorruptIndexException, 
		ParseException, IOException {
	  
		Searcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();

		QueryParser parser = new QueryParser(field, analyzer);
	
		Query query = parser.parse(searchString);
		System.out.println("SearchFiles getHits query: " + query.toString(field));
	
		Hits hits =  searcher.search(query);
	  
		return hits;
	}
  
  
  	
  	/**
  	 * A method to get an index reader, to read a Lucene index. 
  	 * If the index is not found, an exception is thrown, and the user
  	 * gets a dialog asking if they want to create an index, and a 
  	 * file chooser to select the folder of files to index. 
  	 * If they cancel, this method will return null.  
  	 * 
  	 * @param indexPath		Path to the index directory
  	 * @return		An index reader at the location defined by indexPath
  	 */
  	public static IndexReader getIndexReader(String indexPath) {
  		
  		try {
  		  IndexReader reader = IndexReader.open(indexPath);
  	  
  		  return reader;
  		  
  		} catch (IOException ioEx) {
  	    	
  			int result = JOptionPane.showConfirmDialog(null, "Search index not found.\n" +
  					"You need to create an index of all the files you want to search.\n"+
  					"Please choose the root directory containing all your files","Index not found" ,JOptionPane.OK_CANCEL_OPTION);
  			if (result == JOptionPane.YES_OPTION) {
  				IndexFiles.indexFolderContents();
    			
  				// assuming indexing went OK. Try searching again. 
  				try {
  					return getIndexReader(indexPath);
  				} catch (Exception e) {
  					// user didn't index, or indexing failed. 
  					// show error and give user a chance to submit error
  					ExceptionHandler.showErrorDialog("Searching files failed - index not found",
  	    					"", e);
  					e.printStackTrace();
  					return null;
  				}
  			}
  			return null;
  	  	}
  	}
  	
  	
  	/**
  	 * Performs a Lucene search, using the searchString.
  	 * The Hits are used to add reuslt objects to the results list.
  	 * Use the result object's toString() method to get some 
  	 * html for display in a results panel. 
  	 * 
  	 * @param searchString
  	 * @param results
  	 * @throws CorruptIndexException
  	 * @throws IOException
  	 * @throws ParseException
  	 */
  	public static void search(String searchString, List<Object> results) 
  		throws 
  		CorruptIndexException, 
  		IOException, 
  		ParseException {
	  

  		IndexReader reader = getIndexReader(index);
	  
  		/*
  		 * If index couldn't be found and user decided not to index...
  		 */
  		if (reader == null)
  			return;
		  
		  
  		Hits hits = getHits(searchString, reader);
	  
  		if (hits != null) {
  			addHitsToList(hits, results, searchString);
  		}
		  
  		/*
  		 * Have to close the reader AFTER reading the contents to hit list.
  		 */
  		reader.close();
	}
  
  
  	/**
  	 * Performs a Lucene search, using the File to build a more-like-this query.
  	 * The Hits are used to add reuslt objects to the results list.
  	 * Use the result object's toString() method to get some 
  	 * html for display in a results panel. 
  	 * 
  	 * @param findMoreLikeThis
  	 * @param results
  	 * @throws Exception
  	 */
  	public static void search (File findMoreLikeThis, List<Object> results) 
  		throws Exception {
  		Hits hits = null;
	  
  		IndexReader reader = getIndexReader(IndexFiles.INDEX_PATH);
  		
  		/*
  		 * If index could not be found, and user decided not to create one...
  		 */
  		if (reader == null)
  			return;
  
  		Searcher searcher = new IndexSearcher(reader);
	  
  		MoreLikeThis mlt = new MoreLikeThis(reader);
  		//  Reader target = new FileReader(findMoreLikeThis);  // orig source of doc you want to find similarities to
  		Query query = null;
  		try {
  			query = mlt.like(findMoreLikeThis);
  			hits = searcher.search(query);
  			// now the usual iteration thru 'hits' - the only thing to watch for is to make sure
  			// you ignore the doc if it matches your 'target' document, as it should be similar to itself 

  			addHitsToList(hits, results, findMoreLikeThis.getName());
  		} catch (FileNotFoundException ex) {
  			// findMoreLikeThis file not found
  			// show error and give user a chance to submit error
			ExceptionHandler.showErrorDialog("Find-More-Like-This file not found",
					"File Not Found Error", ex);
	  }
	  
	  reader.close();
  }
  
  	/**
  	 * A method used by Lucene searches to convert Hits into Result objects,
  	 * and add them to the results List. 
  	 * 
  	 * @param hits
  	 * @param results
  	 * @param searchString
  	 */
  	public static void addHitsToList(Hits hits, List<Object> results, String searchString) {
	  
  		for (int i = 0; i < hits.length(); i++) {

  			Document doc = null;
			try {
				doc = hits.doc(i);
				results.add(new SearchResultHtml(doc, searchString));
			} catch (CorruptIndexException e) {
				// show error and give user a chance to submit error
				ExceptionHandler.showErrorDialog("CorruptIndexException",
						"", e);
				
			} catch (IOException e) {
				// show error and give user a chance to submit error
				ExceptionHandler.showErrorDialog("I/O Exception",
						"", e);
			}
  		}
  	}
  	
  	
}

