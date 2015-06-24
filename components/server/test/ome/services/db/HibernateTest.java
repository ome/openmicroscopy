/*
 *   Copyright (C) 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.db;

import java.sql.Timestamp;
import java.util.UUID;

import junit.framework.TestCase;
import ome.model.IObject;
import ome.model.annotations.CommentAnnotation;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "integration" )
public class HibernateTest extends TestCase {

    private static Logger log = LoggerFactory.getLogger(HibernateTest.class);

    boolean ok;
    Session s;
    Transaction tx;
    Event ev;
    Experimenter root;
    ExperimenterGroup sys;

    @BeforeMethod
    public void setupSession() {
        ok = false;
        s = HibernateUtil.getSession();
        tx = s.beginTransaction();
    }

    @AfterMethod
    public void closeSession() {
        tx.commit();
        s.close();
    }

    @Test
    public void createEvent() throws Exception {
        root = (Experimenter) s.load(Experimenter.class, 0L);
        sys = (ExperimenterGroup) s.load(ExperimenterGroup.class, 0L);
        EventType t = (EventType) s.load(EventType.class, 1L);
        ev = new Event();
        ev.setExperimenter(root);
        ev.setExperimenterGroup(sys);
        ev.setTime(new Timestamp(System.currentTimeMillis()));
        ev.setType(t);
        ev.setStatus("manual");
        ev = (Event) s.merge(ev);
        s.flush();
    }

    @Test
    public void testDefaultExperimenter() throws Exception {
        createEvent();

        Experimenter e = new Experimenter();
        e.setOmeName(UUID.randomUUID().toString());
        e.setFirstName("Model");
        e.setLastName("Test");
        e.setLdap(false);
        e = (Experimenter) s.merge(e);
        ExperimenterGroup g = (ExperimenterGroup) s.get(
                ExperimenterGroup.class, 0L);
        GroupExperimenterMap m = e.linkExperimenterGroup(g);
        setDetails(m);
        s.save(m);
        s.flush();
    }

    @Test
    public void testMakeLotsOfStuff() throws Exception {
        createEvent();

        EventType newType = new EventType(UUID.randomUUID().toString());
        setDetails(newType);
        newType = (EventType) s.merge(newType);

        s.flush();
    }

    @Test
    public void testFirstAnnotationTests() throws Exception {
        CommentAnnotation ann = new CommentAnnotation();
        ann.setNs("ENVPROP");
        ann.setTextValue("value");

        createEvent();
        setDetails(ann);
        ann = (CommentAnnotation) s.merge(ann);
        s.flush();

        setDetails(root.linkAnnotation(ann));
        s.flush();

        Query q = s
                .createQuery("select e from Experimenter e join fetch e.annotationLinks");
        q.setMaxResults(1);
        Experimenter test = (Experimenter) q.uniqueResult();
        assertTrue(test.sizeOfAnnotationLinks() >= 1);
    }

    @Test
    public void testAnnotatingAnnotations() throws Exception {
        CommentAnnotation tag = new CommentAnnotation();
        tag.setNs("tag");
        tag.setTextValue("value");
        CommentAnnotation group = new CommentAnnotation();
        group.setNs("taggroup");
        tag.setTextValue("value");

        createEvent();
        setDetails(tag);
        setDetails(group);
        setDetails(tag.linkAnnotation(group));
        tag = (CommentAnnotation) s.merge(tag);

    }

    @Test
    public void testExperimenterIndexQuery() throws Exception {

        Query q = s
                .createQuery("select g from ExperimenterGroup g, Experimenter e join e.groupExperimenterMap m "
                        + "where e.id = 0 and g.name != user and m.parent = g.id "
                        + "and m.child = e.id and index(m) = 0");
        ExperimenterGroup test = (ExperimenterGroup) q.uniqueResult();
        assertNotNull(test);
    }

    // ==============================================================

    private void setDetails(IObject o) throws Exception {
        Details details = o.getDetails();
        details.setCreationEvent(ev);
        details.setOwner(root);
        details.setGroup(sys);
        details.setUpdateEvent(ev);
    }
}
