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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;



//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a test class, to look at the possibility of using auto-complete
 * based on the contents of a Lucene index. 
 * Eg. I start typing "Ant..." and the auto-complete suggests 
 * Antibody! 
 * Not sure where you'd want this (all the time)?
 * 
 * One problem is case sensitivity: Everything is lower case in 
 * Lucene index.
 * 
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

	public static String index = IndexFiles.INDEX_PATH;
	public static String field = "contents";
	
	  public static String[] getMatchingTerms(String searchString) {
		  
		  
		  Date start = new Date();
		  
		searchString = searchString.toLowerCase();
		  
		String searchField = "contents";
		
		ArrayList<String> matchingTerms = new ArrayList<String>();

	
	    IndexReader reader;
		try {
			reader = IndexReader.open(index);
			TermEnum terms = reader.terms();
		    
		    int termCounter = 0;
		    while (terms.next()) {
		    	// ignore terms unless from the field of interest
		    	String field = terms.term().field();
		    	if (! field.equals(searchField))
		    		continue;
		    	
		    	String term = terms.term().toString();
		    	term = term.replaceFirst(field + ":", "");
		    	
		    	if (term.startsWith(searchString)) {
		    		System.out.println(termCounter + " " + term);
		    		termCounter++;
		    		
		    		matchingTerms.add(term);
		    	}
		    }
		    
		    Date end = new Date();
		      System.out.println("Search took " + (end.getTime() - start.getTime()) +
		    		  " milliseconds");
		      
		    reader.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    String[] results = new String[matchingTerms.size()]; 
	    int termIndex = 0;
	    for (String term : matchingTerms) {
	    	results[termIndex] = term;
	    	termIndex++;
	    }
	    
	    return results;
	  }
}
