/*
 * Created on Feb 13, 2005
*/
package org.ome.srv.db.jena;

import org.ome.srv.db.Queries;
import org.ome.model.IExperimenter;
import org.ome.model.Vocabulary;
import org.ome.model.IProject;

import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author josh
 */
public class JenaQueries implements Queries {

	protected final static String subject = "subject";
	protected final static String predicate = "predicate";
	protected final static String object = "object";
	protected final static String statement = "(?"+subject+" ?"+predicate+" ?"+object+")";
	
	protected final static String projectsByExperimenterQueryString = "select ?project where "
		+ "(?project " + "<"+ Vocabulary.owner  + "> ?exp ) ,"
		+ "(?project " + "<"+ RDF.type.getURI() +"> <"	+ IProject.URI + "> ) ";
	
	/**
	 * @return Returns the statement.
	 */
	public static String getStatement() {
		return statement;
	}
	
	/**
	 * @return Returns the projectsByExperimenterQueryString.
	 */
	public static String getProjectsByExperimenterQueryString() {
		return projectsByExperimenterQueryString;
	}

	/**
	 * @return
	 */
	public static String getSubject() {
		return subject;
	}
	/**
	 * @return Returns the object.
	 */
	public static String getObject() {
		return object;
	}
	/**
	 * @return Returns the predicate.
	 */
	public static String getPredicate() {
		return predicate;
	}

}
