/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.pixeldata;

import java.sql.Timestamp;
import java.util.concurrent.Callable;

import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.enums.DimensionOrder;
import ome.model.enums.EventType;
import ome.model.enums.PixelsType;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.pixeldata.PersistentEventLogLoader;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "query", "pixeldata" })
public class PersistentEventLogLoaderTest extends AbstractManagedContextTest {

    Executor ex;
    SessionManager sm;
    PersistentEventLogLoader ll;

    @BeforeMethod
    public void setup() {
        ex = (Executor) this.applicationContext.getBean("executor");
        sm = (SessionManager) this.applicationContext.getBean("sessionManager");
        ll = (PersistentEventLogLoader) this.applicationContext
                .getBean("pixelDataEventLogLoader");
    }

    protected <T> T call(String msg, final Callable<T> call) throws Throwable {

        ome.model.meta.Session s = sm.createWithAgent(new Principal("root",
                "system", "Test"), "Test", "127.0.0.1");

        final Throwable exc[] = new Throwable[1];
        @SuppressWarnings("unchecked")
        T result = (T) ex.execute(new Principal(s.getUuid(), "system", "Test"),
                new Executor.SimpleWork(this, msg) {
                    @Transactional(readOnly = false)
                    public Object doWork(Session session, ServiceFactory sf) {
                        try {
                            return call.call();
                        } catch (Throwable e) {
                            exc[0] = e;
                            return null;
                        }
                    }
                });
        if (exc[0] != null) {
            throw exc[0];
        }
        return result;
    }

    public void testNoValueInDatabase() throws Throwable {
        Long l = call("NoValueInDatabase", new Callable<Long>() {
            public Long call() throws Exception {
                ll.deleteCurrentId();
                assertEquals(-1, ll.getCurrentId());
                if (ll.hasNext()) {
                    EventLog log = ll.next();
                    for (EventLog log2 : ll) {
                        if (log2.getId() != null) {
                            continue;
                            // Must load all from a single batch in order to
                            // have the id set.
                        }
                    }
                    Long current = ll.getCurrentId();
                    assertTrue("is " + current, current > 0);
                }
                return null;
            }
        });
    }

    public void testMultipleEvents() throws Throwable {

        loginRoot();
        final EventLog el1 = eventlog();
        final EventLog el2 = eventlog();

        call("MultipleUsers", new Callable<Long>() {
            public Long call() throws Exception {
                ll.setCurrentId(el1.getId() - 1);
                assertEquals(el1.getId() - 1,
                        ll.getCurrentId());
                EventLog t1 = ll.next();
                assertEquals(el1.getId(), t1.getId());
                EventLog t2 = ll.next();
                assertEquals(el2.getId(), t2.getId());
                return null;
            }
        });
    }

    public void testMultipleEventLogs() throws Throwable {
        final Experimenter u1 = loginNewUser();
        final EventContext ec1 = iAdmin.getEventContext();
        final Experimenter u2 = loginNewUserInOtherUsersGroup(u1);
        final EventContext ec2 = iAdmin.getEventContext();

        loginRoot();
        final Event e1 = event(ec1);
        final Event e2 = event(ec2);

        final EventLog el1a = eventlog(e1);
        final EventLog el2a = eventlog(e2);
        final EventLog el2b = eventlog(e2);
        final EventLog el1b = eventlog(e1);

        call("MultipleEventLogs", new Callable<Long>() {
            public Long call() throws Exception {

                ll.setCurrentId(el1a.getId() - 1);
                assertNext(el1a);
                assertNext(el2a);
                // All users used up
                assertNext(el2a); // Duplicate
                assertNext(el1b);
                // All users used up
                assertNext(el2b);
                assertNext(el1b); // Duplicate
                return null;
            }
        });
    }

