/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.db;

import java.util.HashMap;
import java.util.Map;

/**
 * @author josh
 */
public class NamedQuery {

	protected String queryString;
	protected Map bindingMap = new HashMap();
	protected String target = null;
	
	public NamedQuery(String queryString){
		this.queryString = queryString;
	}
	
	/**
	 * @return Returns the bindingMap.
	 */
	public Map getBindingMap() {
		return bindingMap;
	}
	/**
	 * @param bindingMap The bindingMap to set.
	 */
	public void setBindingMap(Map bindingMap) {
		this.bindingMap = bindingMap;
	}
	/**
	 * @return Returns the queryString.
	 */
	public String getQueryString() {
		return queryString;
	}
	/**
	 * @param queryString The queryString to set.
	 */
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
	/**
	 * @return Returns the target.
	 */
	public String getTarget() {
		return target;
	}
	/**
	 * @param target The target to set.
	 */
	public void setTarget(String target) {
		this.target = target;
	}
}
