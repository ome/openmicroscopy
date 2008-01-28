/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import ome.api.Search;
import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import org.testng.annotations.Test;

@Test(groups = { "query", "fulltext", "search" })
public class SearchTest extends AbstractTest {

    @Test
    public void testSerialization() throws Exception {
        Object o = this.applicationContext.getBean("internal:ome.api.Search");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
    }

    // by<Query>
    // =========================================================================
    // This section tests each query method with various combinations of
    // restrictions

    @Test
    public void testByGroupForTags() {
        String groupStr = uuid();
        String tagStr = uuid();

        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(tagStr);

        TagAnnotation grp = new TagAnnotation();
        grp.setTextValue(groupStr);

        tag.linkAnnotation(grp);
        tag = iUpdate.saveAndReturnObject(tag);

        Search search = this.factory.createSearchService();
        search.byGroupForTags(groupStr);
        assertEquals(1, search.results().size());

        // Make another one
        groupStr = uuid();
        grp = new TagAnnotation();
        tag.linkAnnotation(grp);
        tag = iUpdate.saveAndReturnObject(tag);

        // Now we are sure that there are two taggroups in the db;
        // this should return all two then
        search.byGroupForTags(null);
        search.setBatchSize(2);
        assertEquals(2, search.results().size());
        while (search.hasNext()) {
            search.next(); // Clear search
        }

        // Let's now add the tag to another tag group as another user
        // and try to filter out those results

        long oldUser = iAdmin.getEventContext().getCurrentUserId();
        Details d = Details.create();
        d.setOwner(new Experimenter(oldUser, false));

        Experimenter e = loginNewUser();
        grp = new TagAnnotation();
        groupStr = uuid();
        grp.setTextValue(groupStr);
        tag.linkAnnotation(grp);
        tag = iUpdate.saveAndReturnObject(tag);

        // All queries finished?
        assertEquals(0, search.activeQueries());
        assertFalse(search.hasNext());

        search.onlyOwnedBy(d);
        search.byGroupForTags(groupStr);
        assertFalse(search.hasNext());

        d.setOwner(e);
        search.onlyOwnedBy(d);
        search.byGroupForTags(groupStr);
        assertEquals(1, search.results().size());

        search.onlyOwnedBy(null);
        search.byGroupForTags(groupStr);
        assertEquals(1, search.results().size());
    }

    @Test
    public void testByTagForGroup() {
        String groupStr = uuid();
        String tagStr = uuid();

        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(tagStr);

        TagAnnotation grp = new TagAnnotation();
        grp.setTextValue(groupStr);

        tag.linkAnnotation(grp);
        tag = iUpdate.saveAndReturnObject(tag);

        Search search = this.factory.createSearchService();
        search.byTagForGroups(tagStr);
        assertEquals(1, search.results().size());

        // Make another one
        tagStr = uuid();
        tag = new TagAnnotation();
        tag.linkAnnotation(grp);
        tag = iUpdate.saveAndReturnObject(tag);

        // Now we are sure that there are two tags for the one group;
        // this should return all two then
        search.byTagForGroups(null);
        search.setBatchSize(2);
        assertEquals(2, search.results().size());
    }

    @Test
    public void testSimpleFullTextSearch() {

        Image i = new Image();
        i.setName(uuid());
        i = iUpdate.saveAndReturnObject(i);

        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.byFullText(i.getName());
        int count = 0;
        while (search.hasNext()) {
            IObject obj = search.next();
            count++;
            assertNotNull(obj);
        }
        assertTrue(count == 1);
        search.close();

        search.onlyType(Image.class);
        search.byFullText(i.getName());
        Map<IObject, List<Annotation>> map = search.results();
        assertTrue(map.size() == 1);

        search.close();
    }

    String[] sa(String... arr) {
        return arr;
    }

