/*
 * Copyright 2006-2015 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */

package integration.chgrp;

import integration.AbstractServerTest;

import java.util.Arrays;
import java.util.List;

import omero.api.IPixelsPrx;
import omero.api.IRenderingSettingsPrx;
import omero.cmd.Chgrp2;
import omero.gateway.util.Requests;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.sys.EventContext;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Move image with rendering settings.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 3.0-Beta4
 */
public class RenderingSettingsMoveTest extends AbstractServerTest {

    /**
     * Creates an image w/o binary and moves it between groups.
     * The image has rendering setting that may not be owned by the owner of
     * the image.
     * The image may be moved by an administrator.
     * 
     * @param src The permissions of the source group.
     * @param target The permissions of the target group.
     * @param memberRole The role of the user the settings belong to.
     * @param moveMemberRole The role of the user performing the move.
     * @throws Exception Thrown if an error occurred.
     */
    private void moveImage(String src, String target, int memberRole, int
            moveMemberRole) throws Exception
    {
        //Create one user in the source group.
        EventContext ctx = newUserAndGroup(src);
        //Create an image
        Image img = mmFactory.createImage();
        img = (Image) iUpdate.saveAndReturnObject(img);
        Pixels pixels = img.getPrimaryPixels();
        // method already tested in RenderingSettingsServiceTest
        IRenderingSettingsPrx prx = factory.getRenderingSettingsService();
        long pixelsID = pixels.getId().getValue();
        prx.setOriginalSettingsInSet(Pixels.class.getName(),
                Arrays.asList(pixelsID));
        // check if we have settings now.
        IPixelsPrx svc = factory.getPixelsService();
        List<IObject> settings = svc.retrieveAllRndSettings(pixelsID,
                ctx.userId);
        assertEquals(settings.size(), 1);

        EventContext ctx2 = null;
        if (memberRole > 0) { //create a second user in the group.
            ctx2 = newUserInGroup(ctx);
            switch (memberRole) {
            case AbstractServerTest.ADMIN:
                logRootIntoGroup(ctx2);
                break;
            case AbstractServerTest.GROUP_OWNER:
                makeGroupOwner();
            }
            ctx2 = iAdmin.getEventContext();
        }
        //if second user. Create setting for second user.is created.;
        if (ctx2 != null) {
            svc = factory.getPixelsService();
            prx = factory.getRenderingSettingsService();
            prx.setOriginalSettingsInSet(Pixels.class.getName(),
                    Arrays.asList(pixelsID));
            settings = svc.retrieveAllRndSettings(pixelsID, ctx2.userId);
            assertEquals(settings.size(), 1);
        }
        //create a second group
        ExperimenterGroup g = newGroupAddUser(target, ctx.userId);
        iAdmin.getEventContext(); // Refresh

        disconnect();
        loginUser(ctx);
        //login is as root
        if (moveMemberRole == AbstractServerTest.ADMIN) {
            logRootIntoGroup(ctx);
        }

        //move the image(s)
        long id = img.getId().getValue();
        // Move the image
        final Chgrp2 mv = Requests.chgrp("Image", id, g.getId().getValue());
        callback(true, client, mv);

        //Check if the settings have been deleted.
        svc = factory.getPixelsService();
        settings = svc.retrieveAllRndSettings(pixelsID, -1);
        assertEquals(settings.size(), 0);

        disconnect();
        // Log in to other group
        if (moveMemberRole == AbstractServerTest.ADMIN) {
            loginUser(ctx); //require if log as root.
            disconnect();
        }
        loginUser(g);

        //Check that image has been moved.
        assertNotNull(iQuery.find(Image.class.getSimpleName(), id));

        //Load the settings.
        svc = factory.getPixelsService();
        settings = svc.retrieveAllRndSettings(pixelsID, ctx.userId);
        assertEquals(settings.size(), 1);
    }

