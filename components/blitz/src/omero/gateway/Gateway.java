/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package omero.gateway;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ome.formats.OMEROMetadataStoreClient;
import omero.RType;
import omero.ServerError;
import omero.client;
import omero.api.ExporterPrx;
import omero.api.IAdminPrx;
import omero.api.IConfigPrx;
import omero.api.IContainerPrx;
import omero.api.IMetadataPrx;
import omero.api.IPixelsPrx;
import omero.api.IProjectionPrx;
import omero.api.IQueryPrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.IRepositoryInfoPrx;
import omero.api.IRoiPrx;
import omero.api.IScriptPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.SearchPrx;
import omero.api.ServiceFactoryPrx;
import omero.api.StatefulServiceInterfacePrx;
import omero.api.ThumbnailStorePrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.HandlePrx;
import omero.cmd.Request;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.util.NetworkChecker;
import omero.grid.ProcessCallbackI;
import omero.grid.ScriptProcessPrx;
import omero.grid.SharedResourcesPrx;
import omero.log.LogMessage;
import omero.log.Logger;
import omero.model.ExperimenterGroupI;
import omero.util.CommonsLangUtils;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.util.PojoMapper;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class Gateway {

    private Logger log;
    private String serverVersion;
    private NetworkChecker networkChecker;
    private boolean connected = false;
    private ScheduledThreadPoolExecutor keepAliveExecutor;
    private LoginCredentials login;

    private ListMultimap<Long, Connector> groupConnectorMap = Multimaps
            .<Long, Connector> synchronizedListMultimap(LinkedListMultimap
                    .<Long, Connector> create());

    public Gateway(Logger log) {
        this.log = log;
    }

    // Public connection handling methods

    public ExperimenterData connect(LoginCredentials c)
            throws DSOutOfServiceException {
        client client = createSession(c);
        ExperimenterData user = login(client, c);
        connected = true;
        return user;
    }

    public void disconnect() {
        boolean online = isNetworkUp(false);
        List<Connector> connectors = getAllConnectors();
        Iterator<Connector> i = connectors.iterator();
        while (i.hasNext())
            i.next().shutDownServices(true);
        i = connectors.iterator();
        while (i.hasNext()) {
            try {
                i.next().close(online);
            } catch (Throwable e) {
                log.warn(this, new LogMessage("Cannot close connector", e));
            }
        }
        groupConnectorMap.clear();
        keepAliveExecutor.shutdown();
        connected = false;
    }

    /**
     * Tries to rejoin the session.
     *
     * @return See above.
     */
    public boolean joinSession() {
        try {
            isNetworkUp(false); // Force re-check to prevent hang
        } catch (Exception e) {
            // no need to handle the exception.
        }
        boolean networkup = isNetworkUp(false);
        connected = false;
        if (!networkup) {
            log.warn(this, "Network is down");
            return false;
        }
        List<Connector> connectors = removeAllConnectors();
        Iterator<Connector> i = connectors.iterator();
        Connector c;
        int index = 0;
        while (i.hasNext()) {
            c = i.next();
            try {
                log.debug(this, "joining the session ");
                c.joinSession();
                groupConnectorMap.put(c.getGroupID(), c);
            } catch (Throwable t) {
                log.error(this,
                        new LogMessage("Failed to join the session ", t));
                // failed to join so we create a new one, first we shut down
                try {
                    c.shutDownServices(true);
                    c.close(networkup);
                } catch (Throwable e) {
                    log.error(this, new LogMessage(
                            "Failed to close the session ", t));
                }
                if (!groupConnectorMap.containsKey(c.getGroupID())) {
                    try {
                        createConnector(new SecurityContext(c.getGroupID()),
                                false);
                    } catch (Exception e) {
                        log.error(this, new LogMessage(
                                "Failed to create connector ", e));
                        index++;
                    }
                }
            }
        }
        connected = index == 0;
        return connected;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getSessionId(ExperimenterData user) {
        try {
            Connector c = getConnector(new SecurityContext(user.getGroupId()),
                    false, false);
            if (c != null) {
                return c.getClient().getSessionId();
            }
        } catch (DSOutOfServiceException e) {
        }
        return null;
    }

    public String getServerVersion() throws DSOutOfServiceException {
        if (serverVersion == null) {
            throw new DSOutOfServiceException("Not logged in.");
        }
        return serverVersion;
    }

    // General public methods
    
    /**
     * Executes the commands.
     * 
     * @param commands The commands to execute.
     * @param target The target context is any.
     * @return See above.
     */
     public CmdCallbackI submit(SecurityContext ctx, List<Request> commands, SecurityContext target)
            throws Throwable
    {
         Connector c = getConnector(ctx, true, false);
         if (c != null)
             return c.submit(commands, target);
         return null;
    }
     
     public CmdCallbackI submit(SecurityContext ctx, Request cmd)
             throws Throwable
     {
          Connector c = getConnector(ctx, true, false);
          if (c != null) {
              client client = getConnector(ctx, true, false).getClient();
              HandlePrx handle = client.getSession().submit(cmd);
              return new CmdCallbackI(client, handle);
          }
          return null;
     }
     
     public void closeImport(SecurityContext ctx, String userName) {
         try {
             Connector c = getConnector(ctx, false, true);
             if (c != null) {
                 if(CommonsLangUtils.isNotEmpty(userName))
                     c = c.getConnector(userName);
                  c.closeImport();
             }
         } catch (Throwable e) {
             log.warn(this, "Failed to close import: " + e);
         }
     }
     
    public ProcessCallbackI runScript(SecurityContext ctx, long scriptID,
            Map<String, RType> parameters) throws DSOutOfServiceException,
            ServerError {
        Connector c = getConnector(ctx);
        if (c == null)
            return null;
        IScriptPrx svc = c.getScriptService();
        ScriptProcessPrx prx = svc.runScript(scriptID, parameters, null);
        return new ProcessCallbackI(c.getClient(), prx);
    }
     
    // Public service access methods

    /**
     * Returns the {@link SharedResourcesPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public SharedResourcesPrx getSharedResources(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getSharedResources();
        return null;
    }

    /**
     * Returns the {@link IRenderingSettingsPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IRenderingSettingsPrx getRenderingSettingsService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getRenderingSettingsService();
        return null;
    }

    /**
     * Returns the {@link IRepositoryInfoPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IRepositoryInfoPrx getRepositoryService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getRepositoryService();
        return null;
    }

    /**
     * Returns the {@link IScriptPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IScriptPrx getScriptService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getScriptService();
        return null;
    }

    /**
     * Returns the {@link IContainerPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IContainerPrx getPojosService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getPojosService();
        return null;
    }

    /**
     * Returns the {@link IQueryPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IQueryPrx getQueryService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getQueryService();
        return null;
    }

    /**
     * Returns the {@link IUpdatePrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IUpdatePrx getUpdateService(SecurityContext ctx)
            throws DSOutOfServiceException {
        return getUpdateService(ctx, null);
    }
    
    /**
     * Returns the {@link IUpdatePrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IUpdatePrx getUpdateService(SecurityContext ctx, String userName)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (CommonsLangUtils.isNotEmpty(userName)) {
            try {
                c = c.getConnector(userName);
            } catch (Throwable e) {
                throw new DSOutOfServiceException(
                        "Can't get derived connector.", e);
            }
        }
        if (c != null)
            return c.getUpdateService();
        return null;
    }

    /**
     * Returns the {@link IMetadataPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IMetadataPrx getMetadataService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getMetadataService();
        return null;
    }

    /**
     * Returns the {@link IRoiPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IRoiPrx getROIService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getROIService();
        return null;
    }

    /**
     * Returns the {@link IConfigPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IConfigPrx getConfigService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getConfigService();
        return null;
    }

    /**
     * Returns the {@link ThumbnailStorePrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public ThumbnailStorePrx getThumbnailService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getThumbnailService();
        return null;
    }

    /**
     * Returns the {@link ExporterPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws @throws Throwable Thrown if the service cannot be initialized.
     */
    public ExporterPrx getExporterService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getExporterService();
        return null;
    }

    /**
     * Returns the {@link RawFileStorePrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws @throws Throwable Thrown if the service cannot be initialized.
     */
    public RawFileStorePrx getRawFileService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getRawFileService();
        return null;
    }

    /**
     * Returns the {@link RawPixelsStorePrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public RawPixelsStorePrx getPixelsStore(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getPixelsStore();
        return null;
    }

    /**
     * Returns the {@link IPixelsPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IPixelsPrx getPixelsService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getPixelsService();
        return null;
    }

    /**
     * Returns the {@link SearchPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public SearchPrx getSearchService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getSearchService();
        return null;
    }

    /**
     * Returns the {@link IProjectionPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IProjectionPrx getProjectionService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getProjectionService();
        return null;
    }

    /**
     * Returns the {@link IAdminPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IAdminPrx getAdminService(SecurityContext ctx)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getAdminService();
        return null;
    }

    /**
     * Returns the {@link IAdminPrx} service.
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param secure
     *            Pass <code>true</code> to have a secure admin service,
     *            <code>false</code> otherwise.
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public IAdminPrx getAdminService(SecurityContext ctx, boolean secure)
            throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getAdminService();
        return null;
    }

    /**
     * Creates or recycles the import store.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public OMEROMetadataStoreClient getImportStore(SecurityContext ctx) throws DSOutOfServiceException {
        return getImportStore(ctx, null);
    }
    
    /**
     * Creates or recycles the import store.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public OMEROMetadataStoreClient getImportStore(SecurityContext ctx,
            String userName) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        if (CommonsLangUtils.isNotEmpty(userName)) {
            try {
                c = c.getConnector(userName);
            } catch (Throwable e) {
                throw new DSOutOfServiceException(
                        "Can't get derived connector.", e);
            }
        }
        if (c != null)
            return c.getImportStore();
        return null;
    }

    /**
     * Returns the {@link RenderingEnginePrx Rendering service}.
     * 
     * @return See above.
     * @throws Throwable
     *             Thrown if the service cannot be initialized.
     */
    public RenderingEnginePrx getRenderingService(SecurityContext ctx,
            long pixelsID, float compression)
            throws DSOutOfServiceException, ServerError {
        Connector c = getConnector(ctx, true, false);
        if (c != null)
            return c.getRenderingService(pixelsID, compression);
        return null;
    }

    // Internal helper methods

    private List<Connector> removeAllConnectors() {
        synchronized (groupConnectorMap) {
            // This should be the only location which calls values().
            List<Connector> rv = new ArrayList<Connector>(
                    groupConnectorMap.values());
            groupConnectorMap.clear();
            return rv;
        }
    }

    private client createSession(LoginCredentials c)
            throws DSOutOfServiceException {
        client secureClient = null;

        try {
            // client must be cleaned up by caller.
            if (c.getServer().getPort() > 0)
                secureClient = new client(c.getServer().getHostname(), c
                        .getServer().getPort());
            else
                secureClient = new client(c.getServer().getHostname());
            secureClient.setAgent(c.getApplicationName());
            ServiceFactoryPrx entryEncrypted;
            boolean session = true;
            try {
                // Check if it is a session first
                ServiceFactoryPrx guestSession = secureClient.createSession(
                        "guest", "guest");
                guestSession.getSessionService().getSession(
                        c.getUser().getUsername());
            } catch (Exception e) {
                // thrown if it is not a session or session has experied.
                session = false;
            } finally {
                secureClient.closeSession();
            }
            if (session) {
                entryEncrypted = secureClient.joinSession(c.getUser()
                        .getUsername());
            } else {
                entryEncrypted = secureClient.createSession(c.getUser()
                        .getUsername(), c.getUser().getPassword());
            }
            serverVersion = entryEncrypted.getConfigService().getVersion();

            String ip = null;
            try {
                ip = InetAddress.getByName(c.getServer().getHostname())
                        .getHostAddress();
            } catch (Exception e) {
                log.warn(this, new LogMessage("Failed to get inet address: "
                        + c.getServer().getHostname(), e));
            }

            if (c.isCheckNetwork())
                networkChecker = new NetworkChecker(ip, log);

            Runnable r = new Runnable() {
                public void run() {
                    try {
                        keepSessionAlive();
                    } catch (Throwable t) {
                        log.warn(
                                this,
                                new LogMessage(
                                        "Exception while keeping the services alive",
                                        t));
                    }
                }
            };
            keepAliveExecutor = new ScheduledThreadPoolExecutor(1);
            keepAliveExecutor.scheduleWithFixedDelay(r, 60, 60,
                    TimeUnit.SECONDS);
        } catch (Throwable e) {
            if (secureClient != null) {
                secureClient.__del__();
            }
            throw new DSOutOfServiceException(
                    "Can't connect to OMERO. OMERO info not valid", e);
        }
        return secureClient;
    }

    private ExperimenterData login(client client, LoginCredentials cred)
            throws DSOutOfServiceException {
        this.login = cred;
        Connector connector = null;
        SecurityContext ctx = null;
        try {
            ServiceFactoryPrx entryEncrypted = client.getSession();
            IAdminPrx prx = entryEncrypted.getAdminService();
            String userName = prx.getEventContext().userName;
            ExperimenterData exp = (ExperimenterData) PojoMapper
                    .asDataObject(prx.lookupExperimenter(userName));
            if (cred.getGroupID() >= 0) {
                long defaultID = -1;
                try {
                    defaultID = exp.getDefaultGroup().getId();
                } catch (Exception e) {
                }
                ctx = new SecurityContext(defaultID);
                ctx.setServerInformation(cred.getServer());
                connector = new Connector(ctx, client, entryEncrypted,
                        cred.isEncryption(), log);
                groupConnectorMap.put(ctx.getGroupID(), connector);
                if (defaultID == cred.getGroupID())
                    return exp;
                try {
                    changeCurrentGroup(ctx, exp, cred.getGroupID());
                    ctx = new SecurityContext(cred.getGroupID());
                    ctx.setServerInformation(cred.getServer());
                    ctx.setCompression(cred.getCompression());
                    connector = new Connector(ctx, client, entryEncrypted,
                            cred.isEncryption(), log);
                    exp = getUserDetails(ctx, userName, true);
                    groupConnectorMap.put(ctx.getGroupID(), connector);
                } catch (Exception e) {
                    LogMessage msg = new LogMessage();
                    msg.print("Error while changing group.");
                    msg.print(e);
                    log.debug(this, msg);
                }
            }
            // Connector now controls the secureClient for closing.
            ctx = new SecurityContext(exp.getDefaultGroup().getId());
            ctx.setServerInformation(cred.getServer());
            ctx.setCompression(cred.getCompression());
            connector = new Connector(ctx, client, entryEncrypted,
                    cred.isEncryption(), log);
            groupConnectorMap.put(ctx.getGroupID(), connector);
            return exp;
        } catch (Throwable e) {
            throw new DSOutOfServiceException(
                    "Cannot log in. User credentials not valid", e);
        }
    }

    private void keepSessionAlive() throws DSOutOfServiceException {
        // Check if network is up before keeping service otherwise
        // we block until timeout.
        try {
            isNetworkUp(false);
        } catch (Exception e) {
            throw new DSOutOfServiceException("Network not available");
        }
        Iterator<Connector> i = getAllConnectors().iterator();
        Connector c;
        while (i.hasNext()) {
            c = i.next();
            if (c.needsKeepAlive()) {
                if (!c.keepSessionAlive()) {
                    throw new DSOutOfServiceException("Network not available");
                }
            }
        }
    }

    private void changeCurrentGroup(SecurityContext ctx, ExperimenterData exp,
            long groupID) throws DSOutOfServiceException {
        List<GroupData> groups = exp.getGroups();
        Iterator<GroupData> i = groups.iterator();
        GroupData group = null;
        boolean in = false;
        while (i.hasNext()) {
            group = i.next();
            if (group.getId() == groupID) {
                in = true;
                break;
            }
        }
        if (in) {
            Connector c = getConnector(ctx, true, false);
            try {
                IAdminPrx svc = c.getAdminService();
                svc.setDefaultGroup(exp.asExperimenter(),
                        new ExperimenterGroupI(groupID, false));
            } catch (Exception e) {
                throw new DSOutOfServiceException(
                        "Can't modify the current group for user:"
                                + exp.getId(), e);
            }
        }

        String s = "Can't modify the current group.\n\n";
        if (!in) {
            throw new DSOutOfServiceException(s);
        }
    }

    private ExperimenterData getUserDetails(SecurityContext ctx, String name,
            boolean connectionError) throws DSOutOfServiceException {
        Connector c = getConnector(ctx, true, false);
        try {
            IAdminPrx service = c.getAdminService();
            return (ExperimenterData) PojoMapper.asDataObject(service
                    .lookupExperimenter(name));
        } catch (Exception e) {
            throw new DSOutOfServiceException("Cannot retrieve user's data ", e);
        }
    }

    /**
     * Checks if the network interface is up.
     * 
     * @param useCachedValue
     *            Uses the result of the last check instead of really performing
     *            the test if the last check is not older than 5 sec
     * @throws Exception
     */
    public boolean isNetworkUp(boolean useCachedValue) {
        try {
            if (networkChecker != null)
                return networkChecker.isNetworkup(useCachedValue);
            else
                return true;
        } catch (Throwable t) {
            log.warn(this, new LogMessage("Error on isNetworkUp check", t));
            return false;
        }
    }

    private List<Connector> getAllConnectors() {
        synchronized (groupConnectorMap) {
            // This should be the only location which calls values().
            return new ArrayList<Connector>(groupConnectorMap.values());
        }
    }

    public Connector getConnector(SecurityContext ctx)
            throws DSOutOfServiceException {
        return getConnector(ctx, false, false);
    }

    public void closeConnector(SecurityContext ctx) {
        List<Connector> clist = groupConnectorMap.removeAll(ctx.getGroupID());
        if (clist == null || clist.isEmpty())
            return;

        for (Connector c : clist) {
            try {
                c.close(isNetworkUp(true));
            } catch (Throwable e) {
                new Exception("Cannot close the connector", e);
            }
        }
    }
    
    public void closeService(SecurityContext ctx,
            StatefulServiceInterfacePrx svc) {
        try {
            Connector c = getConnector(ctx, false, true);
            if (c != null) {
                c.close(svc);
            } else {
                svc.close(); // Last ditch effort to close.
            }
        } catch (Exception e) {
            log.warn(this, String.format("Failed to close %s: %s", svc, e));
        }
    }

    public RawPixelsStorePrx createPixelsStore(SecurityContext ctx)
            throws DSOutOfServiceException {
        if (ctx == null)
            return null;
        Connector c = getConnector(ctx, true, false);
        return c.getPixelsStore();
    }

    public ThumbnailStorePrx createThumbnailStore(SecurityContext ctx)
            throws DSOutOfServiceException {
        if (ctx == null)
            return null;
        // check import as
        Connector c = getConnector(ctx, true, false);
        ExperimenterData exp = ctx.getExperimenterData();
        if (exp != null && ctx.isSudo()) {
            try {
                c = c.getConnector(exp.getUserName());
            } catch (Throwable e) {
                throw new DSOutOfServiceException(
                        "Cannot create ThumbnailStore", e);
            }
        }
        // Pass close responsibility off to the caller.
        return c.getThumbnailService();
    }

    public boolean isAlive(SecurityContext ctx) throws DSOutOfServiceException {
        return null != getConnector(ctx, true, true);
    }

    /**
     * Returns the connector corresponding to the passed context.
     * 
     * @param ctx
     *            The security context.
     * @param recreate
     *            whether or not to allow the recreation of the
     *            {@link Connector}. A {@link DSOutOfServiceException} is thrown
     *            if this is set to false and no {@link Connector} is available.
     * @param permitNull
     *            whether or not to throw a {@link DSOutOfServiceException} if
     *            no {@link Connector} is available by the end of the execution.
     * @return
     */
    public Connector getConnector(SecurityContext ctx, boolean recreate,
            boolean permitNull) throws DSOutOfServiceException {
        try {
            isNetworkUp(true); // Need safe version?
        } catch (Exception e1) {
            if (permitNull) {
                log.warn(
                        this,
                        new LogMessage(
                                "Failed to check network. Returning null connector",
                                e1));
                return null;
            }
            throw new DSOutOfServiceException("Network not available", e1);
        }

        if (!isNetworkUp(true)) {
            if (permitNull) {
                log.warn(this, "Network down. Returning null connector");
                return null;
            }
            throw new DSOutOfServiceException(
                    "Network down. Returning null connector");
        }

        if (ctx == null) {
            if (permitNull) {
                log.warn(this, "Null SecurityContext");
                return null;
            }
            throw new DSOutOfServiceException("Null SecurityContext");
        }

        Connector c = null;
        List<Connector> clist = groupConnectorMap.get(ctx.getGroupID());
        if (clist.size() > 0) {
            c = clist.get(0);
            if (c.needsKeepAlive()) {
                // Check if network is up before keeping service otherwise
                // we block until timeout.
                try {
                    isNetworkUp(true);
                } catch (Exception e) {
                    throw new DSOutOfServiceException("Network down.", e);
                }
                if (!c.keepSessionAlive()) {
                    throw new DSOutOfServiceException("Network down.");
                }
            }
        }

        // We are going to create a connector and activate a session.
        if (c == null) {
            if (recreate)
                c = createConnector(ctx, permitNull);
            else {
                if (permitNull) {
                    log.warn(this, "Cannot re-create. Returning null connector");
                    return null;
                }
                throw new DSOutOfServiceException("Not allowed to recreate");
            }
        }
        
        ExperimenterData exp = ctx.getExperimenterData();
        if (exp != null && ctx.isSudo()) {
            try {
                c = c.getConnector(exp.getUserName());
            } catch (Throwable e) {
                throw new DSOutOfServiceException("Could not derive connector",
                        e);
            }
        }

        return c;
    }

    /**
     * Shuts down the connectors created while creating/importing data for other
     * users.
     *
     * @param ctx
     * @throws Exception
     *             Thrown if the connector cannot be closed.
     */
    public void shutDownDerivedConnector(SecurityContext ctx) throws Exception {
        Connector c = getConnector(ctx, true, true);
        if (c == null)
            return;
        try {
            c.closeDerived(isNetworkUp(true));
        } catch (Throwable e) {
            new Exception("Cannot close the derived connectors", e);
        }
    }

    /**
     * Returns the rendering engines to re-activate.
     *
     * @return See above.
     */
    public Map<SecurityContext, Set<Long>> getRenderingEngines() {
        Map<SecurityContext, Set<Long>> l = new HashMap<SecurityContext, Set<Long>>();
        Iterator<Connector> i = getAllConnectors().iterator();
        while (i.hasNext()) {
            l.putAll(i.next().getRenderingEngines());
        }
        return l;
    }

    public void shutdownRenderingEngine(SecurityContext ctx, long pixelsID) {
        List<Connector> clist = groupConnectorMap.get(ctx.getGroupID());
        for (Connector c : clist) {
            c.shutDownRenderingEngine(pixelsID);
        }
    }

    private Connector createConnector(SecurityContext ctx, boolean permitNull)
            throws DSOutOfServiceException {
        Connector c = null;
        try {
            ctx.setServerInformation(login.getServer());
            // client will be cleaned up by connector
            client client = new client(login.getServer().getHostname(), login
                    .getServer().getPort());
            ServiceFactoryPrx prx = client.createSession(login.getUser()
                    .getUsername(), login.getUser().getPassword());
            prx.setSecurityContext(new ExperimenterGroupI(ctx.getGroupID(),
                    false));
            c = new Connector(ctx, client, prx, login.isEncryption(), log);
            groupConnectorMap.put(ctx.getGroupID(), c);
        } catch (Throwable e) {
            if (!permitNull) {
                throw new DSOutOfServiceException("Failed to create connector",
                        e);
            }
        }
        return c;
    }
}
