/*
 * Created on Feb 12, 2005
 */
package org.ome.srv.net.rmi;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;

/**
 * @author josh
 */
public class RMIServer {

	public static void main(String[] args) {
		// Create and install a security manager
		if (System.getSecurityManager() == null) {
		    System.setSecurityManager(new RMISecurityManager());
		}
	
		try {
		    RMIServiceFactory obj = new RMIServiceFactory();
	
		    // Bind this object instance to the name "HelloServer" TODO
		    Naming.rebind("//localhost/ServiceFactory", obj);
	
		    System.out.println("Service Factory bound in registry.");
		} catch (Exception e) {
		    System.out.println("RMI Error: " + e.getMessage());
		    e.printStackTrace();
		}
	}
}
