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
public class DatasetsByExperimenterQuery extends NamedQuery {

	public DatasetsByExperimenterQuery(){
		super(JenaQueryStrings.getDatasetsByExperimenterQueryString());
		this.target = "dataset";
	}

	public DatasetsByExperimenterQuery(LSID experimenterId) {
		this();
		setExperimenter(experimenterId);
	}

	public void setExperimenter(LSID lsid){
		this.bindingMap.put("exp",lsid);
	}
	
}
