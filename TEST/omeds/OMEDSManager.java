/*
 * omeds.OMEDSManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package omeds;

//Java imports
import java.net.URL;
import java.util.Properties;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.DataFactory;
import org.openmicroscopy.ds.DataServer;
import org.openmicroscopy.ds.RemoteCaller;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class OMEDSManager
{

	/**
	* The sole instance that provides the connection for all test cases.
	*/
	private static OMEDSManager		singleton;
	

	/**
	 * Returns the <code>OMEDSManager</code> object that will handle the 
	 * connection to the <i>OMEDS</i> server.
	 * 
	 * @return	See above.
	 */
	public static OMEDSManager getInstance()
	{
		if (singleton == null) {
			try {
				Properties config = new Properties();
				config.load(OMEDSManager.class.
									getResourceAsStream("connections.cfg"));
				singleton = new OMEDSManager(config.getProperty("OMEDS_URL"),
												config.getProperty("USER"),
												config.getProperty("PASS"));	
			} catch (Exception e) {
				throw new RuntimeException("Can't initialize the OMEDSManager",
										 	e);
			}
		}
		return singleton;
	}
	

	/** Tells how to connect to the <i>OMEDS</i> server. */
	private URL					omedsURL;

	/** The user name of the test user. */
	private String				user;

	/** The password of the test user. */
	private String				password;

	/** The proxy to the <i>OMEDS</i> server.
	 * Test cases will use this object to access the remote framework.
	 */
	private DataFactory			proxy;
	
	/** Needed to instanciate the {@link #proxy}. */
	private RemoteCaller		caller;
	
	
	/**
	 * Creates a new instance and configures the connection parameters.
	 * 
	 * @param omedsURL	Tells how to connect to the <i>OMEDS</i> server.
	 * @param user		The user name of the test user.
	 * @param pass		The password of the test user.
	 * @throws	Exception If any parameter is not valid.	
	 */
	private OMEDSManager(String omedsURL, String user, String pass)
		throws Exception
	{
		if (omedsURL == null || user == null || pass == null)
			throw new NullPointerException("Invalid connection parameters.");
		this.omedsURL =  new URL(omedsURL);
		this.user = user;
		this.password = pass;
	}
	
	/**
	 * Returns a proxy to the <i>OMEDS</i> server.
	 * Automatically logs into the <i>OMEDS</i> server.
	 * 
	 * @throws Exception	If the connection couldn't be established or loggin
	 * 						fails.
	 */
	public DataFactory getProxy()
		throws Exception
	{
		if (caller == null) {
			caller = DataServer.getDefaultCaller(omedsURL);
			caller.login(user, password);
			proxy = new DataFactory(caller); 
		}
		return proxy;
	}
	
	/**
	 * Makes sure we disconnect when the singleton is garbage collected.
	 */
	public void finalize()
	{
		try {
			if (caller != null)    caller.logout();
		} catch(Exception e) {
			//Ignore
		}
	}
	
}
