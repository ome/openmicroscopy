/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.testing;

import ome.api.IAdmin;
import ome.api.IAnalysis;
import ome.api.IConfig;
import ome.api.IDelete;
import ome.api.ILdap;
import ome.api.IPixels;
import ome.api.IContainer;
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
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import omeis.providers.re.RenderingEngine;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;

/**
 * Wraps all returned services with the given interceptor;
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3.1
 */
public class InterceptingServiceFactory extends ServiceFactory {

    final ServiceFactory sf;
    final MethodInterceptor[] interceptors;

    public InterceptingServiceFactory(ServiceFactory sf, MethodInterceptor... interceptors) {
        this.sf = sf;
        this.interceptors = interceptors;
    }

    @SuppressWarnings("unchecked")
    <T extends ServiceInterface> T wrap(T service) {
        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(service.getClass().getInterfaces());
        for (MethodInterceptor i : interceptors) {
            factory.addAdvice(i);
        }
        factory.setTarget(service);
        return (T) factory.getProxy();
    }

    @Override
    protected String getDefaultContext() {
        return null;
    }

    @Override
    public void closeSession() throws ApiUsageException {
        sf.closeSession();
    }

    @Override
    public JobHandle createJobHandle() {
        return wrap(sf.createJobHandle());
    }

    @Override
    public RawFileStore createRawFileStore() {
        return wrap(sf.createRawFileStore());
    }

    @Override
    public RawPixelsStore createRawPixelsStore() {
        return wrap(sf.createRawPixelsStore());
    }

    @Override
    public RenderingEngine createRenderingEngine() {
        return wrap(sf.createRenderingEngine());
    }

    @Override
    public Search createSearchService() {
        return wrap(sf.createSearchService());
    }

    @Override
    public ThumbnailStore createThumbnailService() {
        return wrap(sf.createThumbnailService());
    }

    @Override
    public IAdmin getAdminService() {
        return wrap(sf.getAdminService());
    }

    @Override
    public IAnalysis getAnalysisService() {
        return wrap(sf.getAnalysisService());
    }

    @Override
    public IConfig getConfigService() {
        return wrap(sf.getConfigService());
    }

    @Override
    public OmeroContext getContext() {
        return sf.getContext();
    }

    @Override
    public IDelete getDeleteService() {
        return wrap(sf.getDeleteService());
    }

    @Override
    public ILdap getLdapService() {
        return wrap(sf.getLdapService());
    }

    @Override
    public IPixels getPixelsService() {
        return wrap(sf.getPixelsService());
    }

    @Override
    public IContainer getContainerService() {
        return wrap(sf.getContainerService());
    }

    @Override
    public IQuery getQueryService() {
        return wrap(sf.getQueryService());
    }

    @Override
    public IRenderingSettings getRenderingSettingsService() {
        return wrap(sf.getRenderingSettingsService());
    }

    @Override
    public IRepositoryInfo getRepositoryInfoService() {
        return wrap(sf.getRepositoryInfoService());
    }

    @Override
    public <T extends ServiceInterface> T getServiceByClass(Class<T> klass) {
        return wrap(sf.getServiceByClass(klass));
    }

    @Override
    public Session getSession() throws ApiUsageException {
        return sf.getSession();
    }

    @Override
    public ISession getSessionService() {
        return wrap(sf.getSessionService());
    }

    @Override
    public IShare getShareService() {
        return wrap(sf.getShareService());
    }

    @Override
    public ITypes getTypesService() {
        return wrap(sf.getTypesService());
    }

    @Override
    public IUpdate getUpdateService() {
        return wrap(sf.getUpdateService());
    }

    @Override
    public void setSession(Session session) throws ApiUsageException {
        sf.setSession(session);
    }

    @Override
    public String toString() {
        return sf.toString();
    }

}
