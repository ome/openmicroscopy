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
import org.openmicroscopy.ds.XmlRpcCaller;
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.OMEDSInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UserCredentials;

/** 
 * A factory for the {@link DataManagementService} and the
 * {@link SemanticTypeService}.
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
    
    private static Registry		registry;
    private static DMSProxy		dms;
    private static STSProxy		sts;
    private static XmlRpcCaller	transport;
    
    
	
	//NB: this can't be called outside of container b/c agents have no refs
	//to the singleton container. So we can be sure this method is going to
	//create services just once.
	public static void createDataServices(Container c)
	{
		if (c == null)
			throw new NullPointerException();  //An agent called this?
		registry = c.getRegistry();
		OMEDSInfo info = (OMEDSInfo) registry.lookup(LookupNames.OMEDS);
		if (info == null)
			throw new NullPointerException("No data server host provided!");
		//TODO: convert the above in a sound exception.
		transport = new XmlRpcCaller(info.getServerAddress()); //URL
		dms = new DMSProxy(transport);
		sts = new STSProxy(transport);
	}
	
	public static DataManagementService getDMS()
	{
		return dms;
	}
	
	public static SemanticTypesService getSTS()
	{
		return sts;
	}
	
	public static void doLogin()
	{
		UserCredentials uc = (UserCredentials)
								registry.lookup(LookupNames.USER_CREDENTIALS);
		//uc can't be null b/c there's no way to call this method b/f init.
		transport.login(uc.getUserName(), uc.getPassword());
		//TODO: check outcome of logging in.
	}
	
}
