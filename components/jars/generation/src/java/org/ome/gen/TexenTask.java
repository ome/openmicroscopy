/*
 * Created on Feb 7, 2005
 */
package org.ome.gen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.context.Context;
import org.ome.utils.OntologicalClass;
import org.ome.utils.OntologicalProperty;
import org.ome.utils.Util;

import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
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

		context.put("classes", getClasses(m));
		context.put("predicates", getPredicates(m));
		context.put("util", new Util());
		
		context.put("package", System.getProperties().get(
				"ome.generated.package"));
		context.put("package", "org.ome"); // FIXME
		
		context.put("version","0.0001");//FIXME
	}

	private List getPredicates(OntModel m) {
		List preds = new ArrayList();
		//TODO could possibly separate Data and Obj props for later parsing!
		for (Iterator i = m.listDatatypeProperties(); i.hasNext();) {
			preds.add(i.next());
		}

		for (Iterator i = m.listObjectProperties(); i.hasNext();) {
			preds.add(i.next());
		}

		preds.add(new OntologicalProperty("NS",
				"http://www.openmicroscopy.org/2005/OME.owl#"));//FIXME

		return preds;
	}

	public List getClasses(OntModel m) {
		List classes = new ArrayList();
		
		CLASS:
		for (Iterator i = m.listClasses(); i.hasNext();) {
			OntClass c = (OntClass) i.next();
			
			if (null == c.getURI()) {
				continue; // This is a non-named class. We don't want to generate stuff from it.
			}

			OntologicalClass myC = new OntologicalClass();

			myC.setPredicates(getPropertiesForClass(c));
			myC.setLocalName(c.getLocalName());
			myC.setURI(c.getURI());

			// XXX This code is tricky
			Iterator iter = c.listSuperClasses(true);
			for (Iterator j = iter; j.hasNext();) {
				OntClass s = (OntClass) j.next();
				if (null != s.getLocalName()) {
					myC.setSuperClass(new OntologicalClass(s.getLocalName(), s
							.getURI()));
					break;
				} else if (s.isRestriction()) {
					if (s.canAs(MinCardinalityRestriction.class)){
						MinCardinalityRestriction minCR = (MinCardinalityRestriction) s.as(MinCardinalityRestriction.class);
						int min = minCR.getMinCardinality();
						String prop = minCR.getOnProperty().getURI();
						myC.addMin(prop,min);
System.out.println(myC+"-->"+prop+"-->"+min);
					}

					if (s.canAs(MaxCardinalityRestriction.class)){
						MaxCardinalityRestriction maxCR = (MaxCardinalityRestriction) s.as(MaxCardinalityRestriction.class);
						int max = maxCR.getMaxCardinality();
						String prop = maxCR.getOnProperty().getURI();
						myC.addMin(prop,max);
System.out.println(myC+"-->"+prop+"-->"+max);						
					}
				}
			}
			classes.add(myC);
			
		}
		return classes;
	}

	private List getPropertiesForClass(OntClass c) {
		List properties = new ArrayList();
		
		PROPERTY:
		for (Iterator i2 = c.listDeclaredProperties(); i2.hasNext();) {
			OntProperty p = (OntProperty) i2.next();
			OntologicalProperty myP = new OntologicalProperty();

			myP.setLocalName(p.getLocalName());
			myP.setURI(p.getURI());
			myP.setType(Util.map(p));
			myP.setXsdType(Util.mapXSD(p));
			myP.setMin(getMinCardinality(p));
			myP.setMax(getMaxCardinality(p));
			
			properties.add(myP);
		}
		return properties;
	}

	private int getMinCardinality(OntProperty p) {
		if (p.isInverseFunctionalProperty()){
			return 1;
		} 
		return 0;
	}

	
	private int getMaxCardinality(OntProperty p) {
		if (p.isFunctionalProperty()){
			return 1;
		}
		
		return Integer.MAX_VALUE; //TODO
	}

	protected OntModel getModel() {
		// Create a model maker object
		ModelMaker mm = ModelFactory.createMemModelMaker();
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM);

		OntModel m = ModelFactory.createOntologyModel(spec, mm.createModel());
		m.read(ontology, "N3");
		return m;
	}

}