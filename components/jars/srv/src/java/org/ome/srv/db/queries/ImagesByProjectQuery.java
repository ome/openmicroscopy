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
public class ImagesByProjectQuery extends NamedQuery {

	public ImagesByProjectQuery(){
		super(JenaQueryStrings.getImagesByProjectQueryString());
		this.target = "image";
	}

	public ImagesByProjectQuery(LSID id) {
		this();
		setProject(id);
	}

	public void setProject(LSID lsid){
		this.bindingMap.put("project",lsid);
	}
	
}
