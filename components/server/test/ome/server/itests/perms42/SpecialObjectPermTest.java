/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import ome.model.annotations.Annotation;
import ome.model.annotations.ExperimenterAnnotationLink;
import ome.model.annotations.FileAnnotation;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

/**
 * Tests the creation of and linkage to special objects such as:
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = { "ticket:1434", "ticket:1784", "ticket:1791", "ticket:1794" })
public class SpecialObjectPermTest extends PermissionsTest {

    @Test
    public void testUserCreatesImageInUserGroup() {

        final Long ugid = roles.getUserGroupId();

        setup(Permissions.PRIVATE);

        // Create an image in the "user" group
        FileAnnotation fa = new FileAnnotation();
        fa.setNs("my photo");
        fa.getDetails().setGroup(new ExperimenterGroup(ugid, false));
        fa = iUpdate.saveAndReturnObject(fa);

        // Make sure it belongs to the "user" group
        assertEquals(ugid, fa.getDetails().getGroup().getId());

        // Make sure we can load it
        iQuery.get(FileAnnotation.class, fa.getId());
        loadUserAnnotations(0);

        // Now link it to the user object
        ExperimenterAnnotationLink link = new ExperimenterAnnotationLink();
        link.link(fixture.user, fa);
        link.getDetails().setGroup(new ExperimenterGroup(ugid, false));
        iUpdate.saveObject(link);

        // And if we change groups we'll be able to load it?
        loginNewUser();
        LogFactory.getLog("XXX").error("YYY");
        loadUserAnnotations(1);

    }

    @Test
    public void test1798LinkNotInUserGroup() {

        final Long ugid = roles.getUserGroupId();

        setup(Permissions.PRIVATE);

        // Create an image in the "user" group
        FileAnnotation fa = new FileAnnotation();
        fa.setNs("my photo");
        fa.getDetails().setGroup(new ExperimenterGroup(ugid, false));
        fa = iUpdate.saveAndReturnObject(fa);

        // Make sure it belongs to the "user" group
        assertEquals(ugid, fa.getDetails().getGroup().getId());

        // Make sure we can load it
        iQuery.get(FileAnnotation.class, fa.getId());
        loadUserAnnotations(0);

        // Now link it to the user object
        ExperimenterAnnotationLink link = new ExperimenterAnnotationLink();
        link.link(fixture.user, fa);
        iUpdate.saveObject(link);

        // And if we change groups we'll be able to load it?
        loginNewUser();
        loadUserAnnotations(1);

    }

    private void loadUserAnnotations(int size) {
        Map<Long, Set<Annotation>> map =
            iMetadata.loadAnnotations(Experimenter.class,
                Collections.singleton(fixture.user.getId()),
                Collections.singleton(FileAnnotation.class.getName()),
                null, null);
        Set<Annotation> anns = map.get(fixture.user.getId());
        assertEquals(size, anns.size());
    }
}
