/*
 * Created on Feb 7, 2005
 */
package org.ome.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * represents the information of an OWL class which will be used to generate code. 
 */
public class OntologicalClass {

	private Map propMap;
	private boolean newProperties = false;
	
	protected String localName;
	protected String URI;
	protected OntologicalClass superClass;
	protected List predicates;	
	
	/**
	 * 
	 */
	public OntologicalClass() {
		super();
	}
	/**
	 * @param localName
	 * @param uri
	 * @param superClass
	 */
	public OntologicalClass(String localName, String uri) {
		super();
		this.localName = localName;
		this.URI = uri;
	}
	/**
	 * @return Returns the localName.
	 */
	public String getLocalName() {
		return localName;
	}
	/**
	 * @param localName The localName to set.
	 */
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	
	/**
	 * @return Returns the predicates.
	 */
	public List getPredicates() {
		return predicates;
	}
	/**
	 * @return Returns the uRI.
	 */
	public String getURI() {
		return URI;
	}
	/**
	 * @param predicates The predicates to set.
	 */
	public void setPredicates(List predicates) {
		this.predicates = predicates;
		newProperties = true;
	}
	/**
	 * @param uri The uRI to set.
	 */
	public void setURI(String uri) {
		URI = uri;
	}
	/**
	 * @return Returns the superClass.
	 */
	public OntologicalClass getSuperClass() {
		return superClass;
	}
	/**
	 * @param superClass The superClass to set.
	 */
	public void setSuperClass(OntologicalClass superClass) {
		this.superClass = superClass;
	}
	
	public void addMin(String prop, int min) {
		if (propMap == null || newProperties == true) {
			generatePropMap();
		}
		((OntologicalProperty)propMap.get(prop)).setMin(min);
	}
	
	public void addMax(String prop, int max) {
		if (propMap == null || newProperties == true) {
			generatePropMap();
		}
		((OntologicalProperty)propMap.get(prop)).setMax(max);
	}

	
	protected void generatePropMap(){
		propMap = new HashMap();
		for (Iterator iter = predicates.iterator(); iter.hasNext();) {
			OntologicalProperty element = (OntologicalProperty) iter.next();
			propMap.put(element.getURI(),element);
		}
		newProperties = false;
	}
	
}
