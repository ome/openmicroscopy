/*
 * Created on Feb 12, 2005
 */
package org.ome.tests.client;

import junit.framework.TestCase;
import java.rmi.Naming;
import java.util.List;

import org.ome.LSID;
import org.ome.client.Properties;
import org.ome.interfaces.AdministrationService;
import org.ome.texen.Vocabulary;

/**
 * @author josh
 */
public class RMITest extends TestCase {

	List value;

	// "obj" is the identifier that we'll use to refer
	// to the remote object that implements the "Hello"
	// interface
	AdministrationService as = null;

	public void testRun() {
		try {
			as = (AdministrationService) Naming.lookup(Properties.getString("RMIServiceFactory.AdminService"));
			value = as.retrieveProjectsByExperimenter(new LSID(Vocabulary.NS+"Josh"));
			System.out.println(value);
		} catch (Exception e) {
			System.out.println("RMI exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

}