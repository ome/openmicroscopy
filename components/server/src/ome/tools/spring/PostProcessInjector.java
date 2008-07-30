/*
 * ome.tools.spring.PostProcessInjector
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.spring;

// Java imports

// Third-party imports
import java.util.List;

import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.security.SecuritySystem;
import ome.security.basic.BasicSecuritySystem;
import ome.security.sharing.SharingSecuritySystem;
import ome.services.sessions.SessionManagerImpl;
import ome.tools.hibernate.ExtendedMetadata;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * catch all {@link BeanPostProcessor} which handles cyclical references.
 */
public class PostProcessInjector implements InitializingBean {
    SessionManagerImpl sessionManager;
    List<SecuritySystem> securitySystems;
    ExtendedMetadata extendedMetadata;
    LocalAdmin adminService;
    LocalQuery queryService;
    LocalUpdate updateService;

    public void afterPropertiesSet() throws Exception {
        for (SecuritySystem ss : securitySystems) {
            if (ss instanceof BasicSecuritySystem) {
                BasicSecuritySystem securitySystem = (BasicSecuritySystem) ss;
                securitySystem.setExtendedMetadata(extendedMetadata);
                securitySystem.setSessionManager(sessionManager);
                securitySystem.setAdminService(adminService);
                securitySystem.setQueryService(queryService);
                securitySystem.setUpdateService(updateService);
            } else if (ss instanceof SharingSecuritySystem) {
                SharingSecuritySystem securitySystem = (SharingSecuritySystem) ss;
                // securitySystem.setSessionManager(sessionManager);
            }
        }

    }

    public void setSecuritySystems(List<SecuritySystem> securitySystems) {
        this.securitySystems = securitySystems;
    }

    public void setAdminService(LocalAdmin adminService) {
        this.adminService = adminService;
    }

    public void setQueryService(LocalQuery queryService) {
        this.queryService = queryService;
    }

    public void setUpdateService(LocalUpdate updateService) {
        this.updateService = updateService;
    }

    public void setSessionManager(SessionManagerImpl sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setExtendedMetadata(ExtendedMetadata extendedMetdata) {
        this.extendedMetadata = extendedMetdata;
    }

}
