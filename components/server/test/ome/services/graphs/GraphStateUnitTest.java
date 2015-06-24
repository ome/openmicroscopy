/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;
import ome.server.utests.DummyExecutor;
import ome.services.delete.DeleteStepFactory;
import ome.services.export.ExporterStepFactory;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.tools.hibernate.ExtendedMetadata;
import ome.util.SqlAction;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test the tree structure parsed on GraphState creation. Initially related to
 * the issues found when ticket:3031 was re-opened. Also contains additional tests
 * of the graph state methods which allow for looking up particular
 * objections from the graph.
 *
 * @see ticket:3628
 */
@Test(groups = "broken")
@SuppressWarnings("deprecation")
public class GraphStateUnitTest extends MockGraphTest {

    private Mock sessionMock;

    private Mock queryMock;

    private Mock emMock;

    private Mock sqlMock;

    private SqlAction sql;

    private ExtendedMetadata em;

    private Session session;

    private Query query;

    private List<List<Long>> table;

    ExperimenterGroup group;
    
    @BeforeMethod
    public void makeMocks() {
        group = new ExperimenterGroup();
        group.getDetails().setPermissions(Permissions.READ_ONLY);
        emMock = mock(ExtendedMetadata.class);
        sessionMock = mock(CombinedSession.class);
        sqlMock = mock(SqlAction.class);
        session = (Session) sessionMock.proxy();
        queryMock = mock(Query.class);
        query = (Query) queryMock.proxy();
        em = (ExtendedMetadata) emMock.proxy();
        sql = (SqlAction) sqlMock.proxy();
        sqlMock.expects(atLeastOnce()).method("groupInfoFor").will(
                returnValue(group));
    }

    @Test
    public void testOneSimpleEntry() throws Exception {

        BaseGraphSpec spec = new BaseGraphSpec("/Test", "/Test") {
            @Override
            public Class<IObject> getHibernateClass(String table) {
                return IObject.class;
            }
        };

        table = Arrays.asList(Arrays.asList(1L));
        prepareQueryBackupIds(table);
        prepareLoadQueryInfluencers();

        // null ctx is okay until .release()
        GraphState state = new GraphState(createEventContext(false),
            new DeleteStepFactory(specXml, em), sql, session, spec);
        assertEquals(1, state.getTotalFoundCount());
    }

    @Test
    public void testTwoSimpleEntries() throws Exception {
        BaseGraphSpec spec = new BaseGraphSpec("/Test", "/Test", "/Foo") {
            @Override
            public Class<IObject> getHibernateClass(String table) {
                return IObject.class;
            }
        };
        prepareLoadQueryInfluencers();

        table = Arrays.asList(Arrays.asList(1L));
        prepareQueryBackupIds(table);

        table = Arrays.asList(Arrays.asList(2L));
        prepareQueryBackupIds(table);

        GraphState state = new GraphState(createEventContext(false),
            new DeleteStepFactory(specXml, em), sql, session, spec);
        assertEquals(2, state.getTotalFoundCount());

    }

    @Test
    public void testSimpleEntryWithMultipleResults() throws Exception {
        BaseGraphSpec spec = new BaseGraphSpec("/Test", "/Test") {
            @Override
            public Class<IObject> getHibernateClass(String table) {
                return IObject.class;
            }
        };

        table = table(new long[] { 1L }, new long[] { 2L });
        prepareQueryBackupIds(table);
        prepareLoadQueryInfluencers();

        GraphState state = new GraphState(createEventContext(false),
            new DeleteStepFactory(specXml, em), sql, session, spec);
        assertEquals(2, state.getTotalFoundCount());

    }

    @Test
    public void testSimpleRoiSubSpec() throws Exception {
        prepareGetRelationship();
        GraphSpec spec = spec("/Roi");

        prepareGetHibernateClass();

        Queries q = new Queries();
        GraphQuery boolAnn = q.roiAnnQueries.get("BooleanAnnotation");
        q.roiWithAnnotation(0, 2, 3, boolAnn);

        prepareTableLookups(q);
        prepareLoadQueryInfluencers();

        GraphState state = new GraphState(createEventContext(false), new DeleteStepFactory(specXml, em), sql, session, spec);
        assertEquals(state.toString(), 3, state.getTotalFoundCount());

    }

