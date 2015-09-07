/*
 * ome.server.itests.ImmutabilityTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests;

import org.testng.annotations.Test;

import ome.model.core.Image;
import ome.model.meta.Event;
import ome.parameters.Filter;
import ome.parameters.Parameters;

/**
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @since 1.0
 */
public class ImmutabilityTest extends AbstractManagedContextTest {

    @Test
    public void testCreationEventWillBeSilentlyUnchanged() throws Exception {

        loginRoot();

        Image i = new_Image("immutable creation");
        i = iUpdate.saveAndReturnObject(i);

        Event oldEvent = i.getDetails().getCreationEvent();
        Event newEvent = iQuery.findByQuery(
                "select e from Event e where id != :id", new Parameters(
                        new Filter().page(0, 1)).addId(oldEvent.getId()));

        i.getDetails().setCreationEvent(newEvent);

        // This fails because it gets silently copied to our new instance. See:
        // http://trac.openmicroscopy.org.uk/ome/ticket/346
        // i = iUpdate.saveAndReturnObject(i);
        // assertEquals( i.getDetails().getCreationEvent().getId(),
        // oldEvent.getId());

        // Saving and reacquiring to be sure.
        iUpdate.saveObject(i);
        // unfortunately still not working properly i = iQuery.refresh(i);
        i = iQuery.get(i.getClass(), i.getId());
        assertEquals(i.getDetails().getCreationEvent().getId(), oldEvent
                .getId());

    }

}
