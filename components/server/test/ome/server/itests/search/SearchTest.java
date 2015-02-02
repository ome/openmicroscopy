/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import ome.api.IUpdate;
import ome.api.Search;
import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.DoubleAnnotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.LongAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TermAnnotation;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.server.itests.FileUploader;
import ome.testing.ObjectFactory;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.aop.framework.Advised;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

/**
 * The following test provides examples of using the {@link Search} interface,
 * tests its completeness, has regression tests for various tickets, as well as
 * implementation specific tests which by and large can be ignored.
 * 
 * The tests generally demonstrate "best practices" for using the {@link Search}
 * API, <em>except</em> for the many calls to
 * {@link IUpdate#indexObject(IObject)}. These are necessary for real-time
 * testing, but this solution would swamp the server if each client tried to
 * specify when indexing should take place.
 * 
 * Instead, the server decides when objects are indexed, and there may be a
 * short delay.
 * 
 * @see <a
 *      href="http://lucene.apache.org/core/4_9_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html">Query
 *      Parser Syntax</a>
 */
@Test(groups = { "query", "fulltext", "search" })
public class SearchTest extends AbstractTest {

    // User Examples
    // =========================================================================
    // This section tests provides various small example tests, and doesn't
    // try to comprehensively test the API

    public void testComplicatedLuceneQueries() {
        Image i = new Image("Image with A - 1 reagent");
        i = this.iUpdate.saveAndReturnObject(i);
        this.iUpdate.indexObject(i);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        search.byFullText("\"A \\- 1\"");
        assertContainsObject(search, i);

        search.byFullText("A \\- 1");
        assertContainsObject(search, i);

        search.bySomeMustNone(new String[] { "name:\"A \\- 1\"" }, null, null);
        assertContainsObject(search, i);

        search.bySomeMustNone(new String[] { "name:reagent" }, null, null);
        assertContainsObject(search, i);

        // The following doesn't work for some reason.
        // search.bySomeMustNone(new String[] { "name:A" }, null, null);

        // And this causes a parse exception
        // search.bySomeMustNone(new String[] { "name:*A*" }, null, null);

        // The following won't work since "-" is a Lucene special character.
        // search.byFullText("A - 1");

    }

    public void testFullTextUsingIQuery() {

        String uuid = uuid();
        String part = uuid.substring(0, uuid.indexOf("DASH"));
        Image i = new Image("myIQueryImageTest");
        TagAnnotation tag = new TagAnnotation();
        tag.setName("theTagNameInMyIQueryTest");
        tag.setNs("theNamespaceInMyIQueryTest");
        tag.setTextValue("some test and an " + uuid + " to search");
        i.linkAnnotation(tag);
        i = this.iUpdate.saveAndReturnObject(i);
        this.iUpdate.indexObject(i);

        List<? extends IObject> list;

        list = this.iQuery.findAllByFullText(Image.class, "myIQueryImageTest",
                null);
        assertTrue(list.toString(), list.size() >= 1);

        list = this.iQuery.findAllByFullText(Image.class,
                "name:myIQueryImageTest", null);
        assertTrue(list.toString(), list.size() >= 1);

        list = this.iQuery.findAllByFullText(Image.class, "name:myIQuery*",
                null);
        assertTrue(list.toString(), list.size() >= 1);

        list = this.iQuery.findAllByFullText(Image.class, part + "*", null);
        assertTrue(list.toString(), list.size() == 1);

        list = this.iQuery.findAllByFullText(Image.class, uuid, null);
        assertTrue(list.toString(), list.size() == 1);

        list = this.iQuery.findAllByFullText(Image.class, "\"some*" + uuid
                + "*\"", null);
        assertTrue(list.toString(), list.size() == 1);

        list = this.iQuery.findAllByFullText(Image.class, "tag:" + uuid, null);
        assertTrue(list.toString(), list.size() == 1);

        list = this.iQuery.findAllByFullText(Image.class, "annotation:" + uuid,
                null);
        assertTrue(list.toString(), list.size() == 1);

        list = this.iQuery.findAllByFullText(Image.class,
                "annotation.name:theTagName*", null);
        assertTrue(list.toString(), list.size() >= 1);

        list = this.iQuery.findAllByFullText(Image.class,
                "annotation.ns:theNamespace*", null);
        assertTrue(list.toString(), list.size() >= 1);

        list = this.iQuery.findAllByFullText(Image.class,
                "annotation.type:TagAnnotation", null);
        assertTrue(list.toString(), list.size() >= 1);

    }

    // by<Query>
    // =========================================================================
    // This section tests each query method with various combinations of
    // restrictions

