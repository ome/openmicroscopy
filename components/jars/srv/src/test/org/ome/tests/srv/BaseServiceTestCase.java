/*
 * Created on Feb 20, 2005
 */
package org.ome.tests.srv;

import org.ome.srv.db.jena.JenaModelFactory;
import org.ome.srv.db.jena.JenaProperties;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class BaseServiceTestCase extends TestCase {

    static JenaModelFactory factory = (JenaModelFactory) SpringTestHarness.ctx
            .getBean("defaultJenaModelFactory");

    static {
        if (true) {
            jenaInit();
        }
        
    }
    
    static void kowariInit() {
    }

    static void jenaInit() {
        ModelMaker mm = factory.getMaker();
        if (mm.hasModel(factory.getModelName())) {
            mm.removeModel(factory.getModelName());
        }
        Model m = factory.getModel();
        m.read(JenaProperties.getString("jena.ontology"), JenaProperties
                .getString("jena.ontology.format"));

    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.run(BaseServiceTestCase.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

}