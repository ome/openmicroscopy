/*
 * Created on Feb 7, 2005
 */
package org.ome.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.context.Context;
import org.ome.utils.OntologicalClass;
import org.ome.utils.OntologicalProperty;
import org.ome.utils.Util;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * ant task to produce OME Bindings from OWL TODO rename JenaGenerationTask
 * 
 * @author josh
 */
public class TexenTask extends org.apache.velocity.texen.ant.TexenTask {

	private String ontology;

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	protected void populateInitialContext(Context context)
			throws java.lang.Exception {
		super.populateInitialContext(context);

		
		OntModel m = getModel();

		List classes = getClasses(m);
		context.put("classes", classes);
		context.put("predicates", getPredicates(classes));
		context.put("util", new Util());
		
		context.put("package", System.getProperties().get(
				"ome.generated.package"));
		context.put("package", "org.ome"); // FIXME
		
		context.put("version","0.0001");//FIXME
	}

	public List getClasses(OntModel m) {
		List classes = new ArrayList();
		
		CLASS:
		for (Iterator i = m.listClasses(); i.hasNext();) {
			OntClass c = (OntClass) i.next();
			if (null==c.getLocalName()||null==c.getURI()){ // FIXME this means it is a restriction or a disjoint, etc.
			    continue;
			}
			OntologicalClass myC = new OntologicalClass(c);
			classes.add(myC);
			
		}
		return classes;
	}

	//TODO possibly need to get non-Owl predicates
	//TODO assert LocalName1==LocalName2 ==> URI1==URI2 (in our NS)
	private List getPredicates(List classes) {
	    List results = new ArrayList();
	    Map uniquePredicates = new HashMap();
	    for (Iterator iter = classes.iterator(); iter.hasNext();) {
            OntologicalClass c = (OntologicalClass) iter.next();
            List preds = c.getPredicates();
            for (Iterator iter2 = preds.iterator(); iter2.hasNext();) {
                OntologicalProperty p = (OntologicalProperty) iter2.next();
                uniquePredicates.put(p.getLocalName(),p);
            }
        }
	    for (Iterator iter3 = uniquePredicates.keySet().iterator(); iter3.hasNext();) {
            String key = (String) iter3.next();
            results.add(uniquePredicates.get(key));
        }
	    results.add(new OntologicalProperty("NS","http://www.openmicroscopy.org/2005/OME.owl#"));//FIXME
	    return results;
	}
	
	protected OntModel getModel() {
		// Create a model maker object
		ModelMaker mm = ModelFactory.createMemModelMaker();
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM);

		OntModel m = ModelFactory.createOntologyModel(spec, mm.createModel());
		m.read(ontology, "N3"); // TODO Jena Model Factory should be in Jena Utils!
		return m;
	}

}