/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import integration.AbstractTest;
import integration.DeleteServiceTest;
import omero.api.delete.DeleteCommand;
import omero.model.Image;
import omero.model.Roi;
import omero.model.RoiI;
import omero.sys.EventContext;

import org.testng.annotations.Test;

/**
 * Tests for deleting rois and images which have rois.
 *
 * @since 4.2.1
 */
@Test(groups = { "delete", "integration", "ticket:2615" })
public class RoiDeleteTest extends AbstractTest {

    @Test(groups = { "ticket:2962", "ticket:3010" })
    public void testDeleteWithAnotherUsersRoi() throws Exception {

        EventContext owner = newUserAndGroup("rwrw--");
        Image i1 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        disconnect();

        newUserInGroup(owner);
        Roi roi = new RoiI();
        roi.setImage((Image) i1.proxy());
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        disconnect();

        loginUser(owner);
        delete(client, new DeleteCommand(DeleteServiceTest.REF_IMAGE, i1
                .getId().getValue(), null));

        assertDoesNotExist(i1);
        assertDoesNotExist(roi);

    }

}