    @Test
    public void testSomeMustNone() {
        final String[] contained = new String[] { "abc", "def", "ghi", "123" };
        final String[] missing = new String[] { "jkl", "mno", "pqr", "456" };

        Image i = new Image();
        i.setName("abc def ghi");
        i = iUpdate.saveAndReturnObject(i);
        indexObject(i);
        loginRoot();

        final Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // Make sure we can find it simply
        search.bySomeMustNone(sa("abc"), sa(), sa());
        assertTrue(search.results().size() >= 1);

        //
        // Now we'll try more complicated queries
        //

        // This should return nothing since none is contained
        search.bySomeMustNone(sa("abc"), sa(), sa("def"));
        assertResults(search, 0);

        // but if the none is not contained should be ok.
        search.bySomeMustNone(sa("abc"), sa("abc"), sa("jkl"));
        assertAtLeastResults(search, 1);

        // Simple must query
        search.bySomeMustNone(sa(), sa("abc"), sa());
        assertAtLeastResults(search, 1);

        // same, but with a matching none
        search.bySomeMustNone(sa(), sa("abc"), sa("def"));
        assertResults(search, 0);

        // same again, but with non-matching none
        search.bySomeMustNone(sa(), sa("abc"), sa("jkl"));
        assertAtLeastResults(search, 1);

        //
        // Mixing some and must
        //

        // Present must
        search.bySomeMustNone(sa("abc"), sa("def"), sa());
        assertAtLeastResults(search, 1);

        // Missing must
        search.bySomeMustNone(sa("abc"), sa("jkl"), sa());
        assertResults(search, 0);

        // Present must, missing some
        search.bySomeMustNone(sa("jkl"), sa("def"), sa());
        assertAtLeastResults(search, 1);

        //
        // Using wildcards
        //

        // some with wildcard
        search.bySomeMustNone(sa("ab*"), sa(), sa());
        assertAtLeastResults(search, 1);

        // must with wildcard
        search.bySomeMustNone(sa(), sa("ab*"), sa());
        assertAtLeastResults(search, 1);

        // none with wildcard
        search.bySomeMustNone(sa(), sa(), sa("ab*"));
        assertResults(search, 0);

        //
        // Multiterms
        //

        search.bySomeMustNone(sa("abc", "def"), null, null);
        assertAtLeastResults(search, 1);

        search.bySomeMustNone(null, sa("abc", "def"), null);
        assertAtLeastResults(search, 1);

        search.bySomeMustNone(null, null, sa("abc", "def"));
        assertResults(search, 0);

        search.bySomeMustNone(sa("ghi", "123"), sa("abc", "def"), null);
        assertAtLeastResults(search, 1);

        search.bySomeMustNone(sa("ghi", "123"), sa("abc", "def"), sa("456"));
        assertAtLeastResults(search, 1);

        search.bySomeMustNone(sa("ghi", "123"), sa("abc", "456"), sa("456"));
        assertResults(search, 0);

        //
        // Completely empty
        //
        try {
            search.bySomeMustNone(null, null, null);
            fail("Should throw");
        } catch (ApiUsageException aue) {
            // ok
        }

        try {
            search.bySomeMustNone(sa(), null, null);
            fail("Should throw");
        } catch (ApiUsageException aue) {
            // ok
        }

        try {
            search.bySomeMustNone(sa(""), null, null);
            fail("Should throw");
        } catch (ApiUsageException aue) {
            // ok
        }

        //
        // Queries with spaces
        // For the moment these return as expected since the parser splits into
        // keywords.
        //
        search.bySomeMustNone(sa("\"abc def\""), null, null);
        assertAtLeastResults(search, 1);
    }

    // restrictions methods
    // ========================================================================
    // The tests in the following sections should include all the by* methods
    // each testing a specific restriction

    @Test
    public void testTypes() {
        fail("nyi");
    }

    @Test
    public void testOnlyOwnedByOwner() {

        Experimenter e = loginNewUser();
        Details user = Details.create();
        user.setOwner(e);

        String name = uuid();
        Image i = new Image(name);
        i = iUpdate.saveAndReturnObject(i);
        indexObject(i);

        loginRoot();
        long id = iAdmin.getEventContext().getCurrentUserId();
        Experimenter self = new Experimenter(id, false);
        Details root = Details.create();
        root.setOwner(self);

        // With no restriction it should be found.
        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.byFullText(name);
        assertEquals(1, search.results().size());

        // Restrict only to root, and then shouldn't be found
        search.onlyOwnedBy(root);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // Now restrict to the user, and again one
        search.onlyOwnedBy(user);
        search.byFullText(name);
        assertEquals(1, search.results().size());
    }

    @Test
    public void testOnlyOwnedByGroup() {

        Experimenter e = loginNewUser();
        ExperimenterGroup g = new ExperimenterGroup(iAdmin.getEventContext()
                .getCurrentGroupId(), false);
        Details user = Details.create();
        user.setGroup(g);

        String name = uuid();
        Image i = new Image(name);
        i = iUpdate.saveAndReturnObject(i);
        indexObject(i);

        loginRoot();
        long id = iAdmin.getEventContext().getCurrentGroupId();
        ExperimenterGroup self = new ExperimenterGroup(id, false);
        Details root = Details.create();
        root.setGroup(self);

        // With no restriction it should be found.
        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.byFullText(name);
        assertEquals(1, search.results().size());

        // Restrict only to root, and then shouldn't be found
        search.onlyOwnedBy(root);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // Now restrict to the user, and again one
        search.onlyOwnedBy(user);
        search.byFullText(name);
        assertEquals(1, search.results().size());
    }

