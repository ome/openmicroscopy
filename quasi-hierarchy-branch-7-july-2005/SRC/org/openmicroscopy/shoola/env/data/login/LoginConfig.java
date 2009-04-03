/*
 * org.openmicroscopy.shoola.env.data.login.LoginConfig
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

package org.openmicroscopy.shoola.env.data.login;


//Java imports
import java.net.URL;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.OMEDSInfo;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Holds the configuration parameters used by the {@link LoginService}.
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
public class LoginConfig
{

    /**
     * The default number of times the {@link LoginService} should attempt to
     * restore an invalid link to <i>OMEDS</i>.
     */
    public static final int     DEFAULT_MAX_RETRY = 3;
    
    /**
     * The default amount of time, in milliseconds, that the 
     * {@link LoginService} should wait between each attempt to restore an
     * invalid link to <i>OMEDS</i>. 
     */
    public static final int     DEFAULT_RETRY_INTERVAL = 500;
    
    
    /** 
     * The address to use for connecting to <i>OMEDS</i>.
     * This field is read from the Container's configuration file.
     * It may not be <code>null</code>.
     */
    private URL             omedsAddress;
    
    /** 
     * The current user's credentials for logging onto <i>OMEDS</i>.
     * This field is <code>null</code> until the user enters their credentials.
     * It is subsequently updated every time the user specifies new credentials. 
     */
    private UserCredentials credentials;
    
    /** 
     * The number of times the {@link LoginService} should attempt to restore
     * an invalid link to <i>OMEDS</i>.
     * This field is read from the Container's configuration file or set to
     * {@link #DEFAULT_MAX_RETRY} if none or an invalid one is found in the
     * configuration file.
     */
    private int             maxRetry;
    
    /**
     * The amount of time, in milliseconds, that the {@link LoginService}
     * should wait between each attempt to restore an invalid link to
     * <i>OMEDS</i>.
     * This field is read from the Container's configuration file or set to
     * {@link #DEFAULT_RETRY_INTERVAL} if none or an invalid one is found in
     * the configuration file.
     */
    private int             retryInterval;
    
    
    /**
     * Initializes this object's fields with the values found in the registry.
     * 
     * @param reg The Container's registry.
     */
    private void readConfig(Registry reg)
    {
        OMEDSInfo info = (OMEDSInfo) reg.lookup(LookupNames.OMEDS);
        if (info != null) omedsAddress = info.getServerAddress();
        Integer x = (Integer) reg.lookup(LookupNames.LOGIN_MAX_RETRY);
        maxRetry = (x == null ? -1 : x.intValue());
        x = (Integer) reg.lookup(LookupNames.LOGIN_RETRY_INTV);
        retryInterval = (x == null ? -1 : x.intValue());
    }
    
    
    void setCredentials(UserCredentials uc) { credentials = uc; }
    
    /**
     * Creates a new instance which reads the configuration parameters from
     * the specified registry.
     * 
     * @param reg The Container's registry.  Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If no <i>OMEDS</i> address was found
     *                                  in the registry. 
     */
    public LoginConfig(Registry reg)
    {
        if (reg == null) throw new NullPointerException("No registry.");
        readConfig(reg);
        if (omedsAddress == null)
            throw new IllegalArgumentException(
                    "No OMEDS address was found in the configuration.");
        //TODO: Get rid of this check when we have an XML schema for config.
        
        if (maxRetry <= 0) maxRetry = DEFAULT_MAX_RETRY;
        if (retryInterval <= 0) retryInterval = DEFAULT_RETRY_INTERVAL;
    }
    
    /**
     * Returns the address to use for connecting to <i>OMEDS</i>.
     * This field is never <code>null</code>.
     * 
     * @return See above.
     */
    public URL getOmedsAddress() { return omedsAddress; }
    
    /**
     * Returns the current user's credentials for logging onto <i>OMEDS</i>.
     * This field is <code>null</code> until the user enters their credentials.
     * It is subsequently updated every time the user specifies new credentials.
     * 
     * @return See above.
     */
    public UserCredentials getCredentials() { return credentials; }
    
    /**
     * Returns the number of times the {@link LoginService} should attempt to 
     * restore an invalid link to <i>OMEDS</i>.
     * This field is always positive.
     * 
     * @return See above.
     */
    public int getMaxRetry() { return maxRetry; }

    /**
     * Returns the amount of time, in milliseconds, the {@link LoginService}
     * should wait between each attempt to restore an invalid link to
     * <i>OMEDS</i>.
     * This field is always positive.
     * 
     * @return See above.
     */
    public int getRetryInterval() { return retryInterval; }
    
}
