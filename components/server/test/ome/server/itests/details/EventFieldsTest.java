/*
 * ome.server.itests.details.EventFieldsTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.details;

// Java imports
import java.sql.Timestamp;

import ome.model.core.Image;
import ome.model.enums.EventType;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

/**
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
@Test(groups = "ticket:82")
public class EventFieldsTest extends AbstractManagedContextTest {

    public void testContainersWork() throws Exception {

        loginRoot();

        Event e1 = makeEvent();
        e1 = iUpdate.saveAndReturnObject(e1);

        Event e2 = makeEvent();
        e2.setContainingEvent(e1);
        e2 = iUpdate.saveAndReturnObject(e2);

        assertTrue(e2.getContainingEvent().getId().equals(e1.getId()));

    }

    public void testExperimenterAndGroupFilled() throws Exception {

        Image dummy = newImage();
        dummy.setName("dummy");
        dummy = iUpdate.saveAndReturnObject(dummy);

        assertNotNull(dummy.getDetails());
        assertNotNull(dummy.getDetails().getCreationEvent());
        assertNotNull(dummy.getDetails().getCreationEvent().getExperimenter());
        assertNotNull(dummy.getDetails().getCreationEvent()
                .getExperimenterGroup());
        assertNotNull(dummy.getDetails().getUpdateEvent());
        assertNotNull(dummy.getDetails().getUpdateEvent().getExperimenter());
        assertNotNull(dummy.getDetails().getUpdateEvent()
                .getExperimenterGroup());

    }

    private Image newImage() {
        Image i = new Image();
        return i;
    }

    // ~ Helpers
    // =========================================================================

    private Event makeEvent() {
        EventType t = new EventType();
        t.setValue("test");
        Event e = new Event();
        e.setType(t);
        e.setTime(new Timestamp(System.currentTimeMillis()));
        e.setExperimenter(new Experimenter(0L, false));
        e.setExperimenterGroup(new ExperimenterGroup(0L, false));
        e.setSession(new Session(1L, false));
        return e;
    }

}
