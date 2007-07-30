/*
 * org.openmicroscopy.shoola.env.data.DataServicesFactory
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

package org.openmicroscopy.shoola.env.data;

//Java imports
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.OMEROInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.views.DataViewsFactory;
import pojos.ExperimenterData;

/** 
 * A factory for the {@link OmeroDataService} and the {@link OmeroImageService}.
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
public class DataServicesFactory
{
	
    /** The sole instance. */
	private static DataServicesFactory		singleton;
	
	/**
	 * Creates a new instance. This can't be called outside of container 
	 * b/c agents have no refs to the singleton container.
	 * So we can be sure this method is going to create services just once.
	 * 
	 * @param c Reference to the singleton container. Mustn't be 
	 * 			<code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException 
	 */
	public static DataServicesFactory getInstance(Container c)
		throws DSOutOfServiceException
	{
		if (c == null)
			throw new NullPointerException();  //An agent called this method?
		if (singleton == null)	singleton = new DataServicesFactory(c);
		return singleton;
	}
	
	/** A reference to the container's registry. */
	private Registry               registry;
	
	/** Unified access point to the various OMERO services. */
    private OMEROGateway            omeroGateway;
	
    /** The omero service adapter. */
    private OmeroDataService           ops;
    
    /** The rendering service adapter. */
    private OmeroImageService        rds;
    
	/**
	 * Attempts to create a new instance.
     * 
	 * @param c	Reference to the container.
	 * @throws DSOutOfServiceException If the connection can't be established
	 * 									or the credentials are invalid.	
	 */
	private DataServicesFactory(Container c)
		throws DSOutOfServiceException
	{
		registry = c.getRegistry();
        OMEROInfo omeroInfo = (OMEROInfo) registry.lookup(LookupNames.OMERODS);
        omeroGateway = new OMEROGateway(omeroInfo.getPort(), this);
		//Create the adapters.
        ops = new OmeroDataServiceImpl(omeroGateway, registry);
        rds = new OmeroImageServiceImpl(omeroGateway, registry);
        //Initialize the Views Factory.
        DataViewsFactory.initialize(c);
	}
	
    /**
     * Returns the {@link OmeroDataService}.
     * 
     * @return See above.
     */
    public OmeroDataService getOS() { return ops; }
    
    /**
     * Returns the {@link OmeroImageService}.
     * 
     * @return See above.
     */
    public OmeroImageService getRDS() { return rds; }
    
    /**
     * Returns the {@link LoginService}. 
     * 
     * @return See above.
     */
    public LoginService getLoginService()
    {
        return (LoginService) registry.lookup(LookupNames.LOGIN);
    }

	/**
	 * Attempts to connect to <i>OMERO</i> server.
	 * 
     * @param uc The user's credentials for logging onto <i>OMERO</i> server.
	 * @throws DSOutOfServiceException If the connection can't be established
     *                                 or the credentials are invalid.							
	 */
	public void connect(UserCredentials uc)
		throws DSOutOfServiceException
	{
		if (uc == null)
            throw new NullPointerException("No user credentials.");
        ExperimenterData exp = omeroGateway.login(uc.getUserName(), 
                									uc.getPassword(),
                                                    uc.getHostName());
        registry.bind(LookupNames.CURRENT_USER_DETAILS, exp);
        //Bind user details to all agents' registry.
        List agents = (List) registry.lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			agentInfo.getRegistry().bind(
			        LookupNames.CURRENT_USER_DETAILS, exp);
		}
	}
	
	/**
	 * Tells whether the communication channel to <i>OMEDS</i> is currently
	 * connected.
	 * This means that we have established a connection and have sucessfully
	 * logged in.
	 * 
	 * @return	<code>true</code> if connected, <code>false</code> otherwise.
	 */
	public boolean isConnected() { return omeroGateway.isConnected(); }
	
    /** Shuts down the connection. */
	public void shutdown()
    { 
        ((OmeroImageServiceImpl) rds).shutDown();
        omeroGateway.logout(); 
    }
	
}
