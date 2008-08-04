 /*
 * search.IndexTermFinder 
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

//Third-party libraries

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;

//Application-internal dependencies

/** 
 * This class queries a Lucene search index for terms (or tokens). 
 * 
 * Can be used for auto-complete:
 * Eg. I start typing "Ant..." and the auto-complete suggests 
 * Antibody
 * 
 * One problem is case sensitivity: Everything is lower case in 
 * Lucene index.
 * 
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class IndexTermFinder {

	/**
	 * The path to the Lucene index. 
	 */
	private String index;
	
	/**
	 * The field of Lucene documents that has been indexed, and contains
	 * all the tokens of interest.
	 */
	private String docField;
	
	/**
	 * Creates an instance of this class.
	 * 
	 * @param index		Path to the index to query. 
	 * 					The field of the Lucene document is null, so all fields
	 * 					will be searched.
	 */
	public IndexTermFinder(String index) {
		this(index, null);
	}
	
	/**
	 * Creates an instance of this class, specifying the Lucene search index
	 * and the field of the index (or document) to search.
	 * 
	 * @param index		Path to the Lucene index
	 * @param field		Field in the Lucene document (or index) to search.
	 */
	public IndexTermFinder(String index, String field) {
		
		this.index = index;
		this.docField = field;
	}
	
	/**
	 * For the index and field specified in the constructor (or setter methods)
	 * this method will return all the tokens that start with the searchString.
	 * 
	 * @param searchString		Look for words in the index that start with this
	 * @return		An array of tokens (words) in the index that match.
	 */
	public String[] getMatchingTerms(String searchString) {
		
		if (index == null) {
			return new String[0];
		}
		
		searchString = searchString.toLowerCase();
		
		ArrayList<String> matchingTerms = new ArrayList<String>();

	
	    IndexReader reader;
		try {
			reader = IndexReader.open(index);
			TermEnum terms = reader.terms();
		    
		    int termCounter = 0;
		    while (terms.next()) {
		    	// ignore terms unless from the field of interest
		    	String field = terms.term().field();
		    	if (! field.equals(docField))
		    		continue;
		    	
		    	String term = terms.term().toString();
		    	term = term.replaceFirst(field + ":", "");
		    	
		    	if (term.startsWith(searchString)) {
		    		termCounter++;
		    		
		    		matchingTerms.add(term);
		    	}
		    }
		      
		    reader.close();
		} catch (CorruptIndexException e) {
			// Ignore if can't find index...
			// e.printStackTrace();
		} catch (IOException e) {
			// ... or if index can't be read.
			//e.printStackTrace();
		}

	    String[] results = new String[matchingTerms.size()]; 
	    int termIndex = 0;
	    for (String term : matchingTerms) {
	    	results[termIndex] = term;
	    	termIndex++;
	    }
	    
	    return results;
	  }
	
	/**
	 * Sets the index.
	 * 
	 * @param index		A path to the Lucene index you want to search.
	 */
	public void setIndex(String index){
		this.index = index;
	}
	
	/**
	 * The field within the Lucene index that you want to search.
	 * If this is null, all fields will be searched. 
	 * 
	 * @param field
	 */
	public void setField(String field) {
		this.docField = field;
	}
}
