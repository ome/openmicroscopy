/*
 * Created on Feb 12, 2005
 */
package org.ome.srv.db.jena;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * @author josh
*/
public class JenaModelFactory {

	public final static String DB_URL = "jdbc:postgresql://localhost/jena";

	public final static String DB_USER = "josh";

	public final static String DB_PASSWD = "";

	public final static String DB = "PostgreSQL";

	public final static String defaultModel = "ome";
	
	public static Model getModel(){
	       // create a spec for the new ont model
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );

        // create the base model as a persistent model
        Model base = JenaModelFactory.getMaker().createModel( defaultModel );
        OntModel m = ModelFactory.createOntologyModel( spec, base );

        return m;
		
	}
	
	public static ModelMaker getMaker() {
		// Load the Driver
		String className = "org.postgresql.Driver";

		try {
			Class.forName(className);
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException("JDBC driver not found.", cnfe);
		}

		// Create database connection
		IDBConnection conn = new DBConnection(DB_URL, DB_USER, DB_PASSWD, DB);

		// Create a model maker object
		return ModelFactory.createModelRDBMaker(conn);
	}

}