    @Test
    public void testBySearchTerms() {

        String base = uuid();
        String base1 = base + "1";
        String base2 = base + "2";
        String base3 = base + "3";

        Image i1 = new Image(base1);
        Image i2 = new Image(base2);
        Image i3 = new Image(base3);

        i1 = iUpdate.saveAndReturnObject(i1);
        i2 = iUpdate.saveAndReturnObject(i2);
        i3 = iUpdate.saveAndReturnObject(i3);

        iUpdate.indexObject(i1);
        iUpdate.indexObject(i2);
        iUpdate.indexObject(i3);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.bySimilarTerms(base1);
        List annotations = search.results();
        List<String> terms = new ArrayList<String>();
        for (Object obj : annotations) {
            terms.add(((CommentAnnotation) obj).getTextValue());
        }
        // Lower-casing is necessary since that's what's stored in the index.
        assertTrue(terms.contains(base2.toLowerCase()));
        assertTrue(terms.contains(base2.toLowerCase()));
        assertFalse(terms.contains(base.toLowerCase()));
        assertFalse(terms.contains(base1.toLowerCase()));
    }

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
        Experimenter e1 = new Experimenter(oldUser, false);
        Details d = Details.create();
        d.setOwner(e1);

        Experimenter e = loginNewUserInOtherUsersGroup(e1);
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
        Experimenter e1 = iAdmin.getExperimenter(oldUser);
        Details d = Details.create();
        d.setOwner(new Experimenter(oldUser, false));

        Experimenter e = loginNewUserInOtherUsersGroup(e1);
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

        iUpdate.indexObject(i);
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
        assertResults(search, 1);

