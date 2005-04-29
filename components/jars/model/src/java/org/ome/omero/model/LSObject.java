/*
 * Created on Feb 16, 2005
 */
package org.ome.model;

import java.util.Map;

/**
 * @author josh
 */
public interface LSObject {
	public boolean put(String predicate, Object object);

	public Object get(String predicate);

	public LSID getLSID();
	public Map getMap();
	
	public boolean save();
	public boolean reset();
}