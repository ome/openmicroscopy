/*
 * org.openmicroscopy.shoola.env.data.Env
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

package org.openmicroscopy.shoola.env.data;


//Java imports
import java.util.Properties;

//Third-party libraries

//Application-internal dependencies

/** 
 * Makes external settings available to {@link DataServicesTestCase}s.
 * These settings define the <i>OMEDS</i> account to use for a test session
 * and are specified through system properties &#151; usually set as a JVM
 * command line arguments, through the <code>-D</code> option.
 * <p>Tests should operate on a test instance of <i>OMEDS</i> in which an
 * experimenter has already been created.  This is the test user that will
 * be used to connect to the server.  The connection to <i>OMEDS</i> is 
 * configured with the values found in the following system properties:</p>
 * <ul>
 *  <li><code>omeds.url</code>: The URL to connect to <i>OMEDS</i>.</li>
 *  <li><code>omeds.user</code>: The login user name of the test user.  This
 *  has to equal the content of the <code>experimenters.ome_name</code> in
 *  the DB.</li>
 *  <li><code>omeds.pass</code>: The login password.</li>
 * </ul>
 * <p>All the above properties are mandatory and need to be specified before
 * each test session; failure to comply will result in a runtime exception.</p>
 * <p>Finally note that the test instance of <i>OMEDS</i> should be configured
 * to run on a test database.</p>
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
public class Env
{

    /** The sole instance. */
    private static Env  singleton;
    
    /** The property name for the URL to connect to <i>OMEDS</i>. */
    public static final String  OMEDS_URL = "omeds.url";
    
    /** 
     * The property name for the login user name of the <i>OMEDS</i> test user. 
     */
    public static final String  OMEDS_USER = "omeds.user";
    
    /** 
     * The property name for the login password of the <i>OMEDS</i> test user.
     */
    public static final String  OMEDS_PASS = "omeds.pass";
    
    /** The property name for the host to connect to <i>OMERO</i>. */
    public static final String  OMEDS_HOST = "server.host";
    
    /** The property name for the port to connect to <i>OMERO</i>. */
    public static final String  OMEDS_PORT = "server.port";  
    
    
    /**
     * Public methods have to call this to retrieve the singleton.
     * The first call will create the singleton by reading in the 
     * system properties &#151; an exception will be thrown if
     * something goes awry.  Subsequent calls will just return the
     * singleton.
     * 
     * @return The sigleton.
     */
    private static Env getInstance()
    {
        if (singleton == null) singleton = new Env();
        return singleton;
    }
    
    /**
     * Returns the URL to connect to <i>OMEDS</i>.
     * 
     * @return See above.
     */
    public static String getOmedsUrl() { return getInstance().get(OMEDS_URL); }
    
    /**
     * The login user name of the <i>OMEDS</i> test user.
     * 
     * @return See above.
     */
    public static String getOmedsUser()
    { 
        return getInstance().get(OMEDS_USER); 
    }
    
    /**
     * Returns the login password of the <i>OMEDS</i> test user.
     * 
     * @return See above.
     */
    public static String getOmedsPass() 
    { 
        return getInstance().get(OMEDS_PASS); 
    }
    
    /**
     * Returns the host to connect to <i>OMERO</i>.
     * 
     * @return See above.
     */
    public static String getOmedsHost()
    {
        return getInstance().get(OMEDS_HOST);
    }
    
    /**
     * Returns the port to connect to <i>OMERO</i>.
     * 
     * @return See above.
     */
    public static String getOmedsPort()
    {
        return getInstance().get(OMEDS_PORT);
    }
    
    /** Holds the the <i>OMEDS</i> tests configuration properties.  */
    private Properties  config;
    
    
    /**
     * Creates a new instance.
     * 
     * @throws IllegalArgumentException If any of the expected properties
     *                                  hasn't been set.
     */
    private Env() 
    {
        String[] propName = {OMEDS_URL, OMEDS_USER, OMEDS_PASS, OMEDS_HOST,
                            OMEDS_PORT};
        config = new Properties();
        String value;
        for (int i = 0; i < propName.length; ++i) {
            value = System.getProperty(propName[i]);
            if (value == null || value.equals(""))
                throw new IllegalArgumentException(propName[i]+
                                    " system property has not been set.");
            config.setProperty(propName[i], value);
        }
    }
    
    /**
     * Returns the value of a configuration property.
     * 
     * @param propName The property name.
     * @return The property value.
     */
    private String get(String propName) { return config.getProperty(propName); }
    
}
