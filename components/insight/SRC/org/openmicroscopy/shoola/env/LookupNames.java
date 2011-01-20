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
    
    /** Field to access the <code>Version</code> information. */
	public static final String VERSION = "Version";	
	
	/** Field to access the <code>Name oof the sofware</code>. */
	public static final String SOFTWARE_NAME = "SoftwareName";	
	
	/** Field to access the <code>About file</code> information. */
	public static final String ABOUT_FILE = "AboutFile";
	
	/** Field to access the <code>Splash screen</code> information. */
	public static final String SPLASH_SCREEN_LOGO = "SplashScreenLogo";
	
	/** Field to access the <code>Splash screen</code> information. */
	public static final String SPLASH_SCREEN_LOGIN = "SplashScreenLogin";
	
	/** Field to access the <code>Help on line</code> information. */
	public static final String HELP_ON_LINE = "HelpOnLine";
	
	/** Field to access the <code>Forum</code> information. */
	public static final String FORUM = "Forum";
	
    /** Field to access the <code>env</code> package. */
	public static final  String ENV = "/env";
	
    /** Field to access the user credentials. */
    public static final String USER_CREDENTIALS = "/user/credentials";
    
    /**
     * Field to access the user's details.
     */
    public static final String CURRENT_USER_DETAILS = "/current_user/details";
    
    /** Field to access the users' groups. */
    public static final String USER_GROUP_DETAILS = "/userGroup/details";
    
    /** Field to access the users contained in the group. */
    public static final String USERS_DETAILS = "/users/details";
    
    /** Field to access the LDAP user information. */
    public static final String USER_AUTHENTICATION = "/user/authentication";
    
    /** Field to indicate if the connection is fast or not. */
    public static final String CONNECTION_SPEED = "/connection/speed";
    
    /** Field to access the <code>agents</code> package. */
	public static final String AGENTS = "/agents";
    
    /** Field to access the <code>OMERO</code> service information. */
    public static final String OMERODS = "/services/OMERODS";
	
    /** Field to access the <code>L&F</code> information. */
	public static final String LOOK_N_FEEL = "LookAndFeel";	
	
    /** Field to access the <code>Icons factory</code> information. */
	public static final String ICONS_FACTORY = 
											"/resources/icons/DefaultFactory";
	
	/** Field to access the <code>OMERO home</code> information. */
	public static final String OMERO_HOME = "/services/OMERO/home";
	
	/** 
	 * Field to access the location of the <code>OMERO folder</code>
	 * on the user's machine. 
	 */
	public static final String USER_HOME_OMERO= "/user/home/omero";
	
    /** Field to access the <code>Log on</code> information. */
	public static final String LOG_ON = "/services/LOG/on";
    
    /** Field to access the <code>Log directory</code> information. */
	public static final String LOG_DIR = "/services/LOG/dir";
    
    /** Field to access the <code>Log file</code> information. */
	public static final String LOG_FILE = "/services/LOG/file";
	
	/** 
	 * Field to access the maximum number of thumbnails retrieved 
	 * asynchronously.
	 */
	public static final String THUMBNAIL_FETCH_SZ = 
										"/services/Thumbnailing/fetchSz";
	
	/** 
	 * Field to access the Factor by which the maximum number of thumbnails 
	 * to fetch is multiplied by when the connection's speed is 
	 * <code>Low</code>.
	 */
	public static final String THUMBNAIL_FETCH_LOW_SPEED = 
										"/services/Thumbnailing/fetchSz";
	
	/** 
	 * Field to access the Factor by which the maximum number of thumbnails 
	 * to fetch is multiplied by when the connection's speed is 
	 * <code>Medium</code>.
	 */
	public static final String THUMBNAIL_FETCH_MEDIUM_SPEED = 
									"/services/Thumbnailing/fetchMediumSpeed";
	
	/** Field to access the <code>Cache on</code> information. */
	public static final String CACHE_ON = "/services/CACHE/on";
	
    /** Field to access the <code>Size</code> of the cache. */
    public static final String RE_CACHE_SZ = "/services/RE/cacheSz";
    
	public static final String RE_STACK_BUF_SZ = "/services/RE/stackBufSz";
	public static final String RE_STACK_BLOCK_SZ = "/services/RE/stackBlockSz";

    public static final String RE_MAX_PRE_FETCH = "/services/RE/maxPreFetch";
    
    public static final String CMD_PROCESSOR = "/services/CmdProcessor";
    
    public static final String MONITOR_FACTORY = 
                                          "/services/data/views/MonitorFactory";
    
    /** Field to access the <code>medium compression quality</code>. */
    public static final String COMPRESSIOM_MEDIUM_QUALITY = 
    								"/services/RE/compressionMedium";
    
    /** Field to access the <code>low compression quality</code>. */
    public static final String COMPRESSIOM_LOW_QUALITY = 
    								"/services/RE/compressionLow";
    
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
    
    /** 
     * Field to access the maximum value allowed to establish 
     * a valid link to an <i>OMERO</i> server.
     */
    public static final String LOGIN_TIME_OUT = "/services/LOGIN/omeds/timeout";
    
    /** Field to access the URL of the server where obtain an token. */
    public static final String TOKEN_URL = "/services/DEBUGGER/hostnameToken";
    
    /** Field to access the URL of the server where to submit the files. */
    public static final String PROCESSING_URL = 
    	"/services/DEBUGGER/hostnameProcessing";
    
    /** Field to access the application number to submit bug. */
    public static final String APPLICATION_NAME_BUG = 
    	"/services/DEBUGGER/applicationNameBug";
    
    /** Field to access the application to submit comment. */
    public static final String APPLICATION_NAME_COMMENT = 
    	"/services/DEBUGGER/applicationNameComment";
    
    /** Field to access the timeout value. */
    public static final String POST_TIMEOUT = "/services/DEBUGGER/postTimeout";
    
    /** Field to access the e-mail address used to collect comments. */
    public static final String DEBUG_EMAIL = "/services/DEBUGGER/email";
    
    /** Field to access the file keeping track of the various ROIs files. */
    public static final String	ROI_MAIN_FILE = "/roi/mainFileName";
    
    /** Field to access the <code>Server Available</code> information. */
	public static final String SERVER_AVAILABLE = "/services/SERVER/available";
	
    //For blitz
    /** The value to replace in the FS configuration file. */
    public static final String FS_HOSTNAME = "/services/FS/defaultDirectory";
    
    /** The value to replace in the FS configuration file. */
    public static final String FS_DEFAUL_DIR = "/services/FS/defaultDirectory";
    
    /** The value indicating that the roi will come from the server. */
    public static final String	SERVER_ROI = "/roi/location/server";
}
