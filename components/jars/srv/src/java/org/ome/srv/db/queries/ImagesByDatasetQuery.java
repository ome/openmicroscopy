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
public class ImagesByDatasetQuery extends NamedQuery {

	public ImagesByDatasetQuery(){
		super(JenaQueryStrings.getImagesByDatasetQueryString());
		this.target = "image";
	}

	public ImagesByDatasetQuery(LSID id) {
		this();
		setDataset(id);
	}

	public void setDataset(LSID lsid){
		this.bindingMap.put("dataset",lsid);
	}
	
}
