package validation;

import java.util.LinkedHashMap;

public class XMLSchema {

	private static LinkedHashMap<String, String> rootAttributes = new LinkedHashMap<String, String>();
	
	public static LinkedHashMap<String, String> getRootAttributes() {
		rootAttributes.put("xmlns", "http://morstonmud.com/omero/schemas");
		rootAttributes.put("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		rootAttributes.put("xsi:schemaLocation", "http://morstonmud.com/omero/schemas http://morstonmud.com/omero/schemas/PE.xsd");
		//rootAttributes.put("xmlns:ome", "http://www.openmicroscopy.org/Schemas/OME/2007-06");
		
		return rootAttributes;
	}
	
}


