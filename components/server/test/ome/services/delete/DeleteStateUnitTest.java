/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ome.model.IObject;
import ome.security.basic.CurrentDetails;
import ome.system.EventContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test the tree structure parsed on DeleteState creation. Initially related to
 * the issues found when ticket:3031 was re-opened.
 */
@Test
public class DeleteStateUnitTest extends MockDeleteTest {

    private Mock sessionMock;

    private Mock queryMock;

    private Session session;

    private Query query;

    private List<List<Long>> table;

    @BeforeMethod
    public void makeMocks() {
        sessionMock = mock(Session.class);
        session = (Session) sessionMock.proxy();
        queryMock = mock(Query.class);
        query = (Query) queryMock.proxy();
    }

    @Test
    public void testOneSimpleEntry() throws Exception {

        BaseDeleteSpec spec = new BaseDeleteSpec("/Test", "/Test") {
            @Override
            public Class<IObject> getHibernateClass(String table) {
                return IObject.class;
            }
        };
        spec.setCurrentDetails(new CurrentDetails() {
            @Override
            public EventContext getCurrentEventContext() {
                return createEventContext(false);
            }
        });

        table = Arrays.asList(Arrays.asList(1L));
        prepareQueryBackupIds(table);

        // null ctx is okay until .release()
        DeleteState state = new DeleteState(null, session, spec);
        assertEquals(1, state.getTotalFoundCount());
    }

    @Test
    public void testTwoSimpleEntries() throws Exception {
        BaseDeleteSpec spec = new BaseDeleteSpec("/Test", "/Test", "/Foo") {
            @Override
            public Class<IObject> getHibernateClass(String table) {
                return IObject.class;
            }
        };
        spec.setCurrentDetails(new CurrentDetails() {
            @Override
            public EventContext getCurrentEventContext() {
                return createEventContext(false);
            }
        });

        table = Arrays.asList(Arrays.asList(1L));
        prepareQueryBackupIds(table);

        table = Arrays.asList(Arrays.asList(2L));
        prepareQueryBackupIds(table);

        DeleteState state = new DeleteState(null, session, spec);
        assertEquals(2, state.getTotalFoundCount());

    }

    @Test
    public void testSimpleEntryWithMultipleResults() throws Exception {
        BaseDeleteSpec spec = new BaseDeleteSpec("/Test", "/Test") {
            @Override
            public Class<IObject> getHibernateClass(String table) {
                return IObject.class;
            }
        };
        spec.setCurrentDetails(new CurrentDetails() {
            @Override
            public EventContext getCurrentEventContext() {
                return createEventContext(false);
            }
        });

        table = table(new long[] { 1L }, new long[] { 2L });
        prepareQueryBackupIds(table);

        DeleteState state = new DeleteState(null, session, spec);
        assertEquals(2, state.getTotalFoundCount());

    }

    @Test
    public void testSimpleRoiSubSpec() throws Exception {
        prepareGetRelationship();
        DeleteSpecFactory factory = specXml.getBean(DeleteSpecFactory.class);
        DeleteSpec spec = factory.get("/Roi");

        prepareGetHibernateClass();

        Map<String, List<List<Long>>> rv = makeRoiLookups(
                table(new long[] { 0L }), table(new long[] { 0L, 1L }),
                table(new long[] { 0L, 2L }), table(new long[] { 0L, 3L }),
                table(new long[] { 5L }));
        prepareTableLookups(rv);

        DeleteState state = new DeleteState(null, session, spec);
        assertEquals(state.toString(), 4, state.getTotalFoundCount());

    }

    @Test
    public void testComplexRoiSubSpec() throws Exception {
        prepareGetRelationship();
        DeleteSpecFactory factory = specXml.getBean(DeleteSpecFactory.class);
        DeleteSpec spec = factory.get("/Roi");

        prepareGetHibernateClass();

        Map<String, List<List<Long>>> rv = makeRoiLookups(
                table(new long[] { 0L }, new long[] { 10L }),
                table(new long[] { 0L, 1L }, new long[] { 10L, 11L }),
                table(new long[] { 0L, 2L }, new long[] { 10L, 12L }),
                table(new long[] { 0L, 3L }, new long[] { 10L, 13L }),
                table(new long[] { 5L }, new long[] { 15L }));
        prepareTableLookups(rv);

        DeleteState state = new DeleteState(null, session, spec);
        assertEquals(state.toString(), 8, state.getTotalFoundCount());

    }

