/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.icy.client;


import java.util.Map;
import java.util.Properties;

import ome.system.Login;
import ome.system.OmeroContext;
import ome.system.Server;
import omero.ServerError;
import omero.api.IAdminPrx;
import omero.api.IConfigPrx;
import omero.api.IPixelsPrx;
import omero.api.IPojosPrx;
import omero.api.IQueryPrx;
import omero.api.ITypesPrx;
import omero.api.IUpdatePrx;
import omero.api.IRepositoryInfoPrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;
import omero.api.ServiceInterfacePrx;
import omero.api.SimpleCallbackPrx;
import omero.api.ThumbnailStorePrx;
import omero.constants.PASSWORD;
import omero.constants.USERNAME;
import omero.romio.RawFileStorePrx;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import Glacier2.SessionNotExistException;
import Glacier2.SessionPrx;
import Ice.ConnectionLostException;
import Ice.ObjectNotFoundException;

/**
 * Entry point for all Ice client calls. Provides methods to obtain proxies for
 * all remote facades.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see OmeroContext
 * @see Ice.Communicator
 * @since 3.0-Beta2
 */
public class IceServiceFactory {

    /**
     * Common name for the {@link OmeroContext} provided partially by
     * classpath:omero/client.xml and defined in classpath:beanRefContext.xml
     */
    public final static String OMERO_CLIENT = "OMERO.client";

    protected OmeroContext ctx;

    protected Ice.Communicator ic;

    protected String conn;

    protected Glacier2.RouterPrx router;

    protected ServiceFactoryPrx sf;

    /**
     * The {@link OmeroContext context instance} which this
     * {@link IceServiceFactory} uses to look up all of its state.
     */
    public OmeroContext getContext() {
        return ctx;
    }

    /**
     * The {@link Ice.Communicator communicator instance} which this
     * {@link IceServiceFactory} uses to communicate with an OMERO server.
     */
    public Ice.Communicator getCommunicator() {
        return ic;
    }

    public Glacier2.RouterPrx getRouter() {
        return router;
    }

    public ServiceFactoryPrx getProxy() {
        return sf;
    }

    /**
     * default constructor which obtains the global static
     * {@link #OMERO_CLIENT client context}. This can be done manually by
     * calling {@link ome.system.OmeroContext#getInstance(String)}
     *
     * @see #OMERO_CLIENT
     * @see OmeroContext#getInstance()
     */
    public IceServiceFactory() {
        this.ctx = OmeroContext.getInstance(getDefaultContext());
        init();
    }

    /**
     * constructor which obtains a new (non-static)
     * {@link #OMERO_CLIENT client context}, passing in the merged
     * {@link Properties} representation of the {@link Login}, {@link Server},
     * and initial {@link Properties} instances for configuration.
     *
     * @see Login#asProperties()
     * @see Server#asProperties()
     * @see #IceServiceFactory(Properties)
     */
    public IceServiceFactory(Properties properties, Server server, Login login) {
        Properties p = properties == null ? new Properties() : properties;
        if (server != null)
            p.putAll(server.asProperties());
        if (login != null)
            p.putAll(login.asProperties());
        this.ctx = OmeroContext.getContext(p, getDefaultContext());
        init();
    }

    /**
     * Constructor which uses the provided {@link OmeroContext} for all
     * loookups.
     */
    public IceServiceFactory(OmeroContext context) {
        this.ctx = context;
        init();
    }

    private void init() {
        this.ic = (Ice.Communicator) ctx.getBean("Ice.Communicator");
        this.conn = ic.getProperties().getProperty("IceServiceFactory_conn");
        Ice.RouterPrx rprx = ic.getDefaultRouter();
        this.router = Glacier2.RouterPrxHelper.uncheckedCast(rprx);
    }

    public void destroy() {
        this.ic.destroy();
        this.ctx.destroy();
    }

    // ~ Accessors
    // =========================================================================

