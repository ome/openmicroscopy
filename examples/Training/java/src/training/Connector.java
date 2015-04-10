/*
 * training.Connector
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package training;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import omero.client;
import omero.api.IAdmin;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.IMetadataPrx;
import omero.api.IPixelsPrx;
import omero.api.IQueryPrx;
import omero.api.IRoiPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ThumbnailStorePrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.DoAll;
import omero.cmd.HandlePrx;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.grid.SharedResourcesPrx;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;

/** 
 * Sample code showing how to connect to an OMERO server.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class Connector {

    //The value used if the configuration file is not used. To edit*/
    /** The server address.*/
    private String hostName = "serverName";

    /** The username.*/
    private String userName = "userName";

    /** The password.*/
    private String password = "password";
    //end edit

    /** Reference to the clients.*/
    protected client client, unsecureClient;

    /** The service factory.*/
    protected ServiceFactoryPrx entryUnencrypted;

    /** The configuration information.*/
    private ConfigurationInfo info;

    /** Connects to the server.*/
    protected void connect()
      throws Exception
    {
        try {
            if (info.getPort() > 0) {
                client = new client(info.getHostName(), info.getPort());
            } else {
                client = new client(info.getHostName());
            }
            client.createSession(info.getUserName(), info.getPassword());
            // if you want to have the data transfer encrypted then you can 
            //use the entry variable otherwise use the following 
            unsecureClient = client.createClient(false);
            entryUnencrypted = unsecureClient.getSession();
            if (info.getGroup() != null && !info.getGroup().isEmpty()) {
                ExperimenterGroup g = getAdminService().lookupGroup(
                        info.getGroup());
                if (g != null) {
                    client.getSession().setSecurityContext(
                          new ExperimenterGroupI(g.getId().getValue(), false));
                }
            }
        } catch (Exception e) {
            throw new Exception("Cannot create a session");
        }
    }

    /** Disconnects.*/
    protected void disconnect()
            throws Exception
    {
        if (client != null) client.__del__(); // No exception
        if (unsecureClient != null) unsecureClient.__del__(); // No exception
    }

    /**
     * Sets the connection information and connect if <code>true</code>.
     *
     * @param info
     * @param connect
     */
    Connector(ConfigurationInfo info, boolean connect)
    {
        if (info == null) { //run from main
            info = new ConfigurationInfo();
            info.setHostName(hostName);
            info.setPassword(password);
            info.setUserName(userName);
        }
        this.info = info;
        if (connect) {
            try {
                connect();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    disconnect(); // Be sure to disconnect
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Shows how to connect to omero.
     * 
     * @param info Configuration info or <code>null</code>.
     */
    Connector(ConfigurationInfo info)
    {
        this(info, false);
    }

    /**
     * Returns the pixels service.
     * 
     * @return See above.
     * @throws ServerError
     */
    IPixelsPrx getPixelsService()
            throws Exception
    {
        try {
            return entryUnencrypted.getPixelsService();
        } catch (Exception e) {
            throw new Exception("Cannot start pixels service", e);
        }
    }

    /**
     * Returns the update service.
     * 
     * @return See above.
     * @throws ServerError
     */
    IUpdatePrx getUpdateService()
            throws Exception
    {
        try {
            return entryUnencrypted.getUpdateService();
        } catch (Exception e) {
            throw new Exception("Cannot start update service", e);
        }
    }

    /**
     * Returns the raw pixels store.
     * 
     * @return See above.
     * @throws ServerError
     */
    RawPixelsStorePrx getRawPixelsStore()
            throws Exception
    {
        try {
            return entryUnencrypted.createRawPixelsStore();
        } catch (Exception e) {
            throw new Exception("Cannot start raw pixels store", e);
        }
    }

    /**
     * Returns the container service.
     * 
     * @return See above.
     * @throws ServerError
     */
    IContainerPrx getContainerService()
            throws Exception
    {
        try {
            return entryUnencrypted.getContainerService();
        } catch (Exception e) {
            throw new Exception("Cannot start container service", e);
        }
    }

    /**
     * Returns the shared resources.
     * 
     * @return See above.
     * @throws ServerError
     */
    SharedResourcesPrx getSharedResources()
            throws Exception
    {
        try {
            return entryUnencrypted.sharedResources();
        } catch (Exception e) {
            throw new Exception("Cannot start shared resources", e);
        }
    }

    /**
     * Returns the thumbnail store.
     * 
     * @return See above.
     * @throws ServerError
     */
    ThumbnailStorePrx getThumbnailStore()
            throws Exception
    {
        try {
            return entryUnencrypted.createThumbnailStore();
        } catch (Exception e) {
            throw new Exception("Cannot start thumbnail store", e);
        }
    }

    /**
     * Returns the rendering engine.
     * 
     * @return See above.
     * @throws ServerError
     */
    RenderingEnginePrx getRenderingEngine()
            throws Exception
    {
        try {
            return entryUnencrypted.createRenderingEngine();
        } catch (Exception e) {
            throw new Exception("Cannot start rendering engine", e);
        }
    }

    /**
     * Returns the admin service.
     * 
     * @return See above.
     * @throws ServerError
     */
    IAdminPrx getAdminService()
            throws Exception
    {
        try {
            return entryUnencrypted.getAdminService();
        } catch (Exception e) {
            throw new Exception("Cannot start admin service", e);
        }
    }

    /**
     * Returns the query service.
     * 
     * @return See above.
     * @throws ServerError
     */
    IQueryPrx getQueryService()
            throws Exception
    {
        try {
            return entryUnencrypted.getQueryService();
        } catch (Exception e) {
            throw new Exception("Cannot start query service", e);
        }
    }

    /**
     * Returns the ROI service.
     * 
     * @return See above.
     * @throws ServerError
     */
    IRoiPrx getRoiService()
            throws Exception
    {
        try {
            return entryUnencrypted.getRoiService();
        } catch (Exception e) {
            throw new Exception("Cannot start ROI service", e);
        }
    }

    /**
     * Returns the Raw File store.
     * 
     * @return See above.
     * @throws ServerError
     */
    RawFileStorePrx getRawFileStore()
            throws Exception
    {
        try {
            return entryUnencrypted.createRawFileStore();
        } catch (Exception e) {
            throw new Exception("Cannot start raw file store", e);
        }
    }

    /**
     * Returns the Metadata service.
     * 
     * @return See above.
     * @throws ServerError
     */
    IMetadataPrx getMetadataService()
            throws Exception
    {
        try {
            return entryUnencrypted.getMetadataService();
        } catch (Exception e) {
            throw new Exception("Cannot start metadata service", e);
        }
    }

    /**
     * Submits the specified commands
     * 
     * @param commands The commands to submit.
     * @return See above.
     * @throws ServerError
     * @throws InterruptedException
     */
    Response submit(List<Request> commands)
            throws Exception
    {
        try {
            DoAll all = new DoAll();
            all.requests = commands;
            final Map<String, String> callContext = new HashMap<String, String>();
            final HandlePrx prx = entryUnencrypted.submit(all, callContext);
            CmdCallbackI cb = new CmdCallbackI(unsecureClient, prx);
            cb.loop(20, 500);
            return cb.getResponse();
        } catch (Exception e) {
            throw new Exception("Cannot start submit request", e);
        }

    }
    /**
     * Runs the script without configuration options.
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        new Connector(null, true);
        System.exit(0);
    }

}
