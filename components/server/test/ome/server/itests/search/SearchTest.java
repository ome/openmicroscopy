/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import ome.api.Search;
import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.DoubleAnnotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.LongAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.testing.ObjectFactory;

import org.springframework.aop.framework.Advised;
import org.testng.annotations.Test;

@Test(groups = { "query", "fulltext", "search" })
public class SearchTest extends AbstractTest {

    @Test
    public void testSerialization() throws Exception {
        Search search = this.factory.createSearchService();
        search.onlyType(Experimenter.class);
        search.byFullText("root");
        search.hasNext();
        Search internal = search;
        while (internal instanceof Advised) {
            internal = (Search) ((Advised) search).getTargetSource()
                    .getTarget();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(internal);
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
            search.results(); // Clear search
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
        while (search.hasNext()) {
            search.results(); // Clear search
        }

        // Let's now add another tag to the tag group as another user
        // and try to filter out those results

        long oldUser = iAdmin.getEventContext().getCurrentUserId();
        Details d = Details.create();
        d.setOwner(new Experimenter(oldUser, false));

        Experimenter e = loginNewUser();
        tag = new TagAnnotation();
        tagStr = uuid();
        tag.setTextValue(tagStr);
        tag.linkAnnotation(grp.proxy());
        tag = iUpdate.saveAndReturnObject(tag);

        // All queries finished?
        assertEquals(0, search.activeQueries());
        assertFalse(search.hasNext());

        search.onlyOwnedBy(d);
        search.byTagForGroups(tagStr);
        assertFalse(search.hasNext());

        d.setOwner(e);
        search.onlyOwnedBy(d);
        search.byTagForGroups(tagStr);
        assertEquals(1, search.results().size());

        search.onlyOwnedBy(null);
        search.byTagForGroups(tagStr);
        assertEquals(1, search.results().size());

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

    @Test
    public void testAnnotatedWith() {

        String uuid = uuid();
        Image i = new Image(uuid);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(uuid);
        i.linkAnnotation(tag);
        i = iUpdate.saveAndReturnObject(i);
        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        TagAnnotation example = new TagAnnotation();
        example.setTextValue(uuid);
        search.byAnnotatedWith(example);

        assertResults(search, 1);

        OriginalFile file1 = ObjectFactory.createFile();
        file1 = iUpdate.saveAndReturnObject(file1);
        OriginalFile file2 = ObjectFactory.createFile();
        file2 = iUpdate.saveAndReturnObject(file2);
        FileAnnotation fa1 = new FileAnnotation();
        fa1.setFile(file1);
        i.linkAnnotation(fa1);
        FileAnnotation fa2 = new FileAnnotation();
        fa2.setFile(file2);
        i.linkAnnotation(fa2);
        i = iUpdate.saveAndReturnObject(i);
        indexObject(i);
        loginRoot();

        // Properly uses the id
        FileAnnotation ex2 = new FileAnnotation();
        ex2.setFile(new OriginalFile(file2.getId(), false));
        search.byAnnotatedWith(ex2);
        assertResults(search, 1);

        // Now check if an empty example return results
        search.byAnnotatedWith(new FileAnnotation());
        assertAtLeastResults(search, 1);

        // Finding by superclass
        TextAnnotation txtAnn = new TextAnnotation();
        txtAnn.setTextValue(uuid);
        search.byAnnotatedWith(txtAnn);
        assertResults(search, 1);
    }

    @Test
    public void testAnnotatedWithNamespace() {
        fail("via namespace");
    }

    @Test
    public void testAnnotatedWithMultiple() {
        Image i1 = new Image("i1");
        Image i2 = new Image("i2");

        String uuid = uuid();
        TagAnnotation ta = new TagAnnotation();
        ta.setTextValue(uuid);
        BooleanAnnotation ba = new BooleanAnnotation();
        ba.setBoolValue(false);
        i1.linkAnnotation(ta);
        i2.linkAnnotation(ta);
        i2.linkAnnotation(ba);

        i1 = iUpdate.saveAndReturnObject(i1);
        i2 = iUpdate.saveAndReturnObject(i2);

        ta = new TagAnnotation();
        ta.setTextValue(uuid);
        ba = new BooleanAnnotation();
        ba.setBoolValue(false);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        search.byAnnotatedWith(ta);
        assertResults(search, 2);

        search.byAnnotatedWith(ba);
        assertAtLeastResults(search, 1);

        search.byAnnotatedWith(ta, ba);
        assertResults(search, 1);

    }

    // restrictions methods
    // ========================================================================
    // The tests in the following sections should include all the by* methods
    // each testing a specific restriction

    @Test
    public void testOnlyIds() {

        // ignored by
        // byTagForGroups, byGroupForTags

        String uuid = uuid();
        Image i1 = new Image(uuid);
        Image i2 = new Image(uuid);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(uuid);
        i1.linkAnnotation(tag);
        i2.linkAnnotation(tag);
        i1 = iUpdate.saveAndReturnObject(i1);
        i2 = iUpdate.saveAndReturnObject(i2);
        tag = new TagAnnotation();
        tag.setTextValue(uuid);
        indexObject(i1);
        indexObject(i2);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // Regular search
        // full text
        search.byFullText(uuid);
        assertResults(search, 2);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 2);

        // Restrict to one id
        search.onlyIds(i1.getId());
        // full text
        search.byFullText(uuid);
        assertResults(search, 1);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);

        // Restrict to both ids
        search.onlyIds(i1.getId(), i2.getId());
        // full text
        search.byFullText(uuid);
        assertResults(search, 2);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 2);

