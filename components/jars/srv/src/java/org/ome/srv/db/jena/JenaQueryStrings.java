/*
 * Created on Feb 13, 2005
*/
package org.ome.srv.db.jena;

import org.ome.srv.db.Queries;
import org.ome.model.IDataset;
import org.ome.model.IImage;
import org.ome.model.Vocabulary;
import org.ome.model.IProject;

import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author josh
 */
public class JenaQueryStrings implements Queries {

	protected final static String subject = "subject";
	protected final static String predicate = "predicate";
	protected final static String object = "object";
	protected final static String statement = "(?"+subject+" ?"+predicate+" ?"+object+")";
	
	protected final static String imagesByDatasetQueryString = "select ?image where "
		+ "(?dataset <"+ Vocabulary.contains + "> ?image ) ";
	
	protected final static String imagesByProjectQueryString = imagesByDatasetQueryString + ","
	+ "(?project <"+ Vocabulary.contains + "> ?dataset ) ";
	
	protected final static String projectsByExperimenterQueryString = makeByExperimenter("project",IProject.URI);

	protected final static String datasetsByExperimenterQueryString = makeByExperimenter("dataset",IDataset.URI);

	protected final static String imagesByExperimenterQueryString = makeByExperimenter("image",IImage.URI);
	
	protected static String makeByExperimenter(String variable, String URI){
		return "select ?"+variable+" where "
		+ "(?"+variable+" <"+ Vocabulary.owner  + "> ?exp ) ,"
		+ "(?"+variable+" <"+ RDF.type.getURI() +"> <"	+ URI + "> ) ";
	}
	
	/**
	 * @return Returns the imagesByExperimenterQueryString.
	 */
	public static String getImagesByExperimenterQueryString() {
		return imagesByExperimenterQueryString;
	}
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
	 * @return Returns the datasetsByExperimenterQueryString.
	 */
	public static String getDatasetsByExperimenterQueryString() {
		return datasetsByExperimenterQueryString;
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

	/**
	 * @return Returns the imagesByDatasetQueryString.
	 */
	public static String getImagesByDatasetQueryString() {
		return imagesByDatasetQueryString;
	}
	/**
	 * @return Returns the imagesByProjectQueryString.
	 */
	public static String getImagesByProjectQueryString() {
		return imagesByProjectQueryString;
	}
}