    public void createSession() throws CannotCreateSessionException,
            PermissionDeniedException {

        String name = getCommunicator().getDefaultContext().get(USERNAME.value);
        String pass = getCommunicator().getDefaultContext().get(PASSWORD.value);
        SessionPrx prx = getRouter().createSession(name,pass);
        sf = ServiceFactoryPrxHelper.checkedCast(prx);
    }

    public String getSessionId() {
        return ic.proxyToString(sf);
    }

    public void useSession(String sessionId) throws ObjectNotFoundException {
        Ice.ObjectPrx obj = ic.stringToProxy(sessionId);
        sf = ServiceFactoryPrxHelper.checkedCast(obj);
        if (sf == null) {
            ObjectNotFoundException onfe = new ObjectNotFoundException();
            throw onfe;
        }

    }

    public void setUmask() {
    }

    // ~ Helpers
    // =========================================================================

    /**
     * used when no {@link OmeroContext context} name is provided to the
     * constructor. Subclasses can override to allow for easier creation.
     *
     * @return name of default context as found in beanRefContext.xml.
     */
    protected String getDefaultContext() {
        return OMERO_CLIENT;
    }

    // ~ Delegation
    // =========================================================================

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#createRawFileStore(java.util.Map)
     */
    public omero.api.RawFileStorePrx createRawFileStore(Map __ctx) {
        return sf.createRawFileStore(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#createRawPixelsStore(java.util.Map)
     */
    public RawPixelsStorePrx createRawPixelsStore(Map __ctx) {
        return sf.createRawPixelsStore(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#createRenderingEngine(java.util.Map)
     */
    public RenderingEnginePrx createRenderingEngine(Map __ctx) {
        return sf.createRenderingEngine(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#createThumbnailStore(java.util.Map)
     */
    public ThumbnailStorePrx createThumbnailStore(Map __ctx) {
        return sf.createThumbnailStore(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#getRepositoryInfoService(java.util.Map)
     */
    public IRepositoryInfoPrx getRepositoryInfoService(Map __ctx) {
        return sf.getRepositoryInfoService(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#getAdminService(java.util.Map)
     */
    public IAdminPrx getAdminService(Map __ctx) {
        return sf.getAdminService(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#getConfigService(java.util.Map)
     */
    public IConfigPrx getConfigService(Map __ctx) {
        return sf.getConfigService(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#getPixelsService(java.util.Map)
     */
    public IPixelsPrx getPixelsService(Map __ctx) {
        return sf.getPixelsService(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#getPojosService(java.util.Map)
     */
    public IPojosPrx getPojosService(Map __ctx) {
        return sf.getPojosService(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#getQueryService(java.util.Map)
     */
    public IQueryPrx getQueryService(Map __ctx) {
        return sf.getQueryService(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#getTypesService(java.util.Map)
     */
    public ITypesPrx getTypesService(Map __ctx) {
        return sf.getTypesService(__ctx);
    }

    /**
     * @param __ctx
     * @return
     * @see omero.api.ServiceFactoryPrx#getUpdateService(java.util.Map)
     */
    public IUpdatePrx getUpdateService(Map __ctx) {
        return sf.getUpdateService(__ctx);
    }

    /**
     * @param callback
     * @param __ctx
     * @see omero.api.ServiceFactoryPrx#setCallback(omero.api.SimpleCallbackPrx, java.util.Map)
     */
    public void setCallback(SimpleCallbackPrx callback, Map __ctx) {
        sf.setCallback(callback, __ctx);
    }

    /**
     * @param __ctx
     * @see omero.api.ServiceFactoryPrx#close()
     */
    public void close(Map __ctx) {
        sf.close(__ctx);
    }


    /**
     * @param name
     * @param __ctx
     * @see omero.api.ServiceFactoryPrx#getByName(String, Map)
     */
    public ServiceInterfacePrx getByName(String name, Map __ctx) throws ServerError {
        return sf.getByName(name, __ctx);
    }

}
