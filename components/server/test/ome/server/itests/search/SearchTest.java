/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.search;

import java.util.List;
import java.util.Map;

import ome.api.Search;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.TagAnnotation;
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import org.testng.annotations.Test;

@Test(groups = { "query", "fulltext", "search" })
public class SearchTest extends AbstractTest {

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

    @Test
    public void testOnlyAnnotatedBetween() {
        Search search = this.factory.createSearchService();
        search.onlyAnnotatedBetween(null, null);
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

}
