/*
 * Created on Feb 7, 2005
*/
package org.ome.gen;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * @author josh
*/
public class Main {

	public final static String ONT1 = "urn:x-hp-jena:test1";

	public final static String ONT2 = "urn:x-hp-jena:test2";

	public final static String file1 = "file:///home/josh/code/owl/prot-test2.owl";

	public final static String DB_URL = "jdbc:postgresql://localhost/jena";

	public final static String DB_USER = "josh";

	public final static String DB_PASSWD = "";

	public final static String DB = "PostgreSQL";

	OntModel model = null;

	VelocityContext context = null;

	String template = null;
	
	StringWriter writer = null;
	
	String outputDirectory = ".";
	String templateDirectory = ".";

	public static void main(String[] args) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
		Main m = new Main(args);
		//m.loadOntology();
		m.loadContext();
		m.writer = new StringWriter();
		m.loadTemplate();
		m.generate();
	}

	public Main(String[] args) throws Exception {
		/* first, we init the runtime engine. Defaults are fine. */
		Velocity.init();
		
		if (0 < args.length && null != args[0] ){
			outputDirectory = args[0];
		}
	}

	public void loadOntology() {
        ModelMaker maker = getMaker();

        if (maker.hasModel( ONT1 )) maker.removeModel( ONT1 );
        
        // create a spec for the new ont model
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setModelMaker( maker );

        // create the base model as a persistent model
        Model base = maker.createModel( ONT1 );
        OntModel m = ModelFactory.createOntologyModel( spec, base );

        // now load the document for test1, which will import ONT2
        m.read( file1 );
	}

	public void loadContext() {
		context = new VelocityContext();

		Collection classes = new ArrayList();
		
		OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setModelMaker( getMaker() );

        OntModel m = ModelFactory.createOntologyModel( spec, getMaker().createModel( ONT1 ) );

        for (Iterator i = m.listClasses(); i.hasNext(); ) {
            OntClass c = (OntClass) i.next();
            if (null == c.getURI()){
            	continue;
            }
            OntologicalClass myC = new OntologicalClass();
			List properties = new ArrayList();
            
            for (Iterator i2 = c.listDeclaredProperties(); i2.hasNext();) {
				OntProperty element = (OntProperty) i2.next();
				properties.add(element);
			}
            
            myC.setLocalName(c.getLocalName());
            myC.setURI(c.getURI());
            myC.setPredicates(properties);
            classes.add(myC);
        }
        
		context.put("classes", classes);
	}

	/**
	 * the real implementation of templates should either
	 * 
	 * 1) be generated as a String from comments in the OWL file or 2) be
	 * multiply evaluated like macros
	 * 
	 * @throws Exception
	 */
	public void loadTemplate() {
		template = 
			"#foreach( $c in $classes )\n"+
			"  Class $c.LocalName   <$c.URI>\n"+
			"  #foreach( $p in $c.Predicates )\n"+
			"    Pred $p.LocalName   <$p>\n"+			
	    	"  #end"+
	    	"#end";
	}

	
	public void generate() throws ResourceNotFoundException,
			ParseErrorException, MethodInvocationException, Exception {

		Velocity.evaluate(context, writer, "OME GENERATION", template);
		System.out.println("***************************************");
		System.out.println(writer);
		
	}

    protected ModelMaker getMaker() {
            // Load the Driver
            String className = "org.postgresql.Driver";
            
            try {
            	Class.forName(className);
            } catch (ClassNotFoundException cnfe){
            	throw new RuntimeException("JDBC driver not found.", cnfe);
            }
            
            // Create database connection
            IDBConnection conn  = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );

            // Create a model maker object
            return ModelFactory.createModelRDBMaker(conn);
    }
	

}