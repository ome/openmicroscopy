/*
 * Created on Feb 20, 2005
*/
package org.ome.tests.srv;

import org.ome.srv.db.jena.JenaModelFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class BaseServiceTestCase extends TestCase {

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(BaseServiceTestCase.class);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ModelMaker mm = JenaModelFactory.getMaker();
		if (mm.hasModel(JenaModelFactory.defaultModel)){
		    mm.removeModel(JenaModelFactory.defaultModel);
		}
		Model m = JenaModelFactory.getModel();
		m.read(JenaModelFactory.ontology,JenaModelFactory.format);
	}

}
