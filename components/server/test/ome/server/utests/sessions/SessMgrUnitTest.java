/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sessions;

import java.util.Collections;

import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.services.sessions.SessionBean;
import ome.services.sessions.SessionManagerImpl;
import ome.system.Principal;
import ome.system.Roles;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessMgrUnitTest extends MockObjectTestCase {

    private Mock adminMock;

    private Mock updateMock;
    
    private Mock queryMock;

    private SessionManagerImpl mgr;

    private LocalAdmin admin;

    private LocalUpdate update;

    private LocalQuery query;
    
    private SessionBean bean;

    private Session session;

    @BeforeTest
    public void config() {
        adminMock = mock(LocalAdmin.class);
        admin = (LocalAdmin) adminMock.proxy();
        updateMock = mock(LocalUpdate.class);
        update = (LocalUpdate) updateMock.proxy();
        queryMock = mock(LocalQuery.class);
        query = (LocalQuery) queryMock.proxy();
        
        mgr = new SessionManagerImpl();
        mgr.setAdminService(admin);
        mgr.setUpdateService(update);
        mgr.setQueryService(query);
        mgr.setRoles(new Roles());

        bean = new SessionBean();
        bean.setLocalAdmin(admin);
        bean.setManager(mgr);

        session = new Session();
        session.setUuid("uuid");
        session.setId(1L);
    }

    @Test( expectedExceptions = ApiUsageException.class)
    public void testChecksForNullPrincipal() throws Exception {
        mgr.create(null);
    }

    @Test( expectedExceptions = ApiUsageException.class)
    public void testChecksForNullPrincipalName() throws Exception {
        mgr.create(new Principal(null,null,null));
    }

    public void testReplacesNullGroupAndType() throws Exception {
        mgr.create(new Principal("fake",null,null));
    }
    
    @Test( expectedExceptions = SecurityViolation.class)
    public void testChecksForDefaultGroups() throws Exception {
    	queryMock.expects(once()).method("findAllByQuery")
    		.will(returnValue(Collections.singletonList(new ExperimenterGroup())));
        mgr.create(new Principal("fake","user","Test"));
    }


    @Test
    public void testCreatingNewSession() throws Exception {
        /*
         * user logs in with: - non-sessioned principal / !E session =>
         * createSession - non-sessioned principal / E session => joinSession -
         * sessionedPrincipal => hasSession() != null, then ok
         */

        updateMock.expects(once()).method("saveAndReturnObject").will(
                returnValue(session));
        Session s = mgr.create(new Principal("name", "group", "type"));

        // now user has an active session, so basic security wiring will
        // allow method executions
        assertNotNull(mgr.find("uuid"));

    }

    @Test
    public void testThatPermissionsAreAlreadyNonReadable() {
        fail("NYI");
    }

}
