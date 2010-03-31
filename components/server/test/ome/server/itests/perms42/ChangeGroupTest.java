/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import java.util.List;

import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;

import org.testng.annotations.Test;

/**
 * Test of the change group functionality in Beta4.2
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = "ticket:1434")
public class ChangeGroupTest extends PermissionsTest {

    /**
     * Possible options for passing into the final changeGroup method
     */
    class Options {
        boolean moveAnnotations;
        boolean copyAnnotations; // OriginalFile? Thumbnails?? (only if Image???)
        boolean unlinkAnnotations;
        boolean containers; // Dataset/Project/Plate/Screen/Well/Etc.
    }
    
    /**
     * Tests that the change group functionality works similar to the
     * IDelete.deleteImage() functionality, i.e. that all attached
     * objects are similarly moved.
     */
    public void testChgrpImageMovesAllDeletedObjects() {
        
        setupOnce(Permissions.PRIVATE);
        ExperimenterGroup otherGroup = fixture.new_group();
        
        Pixels pix = makePixels();
        pix = iQuery.findByQuery("select p from Pixels p join fetch p.image " +
        		"where p.id = " + pix.getId(), null);
        Image img = pix.getImage();
        iAdmin.changeGroup(img, otherGroup.getName());
        
        // Note: Following will return an empty list if the pixels and image
        // objects are in separate groups.
        List<Object[]> rv =
        iQuery.projection("select i.details.group.id, p.details.group.id " +
        		"from Image i join i.pixels p where i.id = " + img.getId(), null);
        Object[] values = rv.get(0);
        assertEquals(values[0], values[1]);
        
    }
    
    /**
     * Tests that some annotations will be copied between groups if necessary.
     * Examples:
     * <pre>
     *   image.linkAnnotation(tag);
     *   chgrp(imgae);
     * </pre>
     * Now there should be two tags with the same ns/textValue, but one in
     * each group.
     */
    public void testChgrpImageCanCopyAnnotations() {
        fail();
    }
    
    public void testChgrpImageWontCopyOriginalFiles() {
        fail();
    }
    
    public void testChgrpImageCanAlsoChgrpOriginalFiles() {
        fail();
    }

    public void testChgrpImageCanCreateAnUnlinkLog() {
        fail();
    }


    public void testChgrpOnCertainTypesIsFlat() {
        fail();
    }
    
    public void testOnlyOwnersOrAdminCanCallChangeGroup() {
        fail();
    }
    
    public void testCanOnlyMoveToMembersGroup() {
        fail();
    }
    
    public void testUseChgrpOrMoveToCommonSpace() {
        fail(); // ? perhaps changeGroup(x, "user") is "move to common space"
    }
}
