/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.query;

import java.util.List;

import ome.model.annotations.FileAnnotation;
import ome.model.annotations.LongAnnotation;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;

import org.testng.annotations.Test;

public class ProjectionTest extends AbstractManagedContextTest {

    @Test
    public void testPrivateData() {
        loginNewUser(Permissions.PRIVATE);
        String uuid = uuid();
        Image i = new_Image(uuid);
        iUpdate.saveObject(i);

        loginNewUser(Permissions.PRIVATE);
        List<Object[]> rv = iQuery.projection(" select count(i.name), i.name "
                + "from Image i where i.name = :uuid group by i.name", new Parameters()
                .addString("uuid", uuid));
        assertEquals(0, rv.size());
    }

    @Test
    public void testCount() {
        iQuery.projection("select count(e.omeName) "
                + "from Experimenter e group by e.omeName", null);
    }

    @Test
    public void testSumMap() {
        loginNewUser();
        List<Object[]> rv = iQuery.projection("select "
                + "p.pixelsType.value, "
                + "sum(p.sizeX * p.sizeY * p.sizeZ * p.sizeT * p.sizeC) "
                + "from Pixels p group by p.pixelsType.value", null);
        assertEquals(0, rv.size());
    }

    @Test
    public void testProjection() {
        loginNewUser();
        List<Object[]> rv = iQuery.projection("select e.entityType, "
                + "min(e.entityId), " + "max(e.entityId), "
                + "count(e.entityId), " + "sum(e.entityId), "
                + "sum(e.entityId)/count(e.entityId) "
                + "from EventLog e where e.action = 'DELETE' "
                + "group by e.entityType", null);
        assertTrue(0 <= rv.size());
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testOnlyOneValueFails() {
        loginNewUser();
        List<Object[]> rv = iQuery.projection("select e from Experimenter e",
                null);
        assertTrue(rv.get(0)[0] instanceof Experimenter);
    }

    @Test
    public void testIObjectUsage() {
        loginNewUser();
        List<Object[]> rv = iQuery.projection("select e.id, " +
			"e from Experimenter e",
                null);
        assertTrue(rv.get(0)[1] instanceof Experimenter);
    }

    @Test
    public void testCollectionUsage() {
        loginNewUser();
        List<Object[]> rv = iQuery.projection(
                "select e.omeName, size(e.groupExperimenterMap) "
                        + "from Experimenter e group by e.omeName", null);
        for (Object[] objects : rv) {
            assertTrue(((Integer) objects[1]).intValue() > 0);
        }
    }

    @Test
    public void testAnnotationCounts() {
        loginNewUser();
        String uuid = uuid();
        Image i = new_Image(uuid);
        FileAnnotation fa = new FileAnnotation();
        fa.setNs("OMIT");
        LongAnnotation la = new LongAnnotation();
        la.setNs("");
        i.linkAnnotation(fa);
        i.linkAnnotation(la);
        i = iUpdate.saveAndReturnObject(i);

        List<Object[]> rv = iQuery.projection(
                "select i.id, count(a) from Image i " +
                "left outer join i.annotationLinks l " +
                "left outer join l.child a where a.ns <> 'OMIT'" +
                "group by i.id", null);
        Object[] values = rv.get(0);
        assertEquals(i.getId(), values[0]);
        assertEquals((long) 1, values[1]);
    }
}
