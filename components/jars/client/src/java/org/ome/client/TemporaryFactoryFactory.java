/*
 * Created on Feb 12, 2005
 */
package org.ome.client;

import org.ome.client.rmi.ServiceFactoryImpl;
import org.ome.interfaces.ServiceFactory;

/**
 * this temporary factory factory is simply here to avoid undue complexity.
 * Currently it simply returns the RMI Service Factory.
 * 
 * @author josh
 */
public class TemporaryFactoryFactory {
	public static ServiceFactory getServiceFactory() {
		return new ServiceFactoryImpl();
	}
}