    @Test
    public void testDeleteStateTablesTopLevel() {
        DeleteState.Tables t = new DeleteState.Tables();
        DeleteEntry entry = new DeleteEntry(new BaseDeleteSpec("foo"), "foo");
        List<List<Long>> results = table(new long[] { 0L }, new long[] { 1L });
        t.add(entry, results);
        Iterator<List<List<Long>>> it = t.columnSets(entry, null);
        List<List<Long>> rows;
        int count = 0;
        while (it.hasNext()) {
            rows = it.next();
            assertEquals(1, rows.size());
            List<Long> cols = rows.get(0);
            assertEquals(1, cols.size());
            assertEquals(count, cols.get(0).intValue());
            count++;
        }
        assertEquals(2, count); // 2 sets
    }

    @Test
    public void testDeleteStateTablesWithMatchLength1() {
        DeleteState.Tables t = new DeleteState.Tables();
        DeleteEntry entry = new DeleteEntry(new BaseDeleteSpec("foo"), "foo");
        List<List<Long>> results = table(new long[] { 0L }, new long[] { 1L });
        t.add(entry, results);
        Iterator<List<List<Long>>> it = t.columnSets(entry, Arrays.asList(0L));
        List<List<Long>> rows;
        int count = 0;
        while (it.hasNext()) {
            rows = it.next();
            assertEquals(count, rows.get(0).get(0).intValue());
            count++;
        }
        assertEquals(2, count); // 2 sets when length is 1
    }

    @Test
    public void testDeleteStateTablesWithMatchLength3() {
        DeleteState.Tables t = new DeleteState.Tables();
        DeleteEntry entry = new DeleteEntry(new BaseDeleteSpec("foo"), "foo");
        List<List<Long>> results = table(new long[] { 0L, 2L, 2L }, new long[] {
                0L, 2L, 3L }, new long[] { 0L, 2L, 4L }, new long[] { 1L, 3L,
                5L }, new long[] { 1L, 3L, 6L });
        t.add(entry, results);
        Iterator<List<List<Long>>> it = t.columnSets(entry,
                Arrays.asList(0L, 2L, 2L));
        List<List<Long>> rows;
        int count = 0;
        while (it.hasNext()) {
            count++;
            rows = it.next();
            for (List<Long> cols : rows) {
                assertEquals((Long) 0L, cols.get(0));
                assertEquals((Long) 2L, cols.get(1));
            }
        }
        assertEquals(1, count);
    }

    @Test
    public void testStackSizeForProject() throws Exception {
        prepareGetRelationship();
        DeleteSpecFactory factory = specXml.getBean(DeleteSpecFactory.class);
        DeleteSpec spec = factory.get("/Project");

        prepareGetHibernateClass();

        Map<String, List<List<Long>>> rv = makeProjLookups(
                table(new long[] { 0L }), table(new long[] { 0L, 1L }),
                table(new long[] { 0L, 1L, 2L }));
        prepareTableLookups(rv);

        DeleteState state = new DeleteState(null, session, spec);
        assertEquals(state.toString(), 3, state.getTotalFoundCount());

    }

    //
    // Helpers
    //

