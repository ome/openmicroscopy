/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.db.queries;

import org.ome.model.LSID;
import org.ome.srv.db.NamedQuery;
import org.ome.srv.db.jena.JenaQueryStrings;

/**
 * @author josh
 */
public class ImagesByExperimenterQuery extends NamedQuery {

	public ImagesByExperimenterQuery(){
		super(JenaQueryStrings.getImagesByExperimenterQueryString());
		this.target = "image";
	}

	public ImagesByExperimenterQuery(LSID experimenterId) {
		this();
		setExperimenter(experimenterId);
	}

	//TODO the "byExperimenters" need an abstract class
	public void setExperimenter(LSID lsid){
		this.bindingMap.put("exp",lsid);
	}
	
}