    @Test
    public void testComplexRoiSubSpec() throws Exception {
        prepareGetRelationship();
        GraphSpec spec = spec("/Roi");

        prepareGetHibernateClass();


        final Queries q = new Queries();
        final GraphQuery boolAnn = q.roiAnnQueries.get("BooleanAnnotation");
        final GraphQuery commAnn = q.roiAnnQueries.get("CommentAnnotation");

        q.roiWithAnnotation(0, 2, 3, boolAnn);
        q.roiShapes.add(0, 1);

        q.roiWithAnnotation(10, 12, 13, commAnn);
        q.roiShapes.add(10, 11);

        prepareTableLookups(q);
        prepareLoadQueryInfluencers();
        GraphState state = new GraphState(createEventContext(false), new DeleteStepFactory(specXml, em), sql, session, spec);
        assertEquals(state.toString(), 8, state.getTotalFoundCount());
        // includes the one parent spec

    }

    @Test
    public void testGraphStateTablesTopLevel() {
        GraphTables t = new GraphTables();
        GraphEntry entry = new GraphEntry(new BaseGraphSpec("foo"), "foo");
        long[][] results = convert(table(new long[] { 0L }, new long[] { 1L }));
        t.add(entry, results);
        Iterator<List<long[]>> it = t.columnSets(entry, null);
        List<long[]> rows;
        int count = 0;
        while (it.hasNext()) {
            rows = it.next();
            assertEquals(1, rows.size());
            long[] cols = rows.get(0);
            assertEquals(1, cols.length);
            assertEquals(count, (int) cols[0]);
            count++;
        }
        assertEquals(2, count); // 2 sets
    }

    @Test
    public void testGraphStateTablesWithMatchLength1() {
        GraphTables t = new GraphTables();
        GraphEntry entry = new GraphEntry(new BaseGraphSpec("foo"), "foo");
        List<List<Long>> results = table(new long[] { 0L }, new long[] { 1L });
        t.add(entry, convert(results));
        Iterator<List<long[]>> it = t.columnSets(entry, new long[]{0L});
        List<long[]> rows;
        int count = 0;
        while (it.hasNext()) {
            rows = it.next();
            assertEquals(count, (int) rows.get(0)[0]);
            count++;
        }
        assertEquals(2, count); // 2 sets when length is 1
    }

    @Test
    public void testGraphStateTablesWithMatchLength3() {
        GraphTables t = new GraphTables();
        GraphEntry entry = new GraphEntry(new BaseGraphSpec("foo"), "foo");
        List<List<Long>> results = table(new long[] { 0L, 2L, 2L }, new long[] {
                0L, 2L, 3L }, new long[] { 0L, 2L, 4L }, new long[] { 1L, 3L,
                5L }, new long[] { 1L, 3L, 6L });
        t.add(entry, convert(results));
        Iterator<List<long[]>> it = t.columnSets(entry, new long[]{0L, 2L, 2L});
        List<long[]> rows;
        int count = 0;
        while (it.hasNext()) {
            count++;
            rows = it.next();
            for (long[] cols : rows) {
                assertEquals(0L, cols[0]);
                assertEquals(2L, cols[1]);
            }
        }
        assertEquals(1, count);
    }

    @Test
    public void testStackSizeForProject() throws Exception {
        prepareGetRelationship();
        GraphSpec spec = spec("/Project");

        prepareGetHibernateClass();

        Queries q = new Queries();
        q.projects.add(0);
        q.projectDatasetLinks.add(0, 1);
        q.projectDatasets.add(0, 1, 2);
        q.pdImageLinks.none();

        prepareTableLookups(q);
        prepareLoadQueryInfluencers();

        GraphState state = new GraphState(createEventContext(false), new DeleteStepFactory(specXml, em), sql, session, spec);
        assertEquals(state.toString(), 4, state.getTotalFoundCount());
        // 4 for the parent spec

    }


    @Test(groups = "ticket:3163")
    public void testSlowGraphStateTablesAdd() {
        GraphTables dst = new GraphTables();
        List<List<Long>> data = new ArrayList<List<Long>>();
        for (int a = 0; a < 100; a++) {
            for (int b = 0; b < 100; b++) {
                for (int c = 0; c < 100; c++) {
                    List<Long> l = new ArrayList<Long>();
                    l.add((long)a);
                    l.add((long)b);
                    l.add((long)c);
                    data.add(l);
                }
            }
        }
        Collections.shuffle(data);

        GraphSpec spec = new BaseGraphSpec("foo", "foo");
        GraphEntry entry = spec.entries().get(0);
        dst.add(entry, convert(data));

        Long ignored = -1L;
        Iterator<List<long[]>> it = dst.columnSets(entry, new long[]{5L, 10L, ignored});
        int count = 0;
        while (it.hasNext()) {
            List<long[]> l = it.next();
            for (long[] i : l) {
                assertEquals(5L, i[0]);
                assertEquals(10L, i[1]);
                count++;
            }
        }
        assertEquals(100, count);
    }

