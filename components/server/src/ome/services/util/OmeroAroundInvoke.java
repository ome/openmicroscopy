/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.logic.HardWiredInterceptor;
import ome.security.SecuritySystem;
import ome.services.query.QueryFactory;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.SelfConfigurableService;
import ome.system.ServiceFactory;
import ome.tools.spring.AOPAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Used to configure this instance in Spring. This is the first non-service
 * component which has needed self-configuration. These needs to be cleaned up.
 * 
 * @DEV.TODO Should all make a ComponentInterface and SelfConfigurableComponent.
 *           compare to {@link HardWiredInterceptor}
 */
interface OmeroAroundInvokeName extends ServiceInterface {
}

/**
 * JavaEE interceptor which applies {@link HardWiredInterceptor} instances to
 * every invocation. These instances are compiled in via server/build.xml. See
 * etc/*.properties for information on which interceptors are configured.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-Beta2
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class OmeroAroundInvoke implements SelfConfigurableService,
        ApplicationContextAware {

    private transient final BeanHelper beanHelper = new BeanHelper(this
            .getClass());

    /**
     * Interceptors that are determinined at compile time by server/build.xml
     * The string "ome.security.basic.BasicSecurityWiring" may be replaced by a
     * comma separated list of strings representing the class names of
     * HardWiredInterceptor subclasses which are prepended to the list of
     * interceptors for each call. Note: these interceptors will NOT be applied
     * to server internal calls.
     */
    private final static List<HardWiredInterceptor> CPTORS = HardWiredInterceptor
            .parse(new String[] { "ome.security.basic.BasicSecurityWiring" });

    private transient final Log logger = LogFactory.getLog(this.getClass());

    private transient OmeroContext applicationContext;

    private transient ServiceFactory serviceFactory;

    private transient SecuritySystem securitySystem;

    private transient QueryFactory queryFactory;

    private transient final List<HardWiredInterceptor> cptors;

    private @Resource
    SessionContext sessionContext;

    /**
     * Interceptor for EJBs which imposes our security model.
     * 
     * Since this will never be created from within Spring, it is safe to always
     * call {@link #selfConfigure()} in the constructor.
     */
    public OmeroAroundInvoke() {
        this(CPTORS);
    }

    /**
     * Constructor mainly for testing. Takes a {@link List} of
     * {@link HardWiredInterceptor} instances, which are normally compiled into
     * this class as {@link #CPTORS}.
     */
    public OmeroAroundInvoke(List<HardWiredInterceptor> cptors) {
        this.cptors = cptors;
        selfConfigure();
        HardWiredInterceptor.configure(cptors, applicationContext);
    }

    public void selfConfigure() {
        beanHelper.configure(this);
    }

    public Class<? extends ServiceInterface> getServiceInterface() {
        return OmeroAroundInvokeName.class;
    }

    // ~ Invocation.
    // =========================================================================

    /**
     * Responsible for using the {@link Principal} in the {@link SessionContext}
     * and for wrapping all calls to Omero services with the method interceptors
     * defined in Spring. No other logic should be added here, otherwise
     * server-side internal calls will no longer work. (They don't use client
     * {@link Principal principals} to login and are already wrapped when
     * acquired from the application context.
     */
    @AroundInvoke
    protected final Object loginAndSpringWrap(InvocationContext context)
            throws Exception {
        try {
            return call(context);
        } catch (Throwable t) {
            throw beanHelper.translateException(t);
        }

    }

    private Object call(InvocationContext context) throws Throwable {

        Object bean = context.getTarget();
        if (bean instanceof SelfConfigurableService) {
            SelfConfigurableService service = (SelfConfigurableService) bean;
            String factoryName = "&managed:"
                    + service.getServiceInterface().getName();
            AOPAdapter adapter = AOPAdapter.create(
                    (ProxyFactoryBean) applicationContext.getBean(factoryName),
                    context, cptors);
            Object o = sessionContext.getCallerPrincipal();
            if (!(o instanceof ome.system.Principal)) {
                throw new ApiUsageException("Callers must provide an instance "
                        + "of ome.system.Principal for login.");
            }

            HardWiredInterceptor.initializeUserAttributes(adapter,
                    serviceFactory, (Principal) sessionContext
                            .getCallerPrincipal());

            return adapter.proceed();
        } else {
            throw new InternalException("Bean is not self-configurable.");
        }

    }

    // ~ Injection
    // =========================================================================

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        beanHelper.throwIfAlreadySet(this.applicationContext,
                applicationContext);
        this.applicationContext = (OmeroContext) applicationContext;
    }

    /**
     * @param queryFactory
     *            the queryFactory to set
     */
    public void setQueryFactory(QueryFactory queryFactory) {
        beanHelper.throwIfAlreadySet(this.queryFactory, queryFactory);
        this.queryFactory = queryFactory;
    }

    /**
     * @param securitySystem
     *            the securitySystem to set
     */
    public void setSecuritySystem(SecuritySystem securitySystem) {
        beanHelper.throwIfAlreadySet(this.securitySystem, securitySystem);
        this.securitySystem = securitySystem;
    }

}
