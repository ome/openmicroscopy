/*
 * Created on Feb 12, 2005
 */
package org.ome.tests.client;

import junit.framework.TestCase;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.util.List;

import org.ome.model.IExperimenter;
import org.ome.model.LSID;
import org.ome.client.Properties;
import org.ome.client.rmi.ServiceFactoryImpl;
import org.ome.interfaces.AdministrationService;
import org.ome.interfaces.ServiceFactory;
import org.ome.model.Vocabulary;


/**
 * @author josh
 */
public class RMITest extends TestCase {

	// "obj" is the identifier that we'll use to refer
	// to the remote object that implements the "Hello"
	// interface
	AdministrationService as = null;
	ServiceFactory f = new ServiceFactoryImpl();

	
	/**
	 * 
	 */
	public RMITest() {
		super();
		if (System.getSecurityManager() == null) {
		    System.setSecurityManager(new SecurityManager());
		}
	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(RMITest.class);
	}
	
	public void testRun() {
		try {
			as = f.getAdministrationService();
			IExperimenter value = as.getExperimenter(new LSID(Vocabulary.NS+"Josh"));
			System.out.println(value);
		} catch (Exception e) {
			System.out.println("RMI exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

}