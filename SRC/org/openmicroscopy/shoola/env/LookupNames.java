/*
 * org.openmicroscopy.shoola.env.LookupNames
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

	public static final  String ENV = "/env";
	
	public static final String USER_DETAILS = "/user/details";
    
	public static final String AGENTS = "/agents";
	
	public static final String OMEDS = "/services/OMEDS";
	
	public static final String OMEIS = "/services/OMEIS";
    
    public static final String OMERODS = "/services/OMERODS";
	
	public static final String LOOK_N_FEEL = "LookAndFeel";	
	
	public static final String ICONS_FACTORY = 
											"/resources/icons/DefaultFactory";
	
	public static final String LOG_ON = "/services/LOG/on";
	public static final String LOG_DIR = "/services/LOG/dir";
	public static final String LOG_FILE = "/services/LOG/file";
	
	public static final String RE_STACK_BUF_SZ = "/services/RE/stackBufSz";
	public static final String RE_STACK_BLOCK_SZ = "/services/RE/stackBlockSz";
    public static final String RE_CACHE_SZ = "/services/RE/cacheSz";
    public static final String RE_MAX_PRE_FETCH = "/services/RE/maxPreFetch";
    
    public static final String CMD_PROCESSOR = "/services/CmdProcessor";
    
    public static final String MONITOR_FACTORY = 
                                          "/services/data/views/MonitorFactory";
    
    public static final String LOGIN = "/services/Login";
    public static final String LOGIN_CFG = "/services/Login/config";
    public static final String LOGIN_MAX_RETRY = 
                                              "/services/LOGIN/omeds/max-retry";
    public static final String LOGIN_RETRY_INTV = 
                                         "/services/LOGIN/omeds/retry-interval";
	
}
