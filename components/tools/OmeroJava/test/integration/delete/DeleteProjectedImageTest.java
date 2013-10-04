/*
 * $Id$
 *
 * Copyright 2013 University of Dundee. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import integration.AbstractServerTest;
import integration.DeleteServiceTest;
import omero.api.IProjectionPrx;
import omero.cmd.Delete;
import omero.cmd.DoAll;
import omero.cmd.Request;
import omero.constants.projection.ProjectionType;
import omero.model.IObject;
import omero.model.Pixels;

import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

/**
 * Deleted projected image and/or source image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
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
     * @param target The permissions of the source group.
     * @param memberRole The role of the other group member projecting the
     * image or <code>-1</code> if the owner projects the image.
     * @param deleteMemberRole The role of the member moving the image
     * image or <code>-1</code> if the owner projects the image.
     * @param action One of the constants defined by this class.
     * @throws Exception Thrown if an error occurred.
     */
    private void deleteImage(String src, String target, int memberRole,
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

        //login is as root
        if (deleteMemberRole == AbstractServerTest.ADMIN)
            logRootIntoGroup(ctx);
        //delete the image(s)
        switch (action) {
        case SOURCE_IMAGE:
            delete(client, new Delete(DeleteServiceTest.REF_IMAGE, id, null));
            break;
        case PROJECTED_IMAGE:
            delete(client, new Delete(DeleteServiceTest.REF_IMAGE, projectedID,
                    null));
            break;
        case BOTH_IMAGES:
            List<Request> commands = new ArrayList<Request>();
            commands.add(new Delete(DeleteServiceTest.REF_IMAGE, id, null));
            commands.add(new Delete(DeleteServiceTest.REF_IMAGE, projectedID,
                    null));
            DoAll all = new DoAll();
            all.requests = commands;
            doChange(all);
        }

        //Check the result
        ParametersI param = new ParametersI();
        StringBuilder sb = new StringBuilder();
        sb.append("select i from Image i ");
        sb.append("where i.id in (:ids)");
        List<Long> ids = new ArrayList<Long>();
        List<IObject> images;
        switch (action) {
        case SOURCE_IMAGE:
            ids.add(id);
            param.addIds(ids);
            images = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(images.size(), 0);
            //check that the projected image is still there
            param = new ParametersI();
            ids.clear();
            ids.add(projectedID);
            param.addIds(ids);
            images = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(images.size(), 1);
            break;
        case PROJECTED_IMAGE:
            ids.add(projectedID);
            param.addIds(ids);
            images = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(images.size(), 0);
           //check that the original image is still there
            param = new ParametersI();
            ids.clear();
            ids.add(id);
            param.addIds(ids);
            images = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(images.size(), 1);
            break;
        case BOTH_IMAGES:
            ids.add(id);
            ids.add(projectedID);
            param.addIds(ids);
            images = iQuery.findAllByQuery(sb.toString(), param);
            assertEquals(images.size(), 0);
        }
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRW-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRA-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWR--- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRtoRW() throws Exception {
        deleteImage("rwr---", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RW---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWR--- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWR--- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRA-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwr---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRA-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", -1, -1, BOTH_IMAGES);
    }
    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRW-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWR--- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr--", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWR--- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, -1, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByOwnerRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RW---- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByOwnerRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw---", -1, -1, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RW---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByOwnerRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw----", -1, -1, BOTH_IMAGES);
    }

    //Project by another member delete by Admin.
    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRW-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRA-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWR--- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWR--- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RW---- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RW---- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RW---- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWR--- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWR--- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWR--- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRA-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRA-- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }
    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRW-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRW-- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWR--- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr--", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWR--- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rwr---", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RW---- group. The projected image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByMemberdeleteByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw---", AbstractServerTest.MEMBER,
                AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RW---- group. Both images are deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByMemberdeleteByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw----", AbstractServerTest.MEMBER, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    //delete by admin
    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRW-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRA-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWR--- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwr---", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRtoRW() throws Exception {
        deleteImage("rwr---", "rw----", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RW---- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RW---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rw----", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWR--- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWR--- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr---", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRA-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRA-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwr---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRA-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }
    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRW-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRW-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwrw--", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwra--", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWR--- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWR--- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr--", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWR--- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RW---- group. The source image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByOwnerdeleteByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rwr---", -1, AbstractServerTest.ADMIN, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RW---- group. The projected image is deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testProjectedImageByOwnerdeleteByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw---", -1, AbstractServerTest.ADMIN, PROJECTED_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RW---- group. Both images are deleted.
     * The projection is done by the owner of the data and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testBothImagesByOwnerdeleteByAdminRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rw----", -1, AbstractServerTest.ADMIN, BOTH_IMAGES);
    }
    
    //Projected by another member delete by data owner.
    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RW---- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWtoRW() throws Exception {
        deleteImage("rw----", "rw----", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWR--- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWtoRWR() throws Exception {
        deleteImage("rw----", "rwr---", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRA-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWtoRWRA() throws Exception {
        deleteImage("rw----", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RW---- to a RWRW-- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWtoRWRW() throws Exception {
        deleteImage("rw----", "rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRW-- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRtoRWRW() throws Exception {
        deleteImage("rwr---", "rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWRA-- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRtoRWRA() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RWR--- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRtoRWR() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWR--- to a RW---- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRtoRW() throws Exception {
        deleteImage("rwr---", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RW---- group. The source image is deleted.
     * The projection is done by a member and deleted by the admin.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRAtoRW() throws Exception {
        deleteImage("rwra--", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWR--- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRAtoRWR() throws Exception {
        deleteImage("rwra--", "rwr--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRA-- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRAtoRWRA() throws Exception {
        deleteImage("rwra--", "rwra--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRA-- to a RWRW-- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRAtoRWRW() throws Exception {
        deleteImage("rwra--", "rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRW-- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRWtoRWRW() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWRA-- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRWtoRWRA() throws Exception {
        deleteImage("rwrw--", "rwrw--", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RWR--- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRWtoRWR() throws Exception {
        deleteImage("rwrw--", "rwr---", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

    /**
     * Test the delete of the image that has been projected from a
     * RWRW-- to a RW---- group. The source image is deleted.
     * The projection is done by a member and deleted by the owner.
     *
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testSourceImageByMemberdeleteByOwnerRWRWtoRW() throws Exception {
        deleteImage("rwrw--", "rwr---", AbstractServerTest.MEMBER,
                -1, SOURCE_IMAGE);
    }

}
