/*
 * ome.formats.OMEROMetadataStore
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package src.adminTool.omero;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;


/**
 * An OMERO metadata store. This particular metadata store requires the user to
 * be logged into OMERO prior to use with the {@link #login()} method. NOTE: All
 * indexes are ignored by this metadata store as they don't make much real
 * sense.
 * 
 * @author Brian W. Loranger brain at lifesci.dundee.ac.uk
 * @author Chris Allan callan at blackcat.ca
 */
public class OMEROMetadataStore 
{

    /** OMERO service factory; all other services are retrieved from here. */
    private ServiceFactory sf;

    /** OMERO raw pixels service */
    private RawPixelsStore pservice;

    /** OMERO query service */
    private IQuery         iQuery;

    /** OMERO update service */
    private IUpdate        iUpdate;

    /** OMERO admin service. */
    private IAdmin			iAdmin;
    
    /** The "root" pixels object */
    private Pixels         pixels = new Pixels();
    
    private Experimenter    exp;
    
    private RawFileStore    rawFileStore;
    
    /**
     * Creates a new instance.
     * 
     * @param username the username to use to login to the OMERO server.
     * @param password the password to use to login to the OMERO server.
     * @param host the hostname of the OMERO server.
     * @param port the port the OMERO server is listening on.
      */
    public OMEROMetadataStore(String username, String password, String host,
            String port)
    {
        // Mask the password information for display in the debug window
        String maskedPswd = "";
        if (password.length() > 0) maskedPswd = "<" +password.length() + "chars>";
        else maskedPswd = "<empty>";
        
        // Attempt to log in
            Server server = new Server(host, Integer.parseInt(port));
            Login login = new Login(username, password);
            // Instantiate our service factory
            sf = new ServiceFactory(server, login);

            // Now initialize all our services
            iAdmin = sf.getAdminService();
            
            iQuery = sf.getQueryService();
            iUpdate = sf.getUpdateService();
            pservice = sf.createRawPixelsStore();
            rawFileStore = sf.createRawFileStore();
            
            exp = iQuery.findByString(Experimenter.class, "omeName", username);
    }
    
    public IAdmin getAdminService()
    {
    	return iAdmin;
    }
    
    public IQuery getQueryService()
    {
    	return iQuery;
    }
   
    public IUpdate getUpdateService()
    {
    	return iUpdate;
    }
}
