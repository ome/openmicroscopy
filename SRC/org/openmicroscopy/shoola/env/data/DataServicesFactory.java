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
import java.net.URL;
import java.net.MalformedURLException;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.RemoteException;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.config.RegistryFactory;
import org.openmicroscopy.shoola.env.config.HostInfo;
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
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class DataServicesFactory
{
    /**
	 * Creates a new {@link DataManagementService} and {@link
	 * SemanticTypesService}.
	 * 
	 * @param reg	Reference to the {@link Registry}.
	 */
	public static void createDataServices(Registry reg)
        throws NotLoggedInException
	{
        UserCredentials uc = (UserCredentials)
            reg.lookup(LookupNames.USER_CREDENTIALS);
        if (uc == null)
            throw new NotLoggedInException("User has not provided credentials");

        HostInfo hi = (HostInfo)
            reg.lookup(LookupNames.OMEDS);
        if (hi == null)
            throw new NotLoggedInException("No data server host provided!");

        URL url = null;
        try
        {
            url = new URL("http",hi.getHost(),hi.getPort().intValue(),"");
        } catch (MalformedURLException e) {
            throw new NotLoggedInException("Malformed data server URL "+
                                           e.getMessage());
        }

        try
        {
            DataManagementService dms =
                new RemoteDataManagementService(url,
                                                uc.getUserName(),
                                                uc.getPassword());
            // sts = new RemoteSemanticTypesService(dms.getRemoteCaller());

            RegistryFactory.linkDMS(dms,reg);
            //RegistryFactory.linkSTS(sts,reg);
        } catch (RemoteException e) {
            throw new NotLoggedInException("Could not log into data server"+
                                           e.getMessage());
        }
	}
	
}
