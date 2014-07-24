/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.query.pojos;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.model.IAnnotated;
import ome.model.annotations.CommentAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.PojosFindAnnotationsQueryDefinition;
import ome.testing.CreatePojosFixture;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = { "ticket:541", "StructuredAnnotations" })
public class FindAnnotationsQueryTest extends AbstractManagedContextTest {
    PojosFindAnnotationsQueryDefinition q;

    List list;

    Set ids;

    CreatePojosFixture DATA;

    @BeforeClass
    public void makePojos() throws Exception {
        try {
            setUp();
            DATA = new CreatePojosFixture(this.factory);
            DATA.pdi();
            DATA.annotations();
        } finally {
            tearDown();
        }
    }

    protected void creation_fails(Parameters parameters) {
        try {
            // new IdsQueryParameterDef(),
            // new OptionsQueryParameterDef(),
            // new QueryParameterDef(QP.CLASS,Class.class,false),
            // new QueryParameterDef("annotatorIds",Collection.class,true));
            q = new PojosFindAnnotationsQueryDefinition(parameters);
            fail("Should have failed!");
        } catch (IllegalArgumentException e) {
        } catch (ApiUsageException e) {
        }
    }

    @Test
    public void test_illegal_arguments() throws Exception {

        creation_fails(new Parameters().addIds(null) // Null
                .addClass(Image.class).addSet("annotatorIds",
                        Collections.emptySet()));

        creation_fails(new Parameters().addIds(Collections.emptySet()) // Empty!
                .addClass(Image.class).addSet("annotatorIds",
                        Collections.emptySet()));

        creation_fails(new Parameters().addIds(Arrays.asList(1l))
                .addClass(null) // Null here
                .addSet("annotatorIds", Collections.emptySet()));

        creation_fails(new Parameters().addIds(Arrays.asList(1)) // Integer
                // not Long
                .addClass(Image.class).addSet("annotatorIds",
                        Collections.emptySet()));
    }

    @Test
    public void test_simple_usage() throws Exception {
        Long doesntExist = -1L;
        q = new PojosFindAnnotationsQueryDefinition(new Parameters().addIds(
                Arrays.asList(doesntExist)).addClass(
                Image.class).addSet("annotatorIds", Collections.emptySet()));

        list = (List) iQuery.execute(q);
    }

    @Test
    public void test_images_exist() throws Exception {
        ids = new HashSet(data.getMax("Image.Annotated.ids", 2));
        q = new PojosFindAnnotationsQueryDefinition(new Parameters()
                .addIds(ids).addClass(Image.class).addSet(
                        "annotatorIds", Collections.emptySet()));

        Collection<IAnnotated> results = (Collection) iQuery.execute(q);
        for (IAnnotated annotated : results) {
            assertTrue(ids.contains(annotated.getId()));
        }
    }

    @Test
    public void test_dataset_exist() throws Exception {
        ids = new HashSet(data.getMax("Dataset.Annotated.ids", 2));
        q = new PojosFindAnnotationsQueryDefinition(new Parameters()
                .addIds(ids).addClass(Dataset.class).addSet(
                        "annotatorIds", Collections.emptySet()));

        Collection<IAnnotated> results = (Collection) iQuery.execute(q);
        for (IAnnotated annotated : results) {
            assertTrue(ids.contains(annotated.getId()));
        }
    }

    @Test(groups = { "ticket:172" })
    public void testFindImageAnnotationsReturnsEventTimes() throws Exception {
        Image i = new Image();
        i.setName("ticket:172");
        CommentAnnotation a = new CommentAnnotation();
        a.setNs("");
        a.setTextValue("ticket:172");
        i.linkAnnotation(a);
        i = iUpdate.saveAndReturnObject(i);

        ids = new HashSet(Arrays.asList(i.getId()));
        q = new PojosFindAnnotationsQueryDefinition(new Parameters()
                .addIds(ids).addClass(Image.class).addSet(
                        "annotatorIds", Collections.emptySet()));

        Collection<IAnnotated> results = (Collection) iQuery.execute(q);
        for (IAnnotated annotated : results) {
            assertNotNull(annotated.getDetails().getCreationEvent().getTime());
            assertNotNull(annotated.getDetails().getUpdateEvent().getTime());
            assertNotNull(annotated.linkedAnnotationList().get(0).getDetails()
                    .getCreationEvent().getTime());
            // assertNotNull(annotated.linkedAnnotationList().get(0).getDetails()
            // .getUpdateEvent().getTime());
        }
    }

}
