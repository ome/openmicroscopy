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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ShareBeanTest extends MockObjectTestCase {

    Mock mockAdmin, mockMgr;
    LocalAdmin admin;
    SessionManager mgr;
    ShareBean bean;

    @BeforeMethod
    public void setup() {

        mockAdmin = mock(LocalAdmin.class);
        mockMgr = mock(SessionManager.class);

        admin = (LocalAdmin) mockAdmin.proxy();
        mgr = (SessionManager) mockMgr.proxy();
        bean = new ShareBean(admin, mgr, null, null);
    }

    @Test
    public void testCreation() {
        bean.createShare("this is my description", null, null, null, null,
                false);
    }

}
