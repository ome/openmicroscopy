/*
 * ome.tools.spring.PostProcessInjector
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.spring;

// Java imports

// Third-party imports
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.security.basic.BasicSecuritySystem;
import ome.services.sessions.SessionManagerImpl;
import ome.tools.hibernate.ExtendedMetadata;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * catch all {@link BeanPostProcessor} which handles cyclical references.
 */
public class PostProcessInjector implements InitializingBean {
    SessionManagerImpl sessionManager;
    BasicSecuritySystem securitySystem;
    ExtendedMetadata extendedMetdata;
    LocalQuery queryService;
    LocalUpdate updateService;

    public void afterPropertiesSet() throws Exception {
        securitySystem.setExtendedMetadata(extendedMetdata);
        securitySystem.setSessionManager(sessionManager);
        securitySystem.setQueryService(queryService);
        securitySystem.setUpdateService(updateService);
    }

    public void setQueryService(LocalQuery queryService) {
        this.queryService = queryService;
    }

    public void setUpdateService(LocalUpdate updateService) {
        this.updateService = updateService;
    }

    public void setSecuritySystem(BasicSecuritySystem securitySystem) {
        this.securitySystem = securitySystem;
    }

    public void setSessionManager(SessionManagerImpl sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setExtendedMetadata(ExtendedMetadata extendedMetdata) {
        this.extendedMetdata = extendedMetdata;
    }

}
