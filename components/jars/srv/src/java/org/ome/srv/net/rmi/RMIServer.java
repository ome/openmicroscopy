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
		    RMIAdministrationFacade obj = new RMIAdministrationFacade();
	
		    // Bind this object instance to the name "HelloServer"
		    Naming.rebind("//localhost/Admin", obj);
	
		    System.out.println("AdminService bound in registry.");
		} catch (Exception e) {
		    System.out.println("Admin Service Error: " + e.getMessage());
		    e.printStackTrace();
		}
	}
}
