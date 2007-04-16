/*
 * org.openmicroscopy.shoola.env.LookupNames
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Its static fields contain the names that have to be used for looking up
 * entries in the container's registry.
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
public class LookupNames
{
    
    /** Field to access the <code>env</code> package. */
	public static final  String ENV = "/env";
	
    /** Field to access the user credentials. */
    public static final String USER_CREDENTIALS = "/user/credentials";
    
    /**
     * Field to access the user credentials. To remove when we have no
     * dependencies to OME-JAVA.
     */
    public static final String CURRENT_USER_DETAILS = "/current_user/details";
    
    /** Field to access the user's details.*/
	public static final String USER_DETAILS = "/user/details";
    
    /** Field to access the <code>agents</code> package. */
	public static final String AGENTS = "/agents";
    
    /** Field to access the <code>OMERO</code> service information. */
    public static final String OMERODS = "/services/OMERODS";
	
    /** Field to access the <code>L&F</code> information. */
	public static final String LOOK_N_FEEL = "LookAndFeel";	
	
    /** Field to access the <code>Icons factory</code> information. */
	public static final String ICONS_FACTORY = 
											"/resources/icons/DefaultFactory";
	
    /** Field to access the <code>Log on</code> information. */
	public static final String LOG_ON = "/services/LOG/on";
    
    /** Field to access the <code>Log directory</code> information. */
	public static final String LOG_DIR = "/services/LOG/dir";
    
    /** Field to access the <code>Log file</code> information. */
	public static final String LOG_FILE = "/services/LOG/file";
	
    /** Field to access the <code>Size</code> of the cache. */
    public static final String RE_CACHE_SZ = "/services/RE/cacheSz";
    
	public static final String RE_STACK_BUF_SZ = "/services/RE/stackBufSz";
	public static final String RE_STACK_BLOCK_SZ = "/services/RE/stackBlockSz";

    public static final String RE_MAX_PRE_FETCH = "/services/RE/maxPreFetch";
    
    public static final String CMD_PROCESSOR = "/services/CmdProcessor";
    
    public static final String MONITOR_FACTORY = 
                                          "/services/data/views/MonitorFactory";
    
    /** Field to access the <code>Log service</code> information. */
    public static final String LOGIN = "/services/Login";
    
    /**
     * Field to access the <code>Log service configuration</code> information.
     */
    public static final String LOGIN_CFG = "/services/Login/config";
    
    /**
     * Field to access the maximum number of tries in order to connect to the 
     * OMERO server.
     */
    public static final String LOGIN_MAX_RETRY = 
                                              "/services/LOGIN/omeds/max-retry";
    
    /** Field to access the interval between each connection try. */
    public static final String LOGIN_RETRY_INTV = 
                                         "/services/LOGIN/omeds/retry-interval";
    
    /** Field to access the url of the server where to post comments. */
    public static final String DEBUG_URL_COMMENT = 
    							"/services/DEBUGGER/hostnameComment";
    
    /** Field to access the url of the server where to post error messages. */
    public static final String DEBUG_URL_BUG = "/services/DEBUGGER/hostnameBug";
    
    /** Field to access the e-mail address used to collect comments. */
    public static final String DEBUG_EMAIL = "/services/DEBUGGER/email";
	
}
