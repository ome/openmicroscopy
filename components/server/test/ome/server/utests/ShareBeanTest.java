/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import ome.api.local.LocalAdmin;
import ome.services.sessions.SessionManager;
import ome.services.sharing.ShareBean;
import ome.services.util.Executor;
import ome.system.EventContext;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ShareBeanTest extends MockObjectTestCase {

    Mock mockAdmin, mockMgr, mockEc, mockEx;
    LocalAdmin admin;
    SessionManager mgr;
    EventContext ec;
    ShareBean bean;
    Executor ex;

    @BeforeMethod
    public void setup() {

        mockAdmin = mock(LocalAdmin.class);
        mockMgr = mock(SessionManager.class);
        mockEc = mock(EventContext.class);
        mockEx = mock(Executor.class);
        
        admin = (LocalAdmin) mockAdmin.proxy();
        mgr = (SessionManager) mockMgr.proxy();
        ec = (EventContext) mockEc.proxy();
        ex = (Executor) mockEx.proxy();
        bean = new ShareBean(admin, mgr, null, ex);
    }

    @Test
    public void testCreation() {
        
        mockAdmin.expects(once()).method("getEventContext").will(returnValue(ec));
        mockEc.expects(once()).method("getCurrentUserName").will(returnValue("user"));
        bean.createShare("this is my description", null, null, null, null,
                false);
    }

}
