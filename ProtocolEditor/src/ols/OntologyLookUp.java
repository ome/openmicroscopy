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

package ols;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

public class OntologyLookUp {
	
	public static Map<String,String> getTermsByName (String name, String ontologyID) {
	
		System.out.println("OntologyLookUp getTermsByName: " + name + " " + ontologyID);
		
		boolean removeObsoleteTerms = false;
		
		Map<String, String> map = null;
		try {
			QueryService locator = new QueryServiceLocator();
			Query qs = locator.getOntologyQuery();
			map = qs.getTermsByName(name, ontologyID, false);
			
			// This would be nice but is FAR TOO SLOW (have to make a SOAP call for every term!). 
			if (removeObsoleteTerms) {
				ArrayList<String> obsoleteTermIds = new ArrayList<String>();
				for (Iterator i = map.keySet().iterator(); i.hasNext();){
					String key = (String) i.next();
					if(qs.isObsolete(key, ontologyID)) {
						obsoleteTermIds.add(key);
						//System.out.println("OLS term isObsolete " + key);
					}
				}
				for(String termId: obsoleteTermIds) {
					map.remove(termId);
				}
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public static Map<String,String> getTermMetadata (String termID, String ontologyID) {
			
		Map<String, String> metaDataMap = null;
			
		try {
			QueryService locator = new QueryServiceLocator();
			Query qs = locator.getOntologyQuery();
			metaDataMap = qs.getTermMetadata(termID, ontologyID);
			
			
			for (Iterator i = metaDataMap.keySet().iterator(); i.hasNext();){
				String key = (String) i.next();
				System.out.println(key + " - "+ metaDataMap.get(key));
			} 
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return metaDataMap;
	}
	
	
	public static Map<String,String> getTermParents (String termID, String ontologyID) {
		
		Map<String, String> parentsMap = null;
			
		try {
			QueryService locator = new QueryServiceLocator();
			Query qs = locator.getOntologyQuery();
			parentsMap = qs.getTermParents(termID, ontologyID);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return parentsMap;
	}
	
	
	public static Map<String,String> getAllTermsFromOntology (String ontologyId) {
		
		Map<String, String> parentsMap = null;
			
		try {
			QueryService locator = new QueryServiceLocator();
			Query qs = locator.getOntologyQuery();
			parentsMap = qs.getAllTermsFromOntology(ontologyId);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return parentsMap;
	}
	
	public static String getTermName(String termId) {
		
		String termName = "";
		try {
			QueryService locator = new QueryServiceLocator();
			Query qs = locator.getOntologyQuery();
			termName = qs.getTermById(termId, Ontologies.getOntologyIdFromTermId(termId));
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return termName;
	}

}
