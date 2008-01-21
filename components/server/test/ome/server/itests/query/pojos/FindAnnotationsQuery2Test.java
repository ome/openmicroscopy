/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.query.pojos;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.model.IAnnotated;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.TextAnnotation;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.PojosFindAnnotationsQueryDefinition;
import ome.testing.CreatePojosFixture;

import org.testng.annotations.Test;

/**
 * Copying {@link FindAnnotationsQueryTest} in order to do things manually and
 * without {@link CreatePojosFixture}, since it is currently broken.
 * 
 * @author josh
 * 
 */
@Test
public class FindAnnotationsQuery2Test extends AbstractManagedContextTest {
    PojosFindAnnotationsQueryDefinition q;

    List list;

    Set ids;

    public void testFindImageAnnotationsReturnsEventTimes() throws Exception {
        Image i = new Image();
        i.setName("ticket:172");
        TextAnnotation a = new TextAnnotation();
        a.setName("");
        a.setTextValue("ticket:172");
        i.linkAnnotation(a);
        i = iUpdate.saveAndReturnObject(i);

        ids = new HashSet(Arrays.asList(i.getId()));
        q = new PojosFindAnnotationsQueryDefinition(new Parameters()
                .addIds(ids).addOptions(null).addClass(Image.class).addSet(
                        "annotatorIds", Collections.emptySet()));

        Collection<IAnnotated> results = (Collection) iQuery.execute(q);
        assertTrue(results.size() > 0);
        for (IAnnotated annotated : results) {
            assertNotNull(annotated.getDetails().getCreationEvent().getTime());
            assertNotNull(annotated.getDetails().getUpdateEvent().getTime());
            assertNotNull(annotated.linkedAnnotationList().get(0).getDetails()
                    .getCreationEvent().getTime());
            // assertNotNull(annotated.linkedAnnotationList().get(0).getDetails()
            // .getUpdateEvent().getTime());
        }
    }

    public void testSameTestAgainstIPojos() throws Exception {
        testFindImageAnnotationsReturnsEventTimes();
        Map<Long, Set<IObject>> map = this.iPojos.findAnnotations(Image.class,
                ids, Collections.<Long> emptySet(), null);
        assertTrue(map.size() > 0);
        Annotation ann = (Annotation) map.values().iterator().next().iterator()
                .next();
        assertNotNull(ann.getDetails().getCreationEvent());
        assertTrue(ann.getDetails().getCreationEvent().isLoaded());
    }

}
