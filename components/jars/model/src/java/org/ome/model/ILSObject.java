/*
 * Created on Feb 16, 2005
 */
package org.ome.model;

/**
 * @author josh
 */
public interface ILSObject {
	public boolean put(String predicate, Object object);

	public Object get(String predicate);

	public LSID getLSID();
}