/*
 * Created on Feb 7, 2005
 */
package org.ome.utils;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntProperty;

/**
 * represents the information of an OWL property which will be used to generate
 * code.
 */
public class OntologicalProperty {

    final static String xsd = "http://www.w3.org/2001/XMLSchema#";

    protected String localName;

    protected String URI;

    protected String range;

    protected String rangeURI;

    protected String type;

    protected String xsdType;

    protected Integer min = new Integer(0);

    protected Integer max = null;

    protected List allValuesFrom = new ArrayList();

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

    public OntologicalProperty(OntProperty p) {

        this.setLocalName(p.getLocalName());
        this.setURI(p.getURI());
        this.setRange(p.getRange().getLocalName());
        this.setRange(p.getRange().getURI());

        // TODO if InverseFunctional is a more complicated concept!!!
        if (p.isFunctionalProperty()) {
            this.setMax(1);
        }

    }

    /**
     * sets the Type and XSDType fields for a property. This is called by
     * OntologicalClass _AFTER_ cardinality and other properties have been
     * decided. It should also evaluate the AllValuesFrom and reset the range to
     * the most restrictive case TODO
     *  
     */
    public void doTypes() {
        if (allValuesFrom.size()>0){
            this.setRange(((OntologicalClass)allValuesFrom.get(0)).getLocalName());
            this.setRangeURI(((OntologicalClass)allValuesFrom.get(0)).getURI());
        }
        
        if (allValuesFrom.size()>1){
            System.err.println("Property "+this+" has more than one AllValuesFromRestrictions!!");//FIXME
        }
        
        this.setType(this.map());
        this.setXsdType(this.mapXSD());
        
    }

    public String mapXSD() {

        if (null == this.getRangeURI()) {
            //FIXME throw new RuntimeException("generalize parsing of
            // restrictions");
            return "xs:string";
        } else if (this.getRangeURI().startsWith(xsd)) {
            return "xs:" + range;
        } else { // FIXME assumes everything else is an LSID (anyURI)
            return "xs:string";
        }
    }

    /**
     * maps a Owl Property to a valid Java class
     * 
     * @param p
     * @return
     */
    public String map() {

        if (null == this.getMax() || this.getMax().intValue() > 1) {
            return "java.util.List"; // TODO generics
        } else if (null == rangeURI || null == range) {
            return "Object"; 
        } else if (rangeURI.equals(xsd + "string")) {
            return "String";
        } else if (rangeURI.equals(xsd + "float")) {
            return "Float";
        } else if (rangeURI.equals(xsd + "int")) {
            return "Integer";
        } else if (rangeURI.equals(xsd + "anyURI")) {
            return "java.net.URI";
        } else if (rangeURI.equals(xsd + "nonNegativeInteger")) { // TODO This logic
            // needs to land
            // somewhere
            return "Integer";
        } else { 
            // the assumption at this point is that range
            // is a generated interface so we can just add "I"
            return "I" + range;
        }
    }

    /**
     * @return Returns the max.
     */
    public Integer getMax() {
        return max;
    }

    /**
     * @param max
     *            The max to set.
     */
    public void setMax(int max) {
        this.max = new Integer(max);
    }

    /**
     * @return Returns the min.
     */
    public Integer getMin() {
        return min;
    }

    /**
     * @param min
     *            The min to set.
     */
    public void setMin(int min) {
        this.min = new Integer(min);
    }

    /**
     * @return Returns the localName.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * @param localName
     *            The localName to set.
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
     * @param type
     *            The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param uri
     *            The uRI to set.
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
     * @param xsdType
     *            The xsdType to set.
     */
    public void setXsdType(String xsdType) {
        this.xsdType = xsdType;
    }

    /**
     * @return Returns the allValuesFrom.
     */
    public List getAllValuesFrom() {
        return allValuesFrom;
    }

    /**
     * @param allValuesFrom
     *            The allValuesFrom to set.
     */
    public void setAllValuesFrom(List allValuesFrom) {
        this.allValuesFrom = allValuesFrom;
    }

    /**
     * @return Returns the range.
     */
    public String getRange() {
        return range;
    }

    /**
     * @param range
     *            The range to set.
     */
    public void setRange(String range) {
        this.range = range;
    }

    /**
     * @return Returns the rangeURI.
     */
    public String getRangeURI() {
        return rangeURI;
    }

    /**
     * @param rangeURI
     *            The rangeURI to set.
     */
    public void setRangeURI(String rangeURI) {
        this.rangeURI = rangeURI;
    }
    
    public String toString(){
        return this.localName+" <"+this.URI+"> ";
    }
}