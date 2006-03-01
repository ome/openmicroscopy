/*
 * org.openmicroscopy.shoola.env.data.DataServicesFactory
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

//Third-party libraries

//Application-internal dependencies
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.OMEDSInfo;
import org.openmicroscopy.shoola.env.config.OMEROInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.views.DataViewsFactory;

import pojos.ExperimenterData;

/** 
 * A factory for the {@link DataManagementService} and the
 * {@link SemanticTypesService}.
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
	
	private static DataServicesFactory		singleton;
	
	//NB: this can't be called outside of container b/c agents have no refs
	//to the singleton container. So we can be sure this method is going to
	//create services just once.
	public static DataServicesFactory getInstance(Container c)
		throws DSOutOfServiceException
	{
		if (c == null)
			throw new NullPointerException();  //An agent called this method?
		if (singleton == null)	singleton = new DataServicesFactory(c);
		return singleton;
	}
	
	//Container cfg.
	private Registry               registry;
	
	//Unified access point to the various OMEDS services.
	private OMEDSGateway           gateway;
	
//  Unified access point to the various OMERO services.
    private OMEROGateway            omeroGateway;
    
	//Our adapters.
	private DMSAdapter             dms;
	private STSAdapter		       sts;
    private PixelsServiceAdapter   ps;
	
    //Omero Adapter;
    private OmeroPojoService           ops;
    
	/**
	 * Try to create a new instance.
	 * @param c		container.
	 * @throws DSOutOfServiceException If the connection can't be established
	 * 									or the credentials are invalid.	
	 */
	private DataServicesFactory(Container c)
		throws DSOutOfServiceException
	{
		registry = c.getRegistry();
		
		//Retrieve the connection URL and create the internal proxy to OMEDS.
		OMEDSInfo info = (OMEDSInfo) registry.lookup(LookupNames.OMEDS);
		if (info == null)  //TODO: get rid of this when we have an XML schema.
			throw new NullPointerException("No data server host provided!");
        gateway = new OMEDSGateway(info.getServerAddress(), this);
		
        OMEROInfo omeroInfo = (OMEROInfo) registry.lookup(LookupNames.OMERODS);
        omeroGateway = new OMEROGateway(omeroInfo.getHostName(), 
                                        omeroInfo.getPort(), this);
		//Create the adapters.
		dms = new DMSAdapter(gateway, registry); 
		sts = new STSAdapter(gateway, registry);
        ps = new PixelsServiceAdapter(gateway.getDataFactory());
        ops = new OmeroPojoServiceImpl(omeroGateway, registry);
        //Initialize the Views Factory.
        DataViewsFactory.initialize(c);
	}
	
    public OmeroPojoService getOPS() { return ops; }
    
	public DataManagementService getDMS() { return dms; }

	public SemanticTypesService getSTS() { return sts; }
    
    public PixelsService getPS() { return ps; }
    
    public LoginService getLoginService()
    {
        return (LoginService) registry.lookup(LookupNames.LOGIN);
    }

	/**
	 * Attempts to connect to <i>OMEDS</i>.
	 * 
     * @param uc The user's credentials for logging onto <i>OMEDS</i>.
	 * @throws DSOutOfServiceException If the connection can't be established
     *                                 or the credentials are invalid.							
	 */
	public void connect(UserCredentials uc)
		throws DSOutOfServiceException
	{
		if (uc == null)
            throw new NullPointerException("No user credentials.");
        ExperimenterData exp = omeroGateway.login(uc.getUserName(), 
                									uc.getPassword());
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
        gateway.login(uc.getUserName(), uc.getPassword());
	}
	
	/**
	 * Tells whether the communication channel to <i>OMEDS</i> is currently
	 * connected.
	 * This means that we have established a connection and have sucessfully
	 * logged in.
	 * 
	 * @return	<code>true</code> if connected, <code>false</code> otherwise.
	 */
	public boolean isConnected() { return gateway.isConnected(); }
	
	public void shutdown() { gateway.logout(); }
	
}
