/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.util.Arrays;
import java.util.List;

import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Namespace;
import ome.parameters.Parameters;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.tools.spring.OnContextRefreshedEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hook run by the context. This hook checks for certain well-defined namespaces
 * on {@link #onApplicationEvent(ContextRefreshedEvent)}.
 *
 * @author Josh Moore, josh at glencoesoftwarecom
 * @since 4.2.1
 */
public class NamespaceCheck extends OnContextRefreshedEventListener {

    public final static Logger log = LoggerFactory.getLogger(NamespaceCheck.class);

    public final static String FLIM = "openmicroscopy.org/omero/analysis/flim";

    private final Executor executor;

    private final Principal principal;

    private final Roles roles;

    public NamespaceCheck(Executor executor, String uuid, Roles roles) {
        this.executor = executor;
        this.principal = new Principal(uuid);
        this.roles = roles;
    }

    @Override
    public void handleContextRefreshedEvent(ContextRefreshedEvent event) {
        executor.execute(principal, new Executor.SimpleWork(this,
                "namespaceCheck") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                Parameters params = new Parameters();
                params.addString("name", FLIM);
                List<Object[]> rv = sf.getQueryService().projection(
                        "select id from Namespace where name = :name", params);
                if (rv.size() == 0) {
                    Namespace ns = new Namespace();
                    ns.setKeywords(Arrays.asList("Cell", "Background"));
                    ns.setName(FLIM);
                    ns.setMultivalued(Boolean.FALSE);
                    ns.getDetails()
                            .setGroup(
                                    new ExperimenterGroup(roles
                                            .getUserGroupId(), false));
                    ns = sf.getUpdateService().saveAndReturnObject(ns);
                    sf.getAdminService().moveToCommonSpace(ns);
                    log.info("Created namespace in common space: " + FLIM);
                }
                return null;
            }
        });
    }
}
