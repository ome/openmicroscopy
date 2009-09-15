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
import ome.system.ServiceFactory;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.testng.annotations.Test;

@Test(groups = { "client", "integration", "security" })
public class AbstractSecurityTest extends TestCase {
    
    protected OmeroContext context = OmeroContext.getInstance("ome.client.test");
    
    protected client c;
    
    protected ServiceFactory tmp = new ServiceFactory("ome.client.test");

    protected DataSource dataSource = (DataSource) tmp.getContext().getBean(
            "dataSource");

    protected SimpleJdbcTemplate jdbc = new SimpleJdbcTemplate(dataSource);

    protected Login rootLogin = (Login) tmp.getContext().getBean("rootLogin");

    protected ServiceFactory rootServices;

    protected ServiceFactoryPrx serviceFactory;

    protected IAdminPrx rootAdmin;

    protected IQueryPrx rootQuery;

    protected IUpdatePrx rootUpdate;

    // shouldn't use beforeTestClass here because called by all subclasses
    // in their beforeTestClass i.e. super.setup(); ...
    protected void init() throws Exception {
        
        // TODO: Make work        
        c = new client("", 666);
        serviceFactory = c.createSession("", "");
        
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