    @Test(groups = "ticket:3163")
    public void testRefactoredGraphStateCorrectness() throws GraphException {

        GraphTables dst = new GraphTables();
        long[][] data = new long[][] {
                new long[]{0,0,0,0},
                new long[]{1,0,0,1},
                new long[]{0,0,0,1},
                new long[]{1,0,0,2},
                new long[]{3,3,3,3},
                new long[]{0,1,0,0},
                new long[]{0,1,1,1},
                new long[]{0,1,0,2}
        };

        GraphSpec spec = new BaseGraphSpec("foo", "foo");
        GraphEntry entry = spec.entries().get(0);
        dst.add(entry, data);

        Iterator<List<long[]>> it;

        // First check that all the values come back out
        it = dst.columnSets(entry, null);
        assertSize(data.length, it);

        // Now check that exact sets are returned
        it = dst.columnSets(entry, new long[]{0,0,0,0});
        assertColumnSets(it, //
                new long[][] {
                    new long[]{0,0,0,0},
                    new long[]{0,0,0,1}
        });

        // Check that higher level sets can be returned
        final int ignored = -1;
        it = dst.columnSets(entry, new long[]{0, ignored});
        assertColumnSets(it, //
                new long[][] {
                    new long[]{0,0,0,0},
                    new long[]{0,0,0,1}},
                new long[][] {
                    new long[]{0,1,0,0},
                    new long[]{0,1,0,2}},
                new long[][] {
                    new long[]{0,1,1,1}
        });

    }

    private void assertSize(int size, Iterator<List<long[]>> it) {
        int count;
        count = 0;
        while (it.hasNext()) {
            count += it.next().size();
        }
        assertEquals(size, count);
    }

    /**
     * Checks that each of the given arrays is returned by the iterator
     * and nothing else.
     */
    private void assertColumnSets(Iterator<List<long[]>> it, long[][]...arrays) {
        final boolean[][] found = new boolean[arrays.length][];
        while (it.hasNext()) {
            Integer set = null;
            List<long[]> l = it.next();
            for (long[] test : l) {
                boolean foundSingle = false;
                for (int s = 0; s < arrays.length; s++) {
                    long[][] rows = arrays[s];
                    NEXTROW: for (int r = 0; r < rows.length; r++) {
                        long[] cols = rows[r];
                        for (int c = 0; c < cols.length; c++) {
                            if (arrays[s][r][c] != test[c]) {
                                continue NEXTROW;
                            }
                        }
                        // If we reach this point then the two match
                        foundSingle = true;
                        if (set == null) {
                            // Then we haven't found anything for this set yet
                            set = s;
                            if (found[s] != null) {
                                fail(String.format("Something's gone wrong." +
						" Already have set at (s,r)=(%s,%s)",
						s, r));
                            } else {
                                found[s] = new boolean[rows.length];
                                found[s][r] = true;
                            }
                        } else {
                            assertFalse("Already set to true! " +
                                    Arrays.toString(test), found[set][r]);
                            found[set][r] = true;
                        }
                    }
                }
                assertTrue("Couldn't find " + Arrays.toString(test),
                        foundSingle);
            }
        }

        // The previous blocked guaranteed that we found a UNIQUE place in
        // "found[][]" for every long[] "test" that was returned from the
        // iterator, now we check that a long[] was found for every location
        // of "found[][]" that was given
        StringBuilder sb = new StringBuilder();
        for (int s = 0; s < found.length; s++) {
            boolean[] rows = found[s];
            if (rows == null) {
                sb.append("\nSet ");
                sb.append(s);
                sb.append(" was not found");
            } else {
                for (int r = 0; r < rows.length; r++) {
                    if (!rows[r]) {
                        sb.append("\nRow ");
                        sb.append(r);
                        sb.append(" of set ");
                        sb.append(s);
                        sb.append(" was not found.");
                    }
                }
            }
        }
        if (sb.length() > 0) {
            sb.append("\nSearched for: " + Arrays.deepToString(arrays));
            sb.append("\nFound: " + Arrays.deepToString(found));
            fail(sb.toString());
        }

    }

