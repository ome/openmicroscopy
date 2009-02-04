/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import ome.model.ILink;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.core.Image;
import ome.parameters.Parameters;

import org.testng.annotations.Test;

/**
 */
@Test(groups = { "counts", "integration", "views" })
public class CountTest extends AbstractManagedContextTest {

    @Test
    public void testQueryWithCounts() throws Exception {
        long self = iAdmin.getEventContext().getCurrentUserId();

        Image i = new_Image("counts");
        i = iUpdate.saveAndReturnObject(i);

        i = loadImageWithAnnotationCounts(i);

        assertNotNull(i.getAnnotationLinksCountPerOwner());
        assertNull(i.getAnnotationLinksCountPerOwner().get(self));

        TextAnnotation ta = new CommentAnnotation();
        ta.setNs("");
        ta.setTextValue("counts");

        ILink link = i.linkAnnotation(ta);
        iUpdate.saveObject(link);

        i = loadImageWithAnnotationCounts(i);

        assertNotNull(i.getAnnotationLinksCountPerOwner());
        assertTrue(i.getAnnotationLinksCountPerOwner().get(self).equals(1L));

        // Attempting to edit them
        i.getAnnotationLinksCountPerOwner().put(self, 1000000L);

        i = loadImageWithAnnotationCounts(i);
        assertTrue(i.getAnnotationLinksCountPerOwner().get(self).equals(1L));
    }

    private Image loadImageWithAnnotationCounts(Image i) {
        i = iQuery.findByQuery("select i from Image i "
                + "left outer join fetch i.annotationLinks l "
                + "left outer join fetch l.child "
                + "left outer join fetch i.annotationLinksCountPerOwner "
                + "where i.id = :id", new Parameters().addId(i.getId()));
        return i;
    }
}
