/*
 * Created on Feb 8, 2005
 */
package org.ome.utils;

import com.hp.hpl.jena.ontology.OntProperty;

/**
 * maps XSD types to Java classes
*/
public class Util {

	/** returns a first-letter capitalized version of
	 * a string for getters and setters
	 * @param str
	 * @return upper case string
	 */
	public static String uc(String str){
		return str.substring(0,1).toUpperCase()+str.substring(1);
	}

	public static String mapXSD(OntProperty p){
		String xsd = "http://www.w3.org/2001/XMLSchema#";
		String uri = p.getRange().getURI();
		String range = p.getRange().getLocalName();

		if (null == uri){
			//FIXME throw new RuntimeException("generalize parsing of restrictions");
			return "xs:string";
		} else if (uri.startsWith(xsd)) {
			return "xs:"+range;			
		} else { // FIXME assumes everything else is an LSID (anyURI)
			return "xs:string";
		}
	}
	
	
	/** maps a Owl Property to a valid Java class
	 * @param p
	 * @return
	 */
	public static String map(OntProperty p) {
		String xsd = "http://www.w3.org/2001/XMLSchema#";
		String uri = p.getRange().getURI();
		String range = p.getRange().getLocalName(); 
		boolean functional = p.isFunctionalProperty();

		if (!functional) {
			return "java.util.List";
		} else if (null == uri || null == range) {
			return "Object"; // FixME AllValuesFrom
		} else if (uri.equals(xsd + "string")) {
			return "String";
		} else if (uri.equals(xsd + "float")) {
			return "Float";
		} else if (uri.equals(xsd + "int")) {
			return "Integer";			
		} else if (uri.equals(xsd + "anyURI")) {
			return "java.net.URI";
		} else if (uri.equals(xsd + "nonNegativeInteger")) { // TODO This logic needs to land somewhere
			return "Integer";
		} else { // FIXME and allValuesFrom??
			// the assumption at this point is that range
			// is a generated interface so we can just add "I"
			return "I"+range;
		}
	}

}