    private void prepareSavepoints() {
        Mock statementMock = mock(CallableStatement.class);
        CallableStatement statement = (CallableStatement) statementMock.proxy();
        statementMock.expects(atLeastOnce()).method("execute").will(returnValue(true));

        Mock connectionMock = mock(Connection.class);
        Connection connection = (Connection) connectionMock.proxy();
        connectionMock.expects(atLeastOnce()).method("prepareCall").will(
                returnValue(statement));

        sessionMock.expects(atLeastOnce()).method("connection").will(
                returnValue(connection));
    }

    @Test(groups = "ticket:3125")
    public void testSavepointsAreHandledProperly() throws Exception {
        prepareGetHibernateClass();
        prepareGetRelationship();
        GraphSpec spec = spec("/Screen");

        Queries q = new Queries();
        q.screens.add(0);
        q.screenPlateLinks.add(0, 1);
        q.screenPlates.add(0, 1, 2);

        prepareTableLookups(q);
        prepareLoadQueryInfluencers();

        GraphState state = new GraphState(createEventContext(false), new DeleteStepFactory(specXml, em), sql, session, spec);
        assertEquals(state.toString(), 4, state.getTotalFoundCount());
    }

    @Test(groups = {"ticket:3125", "ticket:3130"})
    public void testSavepointsAreHandledProperly2() throws Exception {
        prepareGetHibernateClass();
        prepareGetRelationship();
        GraphSpec spec = spec("/Screen");

        Queries q = new Queries();
        q.screens.add(0);
        q.screenPlateLinks.add(0, 1);
        q.screenPlates.add(0, 1, 2);

        prepareTableLookups(q);
        prepareLoadQueryInfluencers();

        GraphState state = new GraphState(createEventContext(false), new DeleteStepFactory(specXml, em), sql, session, spec);
        assertEquals(state.toString(), 4, state.getTotalFoundCount());
    }

    //
    // MetadataRetrieve-methods
    //

    public void testGetProject() throws Exception {
        prepareGetRelationship();
        GraphSpec spec = spec("/Project");

        prepareGetHibernateClass();

        Queries q = new Queries();
        q.projects.add(0);
        q.projectDatasetLinks.add(0, 1);
        q.projectDatasets.add(0, 1, 2);

        prepareTableLookups(q);
        prepareLoadQueryInfluencers();

        final AtomicInteger count = new AtomicInteger();
        GraphStepFactory gsf = new GraphStepFactory() {

            public GraphStep create(int idx, List<GraphStep> stack, GraphSpec spec,
                    GraphEntry entry, long[] ids) throws GraphException {
                String[] path = entry.path(null);
                String name = path[path.length-1];
                if ("Project".equals(name)) {
                    count.incrementAndGet();
                }
                return new GraphStep(em, idx, stack, spec, entry, ids) {

                    @Override
                    public void action(Callback cb, Session session,
                            SqlAction sql, GraphOpts opts) throws GraphException {
                        // no-op
                    }

                    @Override
                    public void onRelease(Class<IObject> k, Set<Long> ids)
                            throws GraphException {
                        // no-op
                    }
                };
            }

            public GraphSteps postProcess(List<GraphStep> steps) {
                return new GraphSteps(steps);
            }
        };

        GraphState state = new GraphState(createEventContext(false), gsf, sql, session, spec);
        assertEquals(state.toString(), 4, state.getTotalFoundCount());
        assertEquals(1, count.get());
    }

    public void testGetProjectOnExport() throws Exception {
        prepareGetRelationship();
        GraphSpec spec = spec("/Project");

        prepareGetHibernateClass();

        Queries q = new Queries();
        q.projects.add(0);
        q.projectDatasetLinks.add(0, 1);
        q.projectDatasets.add(0, 1, 2);

        prepareTableLookups(q);
        prepareLoadQueryInfluencers();

        ExporterStepFactory factory = new ExporterStepFactory(null, null, null);
        GraphState state = new GraphState(createEventContext(false), factory, sql, session, spec);
        assertEquals(state.toString(), 4, state.getTotalFoundCount());
        assertEquals(1, factory.getCount("Project"));
    }

