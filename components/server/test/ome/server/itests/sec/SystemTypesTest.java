/*
 *   $Id$
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import ome.api.ITypes;
import ome.conditions.SecurityViolation;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.EventType;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = { "ticket:156", "ticket:157", "security", "filter" })
public class SystemTypesTest extends AbstractManagedContextTest {

    static String ticket156 = "ticket:156";

    Experimenter e = new Experimenter();

    @BeforeClass
    public void createData() throws Exception {
        setUp();

        loginRoot();

        String gname = uuid();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(gname);
        g.setLdap(false);
        iAdmin.createGroup(g);

        e = new Experimenter();
        e.setOmeName(UUID.randomUUID().toString());
        e.setFirstName(ticket156);
        e.setLastName(ticket156);
        e.setLdap(false);
        e = factory.getAdminService().getExperimenter(
                factory.getAdminService().createUser(e, gname));

        tearDown();
    }

    // ~ Admin types
    // =========================================================================

    @Test(expectedExceptions = SecurityViolation.class)
    public void testCannotCreateExperimenter() throws Exception {

        loginUser(e.getOmeName());

        Experimenter test = new Experimenter();
        test.setOmeName(UUID.randomUUID().toString());
        test.setFirstName(ticket156);
        test.setLastName(ticket156);
        test.setLdap(false);
        factory.getUpdateService().saveObject(test);
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testCannotCreateGroup() throws Exception {

        loginUser(e.getOmeName());

        ExperimenterGroup test = new ExperimenterGroup();
        test.setName(UUID.randomUUID().toString());
        test.setLdap(false);
        factory.getUpdateService().saveObject(test);
    }

    // ~ Events
    // =========================================================================

    @Test(expectedExceptions = SecurityViolation.class)
    public void testCannotCreateEvents() throws Exception {

        loginUser(e.getOmeName());

        String sid = iAdmin.getEventContext().getCurrentSessionUuid();
        Session sess = iSession.getSession(sid);

        Event test = new Event();
        test.setTime(new Timestamp(System.currentTimeMillis()));
        test.setStatus("hi");
        test.setType(new EventType(0L, false));
        test.setExperimenter(new Experimenter(0L, false));
        test.setExperimenterGroup(new ExperimenterGroup(0L, false));
        test.setSession(sess);
        factory.getUpdateService().saveObject(test);
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testCannotCreateEventLogs() throws Exception {

        loginUser(e.getOmeName());

        EventLog test = new EventLog();
        test.setAction("BOINK");
        test.setEvent(new Event(0L, false));
        test.setEntityId(1L);
        test.setEntityType("ome.model.Something");
        factory.getUpdateService().saveObject(test);
    }

    // ~ Enums
    // =========================================================================

    @Test(expectedExceptions = SecurityViolation.class)
    public void testCannotCreateEnumsWithIUpdate() throws Exception {

        loginUser(e.getOmeName());

        AcquisitionMode test = new AcquisitionMode();
        test.setValue("ticket:157");
        factory.getUpdateService().saveObject(test);
    }

    @Test
    public void testRootCanCreateEnumsWithIUpdate() throws Exception {

        loginRoot();

        AcquisitionMode test = new AcquisitionMode();
        test.setValue("ticket:157/" + uuid());
        factory.getUpdateService().saveObject(test);
    }

    @Test
    public void testCanCreateEnumsWithITypes() throws Exception {

        loginUser(e.getOmeName());

        AcquisitionMode test = new AcquisitionMode();
        test.setValue("ticket:157/" + uuid());
        factory.getTypesService().createEnumeration(test);
    }

    @Test
    public void testEnumsWithITypes() throws Exception {

        loginRoot();
        ITypes it = factory.getTypesService();

        AcquisitionMode testc = new AcquisitionMode();
        testc.setValue("ticket:446-840");
        it.createEnumeration(testc);

        AcquisitionMode testu = it.getEnumeration(AcquisitionMode.class,
                "ticket:446-840");
        testu.setValue("ticket:446-840/test");
        factory.getTypesService().updateEnumeration(testu);

        AcquisitionMode testd = iQuery.findByString(AcquisitionMode.class,
                "value", "ticket:446-840/test");
        factory.getTypesService().deleteEnumeration(testd);

    }

    @Test
    public void testEnumsWithITypes2() throws Exception {

        loginRoot();
        ITypes it = factory.getTypesService();

        String uid = uuid();
        List<AcquisitionMode> list = new ArrayList<AcquisitionMode>();
        for (int i = 0; i < 4; i++) {
            AcquisitionMode testc = new AcquisitionMode();
            testc.setValue("ticket:840/" + uid);
            AcquisitionMode val = it.createEnumeration(testc);
            list.add(val);
        }

        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            ((AcquisitionMode) iter.next()).setValue("ticket:840-1/" + uid);
        }

        factory.getTypesService().updateEnumerations(list);

    }

    @Test
    public void testOryginalAllEnumerationTypes() {
        loginRoot();
        ITypes it = factory.getTypesService();
        it.getEnumerationTypes();
        // it.allOryginalEnumerations(ome.model.enums.EventType.class);
        it.getEnumerationsWithEntries();
    }

}