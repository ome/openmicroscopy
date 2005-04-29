/*
 * Created on Feb 20, 2005
 */
package org.ome.tests.srv;

import java.util.Iterator;

import org.apache.commons.pool.KeyedObjectPool;
import org.ome.srv.db.jena.JenaModelFactory;
import org.ome.srv.db.jena.JenaProperties;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import junit.framework.TestCase;

/**
 * @author josh
 */
public class BaseServiceTestCase extends TestCase {

    static KeyedObjectPool pool = (KeyedObjectPool) SpringTestHarness.ctx
            .getBean("modelPool");
    static JenaModelFactory factory = (JenaModelFactory) SpringTestHarness.ctx.
    getBean("modelFactory");

    static {
        
        if (true) {
                try {
                    jenaInit();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to init db for test",e);
                }
        }
        
    }
    
    static void kowariInit() {
    }

    static void jenaInit() throws Exception {
        ModelMaker mm = factory.getMaker();
        for (Iterator iter = mm.listModels(); iter.hasNext();) {
            String model = (String) iter.next();
            mm.removeModel(model);
            
        }
        String key = JenaProperties.getString("jena.model.name.default");
        Model m = (Model) pool.borrowObject(key);
        m.read(JenaProperties.getString("jena.ontology"), JenaProperties
                .getString("jena.ontology.format"));
        pool.returnObject(key,m);
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