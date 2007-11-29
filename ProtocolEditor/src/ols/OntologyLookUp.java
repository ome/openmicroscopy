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

import java.util.Iterator;
import java.util.Map;

import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

public class OntologyLookUp {
	
	public static Map<String,String> getTermsByName (String name, String ontologyID) {
	
		Map<String, String> map = null;
		try {
			QueryService locator = new QueryServiceLocator();
			Query qs = locator.getOntologyQuery();
			map = qs.getTermsByName(name, ontologyID, false);
			
			/* for (Iterator i = map.keySet().iterator(); i.hasNext();){
				String key = (String) i.next();
				System.out.println(key + " - "+ map.get(key));
			}
			
			System.out.println(map.size() + " results found");
			System.out.println();
			
			String termID = "GO:0032315";
			String ontologyName = "GO"; */
			
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
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return metaDataMap;
	}

}
