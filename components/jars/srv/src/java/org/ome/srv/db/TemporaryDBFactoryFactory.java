/*
 * Created on Feb 12, 2005
 */
package org.ome.srv.db;

import org.ome.interfaces.ServiceFactory;
import org.ome.srv.db.jena.JenaStoreFactory;

/**
 * this temporary factory factory is simply here to avoid undue complexity.
 * Currently it simply returns the Jena Service Factory.
 * 
 * @author josh
 */
public class TemporaryDBFactoryFactory {
	public static ServiceFactory getServiceFactory() {
		return new JenaStoreFactory();
	}
}