    public void testGetAnnotationRefOnExport() throws Exception {
        prepareGetRelationship();
        GraphSpec spec = spec("/Image");

        prepareGetHibernateClass();

        Queries q = new Queries();
        GraphQuery boolAnn = q.imgAnnQueries.get("BooleanAnnotation");
        q.images.add(0);
        q.imageAnnotationLinks.add(0, 1);
        q.imageAnnotations.add(0, 1, 2);
        boolAnn.add(0, 1, 2);

        prepareTableLookups(q);
        prepareLoadQueryInfluencers();

        Executor ex = new DummyExecutor(session, null);
        Principal p = new Principal("foo");

        ExporterStepFactory factory = new ExporterStepFactory(ex, p, em);
        GraphState state = new GraphState(createEventContext(false), factory, sql, session, spec);
        assertEquals(state.toString(), 3, state.getTotalFoundCount());

        // test the counts
        assertEquals(state.toString(), 1, factory.getCount("Image"));
        assertEquals(state.toString(), 1, factory.getCount("ImageAnnotationLink"));
        // Not found! FIXME: unclear why the getCount / getTotalFoundCount values have changed.
        // assertEquals(state.toString(), 1, factory.getCount("BooleanAnnotation"));
        // UNSUPPORTED assertEquals(1, factory.getCount("ImageAnnotationRef"));

        // test the actual ids.
        sessionMock.expects(once()).method("get").with(eq("Image"), eq(0L));
        factory.getObject("Image", 0);

        sessionMock.expects(once()).method("get").with(eq("ImageAnnotationLink"), eq(1L));
        factory.getObject("ImageAnnotationLink", 0);

        // See FIXME above.
        // sessionMock.expects(once()).method("get").with(eq("BooleanAnnotation"), eq(2L));
        // factory.getObject("BooleanAnnotation", 0);


    }

    public void testGetChannelOnExport() throws Exception {
        prepareGetRelationship();
        GraphSpec spec = spec("/Image");

        prepareGetHibernateClass();

        Queries q = new Queries();
        q.images.add(0);
        q.pixels.add(0, 1);
        q.imageChannels.add(0, 1, 0);
        q.imageChannels.add(0, 1, 1);
        q.imageChannels.add(0, 1, 2);

        prepareGetRelationship();
        prepareTableLookups(q);
        prepareLoadQueryInfluencers();

        ExporterStepFactory factory = new ExporterStepFactory(null, null, null);
        GraphState state = new GraphState(createEventContext(false), factory, sql, session, spec);
        assertEquals(state.toString(), 7, state.getTotalFoundCount());
        assertEquals(1, factory.getCount("Image"));
        assertEquals(1, factory.getCount("Pixels"));
        assertEquals(3, factory.getCount("Channel"));
    }

    //
    // Helpers
    //

    List<List<Long>> table(long[]... rows) {
        List<List<Long>> rv = new ArrayList<List<Long>>();
        for (long[] l : rows) {
            List<Long> a = new ArrayList<Long>();
            for (int i = 0; i < l.length; i++) {
                a.add(l[i]);
            }
            rv.add(a);
        }
        return rv;
    }

    /**
     * Allows transforming from lists of lists to arrays of arrays.
     */
    long[][] convert(List<List<Long>> table) {
        long[][] arr = new long[table.size()][];
        for (int i = 0; i < arr.length; i++) {
            List<Long> l = table.get(i);
            long[] a = new long[l.size()];
            for (int j = 0; j < a.length; j++) {
                a[j] = l.get(j);
            }
            arr[i] = a;
        }
        return arr;
    }

    private void prepareTableLookups(final Queries qs) {
        sessionMock.setDefaultStub(new Stub() {

            public StringBuffer describeTo(StringBuffer arg0) {
                arg0.append("handles all lookups");
                return arg0;
            }

            public Object invoke(Invocation arg0) throws Throwable {
                if (arg0.invokedMethod.getName().equals("createQuery")) {
                    String str = (String) arg0.parameterValues.get(0);
                    GraphQuery q = qs.get(str);
                    if (q == null) {
                        fail("Unknown query: <" + str + ">");
                    }
                    Mock m = new Mock(Query.class);
                    Query query = (Query) m.proxy();
                    m.expects(once()).method("setParameter");
                    m.expects(once()).method("list").will(returnValue(q.table()));
                    return query;
                } else {
                    fail("Unknown: " + arg0.invokedMethod);
                }
                return null;
            }
        });

    }

    private void prepareQueryBackupIds(List<List<Long>> table) {
        sessionMock.expects(once()).method("createQuery")
                .will(returnValue(query));
        queryMock.expects(once()).method("setParameter");
        queryMock.expects(once()).method("list").will(returnValue(table));
    }


    private void prepareLoadQueryInfluencers() {
        prepareLoadQueryInfluencers(sessionMock);
    }
}
