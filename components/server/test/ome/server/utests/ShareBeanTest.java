/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.util.List;
import java.util.Set;

import ome.api.local.LocalAdmin;
import ome.model.IObject;
import ome.services.sessions.SessionManager;
import ome.services.sharing.ShareBean;
import ome.services.sharing.ShareStore;
import ome.services.sharing.data.ShareData;
import ome.services.sharing.data.ShareItem;

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
        bean = new ShareBean(admin, mgr, new ShareStore() {

            @Override
            public <T extends IObject> boolean doContains(long sessionId,
                    Class<T> kls, long objId) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void doClose() {
                // TODO Auto-generated method stub

            }

            @Override
            public void doInit() {
                // TODO Auto-generated method stub

            }

            @Override
            public void doSet(ShareData data, List<ShareItem> items) {
                // TODO Auto-generated method stub

            }

            @Override
            public ShareData get(long id) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Set<Long> keys() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int totalSharedItems() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int totalShares() {
                // TODO Auto-generated method stub
                return 0;
            }
        });
    }

    @Test
    public void testCreation() {
        bean.createShare("this is my description", null, null, null, null,
                false);
    }

}
