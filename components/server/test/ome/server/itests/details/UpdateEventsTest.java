/*
 * ome.server.itests.details.UpdateEventsTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.details;

import ome.conditions.SecurityViolation;
import ome.model.core.Image;
import ome.model.meta.Event;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

/**
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since 1.0
 */
public class UpdateEventsTest extends AbstractManagedContextTest {

    @Test
    public void testNoOneCanChangeUpdateEvent() throws Exception {

        loginRoot();

        Image i = new_Image();
        i.setName("immutable creation");
        i = iUpdate.saveAndReturnObject(i);

        Event oldEvent = i.getDetails().getUpdateEvent();
        Event newEvent = iQuery.findByQuery(
                "select e from Event e where id != :id", new Parameters(
                        new Filter().page(0, 1)).addId(oldEvent.getId()));

        i.getDetails().setUpdateEvent(newEvent);

        try {
            iUpdate.saveObject(i);
            fail("secvio!");
        } catch (SecurityViolation sv) {
            // ok
        }

    }

    @Test
    public void testInterceptorRevertsChanges() throws Exception {

        loginNewUser();

        Image i = new_Image();
        i.setName("revert changes");
        i = iUpdate.saveAndReturnObject(i);
        Event modification = i.getDetails().getUpdateEvent();

        i.getDetails().setOwner(null);
        i = iUpdate.saveAndReturnObject(i);
        Event test = i.getDetails().getUpdateEvent();

        assertEquals("When interceptor reverts changes, "
                + "there should be no modification", modification.getId(), test
                .getId());
    }

    @Test
    public void testButARealChangeGetsANewModification() throws Exception {

        loginNewUser();

        Image i = new_Image();
        i.setName("new mod");
        i = iUpdate.saveAndReturnObject(i);
        Event modification = i.getDetails().getUpdateEvent();

        i.setName("new mod, take 2");
        i = iUpdate.saveAndReturnObject(i);
        Event test = i.getDetails().getUpdateEvent();

        assertFalse("On a real change, the modification must increment",
                modification.getId().equals(test.getId()));
    }


    private Image new_Image() {
        Image i = new Image();
        return i;
    }
    
}