        search.close();
    }

    @Test
    public void testWildcardFullTextSearch() {

        Image i = new Image();
        i.setName(uuid());
        i = iUpdate.saveAndReturnObject(i);

        iUpdate.indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.byFullText(i.getName());
        assertResults(search, 1);

        String leadingWildcard = "*"
                + i.getName().substring(1, i.getName().length());
        try {
            search.byFullText(leadingWildcard);
            fail("Should throw AUE");
        } catch (ApiUsageException e) {
            // ok, and clear
            while (search.hasNext()) {
                search.results();
            }
        }
        search.setAllowLeadingWildcard(true);
        search.byFullText(leadingWildcard);
        assertResults(search, 1);
        search.close();

    }

    String[] sa(String... arr) {
        return arr;
    }

    @Test
    public void testSomeMustNone() {
        String abc = uuid();
        String def = uuid();
        String ghi = uuid();
        String _123 = uuid();
        String jkl = uuid();
        String mno = uuid();
        String pqr = uuid();
        String _456 = uuid();
        final String[] contained = new String[] { abc, def, ghi, _123 };
        final String[] missing = new String[] { jkl, mno, pqr, _456 };

        Image i = new Image();
        i.setName(abc + " " + def + " " + ghi);
        i = iUpdate.saveAndReturnObject(i);
        iUpdate.indexObject(i);
        loginRootKeepGroup();

        final Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // Make sure we can find it simply
        search.bySomeMustNone(sa(abc), sa(), sa());
        assertTrue(search.results().size() >= 1);

        //
        // Now we'll try more complicated queries
        //

        // This should return nothing since none is contained
        search.bySomeMustNone(sa(abc), sa(), sa(def));
        assertResults(search, 0);

        // but if the none is not contained should be ok.
        search.bySomeMustNone(sa(abc), sa(abc), sa(jkl));
        assertAtLeastResults(search, 1);

        // Simple must query
        search.bySomeMustNone(sa(), sa(abc), sa());
        assertAtLeastResults(search, 1);

        // same, but with a matching none
        search.bySomeMustNone(sa(), sa(abc), sa(def));
        assertResults(search, 0);

        // same again, but with non-matching none
        search.bySomeMustNone(sa(), sa(abc), sa(jkl));
        assertAtLeastResults(search, 1);

        //
        // Mixing some and must
        //

        // Present must
        search.bySomeMustNone(sa(abc), sa(def), sa());
        assertAtLeastResults(search, 1);

        // Missing must
        search.bySomeMustNone(sa(abc), sa(jkl), sa());
        assertResults(search, 0);

        // Present must, missing some
        search.bySomeMustNone(sa(jkl), sa(def), sa());
        assertAtLeastResults(search, 1);

        //
        // Using wildcards
        //

        // some with wildcard
        String part = abc.substring(0, abc.indexOf("DASH")) + "*";
        search.bySomeMustNone(sa(part), sa(), sa());
        assertAtLeastResults(search, 1);

        // must with wildcard
        search.bySomeMustNone(sa(), sa(part), sa());
        assertAtLeastResults(search, 1);

        // none with wildcard
        search.bySomeMustNone(sa(), sa(), sa(part));
        assertResults(search, 0);

        //
        // Multiterms
        //

        search.bySomeMustNone(sa(abc, def), null, null);
        assertAtLeastResults(search, 1);

        search.bySomeMustNone(null, sa(abc, def), null);
        assertAtLeastResults(search, 1);

        search.bySomeMustNone(null, null, sa(abc, def));
        assertResults(search, 0);

        search.bySomeMustNone(sa(ghi, _123), sa(abc, def), null);
        assertAtLeastResults(search, 1);

        search.bySomeMustNone(sa(ghi, _123), sa(abc, def), sa(_456));
        assertAtLeastResults(search, 1);

        search.bySomeMustNone(sa(ghi, _123), sa(abc, _456), sa(_456));
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
        iUpdate.indexObject(i);
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
        iUpdate.indexObject(i);
        loginRoot();

        // Properly uses the id
        FileAnnotation ex2 = new FileAnnotation();
        ex2.setFile(new OriginalFile(file2.getId(), false));
        search.byAnnotatedWith(ex2);
        assertResults(search, 1);

        // Finding by superclass
        // As of 4.0, text annotation is abstract, and so it's not possible
        // to do this.
        /*
         * CommentAnnotation txtAnn = new CommentAnnotation();
         * txtAnn.setTextValue(uuid); search.byAnnotatedWith(txtAnn);
         * assertResults(search, 1);
         */
    }

    @Test
    public void testAnnotatedWithNoValue() {

        String uuid = uuid();
        Image i = new Image(uuid);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(uuid);
        i.linkAnnotation(tag);
        i = iUpdate.saveAndReturnObject(i);
        iUpdate.indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // Now check if an empty example return results
        tag = new TagAnnotation();
        search.byAnnotatedWith(tag);
        assertContainsObject(search, i);

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

    // boolean combinations
    // ========================================================================
    // tests combinations of union, intersection, and complement.

    @Test
    public void testSimpleCombinations() {

        String uuid1 = uuid();
        String uuid2 = uuid();
        Image i1 = new Image(uuid1);
        Image i2 = new Image(uuid2);
        i1 = iUpdate.saveAndReturnObject(i1);
        i2 = iUpdate.saveAndReturnObject(i2);
        iUpdate.indexObject(i1);
        iUpdate.indexObject(i2);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // A + B
        search.byFullText(uuid1);
        search.or();
        search.byFullText(uuid2);
        assertResults(search, 2);

        // A & B
        search.byFullText(uuid1);
        search.and();
        search.byFullText(uuid2);
        assertResults(search, 0);

        // A - B
        search.byFullText(uuid1);
        search.not();
        search.byFullText(uuid2);
        assertResults(search, 1);

        // A + B - B = A
        search.byFullText(uuid1);
        search.or();
        search.byFullText(uuid2);
        search.not();
        search.byFullText(uuid2);
        assertResults(search, 1);

        // With HQL
        search.onlyType(Image.class);
        search.byFullText(uuid1);
        search.and();
        search.byHqlQuery("select i from Image i where i.id = " + i1.getId(),
                null);
        assertResults(search, 1);

    }

    @Test
    public void testCombinationsWithChainedList() {

        String uuid1 = uuid();
        String uuid2 = uuid();
        Image i1 = new Image(uuid1);
        Image i2 = new Image(uuid2);
        TagAnnotation t2 = new TagAnnotation();
        i2.linkAnnotation(t2);
        i1 = iUpdate.saveAndReturnObject(i1);
        i2 = iUpdate.saveAndReturnObject(i2);
        iUpdate.indexObject(i1);
        iUpdate.indexObject(i2);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // No IDLIST-based join
        // here we are purposefully loading image2 in the first call (since
        // it has an annotation) and image1 in the second call (which calls
        // join).
        search.byFullText(uuid2);
        List<IObject> output = assertResults(search, 1);
        Image i = (Image) output.get(0);
        assertTrue(i.sizeOfAnnotationLinks() < 0);

        search.byFullText(uuid2);
        search.and();
        search
                .byHqlQuery(
                        "select i from Image i join fetch i.annotationLinks where i.id in (:IDLIST)",
                        null);
        output = assertResults(search, 1);
        i = (Image) output.get(0);
        assertTrue(i.sizeOfAnnotationLinks() > 0);

    }

    @Test
    public void testCombinationsWithEmptyChainedList() {

        String uuid1 = uuid();
        String uuid2 = uuid();
        Image i1 = new Image(uuid1);
        Image i2 = new Image(uuid2);
        TagAnnotation t2 = new TagAnnotation();
        i2.linkAnnotation(t2);
        i1 = iUpdate.saveAndReturnObject(i1);
        i2 = iUpdate.saveAndReturnObject(i2);
        iUpdate.indexObject(i1);
        iUpdate.indexObject(i2);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        search.byFullText(uuid1);
        search.and();
        search.byFullText(uuid2);
        search.or();
        search
                .byHqlQuery(
                        "select i from Image i join fetch i.annotationLinks where i.id in (:IDLIST)",
                        null);
        List<IObject> output = assertResults(search, 0);
    }

    @Test
    public void testCombinationsWithEmptyButOverwrittenChainedList() {

        String uuid1 = uuid();
        String uuid2 = uuid();
        Image i1 = new Image(uuid1);
        Image i2 = new Image(uuid2);
        TagAnnotation t2 = new TagAnnotation();
        i2.linkAnnotation(t2);
        i1 = iUpdate.saveAndReturnObject(i1);
        i2 = iUpdate.saveAndReturnObject(i2);
        iUpdate.indexObject(i1);
        iUpdate.indexObject(i2);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        search.byFullText(uuid1);
        search.and();
        search.byFullText(uuid2);
        search.or();
        search
                .byHqlQuery(
                        "select i from Image i join fetch i.annotationLinks where i.id in (:IDLIST)",
                        new Parameters().addList("IDLIST", Arrays
                                .asList(new Long[] { i1.getId(), i2.getId() })));
        List<IObject> output = assertResults(search, 1);
        Image i = (Image) output.get(0);
        assertTrue(i.sizeOfAnnotationLinks() > 0);

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
        iUpdate.indexObject(i1);
        iUpdate.indexObject(i2);
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
        search.onlyIds((java.lang.Long[]) null);
        // full text
        search.byFullText(uuid);
        assertResults(search, 2);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 2);
    }

    @Test
    public void testOnlyOwnedByOwner() {

        Experimenter e = loginNewUser(Permissions.COLLAB_READONLY);
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
        iUpdate.indexObject(i);

        loginRootKeepGroup();
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

        Experimenter e = loginNewUser(Permissions.COLLAB_READONLY);
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
        iUpdate.indexObject(i);

        loginRootKeepGroup();
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
        iUpdate.indexObject(i);
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
        iUpdate.indexObject(i);
        loginRootKeepGroup();

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
        iUpdate.indexObject(i);
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
        iUpdate.indexObject(i);
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
        Experimenter e1 = new Experimenter(iAdmin.getEventContext()
                .getCurrentUserId(), false);
        Experimenter e2 = loginNewUserInOtherUsersGroup(e1);
        Details d = Details.create();
        d.setOwner(e2);
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

        iUpdate.indexObject(i);
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

        iUpdate.indexObject(i);
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
            iUpdate.indexObject(object);
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
        iUpdate.indexObject(i1);
        iUpdate.indexObject(i2);
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
        iUpdate.indexObject(i1);
        iUpdate.indexObject(i2);
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
        iUpdate.indexObject(i3);
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
        iUpdate.indexObject(i);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // No fetch returns empty annotations
        // full text
        search.byFullText(uuid);
        Image t = (Image) search.results().get(0);
        assertEquals(-1, t.sizeOfAnnotationLinks());
        // annotated with
        search.byAnnotatedWith(tag);
        t = (Image) search.results().get(0);
        assertEquals(-1, t.sizeOfAnnotationLinks());

        // Fetch only a given type
        search.fetchAnnotations(TagAnnotation.class);
        // annotated with
        search.byAnnotatedWith(tag);
        t = (Image) search.results().get(0);
        assertEquals(1, t.sizeOfAnnotationLinks());
        // full text
        search.byFullText(uuid);
        t = (Image) search.results().get(0);
        assertEquals(3, t.sizeOfAnnotationLinks());

        // fetch only a given type different from annotated-with type
        search.fetchAnnotations(DoubleAnnotation.class);
        // annotated with
        search.byAnnotatedWith(tag);
        t = (Image) search.results().get(0);
        assertEquals(1, t.sizeOfAnnotationLinks());
        // full text
        search.byFullText(uuid);
        t = (Image) search.results().get(0);
        assertEquals(3, t.sizeOfAnnotationLinks());

        // fetch two types
        search.fetchAnnotations(TagAnnotation.class, DoubleAnnotation.class);
        // annotated with
        search.byAnnotatedWith(tag);
        t = (Image) search.results().get(0);
        assertEquals(2, t.sizeOfAnnotationLinks());
        // full text
        search.byFullText(uuid);
        t = (Image) search.results().get(0);
        assertEquals(3, t.sizeOfAnnotationLinks());

        // Fetch all
        search.fetchAnnotations(Annotation.class);
        // annotated with
        search.byAnnotatedWith(tag);
        assertResults(search, 0);
        // TODO t = (Image) search.results().get(0);
        // TODO assertEquals(3, t.sizeOfAnnotationLinks());
        // full text
        search.byFullText(uuid);
        t = (Image) search.results().get(0);
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

    @Test
    public void testFetchAlso() {
        fail("NYI");
    }

    // bugs and particular issues
    // =========================================================================
    // The general reader may want to stop reading at this point.

    @Test
    public void testCommentAnnotationDoesntTryToLoadUpdateEvent() {
        String uuid = uuid();
        CommentAnnotation ta = new CommentAnnotation();
        ta.setTextValue(uuid);
        ta = iUpdate.saveAndReturnObject(ta);
        iUpdate.indexObject(ta);
        loginRoot();

        Search search = this.factory.createSearchService();
        search.onlyType(CommentAnnotation.class);
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

    @Test(groups = "ticket:897", expectedExceptions = ApiUsageException.class)
    public void testLeadingQuestionMarkAlsoNotAllowed() {

        final String query = "?oo";

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        // search.setAllowLeadingWildcard(true);
        search.byFullText(query);
        fail("This should not be reached");
    }

    @Test(groups = "ticket:897", expectedExceptions = ApiUsageException.class)
    public void testOnlyWildcardThrowsException() {

        // This seems only to be caused by a leading "*" and not a
        // leading "?"

        final String query = "*";

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.setAllowLeadingWildcard(true);
        search.byFullText(query);
        fail("This should not be reached");
    }

    /**
     * Attempting to make a lone "*" expand in other situations.
     */
    @Test(groups = "ticket:897")
    public void testWildcardWithTerm() {

        final String query = "* term";

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.setAllowLeadingWildcard(true);
        search.byFullText(query);
        // Seems to be ok.
    }

    /**
     * This was a first test for #975 which always passed.
     */
    @Test(groups = "ticket:975")
    public void testImagesAndTagsReturnedSimple() {

        Image i = new Image("annotation");
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("annotation");
        i.linkAnnotation(tag);

        IUpdate update = this.factory.getUpdateService();
        i = update.saveAndReturnObject(i);
        update.indexObject(i);

        Search search = this.factory.createSearchService();
        search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.bySomeMustNone(new String[] { "an*" }, null, null);

        boolean found = false;
        if (search.hasNext()) {
            List<IObject> l = search.results();
            for (IObject object : l) {
                assertTrue("Must be an image", object instanceof Image);
                if (object.getId().equals(i.getId())) {
                    found = true;
                }
            }
        } else {
            fail("Should have had results.");
        }
        assertTrue("Must be found", found);
    }

    /**
     * This displays the error that Jean-Marie was actually seeing.
     */
    @Test(groups = "ticket:975")
    public void testImagesAndTagsReturnedAccurate() {

        Image i = new Image("annotation");
        IUpdate update = this.factory.getUpdateService();
        i = update.saveAndReturnObject(i);
        update.indexObject(i);

        i = new Image("foo");
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("annotation");
        i.linkAnnotation(tag);
        i = update.saveAndReturnObject(i);
        update.indexObject(i);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.bySomeMustNone(new String[] { "an*" }, null, null);
        for (IObject test : search.results()) {
            assertTrue(test.toString(), test instanceof Image);
        }

        Class[] klass = new Class[1];
        klass[0] = TagAnnotation.class;
        search.onlyAnnotatedWith(klass);
        search.onlyType(Image.class);
        search.bySomeMustNone(new String[] { "an*" }, null, null);

        List<IObject> results = assertContainsObject(search, i);
        for (IObject test : results) {
            assertTrue(test.toString(), test instanceof Image);
        }

    }

    /**
     * Checking for a security leak due to this issue.
     */
    @Test(groups = "ticket:975")
    public void testImagesAndTagsReturnedMultiuser() {

        final List<Long> ids = new ArrayList<Long>();
        final IUpdate update = this.factory.getUpdateService();

        // Save a public image
        Experimenter user1 = loginNewUser(Permissions.COLLAB_READONLY);
        Image i = new Image("foo");
        i = update.saveAndReturnObject(i);
        update.indexObject(i);
        ids.add(i.getId());

        // Create a private tag on the image
        Experimenter user2 = loginNewUserInOtherUsersGroup(user1);
        i = reloadImageWithAnnotationLinks(i);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("annotation");
        tag.getDetails().setPermissions(Permissions.USER_PRIVATE);
        i.linkAnnotation(tag);
        i = update.saveAndReturnObject(i);
        update.indexObject(i);

        // Return to first user and see if it sees the TagAnnotation
        loginUser(user1.getOmeName());
        Search search = this.factory.createSearchService();
        Class[] klass = new Class[1];
        klass[0] = TagAnnotation.class;
        search.onlyAnnotatedWith(klass);
        search.onlyType(Image.class);
        search.bySomeMustNone(new String[] { "an*" }, null, null);

        for (IObject test : search.results()) {
            assertTrue(test.toString(), test instanceof Image);
            ids.remove(test.getId());
        }
        assertTrue(ids + " should be empty", ids.size() == 0);

    }

    /**
     * Attempts to solve #975 by using the {@link Search#or()} method.
     */
    @Test(groups = "ticket:975")
    public void testImagesAndTagsReturnedIntersection() {

        IUpdate update = this.factory.getUpdateService();

        Image i = new Image("foo");
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("annotation");
        i.linkAnnotation(tag);
        i = update.saveAndReturnObject(i);
        update.indexObject(i);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        String[] q = new String[] { "an*" };

        // checking manually
        search.bySomeMustNone(q, null, null);
        assertContainsObject(search, i);
        search.byAnnotatedWith(new TagAnnotation());
        assertContainsObject(search, i);

        // checking via intersection
        search.bySomeMustNone(q, null, null);
        search.and();
        search.byAnnotatedWith(new TagAnnotation());

        assertContainsObject(search, i);

    }

    /**
     * Attempts to solve #975 by using the {@link Search#or()} method.
     */
    @Test(groups = "ticket:975")
    public void testImagesAndTagsReturnedMultipleIntersection() {

        IUpdate update = this.factory.getUpdateService();

        Image i = new Image("foo");
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("annotation");
        FileAnnotation file = new FileAnnotation();
        i.linkAnnotation(tag);
        i.linkAnnotation(file);
        i = update.saveAndReturnObject(i);
        update.indexObject(i);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        String[] q = new String[] { "an*" };

        // checking manually
        search.bySomeMustNone(q, null, null);
        assertContainsObject(search, i);
        search.byAnnotatedWith(new TagAnnotation(), new FileAnnotation());
        assertContainsObject(search, i);

        // checking via intersection
        search.bySomeMustNone(q, null, null);
        search.and();
        search.byAnnotatedWith(new TagAnnotation(), new FileAnnotation());

        assertContainsObject(search, i);

    }

    /**
     * Checking for a security leak due to this issue when using union.
     */
    @Test(groups = "ticket:975")
    public void testImagesAndTagsReturnedMultiuserIntersection() {

        final List<Long> ids = new ArrayList<Long>();
        final IUpdate update = this.factory.getUpdateService();

        // Save a public image
        Experimenter user1 = loginNewUser();
        Image i = new Image("foo");
        i = update.saveAndReturnObject(i);
        update.indexObject(i);
        ids.add(i.getId());

        // Create a private tag on the image
        Experimenter user2 = loginNewUserInOtherUsersGroup(user1);
        i = reloadImageWithAnnotationLinks(i);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("annotation");
        tag.getDetails().setPermissions(Permissions.USER_PRIVATE);
        i.linkAnnotation(tag);
        i = update.saveAndReturnObject(i);
        update.indexObject(i);

        // Return to first user and see if it sees the TagAnnotation
        loginUser(user1.getOmeName());
        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.bySomeMustNone(new String[] { "an*" }, null, null);
        search.and();
        search.byAnnotatedWith(new TagAnnotation());

        for (IObject test : search.results()) {
            assertTrue(test.toString(), test instanceof Image);
            ids.remove(test.getId());
        }
        assertTrue(ids + " should be empty", ids.size() == 0);

    }

    /**
     * Checking for a security leak due to this issue when using union.
     */
    @Test(groups = "ticket:975")
    public void testCriteriaSearchToReproduceSecurityViolation() {

        Executor ex = (Executor) this.applicationContext.getBean("executor");

        Experimenter user1 = loginNewUser();
        Image i = new Image("user1");
        i = this.iUpdate.saveAndReturnObject(i);

        Experimenter user2 = loginNewUser();
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("tag");
        i = reloadImageWithAnnotationLinks(i);
        tag.getDetails().setPermissions(Permissions.USER_PRIVATE);
        tag = this.iUpdate.saveAndReturnObject(tag);

        loginUser(user1.getOmeName());
        String uuid = this.iAdmin.getEventContext().getCurrentSessionUuid();
        Principal p = new Principal(uuid, "user", "Test");
        ex.execute(p, new Executor.SimpleWork(this, "reproduce sec vio") {
            @Transactional(readOnly=true)
            public Object doWork(Session session, ServiceFactory sf) {
                Criteria c = session.createCriteria(Image.class);
                Criteria links = c.createCriteria("annotationLinks");
                Criteria ann = links.createCriteria("child");
                ann.add(Restrictions.eq("textValue", "tag"));
                c.list();
                return null;
            }
        });
        fail("Surprising to reach here");
    }

    @Test(groups = "ticket:995")
    public void testOnlyOwnedByReturnsWrongContent() {

        // Create user which will own the image.
        String uuid = uuid();
        Experimenter owner = loginNewUser(Permissions.COLLAB_READONLY);
        Details d_owner = Details.create();
        d_owner.setOwner(owner);

        // Add an image as the owner
        Image i = new Image();
        i.setName("Some text " + uuid + " blah blah");
        i = this.iUpdate.saveAndReturnObject(i);

        loginRoot();
        this.iUpdate.indexObject(i);

        // Now login as another user who doesn't own that image
        Experimenter searcher = loginNewUserInOtherUsersGroup(owner);
        Details d_searcher = Details.create();
        d_searcher.setOwner(searcher);

        Search search = this.factory.createSearchService();
        search.setAllowLeadingWildcard(true);
        search.onlyType(Image.class);

        // We shouldn't find any results
        assertTicket955(search, uuid, d_searcher, 0);
        // Now let's change to the owner and see the results
        assertTicket955(search, uuid, d_owner, 1);

        // Now let's add annotations and similar and see the results
        loginUser(owner.getOmeName());
        ImageAnnotationLink link = new ImageAnnotationLink();
        link.setParent(new Image(i.getId(), false));
        link.setChild(new TagAnnotation());
        this.iUpdate.saveObject(link);
        loginRoot();
        this.iUpdate.indexObject(i);

        loginUser(searcher.getOmeName());
        // We stil shouldn't find any results
        assertTicket955(search, uuid, d_searcher, 0);
        // Now let's change to the owner and see the results
        assertTicket955(search, uuid, d_owner, 1);

        // Even searching as the owner should produce the same results
        loginUser(owner.getOmeName());
        assertTicket955(search, uuid, d_searcher, 0);
        assertTicket955(search, uuid, d_owner, 1);

    }

    private void assertTicket955(Search search, String uuid, Details d, int n) {
        search.onlyOwnedBy(d);
        search.bySomeMustNone(new String[] { "*" + uuid + "*" }, null, null);
        assertResults(search, n);
        search.bySomeMustNone(new String[] { "*blah blah*" }, null, null);
        assertResults(search, n);
    }

    @Test
    public void testAddingTermAnnotation() throws Exception {

        String uuid = uuid();
        Image i = new Image("name");
        TermAnnotation term = new TermAnnotation();
        term.setTermValue("go:" + uuid);
        i.linkAnnotation(term);

        loginRoot();
        i = this.iUpdate.saveAndReturnObject(i);
        term = (TermAnnotation) i.linkedAnnotationList().get(0);
        this.iUpdate.indexObject(i);
        this.iUpdate.indexObject(term);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.byFullText("term:" + uuid);
        assertResults(search, 1);

        search.onlyType(Image.class);
        search.byFullText("go:" + uuid);
        assertResults(search, 1);

    }

    @Test
    public void testFileAnnotationIsFindableByFileName() throws Exception {

        String uuid = uuid();
        Image i = new Image("name");
        FileAnnotation fa = new FileAnnotation();
        FileUploader uploader = new FileUploader(this.factory, "my-text", uuid,
                "/dev/null");
        uploader.run();
        fa.setFile(new OriginalFile(uploader.getId(), false));
        i.linkAnnotation(fa);

        loginRoot();
        i = this.iUpdate.saveAndReturnObject(i);
        fa = (FileAnnotation) i.linkedAnnotationList().get(0);
        this.iUpdate.indexObject(i);
        this.iUpdate.indexObject(fa);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.byFullText("file.name:" + uuid);
        assertResults(search, 1);

        search.onlyType(FileAnnotation.class);
        search.byFullText("file.name:" + uuid);
        assertResults(search, 1);

    }

    @Test
    public void testAttachingAnnotationAfterTheFact() throws Exception {

        String name = uuid();
        String tag = uuid();
        Image i = new Image(name);
        i = iUpdate.saveAndReturnObject(i);
        iUpdate.indexObject(i);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);
        search.byFullText(name);
        assertResults(search, 1);

        TagAnnotation ta = new TagAnnotation();
        ta.setTextValue(tag);
        ImageAnnotationLink link = new ImageAnnotationLink(new Image(i.getId(),
                false), ta);
        link = iUpdate.saveAndReturnObject(link);
        iUpdate.indexObject(link);
        // This indexing should cause another object be added to
        // PersistentEventLogLoader
        // For that to work we need to run FullTextThread once.
        ((Runnable) this.applicationContext.getBean("fullTextThread")).run();

        search.byFullText(tag);
        assertResults(search, 1);

    }

    @Test
    public void testNewTokenizerTokenizesQueryAsExpected() throws Exception {

        // Setup
        String uuid = uuid().replaceAll("DASH", "-");
        String[] parts = uuid.split("-");
        String rejoined = parts[1] + "-" + parts[3] + "-" + parts[2];

        Image image1 = new Image(uuid);
        Image image2 = new Image(rejoined);
        image1 = iUpdate.saveAndReturnObject(image1);
        image2 = iUpdate.saveAndReturnObject(image2);
        iUpdate.indexObject(image1);
        iUpdate.indexObject(image2);

        Search search = factory.createSearchService();
        search.onlyType(Image.class);

        // Searching by one part should return both
        search.byFullText(parts[1]);
        assertResults(search, 2);

        // Searching by parts separated with a space should also return both
        // They are implicitly joined with "OR"
        search.byFullText(parts[1] + " " + parts[3]);
        assertResults(search, 2);

        // If the terms are joined by a hyphen, it will get stripped, but the
        // term remains one. Equivalent to "PART1 PART2"
        search.byFullText(parts[1] + "-" + parts[3]);
        assertResults(search, 1);

        search.byFullText("\"" + parts[1] + " " + parts[3] + "\"");
        assertResults(search, 1);

        // Search by uuid and should only find one.
        search.byFullText(uuid);
        assertResults(search, 1);

    }

    @Test(groups = "shoola:ticket:663")
    public void testParseException() throws Exception {

        String query = "(tag:100x, OR tag:aurora) +tag:eg5,+tag:jj99 -tag:ph3";

        String name = uuid();
        String tag = "aurora";
        Image i = new Image(name);
        TagAnnotation aurora = new TagAnnotation();
        aurora.setTextValue("aurora");
        TagAnnotation jj99 = new TagAnnotation();
        jj99.setTextValue("jj99");
        TagAnnotation eg5 = new TagAnnotation();
        eg5.setTextValue("eg5");
        i.linkAnnotation(aurora);
        i.linkAnnotation(jj99);
        i.linkAnnotation(eg5);

        i = iUpdate.saveAndReturnObject(i);
        iUpdate.indexObject(i);

        Search search = this.factory.createSearchService();
        search.onlyType(Image.class);

        // This string is corrupt, but it came from someMustNone
        try {
            search.byFullText(query);
        } catch (Exception e) {
            // Known problem.
        }

        search.bySomeMustNone(new String[] { "tag:100x", "tag:aurora" },
                new String[] { "tag:eg5", "tag:jj99" },
                new String[] { "tag:ph3" });
        assertAtLeastResults(search, 1);
    }

    // Implementation specifics
    // =========================================================================

    @Test
    public void testSerialization() throws Exception {
        Search search = this.factory.createSearchService();
        search.onlyType(Experimenter.class);
        search.byFullText("root");
        search.hasNext();
        Search internal = search;
        int count = 0;
        while (internal instanceof Advised) {
            count++;
            if (count > 100) {
                throw new RuntimeException("Something's funky");
            }
            internal = (Search) ((Advised) search).getTargetSource()
                    .getTarget();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(internal);

        byte[] array = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        ObjectInputStream ois = new ObjectInputStream(bais);
        internal = (Search) ois.readObject();
        assertAtLeastResults(search, 1);
    }

    // Helpers
    // =========================================================================

    List<IObject> assertResults(Search search, int k) {
        if (k == 0) {
            assertFalse(search.hasNext());
            return null;
        } else {
            List<IObject> output = new ArrayList<IObject>();
            while (search.hasNext()) {
                output.addAll(search.results());
            }
            assertEquals(k, output.size());
            return output;
        }
    }

    void assertAtLeastResults(Search search, int k) {
        assertTrue(search.results().size() >= k);
        // Clearing possible overflowing values.
        while (search.hasNext()) {
            search.results();
        }
    }

    List<IObject> assertContainsObject(Search search, IObject test) {

        // Because of the weird subclassing issues, we use reflection here
        // to obtain a "true" proxy of this object, and then compare
        // against that.
        test = proxy(test);

        boolean found = false;
        List<IObject> results = new ArrayList<IObject>();
        while (search.hasNext()) {
            for (IObject obj : search.results()) {
                if (test.getClass().isAssignableFrom(obj.getClass())) {
                    if (obj.getId().equals(test.getId())) {
                        found = true;
                    }
                } else {
                    results.add(obj);
                }
            }
        }
        assertTrue(test + " not found in results:" + results, found);
        return results;
    }

    private IObject proxy(IObject test) {
        try {
            return (IObject) test.getClass().getMethod("proxy").invoke(test);
        } catch (Exception e) {
            fail("Could not obtain a proxy for:" + test);
        }
        return null;
    }

    private Image reloadImageWithAnnotationLinks(Image i) {
        i = this.factory.getQueryService()
                .findByQuery(
                        "select i from Image i "
                                + "left outer join fetch i.annotationLinks "
                                + "where i.id = :id",
                        new Parameters().addId(i.getId()));
        return i;
    }

}
