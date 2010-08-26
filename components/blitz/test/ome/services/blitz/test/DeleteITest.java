/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import omero.api.AMD_IDelete_queueDelete;
import omero.api.IDeletePrx;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteHandlePrx;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests call to {@link IDeletePrx}, especially important for testing
 * the {@link IDeletePrx#queueDelete(omero.api.delete.DeleteCommand[])
 * since it is not available from {@link ome.api.IDelete}
 */
@Test(groups = {"integration","delete"})
public class DeleteITest extends AbstractServantTest {

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testBasicUsageOfQueueDelete() throws Exception {
        long imageId = this.makeImage();
        DeleteCommand dc = new DeleteCommand("/Image", imageId, null);
        DeleteHandlePrx handle = queueDelete(dc);
        assertEquals(dc, handle.commands()[0]);
        assertEquals(0, handle.errors());
    }

    //
    // Helpers
    //

    private DeleteHandlePrx queueDelete(DeleteCommand... dc) throws Exception {
        final RV rv = new RV();
        user_delete.queueDelete_async(new AMD_IDelete_queueDelete() {

            public void ice_response(DeleteHandlePrx __ret) {
                rv.rv = __ret;
            }

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }
        }, dc, current("queueDelete"));
        rv.assertPassed();
        assertNotNull(rv.rv);
        return (DeleteHandlePrx) rv.rv;
    }

}
