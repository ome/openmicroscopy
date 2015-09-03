/*
 * Copyright 2013-2015 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */

package integration.delete;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import integration.AbstractServerTest;
import omero.SecurityViolation;
import omero.api.IProjectionPrx;
import omero.cmd.Delete2;
import omero.constants.projection.ProjectionType;
import omero.gateway.util.Requests;
import omero.model.Image;
import omero.model.Pixels;
import omero.sys.EventContext;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

/**
 * Deleted projected image and/or source image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4.9
 */
public class DeleteProjectedImageTest  extends AbstractServerTest {

    /** Indicates to delete the source image. */
    private static final int SOURCE_IMAGE = 0;

    /** Indicates to delete the projected image. */
    private static final int PROJECTED_IMAGE = 1;

    /** Indicates to delete the both images. */
    private static final int BOTH_IMAGES = 2;

    /**
     * Imports the small dv.
     * The image has 5 z-sections, 6 timepoints, 1 channel, signed 16-bit.
     * 
     * @return The id of the pixels set.
     * @throws Exception Thrown if an error occurred.
     */
    private Pixels importImage() throws Exception
    {
        File srcFile = ResourceUtils.getFile("classpath:tinyTest.d3d.dv");
        List<Pixels> pixels = null;
        try {
            pixels = importFile(srcFile, "dv");
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        return pixels.get(0);
    }

    /** 
     * Creates an image and projects it either by the owner or by another
     * member of the group. The image is then deleted by the owner of the image
     * or by another user.
     *
     * @param src The permissions of the source group.
     * @param memberRole The role of the other group member projecting the
     * image or <code>-1</code> if the owner projects the image.
     * @param deleteMemberRole The role of the member deleting the image
     * image or <code>-1</code> if the owner projects the image.
     * @param action One of the constants defined by this class.
     * @throws Exception Thrown if an error occurred.
     */
    private void deleteImage(String src, int memberRole,
            int deleteMemberRole, int action)
            throws Exception
    {
        deleteImage(true, src, memberRole, deleteMemberRole, action);
    }

    /** 
     * Creates an image and projects it either by the owner or by another
     * member of the group. The image is then deleted by the owner of the image
     * or by another user.
     *
     * @param passes if the delete request's response is expected to be
     * {@link omero.cmd.OK}
     * @param src The permissions of the source group.
     * @param memberRole The role of the other group member projecting the
     * image or <code>-1</code> if the owner projects the image.
     * @param deleteMemberRole The role of the member deleting the image
     * image or <code>-1</code> if the owner projects the image.
     * @param action One of the constants defined by this class.
     * @throws Exception Thrown if an error occurred.
     */
    private void deleteImage(boolean passes, String src, int memberRole,
            int deleteMemberRole, int action)
            throws Exception
    {
        EventContext ctx = newUserAndGroup(src);
        if (memberRole > 0) { //create a second user in the group.
            EventContext ctx2 = newUserInGroup(ctx);
            switch (memberRole) {
            case AbstractServerTest.ADMIN:
                logRootIntoGroup(ctx2);
                break;
            case AbstractServerTest.GROUP_OWNER:
                makeGroupOwner();
            }
        }
        Pixels pixels = importImage();
        long id = pixels.getImage().getId().getValue();
        List<Integer> channels = Arrays.asList(0);
        IProjectionPrx svc = factory.getProjectionService();
        long projectedID = svc.projectPixels(pixels.getId().getValue(), null,
                ProjectionType.MAXIMUMINTENSITY, 0, 1, channels, 1, 0, 1,
                "projectedImage");

        disconnect();
        init(ctx);
        //login is as root
        if (deleteMemberRole == AbstractServerTest.ADMIN)
            logRootIntoGroup(ctx);
        //delete the image(s)
        Delete2 dc;
        switch (action) {
        case SOURCE_IMAGE:
            dc = Requests.delete("Image", id);
            callback(passes, client, dc);
            break;
        case PROJECTED_IMAGE:
            dc = Requests.delete("Image", projectedID);
            callback(passes, client, dc);
            break;
        case BOTH_IMAGES:
            dc = Requests.delete("Image", Arrays.asList(id, projectedID));
            callback(passes, client, dc);
            break;
        }

        //Check the result
        switch (action) {
        case SOURCE_IMAGE:
            assertNull(iQuery.find(Image.class.getSimpleName(), id));
            //check that the projected image is still there
            assertNotNull(iQuery.find(Image.class.getSimpleName(), projectedID));
            break;
        case PROJECTED_IMAGE:
            assertNull(iQuery.find(Image.class.getSimpleName(), projectedID));
           //check that the original image is still there
            assertNotNull(iQuery.find(Image.class.getSimpleName(), id));
            break;
        case BOTH_IMAGES:
            assertNull(iQuery.find(Image.class.getSimpleName(), projectedID));
            assertNull(iQuery.find(Image.class.getSimpleName(), id));
        }
    }

    /**
     * Test the delete of the image that has been projected in a
     * RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRW() throws Exception {
        deleteImage("rw----", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RW---- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRW() throws Exception {
        deleteImage("rw----",  -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RW---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRW() throws Exception {
        deleteImage("rw----", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWR--- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWR() throws Exception {
        deleteImage("rwr---", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWR() throws Exception {
        deleteImage("rwr---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWR---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWR() throws Exception {
        deleteImage("rwr---", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRA-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRA() throws Exception {
        deleteImage("rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRA-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRA() throws Exception {
        deleteImage("rwra--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRA-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRA() throws Exception {
        deleteImage("rwra--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRW-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRW() throws Exception {
        deleteImage("rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRW-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRW() throws Exception {
        deleteImage("rwrw--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRW() throws Exception {
        deleteImage("rwrw--", -1, -1, BOTH_IMAGES);
    }

    //Project by another member delete by Admin.
    /**
     * Test the delete of the image that has been projected in a
     * RW---- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRW() throws Exception {
        deleteImage("rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RW---- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRW() throws Exception {
        deleteImage("rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RW---- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRW() throws Exception {
        deleteImage("rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWR--- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWR() throws Exception {
        deleteImage("rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWR--- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWR() throws Exception {
        deleteImage("rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWR--- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWR() throws Exception {
        deleteImage("rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRA-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRA() throws Exception {
        deleteImage("rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRA--  group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRA() throws Exception {
        deleteImage("rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRA-- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRA() throws Exception {
        deleteImage("rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRW-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRW() throws Exception {
        deleteImage("rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRW-- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRW() throws Exception {
        deleteImage("rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRW() throws Exception {
        deleteImage("rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    //delete by admin
    /**
     * Test the delete of the image that has been projected in a
     * RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRW() throws Exception {
        deleteImage("rw----", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RW---- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRW() throws Exception {
        deleteImage("rw----", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RW---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRW() throws Exception {
        deleteImage("rw----", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWR--- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWR() throws Exception {
        deleteImage("rwr---", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWR() throws Exception {
        deleteImage("rwr---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWR--- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWR() throws Exception {
        deleteImage("rwr---", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRA-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRA() throws Exception {
        deleteImage("rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRA-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRA() throws Exception {
        deleteImage("rwra--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRA-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRA() throws Exception {
        deleteImage("rwra--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }


    /**
     * Test the delete of the image that has been projected in a
     * RWRW-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRW() throws Exception {
        deleteImage("rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRW-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRW() throws Exception {
        deleteImage("rwrw--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRW() throws Exception {
        deleteImage("rwrw--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    //Projected by another member delete by data owner.
    /**
     * Test the delete of the image that has been projected in a
     * RW---- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = SecurityViolation.class)
    public void testSourceImageByMemberdeleteByOwnerRW() throws Exception {
        deleteImage(false, "rw----", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWR--- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testSourceImageByMemberdeleteByOwnerRWR() throws Exception {
        deleteImage("rwr---", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRA-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testSourceImageByMemberdeleteByOwnerRWRA() throws Exception {
        deleteImage("rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected in a
     * RWRW-- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRW() throws Exception {
        deleteImage("rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

}