    /**
     * Extended scenario described in 
     * https://www.openmicroscopy.org/community/viewtopic.php?f=4&t=7593&p=14636#p14638
     * after it was noticed that some events were being skipped.
     */
    public void testMoreMultipleEventLogs() throws Throwable {
        final Experimenter u1 = loginNewUser();
        final EventContext ec1 = iAdmin.getEventContext();
        loginNewUserInOtherUsersGroup(u1);
        final EventContext ec2 = iAdmin.getEventContext();
        loginNewUserInOtherUsersGroup(u1);
        final EventContext ec3 = iAdmin.getEventContext();

        loginRoot();
        final Event e1 = event(ec1);
        final Event e2 = event(ec2);
        final Event e3 = event(ec3);

        final EventLog el1a = eventlog(e1);
        final EventLog el1b = eventlog(e1);
        final EventLog el1c = eventlog(e1);
        final EventLog el1d = eventlog(e1);
        final EventLog el1e = eventlog(e1);
        final EventLog el1f = eventlog(e1);
        final EventLog el1g = eventlog(e1);
        final EventLog el1h = eventlog(e1);
        final EventLog el1i = eventlog(e1);
        final EventLog el1j = eventlog(e1);
        final EventLog el2a = eventlog(e2);
        final EventLog el3a = eventlog(e3);

        call("MultipleEventLogs", new Callable<Long>() {
            public Long call() throws Exception {

                ll.setCurrentId(el1a.getId() - 1);
                assertNext(el1a);
                assertNext(el2a);
                assertNext(el3a);
                assertNext(el1b);
                assertNext(el2a); // Duplicate
                assertNext(el3a); // Duplicate
                assertNext(el1c);
                assertNext(el2a); // Duplicate
                assertNext(el3a); // Duplicate
                assertNext(el1d);
                assertNext(el2a); // Duplicate
                assertNext(el3a); // Duplicate
                assertNext(el1e);
                assertNext(el2a); // Duplicate
                assertNext(el3a); // Duplicate
                assertNext(el1f);
                assertNext(el2a); // Duplicate
                assertNext(el3a); // Duplicate
                assertNext(el1g);
                assertNext(el2a); // Duplicate
                assertNext(el3a); // Duplicate
                assertNext(el1h);
                assertNext(el2a); // Duplicate
                assertNext(el3a); // Duplicate
                assertNext(el1i);
                assertNext(el2a); // Duplicate
                assertNext(el3a); // Duplicate
                assertNext(el1j);
                assertNext(el2a); // Duplicate
                assertNext(el3a); // Duplicate
                return null;
            }
        });
    }
    
    //
    // Helpers
    //

    protected void assertNext(EventLog el) {
        EventLog test = ll.next();
        assertEquals(el.getId(), test.getId());
    }

    protected EventLog eventlog() throws Exception {
        return eventlog(0l, 0l, null); // use root
    }

    protected EventLog eventlog(Event event) {
        return eventlog(event.getExperimenter().getId(),
                event.getExperimenterGroup().getId(),
                event.getId());
    }

    protected EventLog eventlog(Long user, Long group, Long event) {

        Event e;
        if (event == null) {
            e = event(user, group);
        } else {
            e = iQuery.findByQuery("select e from Event e " +
			"left outer join fetch e.logs where e.id = " + event, null);
        }

        EventLog el = new EventLog();
        el.setAction("PIXELDATA");
        el.setEntityId(pixels().getId());
        el.setEntityType(Pixels.class.getName());
        el.setEvent(e);
        e.addEventLog(el);

        EventContext ec = iAdmin.getEventContext();
        loginRootKeepGroup();
        try {
            return iUpdate.saveAndReturnObject(el);
        } finally {
            login(ec);
        }

    }

    protected Event event(EventContext ec) {
        return event(ec.getCurrentUserId(), ec.getCurrentGroupId());
    }

    protected Event event(Long user, Long group) {
        Event e;
        e = new Event();
        e.setExperimenter(new Experimenter(user, false));
        e.setExperimenterGroup(new ExperimenterGroup(group, false));
        e.setStatus("Test");
        e.setType(new EventType("Test"));
        e.setTime(new Timestamp(System.currentTimeMillis()));
        e.setSession(new ome.model.meta.Session(0l, false));
        e = iUpdate.saveAndReturnObject(e);
        return e;
    }

    protected Pixels pixels() {
        Image image = new Image();
        image.setName("pixeldatatest");

        Pixels pixels = new Pixels();
        pixels.setSizeX(1);
        pixels.setSizeY(1);
        pixels.setSizeZ(1);
        pixels.setSizeT(1);
        pixels.setSizeC(1);
        pixels.setSha1("UNKNOWN");
        pixels.setPixelsType(new PixelsType("int8"));
        pixels.setDimensionOrder(new DimensionOrder("XYZCT"));
        image.addPixels(pixels);
        image = iUpdate.saveAndReturnObject(image);
        pixels = image.getPrimaryPixels();
        return pixels;
    }

}
