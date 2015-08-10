/*
 * ome.system.ServiceFactory
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

// Java imports
import java.util.Properties;

import ome.api.IAdmin;
import ome.api.IAnalysis;
import ome.api.IConfig;
import ome.api.IContainer;
import ome.api.IDelete;
import ome.api.ILdap;
import ome.api.IMetadata;
import ome.api.IPixels;
import ome.api.IProjection;
import ome.api.IQuery;
import ome.api.IRenderingSettings;
import ome.api.IRepositoryInfo;
import ome.api.ISession;
import ome.api.IShare;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.JobHandle;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.api.Search;
import ome.api.ServiceInterface;
import ome.api.ThumbnailStore;
import ome.conditions.ApiUsageException;
import ome.model.meta.Session;
import omeis.providers.re.RenderingEngine;

import org.springframework.beans.BeansException;

/**
 * Entry point for all client calls. Provides methods to obtain proxies for all
 * remote facades.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see OmeroContext
 * @since 3.0
 */
public class ServiceFactory {

    /**
     * the {@link OmeroContext context instance} which this
     * {@link ServiceFactory} uses to look up all of its state.
     */
    protected OmeroContext ctx;

    /**
     * public access to the context. This may not always be available, but for
     * this initial phase, it makes some sense. Completely non-dangerous on the
     * client-side.
     * 
     * @deprecated
     */
    @Deprecated
    public OmeroContext getContext() {
        return ctx;
    }

    /**
     * default constructor which obtains the global static
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context} from
     * {@link ome.system.OmeroContext}. This can be done manually by calling
     * {@link ome.system.OmeroContext#getClientContext()}
     * 
     * @see OmeroContext#CLIENT_CONTEXT
     * @see OmeroContext#getClientContext()
     */
    public ServiceFactory() {
        if (getDefaultContext() != null) {
            this.ctx = OmeroContext.getInstance(getDefaultContext());
        }
    }

    /**
     * constructor which obtains a new (non-static)
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context}, passing
     * in the {@link Properties} representation of the {@link Login} for
     * configuration.
     * 
     * @see Login#asProperties()
     * @see #ServiceFactory(Properties)
     */
    public ServiceFactory(Login login) {
        this.ctx = OmeroContext.getClientContext(login.asProperties());
    }

    /**
     * constructor which obtains a new (non-static)
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context}, passing
     * in the {@link Properties} representation of the {@link Server} for
     * configuration.
     * 
     * @see Server#asProperties()
     * @see #ServiceFactory(Properties)
     */
    public ServiceFactory(Server server) {
        this.ctx = OmeroContext.getClientContext(server.asProperties());
    }

    /**
     * constructor which obtains a new (non-static)
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context}, passing
     * in the {@link Properties} representation of both the {@link Server} and
     * the {@link Login} for configuration.
     * 
     * @see Login#asProperties()
     * @see #ServiceFactory(Properties)
     */
    public ServiceFactory(Server server, Login login) {
        Properties s = server.asProperties();
        Properties l = login.asProperties();
        s.putAll(l);
        this.ctx = OmeroContext.getClientContext(s);
    }

    /**
     * constructor which obtains a new
     * {@link ome.system.OmeroContext#CLIENT_CONTEXT client context}, passing
     * in the provided properties for configuration.
     * 
     * @see OmeroContext#getClientContext(Properties)
     */
    public ServiceFactory(Properties properties) {
        this.ctx = OmeroContext.getClientContext(properties);
    }

    /**
     * constructor which uses the provided {@link OmeroContext} for all
     * loookups.
     */
    public ServiceFactory(OmeroContext context) {
        this.ctx = context;
    }

    /**
     * constructor which finds the global static {@link OmeroContext} with the
     * given name.
     * 
     * @see OmeroContext#CLIENT_CONTEXT
     * @see OmeroContext#MANAGED_CONTEXT
     */
    public ServiceFactory(String contextName) {
        this.ctx = OmeroContext.getInstance(contextName);
    }

    // ~ Stateless services
    // =========================================================================

    public IAdmin getAdminService() {
        return getServiceByClass(IAdmin.class);
    }

    public IAnalysis getAnalysisService() {
        return getServiceByClass(IAnalysis.class);
    }

    public IConfig getConfigService() {
        return getServiceByClass(IConfig.class);
    }

    public IContainer getContainerService() {
        return getServiceByClass(IContainer.class);
    }

    public IDelete getDeleteService() {
        return getServiceByClass(IDelete.class);
    }

    public ILdap getLdapService() {
        return getServiceByClass(ILdap.class);
    }

