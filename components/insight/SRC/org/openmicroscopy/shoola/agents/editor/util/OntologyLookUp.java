/*
 * org.openmicroscopy.shoola.agents.editor.util.OntologyLookUp
 * 
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

package org.openmicroscopy.shoola.agents.editor.util;

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


//Third-party libraries
import uk.ac.ebi.ook.web.services.Query;
import uk.ac.ebi.ook.web.services.QueryService;
import uk.ac.ebi.ook.web.services.QueryServiceLocator;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.env.log.LogMessage;

/** 
 * This class handles common calls to the Ontology Lookup Service.
 * Uses functionality provided by the ols-client.jar. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class OntologyLookUp
{
	
	/**
	 * Handles the exception while retrieving data from an ontology.
	 * 
	 * @param e The exception to handle.
	 * @param name The name of the method.
	 */
	private static void handleException(Exception e, String name)
	{
		LogMessage msg = new LogMessage();
		msg.print(name);
		msg.print(e);
		EditorAgent.getRegistry().getLogger().error(OntologyLookUp.class, msg);
	}
	
	/**
	 * Searches a specified ontology for terms that match the partial name 
	 * given. Doesn't remove obsolete terms! 
	 * Returns the results as a Map, with terms identified by their
	 * Ontology:TermID string. E.g:
	 * Map key "GO:0000236"
	 * Map object "mitotic prometaphase"
	 * 
	 * @param name			The partial name of the term. E.g. "mito"
	 * @param ontologyID	The ontology identifier. E.g. "GO"
	 * 
	 * @return		Map of terms 
	 */
	public static Map<String,String> getTermsByName(String name, 
			String ontologyID)
	{
	
		boolean removeObsoleteTerms = false;
		
		Map<String, String> map = null;
		try {
			QueryService locator = new QueryServiceLocator();
			
			// this requires the axis.jar and jaxrpc.jar
			// jaxrpc project is now jax-ws (web services) but the 
			// JAXWS2.1.5 jar can't replace the jaxrpc.jar. 
			Query qs = locator.getOntologyQuery();
			map = qs.getTermsByName(name, ontologyID, false);
			
			// This would be nice but is FAR TOO SLOW (have to make a SOAP call for every term!). 
			if (removeObsoleteTerms) {
				List<String> obsoleteTermIds = new ArrayList<String>();
				for (Iterator i = map.keySet().iterator(); i.hasNext();){
					String key = (String) i.next();
					if(qs.isObsolete(key, ontologyID)) {
						obsoleteTermIds.add(key);
					}
				}
				for(String termId: obsoleteTermIds) {
					map.remove(termId);
				}
			}
			
		} catch (Exception e) {
			handleException(e, "getTermsByName");
		}
		return map;
	}
	
	/**
	 * Returns a Map of metadata associated with an ontology term. 
	 * Includes..
	 * definition: 		Text description that defines the term's meaning
	 * exact_synonym:
	 * related_synonym:
	 * 
	 * @param termID	The id of the term to handle.
	 * @param ontologyID The Ontology identifier. E.g. "GO"
	 * @return
	 */
	public static Map<String,String> getTermMetadata(String termID,
			String ontologyID)
	{
			
		Map<String, String> metaDataMap = null;
			
		try {
			QueryService locator = new QueryServiceLocator();
			Query qs = locator.getOntologyQuery();
			metaDataMap = qs.getTermMetadata(termID, ontologyID);
			
		} catch (Exception e) {
			handleException(e, "getTermMetadata");
		}
		return metaDataMap;
	}
	
	/**
	 * Gets the terms that are the parents of the defined term.
	 * 
	 *  @param termID	The id of the term to handle.
	 * @param ontologyID The Ontology identifier. E.g. "GO"
	 * @return
	 */
	public static Map<String,String> getTermParents(String termID,
			String ontologyID)
	{
		
		Map<String, String> parentsMap = null;
			
		try {
			QueryService locator = new QueryServiceLocator();
			Query qs = locator.getOntologyQuery();
			parentsMap = qs.getTermParents(termID, ontologyID);
			
		} catch (Exception e) {
			handleException(e, "getTermParents");
		}
		return parentsMap;
	}
	
	/**
	 * Method for getting the name of a term, based on its term ID and 
	 * ontology ID. 
	 * 
	 * @param termId	The ID of the term. 
	 * @param ontologyID 	The ontology to search. E.g. "GO"
	 * @return
	 */
	public static String getTermName(String termId, String ontologyID)
	{
		String termName = "";
		try {
			QueryService locator = new QueryServiceLocator();
			Query qs = locator.getOntologyQuery();
			termName = qs.getTermById(termId, ontologyID);
			
		} catch (Exception e) {
			handleException(e, "getTermName");
		}
		return termName;
	}

}