    static Timestamp oneHourAgo, inOneHour, now;
    static {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR, today.get(Calendar.HOUR) - 1);
        oneHourAgo = new Timestamp(today.getTimeInMillis());
        today = Calendar.getInstance();
        today.set(Calendar.HOUR, today.get(Calendar.HOUR) + 1);
        inOneHour = new Timestamp(today.getTimeInMillis());
        now = new Timestamp(System.currentTimeMillis());
    }

    @Test
    public void testOnlyCreateBetween() {

        String name = uuid();
        Image i = new Image();
        i.setName(name);
        i = iUpdate.saveAndReturnObject(i);

        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();

        // Find the Image
        search.onlyType(Image.class);
        search.byFullText(name);
        assertEquals(1, search.results().size());

        // Now restrict the search to past
        search.onlyCreatedBetween(null, oneHourAgo);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // Future
        search.onlyCreatedBetween(inOneHour, null);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // 2 hour period around now
        search.onlyCreatedBetween(oneHourAgo, inOneHour);
        search.byFullText(name);
        assertEquals(1, search.results().size());

        // Starting at now old 'now'
        search.onlyCreatedBetween(null, now);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // Open them up again and should be found
        search.onlyCreatedBetween(null, null);
        search.byFullText(name);
        assertEquals(1, search.results().size());

    }

    @Test
    public void testOnlyModifiedBetween() {

        String name = uuid();
        Image i = new Image();
        i.setName(name);
        i = iUpdate.saveAndReturnObject(i);

        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();

        // Find the Image
        search.onlyType(Image.class);
        search.byFullText(name);
        assertEquals(1, search.results().size());

        // Now restrict the search to past
        search.onlyModifiedBetween(null, oneHourAgo);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // Future
        search.onlyModifiedBetween(inOneHour, null);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // 2 hour period around now
        search.onlyModifiedBetween(oneHourAgo, inOneHour);
        search.byFullText(name);
        assertEquals(1, search.results().size());

        // Starting at now old 'now'
        search.onlyModifiedBetween(null, now);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // Open them up again and should be found
        search.onlyModifiedBetween(null, null);
        search.byFullText(name);
        assertEquals(1, search.results().size());

    }

    @Test
    public void testOnlyAnnotatedBetween() {

        String name = uuid();
        Image i = new Image();
        i.setName(name);
        TagAnnotation ta = new TagAnnotation();
        ta.setTextValue("");
        i.linkAnnotation(ta);

        i = iUpdate.saveAndReturnObject(i);

        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();

        // Find the Image
        search.onlyType(Image.class);
        search.byFullText(name);
        assertEquals(1, search.results().size());

        // Now restrict the search to past
        search.onlyAnnotatedBetween(null, oneHourAgo);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // Future
        search.onlyAnnotatedBetween(inOneHour, null);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // 2 hour period around now
        search.onlyAnnotatedBetween(oneHourAgo, inOneHour);
        search.byFullText(name);
        assertEquals(1, search.results().size());

        // Starting at now old 'now'
        search.onlyAnnotatedBetween(null, now);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // Open them up again and should be found
        search.onlyAnnotatedBetween(null, null);
        search.byFullText(name);
        assertEquals(1, search.results().size());
    }

    @Test
    public void testOnlyAnnotatedBy() {
        String name = uuid();
        Image i = new Image();
        i.setName(name);
        TagAnnotation t = new TagAnnotation();
        t.setTextValue(uuid());
        i.linkAnnotation(t);
        i = iUpdate.saveAndReturnObject(i);
        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // Find the annotation
        search.byFullText(name);
        assertEquals(1, search.results().size());

        // But if we restrict it to another user, there should be none
        Experimenter e = loginNewUser();
        Details d = Details.create();
        d.setOwner(e);
        search.onlyAnnotatedBy(d);
        search.byFullText(name);
        assertFalse(search.hasNext());
    }

    @Test
    public void testOnlyAnnotatedWith() {

        String name = uuid();
        Image i = new Image();
        i.setName(name);
        i = iUpdate.saveAndReturnObject(i);

        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();

        // Search for tagged image, which shouldn't be there
        search.onlyAnnotatedWith(TagAnnotation.class);
        search.onlyType(Image.class);
        search.byFullText(name);
        assertFalse(search.hasNext());

        // But if we ask for Images which aren't annotated it should appear
        search.onlyAnnotatedWith(new Class[] {});
        search.onlyType(Image.class);
        search.byFullText(name);
        assertEquals(1, search.results().size());

        // Now let's tag it and see if it shows up
        TagAnnotation t = new TagAnnotation();
        t.setTextValue(uuid());
        t = iUpdate.saveAndReturnObject(t);

        ImageAnnotationLink link = new ImageAnnotationLink(i, t);
        iUpdate.saveObject(link);

        indexObject(i);
        loginRoot();

        // Since we're looking for "no annotations" there should be no results
        search.byFullText(name);
        assertFalse(search.hasNext());

        // And if we turn the annotations back on?
        search.onlyAnnotatedWith(TagAnnotation.class);
        search.byFullText(name);
        assertEquals(1, search.results().size());
    }

    @Test(groups = { "HHH-879", "broken" })
    public void testOnlyAnnotatedWithMultiple() {
        String name = uuid();
        Image onlyTag = new Image(name);
        Image onlyBool = new Image(name);
        Image both = new Image(name);

        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("tag");
        BooleanAnnotation bool = new BooleanAnnotation();
        bool.setBoolValue(false);

        onlyTag.linkAnnotation(tag);
        both.linkAnnotation(tag);
        both.linkAnnotation(bool);
        onlyBool.linkAnnotation(bool);

        IObject[] arr = iUpdate.saveAndReturnArray(new IObject[] { onlyTag,
                onlyBool, both });
        for (IObject object : arr) {
            indexObject(object);
        }
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        search.onlyAnnotatedWith(TagAnnotation.class);
        search.byFullText(name);
        assertEquals(2, search.results().size());

        search.onlyAnnotatedWith(BooleanAnnotation.class);
        search.byFullText(name);
        assertEquals(2, search.results().size());

        search.onlyAnnotatedWith(BooleanAnnotation.class, TagAnnotation.class);
        search.byFullText(name);
        assertEquals(1, search.results().size());

    }

    // bugs
    // =========================================================================

    @Test
    public void testTextAnnotationDoesntTryToLoadUpdateEvent() {
        String uuid = uuid();
        TextAnnotation ta = new TextAnnotation();
        ta.setTextValue(uuid);
        ta = iUpdate.saveAndReturnObject(ta);
        indexObject(ta);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(TextAnnotation.class);
        search.byFullText(uuid);
        assertResults(search, 1);
    }

    @Test
    public void testExperimenterDoesntTryToLoadOwner() {
        Search search = this.factory.createSearchService();
        search.onlyType(Experimenter.class);
        search.byFullText("root");
        assertAtLeastResults(search, 1);
    }

    // misc
    // =========================================================================

    @Test
    public void testMergedBatches() {
        String uuid1 = uuid(), uuid2 = uuid();
        Image i1 = new Image(uuid1);
        Image i2 = new Image(uuid2);
        i1 = iUpdate.saveAndReturnObject(i1);
        i2 = iUpdate.saveAndReturnObject(i2);
        indexObject(i1);
        indexObject(i2);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.byFullText(uuid1);
        assertResults(search, 1);

        search.byFullText(uuid2);
        assertResults(search, 1);

        search.bySomeMustNone(sa(uuid1, uuid2), null, null);
        assertResults(search, 2);

        // Everything looks ok, now try with batch
        search.setMergedBatches(true);
        search.byFullText(uuid1);
        search.byFullText(uuid2);
        assertResults(search, 2);
    }

    @Test
    public void testLookingForExperimenterWithOwner() {
        Search search = this.factory.createSearchService();
        Details d = Details.create();
        d.setOwner(new Experimenter(0L, false));
        search.onlyOwnedBy(d);
        search.byFullText("root");
        search.next();
        fail("This should have (currently) failed due to a bad association path.");
    }

    // Helpers
    // =========================================================================

    void assertResults(Search search, int k) {
        if (k == 0) {
            assertFalse(search.hasNext());
        } else {
            assertEquals(k, search.results().size());
        }
    }

    void assertAtLeastResults(Search search, int k) {
        assertTrue(search.results().size() >= k);
    }

}
