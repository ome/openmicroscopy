/*
 *   Copyright 20078 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import omero.api.AMD_IAdmin_getEventContext;
import omero.api.AMD_IAdmin_getGroup;
import omero.model.ExperimenterGroup;
import omero.sys.EventContext;

import org.testng.annotations.Test;

@Test(groups = "integration")
public class AdminITest extends AbstractServantTest {

    @Test
    public void testgetEventContext() throws Exception {
        final RV rv = new RV();
        user.admin.getEventContext_async(new AMD_IAdmin_getEventContext() {
            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

            public void ice_response(EventContext __ret) {
                rv.rv = __ret;
            }
        }, current("getEventContext"));
        rv.assertPassed();
    }

    @Test(groups = "ticket:1204")
    public void testGetGroup() throws Exception {
        final RV rv = new RV();
        user.admin.getGroup_async(new AMD_IAdmin_getGroup() {

            public void ice_exception(Exception exc) {
                rv.ex = exc;
            }

            public void ice_response(ExperimenterGroup __ret) {
                rv.rv = __ret;
            }
        }, 0L, current("getGroup"));
        rv.assertPassed();
    }
}
