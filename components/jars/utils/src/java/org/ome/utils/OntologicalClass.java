/*
 * Created on Feb 7, 2005
 */
package org.ome.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * represents the information of an OWL class which will be used to generate
 * code.
 */
public class OntologicalClass {

    protected String _localName;

    protected String _URI;

    protected OntologicalClass _superClass;

    protected List _predicates = new ArrayList();
    
    protected List _restrictions = new ArrayList();
    
    /**
     *  
     */
    public OntologicalClass() {
        super();
    }

    /** this constructor should be used only when the full information 
     * about a class is not needed, e.g. super classes since these will 
     * be evaluated in a separate loop.
     * @param localName
     * @param uri
     */
    public OntologicalClass(String localName, String uri) {
        super();
        this._localName = localName;
        this._URI = uri;
    }

    /** does the majority of the work of defining a class. Beware.*/
    public OntologicalClass(OntClass c) {
        this.setLocalName(c.getLocalName());
        this.setURI(c.getURI());
        
        getPropertiesForClass(c);
        HashMap propMap = generatePropMap();
        
        Iterator iter = c.listSuperClasses(true);
        for (Iterator j = iter; j.hasNext();) {
            OntClass s = (OntClass) j.next();
            if (null != s.getLocalName()) {
                this.setSuperClass(new OntologicalClass(s.getLocalName(), s
                        .getURI()));
                break;
            } else if (s.isRestriction()) {

                if (s.canAs(MinCardinalityRestriction.class)) {
                    MinCardinalityRestriction minCR = (MinCardinalityRestriction) s
                            .as(MinCardinalityRestriction.class);
                    int min = minCR.getMinCardinality();

                    String prop = minCR.getOnProperty().getURI();
                    OntologicalProperty p = (OntologicalProperty) propMap.get(prop);

                    int oldMin = p.getMin().intValue();
                    if (min > oldMin) p.setMin(min);

                }

                if (s.canAs(MaxCardinalityRestriction.class)) {
                    MaxCardinalityRestriction maxCR = (MaxCardinalityRestriction) s
                            .as(MaxCardinalityRestriction.class);
                    int max = maxCR.getMaxCardinality();

                    String prop = maxCR.getOnProperty().getURI();
                    OntologicalProperty p = (OntologicalProperty) propMap.get(prop);
                    
                    if (null!=p.getMin()){
                        int oldMax = p.getMax().intValue();
                        if (max < oldMax) p.setMax(max);
                    } else {
                        p.setMax(max);
                    }

                }

                if (s.canAs(AllValuesFromRestriction.class)) {
                    AllValuesFromRestriction avr = (AllValuesFromRestriction) s
                            .as(AllValuesFromRestriction.class);
                    Resource type = avr.getAllValuesFrom();

                    String prop = avr.getOnProperty().getURI();
                    OntologicalProperty p = (OntologicalProperty) propMap.get(prop);

                    OntologicalClass a = new OntologicalClass(type.getLocalName(),type.getURI());
                    p.getAllValuesFrom().add(a); // FIXME could already be defined
                }
              
                //TODO disjoint, someValuesFrom etc.
            } else {
                System.err.println("Class "+c+" has multiple named superclasses!!"); //FIXME
            }
        }

        //FINALIZING
        for (Iterator i = _predicates.iterator(); i.hasNext();) {
            OntologicalProperty p = (OntologicalProperty) i.next();
            p.doTypes();
          
        }
    }

    /** this produces a list of properties on this class. Further information will 
     * come from the Restrictions on the class and will access. Note: uses listDeclaredProperties TODO is this enough
     * @param c
     * @return
     */
	private void getPropertiesForClass(OntClass c) {
    
		PROPERTY:
		for (Iterator i2 = c.listDeclaredProperties(); i2.hasNext();) {
			OntProperty p = (OntProperty) i2.next();
			OntologicalProperty myP = new OntologicalProperty(p);
			_predicates.add(myP);
		}
	}    

    protected HashMap generatePropMap() {
        HashMap propMap = new HashMap();
        for (Iterator iter = _predicates.iterator(); iter.hasNext();) {
            OntologicalProperty element = (OntologicalProperty) iter.next();
            propMap.put(element.getURI(), element);
        }
        return propMap;
    }
	
    /**
     * @return Returns the localName.
     */
    public String getLocalName() {
        return _localName;
    }

    /**
     * @param localName
     *            The localName to set.
     */
    public void setLocalName(String localName) {
        this._localName = localName;
    }

    /**
     * @return Returns the predicates.
     */
    public List getPredicates() {
        return _predicates;
    }

    /**
     * @return Returns the uRI.
     */
    public String getURI() {
        return _URI;
    }

    /**
     * @param predicates
     *            The predicates to set.
     */
    public void setPredicates(List predicates) {
        this._predicates = predicates;
    }

    /**
     * @param uri
     *            The uRI to set.
     */
    public void setURI(String uri) {
        _URI = uri;
    }

    /**
     * @return Returns the superClass.
     */
    public OntologicalClass getSuperClass() {
        return _superClass;
    }

    /**
     * @param superClass
     *            The superClass to set.
     */
    public void setSuperClass(OntologicalClass superClass) {
        this._superClass = superClass;
    }

    /**
     * @return Returns the restrictions.
     */
    public List getRestrictions() {
        return _restrictions;
    }
    /**
     * @param restrictions The restrictions to set.
     */
    public void setRestrictions(List restrictions) {
        this._restrictions = restrictions;
    }
    
    public String toString(){
        return this._localName+" <"+this._URI+"> ";
    }
    
}
