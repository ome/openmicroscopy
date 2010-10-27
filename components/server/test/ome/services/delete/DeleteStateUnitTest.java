/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public void testDeleteStateTablesWithMatchLength1() {
        DeleteState.Tables t = new DeleteState.Tables();
        DeleteEntry entry = new DeleteEntry(new BaseDeleteSpec("foo"), "foo");
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
    public void testDeleteStateTablesWithMatchLength3() {
        DeleteState.Tables t = new DeleteState.Tables();
        DeleteEntry entry = new DeleteEntry(new BaseDeleteSpec("foo"), "foo");
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
        DeleteSpecFactory factory = specXml.getBean(DeleteSpecFactory.class);
        DeleteSpec spec = factory.get("/Project");

        prepareGetHibernateClass();

        Map<String, List<List<Long>>> rv = makeProjLookups(
                table(new long[] { 0L }), table(new long[] { 0L, 1L }),
                table(new long[] { 0L, 1L, 2L }));
        prepareTableLookups(rv);

        DeleteState state = new DeleteState(null, session, spec);
        assertEquals(state.toString(), 4, state.getTotalFoundCount());
        // 4 for the parent spec

    }


    @Test(groups = "ticket:3163")
    public void testSlowDeleteStateTablesAdd() {
        DeleteState.Tables dst = new DeleteState.Tables();
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

        DeleteSpec spec = new BaseDeleteSpec("foo", "foo");
        DeleteEntry entry = spec.entries().get(0);
        dst.add(entry, convert(data));

        Long ignored = -1L;
        Iterator<List<long[]>> it = dst.columnSets(entry, new long[]{0L, ignored});
        int count = 0;
        while (it.hasNext()) {
            List<long[]> l = it.next();
            for (long[] i : l) {
                assertEquals(0L, i[0]);
                count++;
            }
        }
        assertEquals(10000, count);
    }

    @Test(groups = "ticket:3125")
    public void testSavepointsAreHandledProperly() throws Exception {
        prepareGetHibernateClass();
        prepareGetRelationship();
        DeleteSpecFactory factory = specXml.getBean(DeleteSpecFactory.class);
        DeleteSpec spec = factory.get("/Screen");
        Map<String, List<List<Long>>> rv = makeScreenLookups(
                table(new long[] { 0L }), // Screen
                table(new long[] { 0L, 1L }), // link
                table(new long[] { 0L, 1L, 2L }), // plate
                table(new long[] { 0L, 1L, 2L, 3L }), // well
                table(new long[] { 3L, 4L }), // samples
                table()); // images

        prepareTableLookups(rv);

        DeleteState state = new DeleteState(null, session, spec);
        assertEquals(state.toString(), 100, state.getTotalFoundCount());
    }


    @Test(groups = {"ticket:3125", "ticket:3130"})
    public void testSavepointsAreHandledProperly2() throws Exception {
        prepareGetHibernateClass();
        prepareGetRelationship();
        DeleteSpecFactory factory = specXml.getBean(DeleteSpecFactory.class);
        DeleteSpec spec = factory.get("/Screen");
        Map<String, List<List<Long>>> rv = makeScreenLookups(
                table(new long[] { 0L }), // Screen
                table(new long[] { 0L, 1L }), // link
                table(new long[] { 0L, 1L, 2L }), // plate
                table(new long[] { 0L, 1L, 2L, 3L }), // well
                table(new long[] { 3L, 4L }), // samples
                table()); // images

        prepareTableLookups(rv);

        DeleteState state = new DeleteState(null, session, spec);
        assertEquals(state.toString(), 100, state.getTotalFoundCount());
    }
    //
    // Helpers
    //

    private void assertColumns(int numberOfColumns, List<List<Long>> rows) {
        for (List<Long> cols: rows) {
            assertEquals(rows.toString(), numberOfColumns, cols.size());
        }
    }

    private List<List<Long>> lastColumns(int numberOfColumns, List<List<Long>> rows) {
        List<List<Long>> rv = new ArrayList<List<Long>>();
        for (List<Long> cols : rows) {
            assertTrue(numberOfColumns <= cols.size());
            List<Long> rv2 = new ArrayList<Long>();
            for (int i = cols.size() - numberOfColumns; i < cols.size(); i++) {
                rv2.add(cols.get(i));
            }
            rv.add(rv2);
        }
        return rv;
    }

    private Map<String, List<List<Long>>> makeScreenLookups(
            List<List<Long>> screens, List<List<Long>> plateLinks,
            List<List<Long>> plates, List<List<Long>> wells,
            List<List<Long>> samples, List<List<Long>> images) {

        Map<String, List<List<Long>>> rv = new HashMap<String, List<List<Long>>>();
        rv.put("select ROOT0.id , ROOT1.id from Screen as ROOT0 join ROOT0.plateLinks as ROOT1 where ROOT0.id = :id ",
                plateLinks);
        assertColumns(2, plateLinks);

        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Screen as ROOT0 join ROOT0.plateLinks as ROOT1 join ROOT1.child as ROOT2 where ROOT0.id = :id ",
                plates);
        assertColumns(3, plates);

        rv.put("select ROOT0.id , ROOT1.id from Screen as ROOT0 join ROOT0.annotationLinks as ROOT1 where ROOT0.id = :id ",
                table()); // No annotation links
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Screen as ROOT0 join ROOT0.annotationLinks as ROOT1 join ROOT1.child as ROOT2 where ROOT0.id = :id ",
                table()); // No annotations
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Screen as ROOT0 join ROOT0.reagent as ROOT1 join ROOT1.annotationLinks as ROOT2 where ROOT0.id = :id ",
                table()); // No reagent annotation links
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id , ROOT3.id from Screen as ROOT0 join ROOT0.reagent as ROOT1 join ROOT1.annotationLinks as ROOT2 join ROOT2.child as ROOT3 where ROOT0.id = :id ",
                table()); // No reagent annotations
        rv.put("select ROOT0.id , ROOT1.id from Screen as ROOT0 join ROOT0.reagent as ROOT1 where ROOT0.id = :id ",
                table()); // No reagents
        rv.put("select ROOT0.id from Screen as ROOT0 where ROOT0.id = :id ",
                screens);
        assertColumns(1, screens);

        rv.put("select ROOT0.id , ROOT1.id from Plate as ROOT0 join ROOT0.wells as ROOT1 where ROOT0.id = :id ",
                lastColumns(2, wells));
        assertColumns(2, lastColumns(2, wells));

        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Well as ROOT0 join ROOT0.wellSamples as ROOT1 join ROOT1.annotationLinks as ROOT2 where ROOT0.id = :id ",
                table()); // No field annotation links
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id , ROOT3.id from Well as ROOT0 join ROOT0.wellSamples as ROOT1 join ROOT1.annotationLinks as ROOT2 join ROOT2.child as ROOT3 where ROOT0.id = :id ",
                table()); // No field annotations
        rv.put("select ROOT0.id , ROOT1.id from Well as ROOT0 join ROOT0.wellSamples as ROOT1 where ROOT0.id = :id ",
                samples);
        assertColumns(2, samples);

        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Well as ROOT0 join ROOT0.wellSamples as ROOT1 join ROOT1.image as ROOT2 where ROOT0.id = :id ",
                images);
        assertColumns(3, images);

        rv.put("select ROOT0.id , ROOT1.id from Image as ROOT0 join ROOT0.datasetLinks as ROOT1 where ROOT0.id = :id ",
                table()); // No dataset links
        rv.put("select ROOT0.id , ROOT1.id from Well as ROOT0 join ROOT0.annotationLinks as ROOT1 where ROOT0.id = :id ",
                table()); // No well annotation links
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Well as ROOT0 join ROOT0.annotationLinks as ROOT1 join ROOT1.child as ROOT2 where ROOT0.id = :id ",
                table()); // No well annotations
        rv.put("select ROOT0.id , ROOT1.id from Well as ROOT0 join ROOT0.reagentLinks as ROOT1 where ROOT0.id = :id ",
                table()); // No well-reagent links
        rv.put("select ROOT0.id from Well as ROOT0 where ROOT0.id = :id ",
                lastColumns(1, wells));
        assertColumns(1, lastColumns(1, wells));

        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Plate as ROOT0 join ROOT0.plateAcquisition as ROOT1 join ROOT1.annotationLinks as ROOT2 where ROOT0.id = :id ",
                table()); // No plate acquisition annotation links
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id , ROOT3.id from Plate as ROOT0 join ROOT0.plateAcquisition as ROOT1 join ROOT1.annotationLinks as ROOT2 join ROOT2.child as ROOT3 where ROOT0.id = :id ",
                table()); // No plate acquisition annotations
        rv.put("select ROOT0.id , ROOT1.id from Plate as ROOT0 join ROOT0.plateAcquisition as ROOT1 where ROOT0.id = :id ",
                table()); // No plate acquisitions
        rv.put("select ROOT0.id , ROOT1.id from Plate as ROOT0 join ROOT0.annotationLinks as ROOT1 where ROOT0.id = :id ",
                table()); // No plate annotation links
        rv.put("select ROOT0.id , ROOT1.id , ROOT2.id from Plate as ROOT0 join ROOT0.annotationLinks as ROOT1 join ROOT1.child as ROOT2 where ROOT0.id = :id ",
                table()); // No plate annotations
        rv.put("select ROOT0.id from Plate as ROOT0 where ROOT0.id = :id ",
                lastColumns(1, plates));
        assertColumns(1, lastColumns(1, plates));
        return rv;
    }

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
