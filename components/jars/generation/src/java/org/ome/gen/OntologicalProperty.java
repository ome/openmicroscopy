/*
 * Created on Feb 7, 2005
 */
package org.ome.gen;

/**
 * represents the information of an OWL property which will be used to generate code. 
 */
public class OntologicalProperty {

	/**
	 * @param localName
	 * @param uri
	 */
	public OntologicalProperty(String localName, String uri) {
		super();
		this.localName = localName;
		URI = uri;
	}
	
	/**
	 * 
	 */
	public OntologicalProperty() {
		super();
	}

	protected String localName;
	protected String URI;
	protected String type;
	protected String xsdType;
	protected int min;
	protected int max;

	
	/**
	 * @return Returns the max.
	 */
	public int getMax() {
		return max;
	}
	/**
	 * @param max The max to set.
	 */
	public void setMax(int max) {
		this.max = max;
	}
	/**
	 * @return Returns the min.
	 */
	public int getMin() {
		return min;
	}
	/**
	 * @param min The min to set.
	 */
	public void setMin(int min) {
		this.min = min;
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
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	/**
	 * @return Returns the uRI.
	 */
	public String getURI() {
		return URI;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @param uri The uRI to set.
	 */
	public void setURI(String uri) {
		URI = uri;
	}

	/**
	 * @return Returns the xsdType.
	 */
	public String getXsdType() {
		return xsdType;
	}
	/**
	 * @param xsdType The xsdType to set.
	 */
	public void setXsdType(String xsdType) {
		this.xsdType = xsdType;
	}
}
