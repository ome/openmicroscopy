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

	private static ModelMaker maker = null;

	//TODO
	
	public static String ontology = JenaProperties.getString("jena.ontology"); //$NON-NLS-1$
	public static String format = JenaProperties.getString("jena.ontology.format"); //$NON-NLS-1$
	public static String DB_URL = JenaProperties.getString("jena.db.url"); //$NON-NLS-1$
	public static String DB_USER = JenaProperties.getString("jena.db.user"); //$NON-NLS-1$
	public static String DB_PASSWD = JenaProperties.getString("jena.db.password"); //$NON-NLS-1$
	public final static String DB = JenaProperties.getString("jena.db.type"); //$NON-NLS-1$
	public final static String defaultModel = JenaProperties.getString("jena.model.name.default"); //$NON-NLS-1$
	public static String driver = JenaProperties.getString("jena.db.driver"); //$NON-NLS-1$
	
	public static Model getModel() {
		// create a spec for the new ont model
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM);

		// create the base model as a persistent model
		Model base = JenaModelFactory.getMaker().createModel(defaultModel);
		OntModel m = ModelFactory.createOntologyModel(spec, base);

		return m;

	}

	public static ModelMaker getMaker() {
		if (null == maker) {
			// 	Load the Driver
			
			try {
				Class.forName(driver);
			} catch (ClassNotFoundException cnfe) {
				throw new RuntimeException("JDBC driver not found.", cnfe); //$NON-NLS-1$
			}

			// Create database connection
			IDBConnection conn = new DBConnection(DB_URL, DB_USER, DB_PASSWD,
					DB);

			// Create a model maker object
			maker = ModelFactory.createModelRDBMaker(conn);
		}
		return maker;
	}

}