/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import ome.conditions.InternalException;
import ome.model.meta.Experimenter;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

@Test(groups = { "security" })
public class DisablingTest extends AbstractManagedContextTest {

    @Test
    public void testSimpleDisabling() throws Exception {
        loginRoot();
        loadSucceeds();
        securitySystem.disable("load");
        loadFails();
        securitySystem.enable();
        loadSucceeds();
    }

    /**
     * As of the changes for IShare, this no longer holds true. Only logging out
     * or invalidating the event context will reset the disabled flag.
     */
    @Test
    public void testDoesntGetsReset() throws Exception {
        loginRoot();
        securitySystem.disable("load");
        loadFails(); // this implicitly resets
        assertTrue(securitySystem.isDisabled("load"));
    }

    // ~ Helpers
    // =========================================================================

    private void loadSucceeds() {
        iQuery.get(Experimenter.class, 0L);
    }

    private void loadFails() {
        try {
            loadSucceeds();
        } catch (InternalException ie) {
            // good.
        }
    }

}