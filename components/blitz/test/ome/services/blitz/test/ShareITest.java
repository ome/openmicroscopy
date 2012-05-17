/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import omero.api.AMD_IShare_createShare;
import omero.api.AMD_IShare_getShare;
import omero.model.Share;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 */
@Test(groups = { "integration", "share" })
public class ShareITest extends AbstractServantTest {

    @BeforeClass
    public void setup() throws Exception {
        super.setUp();
    }


    /**
     * Reproducing the ClassCastException
     */
    @Test(groups = "ticket:2733")
    public void testTicket2733() throws Exception {
        long sid = assertCreateShare();
        Share share = assertGetShare(sid);
        assertNotNull(share);
    }

    //
    // Helpers
    //

    private long assertCreateShare() throws Exception {

        final RV rv = new RV();
        user.share.createShare_async(new AMD_IShare_createShare() {

            public void ice_response(long __ret) {
                rv.rv = __ret;
            }

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

        }, "", null, null, null, null, true, current("createShare"));
        rv.assertPassed();
        return (Long) rv.rv;
    }

    private Share assertGetShare(long sid) throws Exception {
        final RV rv = new RV();
        user.share.getShare_async(new AMD_IShare_getShare() {

            public void ice_response(Share __ret) {
                rv.rv = __ret;
            }

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }

        }, sid, current("getShare"));
        rv.assertPassed();
        return (Share) rv.rv;
    }

}
