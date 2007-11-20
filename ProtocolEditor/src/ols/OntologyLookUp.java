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
