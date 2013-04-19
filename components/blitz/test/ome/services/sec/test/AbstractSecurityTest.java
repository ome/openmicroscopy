/*
 *   $Id: AbstractSecurityTest.java 2147 2008-02-07 11:21:51Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sec.test;

import javax.sql.DataSource;

import junit.framework.TestCase;

import ome.system.Login;
import ome.system.OmeroContext;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.Test;

@Test(enabled = false, groups = { "broken", "client", "integration", "security" })
public class AbstractSecurityTest extends TestCase {

    protected OmeroContext context = null;

    protected client c;

    protected ome.system.ServiceFactory tmp = null; // new ome.system.ServiceFactory(context);

    protected DataSource dataSource = null; // (DataSource) tmp.getContext().getBean( "omero.security.test");

    protected SimpleJdbcTemplate jdbc = null; // new SimpleJdbcTemplate(dataSource);

    protected Login rootLogin = null; // (Login) tmp.getContext().getBean("rootLogin");

    protected ServiceFactoryPrx rootServices;

    protected ServiceFactoryPrx serviceFactory;

    protected IAdminPrx rootAdmin;

    protected IQueryPrx rootQuery;

    protected IUpdatePrx rootUpdate;

    // shouldn't use beforeTestClass here because called by all subclasses
    // in their beforeTestClass i.e. super.setup(); ...
    protected void init() throws Exception {

        // See ticket:10560 for issues with omero.db.poolsize
        context = OmeroContext.getInstance("OMERO.security.test");

        // TODO: Make work
        c = new client();
        serviceFactory = c.createSession(rootLogin.getName(), rootLogin.getPassword());

        // Blitz services
        rootUpdate = serviceFactory.getUpdateService();
        rootQuery = serviceFactory.getQueryService();
        rootAdmin = serviceFactory.getAdminService();

        try {
            rootQuery.get("Experimenter", 0l);
        } catch (Throwable t) {
            // TODO no, no, really. This is ok. (And temporary)
        }
    }
}