    public IPixels getPixelsService() {
        return getServiceByClass(IPixels.class);
    }

    public IProjection getProjectionService() {
        return getServiceByClass(IProjection.class);
    }

    public IQuery getQueryService() {
        return getServiceByClass(IQuery.class);
    }

    public IShare getShareService() {
        return getServiceByClass(IShare.class);
    }

    public ITypes getTypesService() {
        return getServiceByClass(ITypes.class);
    }

    public IUpdate getUpdateService() {
        return getServiceByClass(IUpdate.class);
    }

    public IRenderingSettings getRenderingSettingsService() {
        return getServiceByClass(IRenderingSettings.class);
    }

    public IRepositoryInfo getRepositoryInfoService() {
        return getServiceByClass(IRepositoryInfo.class);
    }

    public IMetadata getMetadataService() {
        return getServiceByClass(IMetadata.class);
    }
    
    // ~ Stateful services
    // =========================================================================

    /**
     * create a new {@link JobHandle} proxy. This proxy will have to be
     * initialized using {@link JobHandle#attach(long)} or
     * {@link JobHandle#submit(ome.model.jobs.Job)}.
     */
    public JobHandle createJobHandle() {
        return getServiceByClass(JobHandle.class);
    }

    /**
     * create a new {@link RawPixelsStore} proxy. This proxy will have to be
     * initialized using {@link RawPixelsStore#setPixelsId(long, boolean)}
     */
    public RawPixelsStore createRawPixelsStore() {
        return getServiceByClass(RawPixelsStore.class);
    }

    /**
     * create a new {@link RawFileStore} proxy. This proxy will have to be
     * initialized using {@link RawFileStore#setFileId(long)}
     */
    public RawFileStore createRawFileStore() {
        return getServiceByClass(RawFileStore.class);
    }

    /**
     * create a new {@link RenderingEngine} proxy. This proxy will have to be
     * initialized using {@link RenderingEngine#lookupPixels(long)} and
     * {@link RenderingEngine#load()}
     */
    public RenderingEngine createRenderingEngine() {
        return getServiceByClass(RenderingEngine.class);
    }

    /**
     * create a new {@link Search} proxy.
     */
    public Search createSearchService() {
        return getServiceByClass(Search.class);
    }

    /**
     * create a new {@link ThumbnailStore} proxy. This proxy will have to be
     * initialized using {@link ThumbnailStore#setPixelsId(long)}
     */
    public ThumbnailStore createThumbnailService() {
        return getServiceByClass(ThumbnailStore.class);
    }

    // ~ Sessions
    // =========================================================================

    public ISession getSessionService() {
        return getServiceByClass(ISession.class);
    }

    public Session getSession() throws ApiUsageException {
        return getSessionInitializer().getSession();
    }

    public void setSession(Session session) throws ApiUsageException {
        SessionInitializer si = getSessionInitializer();
        si.setSession(session);
    }

    public void closeSession() throws ApiUsageException {
        ISession is = getSessionService();
        SessionInitializer si = getSessionInitializer();
        if (si.hasSession()) {
            Session s = si.getSession();
            try {
                is.closeSession(s);
            } finally {
                si.setSession(null);
            }
        }
    }

    protected SessionInitializer getSessionInitializer() {
        SessionInitializer si;
        try {
            si = (SessionInitializer) this.ctx.getBean("init");
        } catch (Exception e) {
            throw new ApiUsageException("This ServiceFactory is not configured "
                    + "for sessions");
        }
        return si;
    }

    // ~ Helpers
    // =========================================================================

    /**
     * looks up services based on the current {@link #getPrefix() prefix} and
     * the class name of the service type.
     */
    public <T extends ServiceInterface> T getServiceByClass(Class<T> klass) {
        try {
            return klass.cast(this.ctx.getBean(getPrefix() + klass.getName()));
        } catch (BeansException be) {
            if (be.getCause() instanceof RuntimeException) {
                throw (RuntimeException) be.getCause();
            } else {
                throw be;
            }
        }
    }

    /**
     * used by {@link #getServiceByClass(Class)} to find the correct service
     * proxy in the {@link #ctx}
     * 
     * @return a {@link String}, usually "internal-" or "managed-"
     */
    protected String getPrefix() {
        return "managed-";
    }

    /**
     * used when no {@link OmeroContext context} name is provided to the
     * constructor. Subclasses can override to allow for easier creation.
     * 
     * @return name of default context as found in beanRefContext.xml.
     */
    protected String getDefaultContext() {
        return OmeroContext.CLIENT_CONTEXT;
    }
}