        // Restrict to unknown ids
        search.onlyIds(-1L, -2L, -3L);
        // full text
        search.byFullText(uuid);
        assertResults(search, 0);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);

        // unrestrict
        search.onlyIds(null);
        // full text
        search.byFullText(uuid);
        assertResults(search, 2);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 2);
    }

    @Test
    public void testOnlyOwnedByOwner() {

        Experimenter e = loginNewUser();
        Details user = Details.create();
        user.setOwner(e);

        String name = uuid();
        Image i = new Image(name);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(name);
        i.linkAnnotation(tag);
        TagAnnotation grp = new TagAnnotation();
        grp.setTextValue(name);
        tag.linkAnnotation(grp);
        i = iUpdate.saveAndReturnObject(i);
        // Recreating instance as example
        tag = new TagAnnotation();
        tag.setTextValue(name);
        indexObject(i);

        loginRoot();
        long id = iAdmin.getEventContext().getCurrentUserId();
        Experimenter self = new Experimenter(id, false);
        Details root = Details.create();
        root.setOwner(self);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // With no restriction it should be found.
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tag
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Restrict only to root, and then shouldn't be found
        search.notOwnedBy(null);
        search.onlyOwnedBy(root);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 0);

        // Restrict to not root, and then should be found again.
        search.onlyOwnedBy(null);
        search.notOwnedBy(root);
        // full text
        search.byFullText(name);
        assertResults(search, 1);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Now restrict to the user, and again one
        search.notOwnedBy(null);
        search.onlyOwnedBy(user);
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // But not-user should return nothing
        search.notOwnedBy(user);
        search.onlyOwnedBy(null);
        // full text
        search.byFullText(name);
        assertResults(search, 0);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 0);
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
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(name);
        i.linkAnnotation(tag);
        TagAnnotation grp = new TagAnnotation();
        grp.setTextValue(name);
        tag.linkAnnotation(grp);
        i = iUpdate.saveAndReturnObject(i);
        // Recreating instance as example
        tag = new TagAnnotation();
        tag.setTextValue(name);
        indexObject(i);

        loginRoot();
        long id = iAdmin.getEventContext().getCurrentGroupId();
        ExperimenterGroup self = new ExperimenterGroup(id, false);
        Details root = Details.create();
        root.setGroup(self);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // With no restriction it should be found.
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Restrict only to root, and then shouldn't be found
        search.onlyOwnedBy(root);
        search.notOwnedBy(null);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 0);

        // Restrict to not root, and then should be found again.
        search.onlyOwnedBy(null);
        search.notOwnedBy(root);
        // full text
        search.byFullText(name);
        assertResults(search, 1);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Now restrict to the user, and again one
        search.onlyOwnedBy(user);
        search.notOwnedBy(null);
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // But not-user should return nothing
        search.notOwnedBy(user);
        search.onlyOwnedBy(null);
        // full text
        search.byFullText(name);
        assertResults(search, 0);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 0);
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
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(name);
        i.linkAnnotation(tag);
        TagAnnotation grp = new TagAnnotation();
        grp.setTextValue(name);
        tag.linkAnnotation(grp);
        i = iUpdate.saveAndReturnObject(i);
        tag = new TagAnnotation();
        tag.setTextValue(name);
        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // Find the Image
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Now restrict the search to past
        search.onlyCreatedBetween(null, oneHourAgo);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 0);

        // Future
        search.onlyCreatedBetween(inOneHour, null);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 0);

        // 2 hour period around now
        search.onlyCreatedBetween(oneHourAgo, inOneHour);
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Starting at now old 'now'
        search.onlyCreatedBetween(null, now);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 0);

        // Open them up again and should be found
        search.onlyCreatedBetween(null, null);
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);
    }

    @Test
    public void testOnlyModifiedBetween() {

        // Ignored by
        // byTagForGroups, byGroupForTags (tags are immutable) results always 1

        String name = uuid();
        Image i = new Image();
        i.setName(name);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(name);
        i.linkAnnotation(tag);
        TagAnnotation grp = new TagAnnotation();
        grp.setTextValue(name);
        tag.linkAnnotation(grp);
        i = iUpdate.saveAndReturnObject(i);
        tag = new TagAnnotation();
        tag.setTextValue(name);
        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // Find the Image
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Now restrict the search to past
        search.onlyModifiedBetween(null, oneHourAgo);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);

        // Future
        search.onlyModifiedBetween(inOneHour, null);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // 2 hour period around now
        search.onlyModifiedBetween(oneHourAgo, inOneHour);
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Starting at now old 'now'
        search.onlyModifiedBetween(null, now);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Open them up again and should be found
        search.onlyModifiedBetween(null, null);
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);
    }

    @Test
    public void testOnlyAnnotatedBetween() {

        String name = uuid();
        Image i = new Image();
        i.setName(name);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(name);
        i.linkAnnotation(tag);
        TagAnnotation grp = new TagAnnotation();
        grp.setTextValue(name);
        tag.linkAnnotation(grp);
        i = iUpdate.saveAndReturnObject(i);
        tag = new TagAnnotation();
        tag.setTextValue(name);
        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // Find the Image
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Now restrict the search to past
        search.onlyAnnotatedBetween(null, oneHourAgo);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 0);

        // Future
        search.onlyAnnotatedBetween(inOneHour, null);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 0);

        // 2 hour period around now
        search.onlyAnnotatedBetween(oneHourAgo, inOneHour);
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);

        // Starting at now old 'now'
        search.onlyAnnotatedBetween(null, now);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 0);

        // Open them up again and should be found
        search.onlyAnnotatedBetween(null, null);
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(name);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(name);
        assertResults(search, 1);
    }

    @Test
    public void testOnlyAnnotatedBy() {
        String name = uuid();
        String tag = uuid();
        Image i = new Image();
        i.setName(name);
        TagAnnotation t = new TagAnnotation();
        t.setTextValue(tag);
        i.linkAnnotation(t);
        TagAnnotation grp = new TagAnnotation();
        grp.setTextValue(tag);
        t.linkAnnotation(grp);
        i = iUpdate.saveAndReturnObject(i);
        t = new TagAnnotation();
        t.setTextValue(tag);
        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // Find the annotation
        // full text
        search.byFullText(name);
        assertEquals(1, search.results().size());
        // annotated with
        search.byAnnotatedWith(t);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(tag);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(tag);
        assertResults(search, 1);

        // But if we restrict it to another user, there should be none
        Experimenter e = loginNewUser();
        Details d = Details.create();
        d.setOwner(e);
        search.onlyAnnotatedBy(d);
        search.notAnnotatedBy(null);
        // full text
        search.byFullText(name);
        assertFalse(search.hasNext());
        // annotated with
        search.byAnnotatedWith(t);
        assertResults(search, 0);
        // tag for group
        search.byTagForGroups(tag);
        assertResults(search, 0);
        // group for tags
        search.byGroupForTags(tag);
        assertResults(search, 0);

        // Reversing the ownership should give results
        search.onlyAnnotatedBy(null);
        search.notAnnotatedBy(d);
        // full text
        search.byFullText(name);
        assertResults(search, 1);
        // annotated with
        search.byAnnotatedWith(t);
        assertResults(search, 1);
        // tag for group
        search.byTagForGroups(tag);
        assertResults(search, 1);
        // group for tags
        search.byGroupForTags(tag);
        assertResults(search, 1);
    }

    @Test
    public void testOnlyAnnotatedWith() {

        // ignored by byAnnotatedWith
        // ignored by byTagForGroups, byGroupForTags

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

    // other
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
    public void testOrderBy() throws Exception {
        String uuid = uuid();
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(uuid);
        Image i1 = new Image(uuid);
        i1.setDescription("a");
        i1.linkAnnotation(tag);
        Image i2 = new Image(uuid);
        i2.setDescription("b");
        i2.linkAnnotation(tag);
        i1 = iUpdate.saveAndReturnObject(i1);
        Thread.sleep(2000L); // Waiting to test creation time ordering better
        i2 = iUpdate.saveAndReturnObject(i2);
        indexObject(i1);
        indexObject(i2);
        loginRoot();
        tag = new TagAnnotation();
        tag.setTextValue(uuid);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // Order by description desc
        search.unordered();
        search.addOrderByDesc("description");
        // full text
        search.byFullText(uuid);
        List<String> desc = new ArrayList<String>();
        desc.add(i2.getDescription());
        desc.add(i1.getDescription());
        while (search.hasNext()) {
            assertEquals(desc.remove(0), ((Image) search.next())
                    .getDescription());
        }
        // annotated with
        search.byAnnotatedWith(tag);
        desc = new ArrayList<String>();
        desc.add(i2.getDescription());
        desc.add(i1.getDescription());
        while (search.hasNext()) {
            assertEquals(desc.remove(0), ((Image) search.next())
                    .getDescription());
        }

        // Order by descript asc
        search.unordered();
        search.addOrderByAsc("description");
        // full text
        search.byFullText(uuid);
        List<String> asc = new ArrayList<String>();
        asc.add(i1.getDescription());
        asc.add(i2.getDescription());
        while (search.hasNext()) {
            assertEquals(asc.remove(0), ((Image) search.next())
                    .getDescription());
        }
        // annotated with
        search.byAnnotatedWith(tag);
        asc = new ArrayList<String>();
        asc.add(i1.getDescription());
        asc.add(i2.getDescription());
        while (search.hasNext()) {
            assertEquals(asc.remove(0), ((Image) search.next())
                    .getDescription());
        }

        // Ordered by id
        search.unordered();
        search.addOrderByDesc("id");
        // full text
        search.byFullText(uuid);
        List<Long> ids = new ArrayList<Long>();
        ids.add(i2.getId());
        ids.add(i1.getId());
        while (search.hasNext()) {
            assertEquals(ids.remove(0), search.next().getId());
        }
        // annotated with
        search.byAnnotatedWith(tag);
        ids = new ArrayList<Long>();
        ids.add(i2.getId());
        ids.add(i1.getId());
        while (search.hasNext()) {
            assertEquals(ids.remove(0), search.next().getId());
        }

        // Ordered by creation event id
        search.unordered();
        search.addOrderByDesc("details.creationEvent.id");
        // full text
        search.byFullText(uuid);
        ids = new ArrayList<Long>();
        ids.add(i2.getId());
        ids.add(i1.getId());
        while (search.hasNext()) {
            assertEquals(ids.remove(0), search.next().getId());
        }
        // annotated with
        search.byAnnotatedWith(tag);
        ids = new ArrayList<Long>();
        ids.add(i2.getId());
        ids.add(i1.getId());
        while (search.hasNext()) {
            assertEquals(ids.remove(0), search.next().getId());
        }

        // ordered by creation event time
        search.unordered();
        search.addOrderByDesc("details.creationEvent.time");
        // full text
        search.byFullText(uuid);
        ids = new ArrayList<Long>();
        ids.add(i2.getId());
        ids.add(i1.getId());
        while (search.hasNext()) {
            assertEquals(ids.remove(0), search.next().getId());
        }
        // annotated with
        search.byAnnotatedWith(tag);
        ids = new ArrayList<Long>();
        ids.add(i2.getId());
        ids.add(i1.getId());
        while (search.hasNext()) {
            assertEquals(ids.remove(0), search.next().getId());
        }

        // To test multiple sort fields, we add another image with an "a"
        // description, which should could before the other image with the "a"
        // description if we reverse the id order

        Image i3 = new Image(uuid);
        i3.setDescription("a");
        i3.linkAnnotation(tag);
        i3 = iUpdate.saveAndReturnObject(i3);
        indexObject(i3);
        loginRoot();
        tag = new TagAnnotation();
        tag.setTextValue(uuid);

        // multi-ordering
        search.unordered();
        search.addOrderByAsc("description");
        search.addOrderByDesc("id");
        // annotated with
        search.byAnnotatedWith(tag);
        List<Long> multi = new ArrayList<Long>();
        multi.add(i3.getId());
        multi.add(i1.getId());
        multi.add(i2.getId());
        while (search.hasNext()) {
            assertEquals(multi.remove(0), search.next().getId());
        }
        // full text
        search.byFullText(uuid);
        multi = new ArrayList<Long>();
        multi.add(i3.getId());
        multi.add(i1.getId());
        multi.add(i2.getId());
        while (search.hasNext()) {
            assertEquals(multi.remove(0), search.next().getId());
        }

    }

    @Test
    public void testFetchAnnotations() {
        String uuid = uuid();
        Image i = new Image(uuid);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(uuid);
        LongAnnotation la = new LongAnnotation();
        la.setLongValue(1L);
        DoubleAnnotation da = new DoubleAnnotation();
        da.setDoubleValue(0.0);
        i.linkAnnotation(tag);
        i.linkAnnotation(la);
        i.linkAnnotation(da);
        i = iUpdate.saveAndReturnObject(i);
        tag = new TagAnnotation();
        tag.setTextValue(uuid);
        indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // No fetch returns empty annotations
        // full text
        search.byFullText(uuid);
        Image t = (Image) search.results().keySet().iterator().next();
        assertEquals(-1, t.sizeOfAnnotationLinks());
        // annotated with
        search.byAnnotatedWith(tag);
        t = (Image) search.results().keySet().iterator().next();
        assertEquals(-1, t.sizeOfAnnotationLinks());

        // Fetch only a given type
        search.fetchAnnotations(TagAnnotation.class);
        // annotated with
        search.byAnnotatedWith(tag);
        t = (Image) search.results().keySet().iterator().next();
        assertEquals(1, t.sizeOfAnnotationLinks());
        // full text
        search.byFullText(uuid);
        t = (Image) search.results().keySet().iterator().next();
        assertEquals(3, t.sizeOfAnnotationLinks());

        // fetch only a given type different from annotated-with type
        search.fetchAnnotations(DoubleAnnotation.class);
        // annotated with
        search.byAnnotatedWith(tag);
        t = (Image) search.results().keySet().iterator().next();
        assertEquals(1, t.sizeOfAnnotationLinks());
        // full text
        search.byFullText(uuid);
        t = (Image) search.results().keySet().iterator().next();
        assertEquals(3, t.sizeOfAnnotationLinks());

        // fetch two types
        search.fetchAnnotations(TagAnnotation.class, DoubleAnnotation.class);
        // annotated with
        search.byAnnotatedWith(tag);
        t = (Image) search.results().keySet().iterator().next();
        assertEquals(2, t.sizeOfAnnotationLinks());
        // full text
        search.byFullText(uuid);
        t = (Image) search.results().keySet().iterator().next();
        assertEquals(3, t.sizeOfAnnotationLinks());

        // Fetch all
        search.fetchAnnotations(Annotation.class);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // TODO t = (Image) search.results().keySet().iterator().next();
        // TODO assertEquals(3, t.sizeOfAnnotationLinks());
        // full text
        search.byFullText(uuid);
        t = (Image) search.results().keySet().iterator().next();
        assertEquals(3, t.sizeOfAnnotationLinks());

        // resave and see if there is data loss
        search.fetchAnnotations(TagAnnotation.class);
        search.byAnnotatedWith(tag);
        t = (Image) search.next();
        FileAnnotation f = new FileAnnotation();
        t.linkAnnotation(f);
        iUpdate.saveObject(t);
        t = iQuery
                .findByQuery(
                        "select t from Image t join fetch t.annotationLinks where t.id = :id",
                        new Parameters().addId(t.getId()));
        assertEquals(4, t.sizeOfAnnotationLinks());
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

    @Test
    public void testLookingForExperimenterWithOwner() {
        Search search = this.factory.createSearchService();
        search.onlyType(Experimenter.class);

        // Just root should work
        search.byFullText("root");
        search.next();

        // And filtered on "owner" (experimenter has none) should work, too.
        Details d = Details.create();
        d.setOwner(new Experimenter(0L, false));
        search.onlyOwnedBy(d);
        search.byFullText("root");
        search.next();
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