    private Map<String, List<List<Long>>> makeRoiLookups(List<List<Long>> rois,
            List<List<Long>> shapes, List<List<Long>> links,
            List<List<Long>> annotations, List<List<Long>> justAnnotations) {

        Map<String, List<List<Long>>> rv = new HashMap<String, List<List<Long>>>();
        makeAnnLookups(rv);
        rv.put("select ROOT0.id from Roi as ROOT0 where ROOT0.id = :id ", rois);
        rv.put("select ROOT0.id , ROOT1.id from Roi as ROOT0 join ROOT0.shapes as ROOT1 where ROOT0.id = :id ",
                shapes);
        rv.put("select ROOT0.id , ROOT1.id from Roi as ROOT0 join ROOT0.annotationLinks as ROOT1 where ROOT0.id = :id ",
                links);
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = BooleanAnnotation) ",
                justAnnotations);
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Roi as ROOT0 join ROOT0.annotationLinks as ROOT1 join ROOT1.child as ROOT2 where ROOT0.id = :id ",
                annotations);

        return rv;
    }

    private void makeAnnLookups(Map<String, List<List<Long>>> rv) {
        rv.put("select ROOT0.id , ROOT1.id from Annotation as ROOT0 join ROOT0.annotationLinks as ROOT1 where ROOT0.id = :id and (ROOT0.class = Annotation) ",
                table());
        rv.put("select ROOT0.id , ROOT1.id from Annotation as ROOT0 join ROOT0.parent as ROOT1 where ROOT0.id = :id and (ROOT0.class = Annotation) ",
                table());
        rv.put("select ROOT0.id , ROOT1.id from Annotation as ROOT0 join ROOT0.file as ROOT1 where ROOT0.id = :id and (ROOT0.class = FileAnnotation) ",
                table());
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = FileAnnotation) ",
                table());
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = ListAnnotation) ",
                table());
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = XmlAnnotation) ",
                table());
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = TagAnnotation) ",
                table());
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = CommentAnnotation) ",
                table());
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = LongAnnotation) ",
                table());
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = TermAnnotation) ",
                table());
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = TimestampAnnotation) ",
                table());
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = DoubleAnnotation) ",
                table());
        rv.put("select ROOT0.id from Annotation as ROOT0 where ROOT0.id = :id and (ROOT0.class = BooleanAnnotation) ",
                table());

    }

    private Map<String, List<List<Long>>> makeProjLookups(
            List<List<Long>> projects, List<List<Long>> projectDatasetLinks,
            List<List<Long>> datasets) {
        Map<String, List<List<Long>>> rv = new HashMap<String, List<List<Long>>>();
        rv.put("select ROOT0.id , ROOT1.id from Project as ROOT0 join ROOT0.datasetLinks as ROOT1 where ROOT0.id = :id ",
                projectDatasetLinks);
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Project as ROOT0 join ROOT0.datasetLinks as ROOT1 join ROOT1.child as ROOT2 where ROOT0.id = :id ",
                datasets);
        rv.put("select ROOT0.id , ROOT1.id from Dataset as ROOT0 join ROOT0.imageLinks as ROOT1 where ROOT0.id = :id ",
                table()); // Not a subspec
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Dataset as ROOT0 join ROOT0.imageLinks as ROOT1 join ROOT1.child as ROOT2 where ROOT0.id = :id ",
                table()); // No images, so other methods should not be called.
        rv.put("select ROOT0.id , ROOT1.id from Project as ROOT0 join ROOT0.annotationLinks as ROOT1 where ROOT0.id = :id ",
                table()); // This is not a subspec, and so can't use the simple
                          // optimization
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Project as ROOT0 join ROOT0.annotationLinks as ROOT1 join ROOT1.child as ROOT2 where ROOT0.id = :id ",
                table()); // Again, no annotations.
        rv.put("select ROOT0.id , ROOT1.id from Annotation as ROOT0 join ROOT0.parent as ROOT1 where ROOT0.id = :id and (ROOT0.class = Annotation) ",
                table());
        rv.put("select ROOT0.id from Project as ROOT0 where ROOT0.id = :id ",
                projects);
        rv.put("select ROOT0.id , ROOT1.id from Dataset as ROOT0 join ROOT0.annotationLinks as ROOT1 where ROOT0.id = :id ",
                table());
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Dataset as ROOT0 join ROOT0.annotationLinks as ROOT1 join ROOT1.child as ROOT2 where ROOT0.id = :id ",
                table());
        rv.put("select ROOT0.id from Dataset as ROOT0 where ROOT0.id = :id ",
                datasets);

        makeAnnLookups(rv);
        return rv;
    }

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

    private void prepareTableLookups(final Map<String, List<List<Long>>> values) {
        sessionMock.setDefaultStub(new Stub() {

            public StringBuffer describeTo(StringBuffer arg0) {
                arg0.append("handles all lookups");
                return arg0;
            }

            public Object invoke(Invocation arg0) throws Throwable {
                if (arg0.invokedMethod.getName().equals("createQuery")) {
                    String str = (String) arg0.parameterValues.get(0);
                    List<List<Long>> table = (List<List<Long>>) values.get(str);
                    if (table == null) {
                        fail("Unknown query: <" + str + ">");
                    }
                    Mock m = new Mock(Query.class);
                    Query q = (Query) m.proxy();
                    m.expects(once()).method("setParameter");
                    m.expects(once()).method("list").will(returnValue(table));
                    return q;
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

}
