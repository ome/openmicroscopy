/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ome.model.IObject;
import ome.services.export.ExporterIndex;
import ome.tools.hibernate.ExtendedMetadata;
import ome.util.SqlAction;

import org.hibernate.Session;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the basic state management of {@link ExporterIndex}. This is a subset
 * of the functionality tested in some of the {@link GraphStateUnitTest} methods
 * (among others).
 */
@Test
@SuppressWarnings("deprecation")
public class ExportIndexUnitTest extends MockGraphTest {

    class Step extends GraphStep {

        public Step(ExtendedMetadata em, int idx, List<GraphStep> stack, GraphSpec spec,
                GraphEntry entry, long[] ids) {
            super(em, idx, stack, spec, entry, ids);
        }

        @Override
        public void action(Callback cb, Session session, SqlAction sql, GraphOpts opts)
                throws GraphException {
            // No-op
        }

        @Override
        public void onRelease(Class<IObject> k, Set<Long> ids)
                throws GraphException {
            // no-op
        }

    }

    @BeforeMethod
    public void beforeMethod() {
        prepareGetHibernateClass();
    }

    @Test
    public void testSimpleIndexing() throws Exception {

        ExporterIndex v = new ExporterIndex(1);
        assertEquals(0, v.size());

        final List<GraphStep> stack = Collections.<GraphStep> emptyList();
        final long ids[] = new long[] { 0 };
        final GraphSpec image = spec("/Image");
        final GraphEntry entry = image.entries().get(0);
        final Step s = new Step(null, 0, stack, image, entry, ids);

        v.add(s, ids);
        assertEquals(1, v.size());
        assertEquals(0L, v.getIdByOrder(0));
    }
}