    //move by owner
    /**
     * Test to move an image viewed only by the owner between a
     * RW---- group to a RW---- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWtoRW() throws Exception {
        moveImage("rw----", "rw----", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RW---- group to a RWR--- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWtoRWR() throws Exception {
        moveImage("rw----", "rwr---", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RW---- group to a RWRA-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWtoRWRA() throws Exception {
        moveImage("rw----", "rwra--", -1, -1);
    }


    /**
     * Test to move an image viewed only by the owner between a
     * RW---- group to a RWRW-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWtoRWRW() throws Exception {
        moveImage("rw----", "rwrw--", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RWRW-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRtoRWRW() throws Exception {
        moveImage("rwr---", "rwrw--", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RWRA-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRtoRWRA() throws Exception {
        moveImage("rwr---", "rwrw--", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RWR--- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRtoRWR() throws Exception {
        moveImage("rwr---", "rwr---", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RW---- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRtoRW() throws Exception {
        moveImage("rwr---", "rw----", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RW---- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRAtoRW() throws Exception {
        moveImage("rwr---", "rw----", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RWR--- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRAtoRWR() throws Exception {
        moveImage("rwra--", "rwr---", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RWRA-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRAtoRWRA() throws Exception {
        moveImage("rwra--", "rwra--", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RWRW-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRAtoRWRW() throws Exception {
        moveImage("rwra--", "rwrw--", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRW-- group to a RWRW-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRWtoRWRW() throws Exception {
        moveImage("rwrw--", "rwrw--", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRW-- group to a RWRA-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRWtoRWRA() throws Exception {
        moveImage("rwrw--", "rwra--", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRW-- group to a RWR--- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRWtoRWR() throws Exception {
        moveImage("rwrw--", "rwr---", -1, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRW-- group to a RW---- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByOwnerRWRWtoRW() throws Exception {
        moveImage("rwrw--", "rw----", -1, -1);
    }

    //move by Admin
    /**
     * Test to move an image viewed only by the owner between a
     * RW---- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWtoRW() throws Exception {
        moveImage("rw----", "rw----", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RW---- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWtoRWR() throws Exception {
        moveImage("rw----", "rwr---", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RW---- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWtoRWRA() throws Exception {
        moveImage("rw----", "rwra--", -1, -1);
    }


    /**
     * Test to move an image viewed only by the owner between a
     * RW---- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWtoRWRW() throws Exception {
        moveImage("rw----", "rwrw--", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RWRW-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRtoRWRW() throws Exception {
        moveImage("rwr---", "rwrw--", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRtoRWRA() throws Exception {
        moveImage("rwr---", "rwrw--", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRtoRWR() throws Exception {
        moveImage("rwr---", "rwr---", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRtoRW() throws Exception {
        moveImage("rwr---", "rw----", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRAtoRW() throws Exception {
        moveImage("rwr---", "rw----", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRAtoRWR() throws Exception {
        moveImage("rwra--", "rwr---", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRAtoRWRA() throws Exception {
        moveImage("rwra--", "rwra--", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRAtoRWRW() throws Exception {
        moveImage("rwra--", "rwrw--", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRW-- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRWtoRWRW() throws Exception {
        moveImage("rwrw--", "rwrw--", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRW-- group to a RWRA-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRWtoRWRA() throws Exception {
        moveImage("rwrw--", "rwra--", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRW-- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRWtoRWR() throws Exception {
        moveImage("rwrw--", "rwr---", -1, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRW-- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByOwnerMoveImageByAdminRWRWtoRW() throws Exception {
        moveImage("rwrw--", "rw----", -1, AbstractServerTest.ADMIN);
    }

    //member viewed the image, move by owner
    /**
     * Test to move an image viewed only by the member between a
     * RWR--- group to a RWRW-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRtoRWRW() throws Exception {
        moveImage("rwr---", "rwrw--", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWR--- group to a RWRA-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRtoRWRA() throws Exception {
        moveImage("rwr---", "rwrw--", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWR--- group to a RWR--- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRtoRWR() throws Exception {
        moveImage("rwr---", "rwr---", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWR--- group to a RW---- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRtoRW() throws Exception {
        moveImage("rwr---", "rw----", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRA-- group to a RW---- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRAtoRW() throws Exception {
        moveImage("rwr---", "rw----", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRA-- group to a RWR--- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRAtoRWR() throws Exception {
        moveImage("rwra--", "rwr---", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRA-- group to a RWRA-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRAtoRWRA() throws Exception {
        moveImage("rwra--", "rwra--", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRA-- group to a RWRW-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRAtoRWRW() throws Exception {
        moveImage("rwra--", "rwrw--", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRW-- group to a RWRW-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRWtoRWRW() throws Exception {
        moveImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRW-- group to a RWRA-- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRWtoRWRA() throws Exception {
        moveImage("rwrw--", "rwra--", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRW-- group to a RWR--- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRWtoRWR() throws Exception {
        moveImage("rwrw--", "rwr---", AbstractServerTest.MEMBER, -1);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRW-- group to a RW---- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByOwnerRWRWtoRW() throws Exception {
        moveImage("rwrw--", "rw----", AbstractServerTest.MEMBER, -1);
    }

    //member viewed the image, move by admin
    /**
     * Test to move an image viewed only by the member between a
     * RWR--- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRtoRWRW() throws Exception {
        moveImage("rwr---", "rwrw--", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWR--- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRtoRWRA() throws Exception {
        moveImage("rwr---", "rwrw--", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWR--- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRtoRWR() throws Exception {
        moveImage("rwr---", "rwr---", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWR--- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRtoRW() throws Exception {
        moveImage("rwr---", "rw----", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRA-- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRAtoRW() throws Exception {
        moveImage("rwr---", "rw----", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRA-- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRAtoRWR() throws Exception {
        moveImage("rwra--", "rwr---", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRA-- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRAtoRWRA() throws Exception {
        moveImage("rwra--", "rwra--", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRA-- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRAtoRWRW() throws Exception {
        moveImage("rwra--", "rwrw--", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRW-- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRWtoRWRW() throws Exception {
        moveImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRW-- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRWtoRWRA() throws Exception {
        moveImage("rwrw--", "rwra--", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRW-- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRWtoRWR() throws Exception {
        moveImage("rwrw--", "rwr---", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the member between a
     * RWRW-- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByMemberMoveImageByAdminRWRWtoRW() throws Exception {
        moveImage("rwrw--", "rw----", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN);
    }

    //admin viewed the image, move by admin
    /**
     * Test to move an image viewed only by the admin between a
     * RWR--- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRtoRWRW() throws Exception {
        moveImage("rwr---", "rwrw--", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWR--- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRtoRWRA() throws Exception {
        moveImage("rwr---", "rwrw--", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWR--- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRtoRWR() throws Exception {
        moveImage("rwr---", "rwr---", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWR--- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRtoRW() throws Exception {
        moveImage("rwr---", "rw----", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWRA-- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRAtoRW() throws Exception {
        moveImage("rwr---", "rw----", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWRA-- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRAtoRWR() throws Exception {
        moveImage("rwra--", "rwr---", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWRA-- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRAtoRWRA() throws Exception {
        moveImage("rwra--", "rwra--", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWRA-- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRAtoRWRW() throws Exception {
        moveImage("rwra--", "rwrw--", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWRW-- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRWtoRWRW() throws Exception {
        moveImage("rwrw--", "rwrw--", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWRW-- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRWtoRWRA() throws Exception {
        moveImage("rwrw--", "rwra--", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWRW-- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRWtoRWR() throws Exception {
        moveImage("rwrw--", "rwr---", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWRW-- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByAdminRWRWtoRW() throws Exception {
        moveImage("rwrw--", "rw----", AbstractServerTest.ADMIN, AbstractServerTest.ADMIN);
    }

    //view by admin, move by owner
    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRtoRWRW() throws Exception {
        moveImage("rwr---", "rwrw--", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRtoRWRA() throws Exception {
        moveImage("rwr---", "rwrw--", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWR--- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRtoRWR() throws Exception {
        moveImage("rwr---", "rwr---", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the onwer between a
     * RWR--- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRtoRW() throws Exception {
        moveImage("rwr---", "rw----", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RW---- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRAtoRW() throws Exception {
        moveImage("rwr---", "rw----", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RWR--- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRAtoRWR() throws Exception {
        moveImage("rwra--", "rwr---", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRAtoRWRA() throws Exception {
        moveImage("rwra--", "rwra--", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRA-- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRAtoRWRW() throws Exception {
        moveImage("rwra--", "rwrw--", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRW-- group to a RWRW-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRWtoRWRW() throws Exception {
        moveImage("rwrw--", "rwrw--", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the owner between a
     * RWRW-- group to a RWRA-- group. The admin moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRWtoRWRA() throws Exception {
        moveImage("rwrw--", "rwra--", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWRW-- group to a RWR--- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRWtoRWR() throws Exception {
        moveImage("rwrw--", "rwr---", AbstractServerTest.ADMIN, -1);
    }

    /**
     * Test to move an image viewed only by the admin between a
     * RWRW-- group to a RW---- group. The owner moves the image.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testSettingByAdminMoveImageByOwnerRWRWtoRW() throws Exception {
        moveImage("rwrw--", "rw----", AbstractServerTest.ADMIN, -1);
    }
}
